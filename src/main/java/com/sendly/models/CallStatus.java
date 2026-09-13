package com.sendly.models;

/**
 * Where a call is in its life, as reported by {@code Call.getStatus()}.
 * <p>
 * Statuses are plain strings so a status introduced later still decodes;
 * compare against these constants.
 * </p>
 */
public final class CallStatus {
    /** The far end is being rung; nothing has been charged yet. */
    public static final String RINGING = "ringing";
    /** Answered and in progress. */
    public static final String ACTIVE = "active";
    /** Ended after being answered. */
    public static final String COMPLETED = "completed";
    /** Rang out without an answer. */
    public static final String NO_ANSWER = "no_answer";
    /** The far end was busy. */
    public static final String BUSY = "busy";
    /** Torn down before it was answered. */
    public static final String CANCELLED = "cancelled";
    /** The far end declined. */
    public static final String DECLINED = "declined";
    /** Could not be set up or was cut short by a fault. */
    public static final String FAILED = "failed";
    /** An internal call whose media dropped and may recover. */
    public static final String SUSPENDED = "suspended";

    private CallStatus() {}
}
