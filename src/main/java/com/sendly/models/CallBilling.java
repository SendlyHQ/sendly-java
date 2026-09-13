package com.sendly.models;

/**
 * Whether a call's charges are still accruing, as reported by
 * {@code Call.getBilling()}.
 */
public final class CallBilling {
    /** A phone call in progress, charged per started minute. */
    public static final String METERED = "metered";
    /** Ended; {@code creditsCharged} is final. */
    public static final String SETTLED = "settled";
    /** Never charged: internal calls, and calls from before metering. */
    public static final String UNBILLED = "unbilled";

    private CallBilling() {}
}
