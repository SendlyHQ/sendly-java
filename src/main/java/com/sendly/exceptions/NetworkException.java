package com.sendly.exceptions;

/**
 * Thrown when a network error occurs.
 */
public class NetworkException extends SendlyException {
    public NetworkException(String message) {
        super(message, 0, "NETWORK_ERROR");
    }
}
