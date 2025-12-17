package com.sendly.exceptions;

/**
 * Thrown when the rate limit is exceeded.
 */
public class RateLimitException extends SendlyException {
    private final int retryAfter;

    public RateLimitException(String message) {
        this(message, 0);
    }

    public RateLimitException(String message, int retryAfter) {
        super(message, 429, "RATE_LIMIT_EXCEEDED");
        this.retryAfter = retryAfter;
    }

    /**
     * Get the number of seconds to wait before retrying.
     */
    public int getRetryAfter() {
        return retryAfter;
    }
}
