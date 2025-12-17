package com.sendly.exceptions;

/**
 * Thrown when the requested resource is not found.
 */
public class NotFoundException extends SendlyException {
    public NotFoundException(String message) {
        super(message, 404, "NOT_FOUND");
    }
}
