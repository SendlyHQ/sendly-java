package com.sendly.models;

/**
 * Why a transfer failed, as reported by
 * {@code CallTransfer.getFailureReason()} ({@code null} unless the status is
 * {@code failed}). A reason added later is returned as sent.
 */
public final class CallTransferFailureReason {
    /** The number was busy. */
    public static final String BUSY = "busy";
    /** Nobody answered. */
    public static final String NO_ANSWER = "no_answer";
    /** The call was declined. */
    public static final String DECLINED = "declined";
    /** The number could not be reached. */
    public static final String UNAVAILABLE = "unavailable";
    /** No definite answer came back. */
    public static final String UNKNOWN = "unknown";

    private CallTransferFailureReason() {}
}
