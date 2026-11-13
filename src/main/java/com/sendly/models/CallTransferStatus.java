package com.sendly.models;

/**
 * Where a transfer stands, as reported by
 * {@code CallTransfer.getStatus()}. A status added later is returned as sent.
 */
public final class CallTransferStatus {
    /** The number is ringing. */
    public static final String PENDING = "pending";
    /** The caller was handed over; the call ends {@code completed} with {@code hangupClass} "transferred". */
    public static final String COMPLETED = "completed";
    /** The transfer did not go through, and the caller is still on the call. */
    public static final String FAILED = "failed";

    private CallTransferStatus() {}
}
