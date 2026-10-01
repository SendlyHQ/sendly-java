package com.sendly.exceptions;

/**
 * Thrown when the API answers 429.
 * <p>
 * {@link #getApiErrorCode()} says why: {@code rate_limit_exceeded} for the
 * per-minute API limit and the verification (OTP) limits,
 * {@code too_many_concurrent_verifications} when too many API key checks
 * are already running, {@code provision_rate_limit} for the enterprise
 * workspace provisioning limits, and {@code too_many_failed_key_attempts}
 * when this address has sent too many invalid API keys, which means the key
 * is wrong. The client waits out and retries {@code rate_limit_exceeded},
 * {@code rate_limited}, {@code too_many_concurrent_verifications},
 * {@code provision_rate_limit} and a 429 without a code, such as a proxy's,
 * and only when the wait is 60 seconds or less; anything else is thrown at
 * once.
 * </p>
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
     * @param message      The API's message
     * @param retryAfter   Seconds to wait before trying again
     * @param apiErrorCode The API's {@code error} code, returned by
     *                     {@link #getApiErrorCode()}; may be null
     */
    public RateLimitException(String message, int retryAfter, String apiErrorCode) {
        this(message, retryAfter);
        withApiError(apiErrorCode, null);
    }

    /**
     * Get the number of seconds to wait before retrying: the
     * {@code Retry-After} header, or the body's {@code retryAfter} when the
     * header is missing; 0 when the API gave neither.
     */
    public int getRetryAfter() {
        return retryAfter;
    }
}
