package com.sendly.exceptions;

/**
 * Thrown when the request contains invalid parameters.
 */
public class ValidationException extends SendlyException {
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }
}
