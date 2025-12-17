package com.sendly.exceptions;

/**
 * Thrown when the API key is invalid or missing.
 */
public class AuthenticationException extends SendlyException {
    public AuthenticationException(String message) {
        super(message, 401, "AUTHENTICATION_ERROR");
    }
}
