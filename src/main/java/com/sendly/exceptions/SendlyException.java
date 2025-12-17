package com.sendly.exceptions;

/**
 * Base exception for all Sendly errors.
 */
public class SendlyException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

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
}
