package com.sendly.models;

/**
 * Per-call options accepted by mutating resource methods.
 */
public class IdempotentRequestOptions {
    private final String idempotencyKey;

    /**
     * Create request options with an idempotency key.
     * <p>
     * The SDK already generates a key per logical request automatically, so
     * the server can dedupe the SDK's own timeout retries. Supply your own key
     * when you need idempotency across process restarts or your own retry
     * loops — repeating a request with the same key within 24 hours returns
     * the original response instead of executing again.
     * </p>
     * <p>
     * Note: a response is cached under the key once the original attempt
     * completes, including error responses — retrying a failed request with
     * the same key returns the recorded failure; use a fresh key to
     * re-execute.
     * </p>
     *
     * @param idempotencyKey Idempotency key for this operation (1-255
     *                       printable ASCII characters)
     */
    public IdempotentRequestOptions(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
