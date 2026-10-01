package com.sendly.exceptions;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base exception for all Sendly errors.
 */
public class SendlyException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;
    private String apiErrorCode;
    private List<FieldError> fieldErrors = Collections.emptyList();
    private JsonObject responseBody;

    public SendlyException(String message) {
        this(message, 0);
    }

    public SendlyException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = null;
    }

    public SendlyException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /**
     * Get the HTTP status code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * The machine-readable {@code error} string from the API response body
     * (e.g. {@code rcs_not_enabled}, {@code rcs_field_locked}), or null when
     * the response carried none. Unlike {@link #getErrorCode()}, which is a
     * fixed per-exception-class constant, this distinguishes the different
     * reasons a route can answer with the same HTTP status.
     */
    public String getApiErrorCode() {
        return apiErrorCode;
    }

    /**
     * Per-field problems from the API response body's {@code errors} array,
     * when the response carried one (e.g. 422 {@code rcs_invalid_content}).
     * Empty otherwise.
     */
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * Attach the API error code and per-field errors parsed from a response
     * body. Called by the client when it maps a response to an exception.
     *
     * @param apiErrorCode The body's {@code error} string (may be null)
     * @param fieldErrors  The body's {@code errors} entries (may be null)
     * @return this exception
     */
    public SendlyException withApiError(String apiErrorCode, List<FieldError> fieldErrors) {
        this.apiErrorCode = apiErrorCode;
        this.fieldErrors = fieldErrors == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(fieldErrors));
        return this;
    }

    /**
     * The API response body this error was mapped from, or null when the
     * error did not come from an API response (a network failure, a refusal
     * raised client-side, or a body that is not a JSON object, such as an
     * HTML error page from a proxy). Use it for fields beyond {@code error} and
     * {@code message}: the {@code numbers} still answering with an agent on
     * 409 {@code agent_in_use}, or the {@code suggested} address on 422
     * {@code invalid_address}.
     */
    public JsonObject getResponseBody() {
        return responseBody;
    }

    /**
     * Attach the parsed API response body. Called by the client when it maps
     * a response to an exception.
     *
     * @param responseBody The response body (may be null)
     * @return this exception
     */
    public SendlyException withResponseBody(JsonObject responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    /**
     * One entry of an API response's {@code errors} array — the path of the
     * offending field and what is wrong with it.
     */
    public static final class FieldError {
        private final String path;
        private final String message;

        public FieldError(String path, String message) {
            this.path = path;
            this.message = message;
        }

        /** Dotted path of the field (e.g. {@code brand.ein}, {@code devices.0.phoneNumber}). */
        public String getPath() {
            return path;
        }

        /** What is wrong with the field. */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return path + ": " + message;
        }
    }
}
