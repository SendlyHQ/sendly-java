package com.sendly.exceptions;

/**
 * Thrown when the request contains invalid parameters.
 */
public class ValidationException extends SendlyException {
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }

    /**
     * A validation error with the HTTP status the API answered, such as 422.
     */
    public ValidationException(String message, int statusCode) {
        super(message, statusCode, "VALIDATION_ERROR");
    }
}
