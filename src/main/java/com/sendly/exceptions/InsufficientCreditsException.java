package com.sendly.exceptions;

/**
 * Thrown when the account has insufficient credits.
 */
public class InsufficientCreditsException extends SendlyException {
    public InsufficientCreditsException(String message) {
        super(message, 402, "INSUFFICIENT_CREDITS");
    }
}
