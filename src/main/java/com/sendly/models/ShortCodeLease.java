package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * The monthly lease of a short code.
 */
public class ShortCodeLease {
    public static final String STATE_NOT_STARTED = "not_started";
    public static final String STATE_RUNNING = "running";
    public static final String STATE_PAST_DUE = "past_due";
    public static final String STATE_PAUSED = "paused";
    public static final String STATE_ENDING = "ending";
    public static final String STATE_ENDED = "ended";

    private final int monthlyCents;
    private final int minimumTermMonths;
    private final String startsAt;
    private final String state;
    private final boolean billingEnabled;
    private final String startedAt;
    private final String nextChargeAt;
    private final String paidThrough;
    private final String termEndsAt;
    private final String endsAt;
    private final String cancelRequestedAt;
    private final ShortCodeLeasePastDue pastDue;
    private final int dueMonths;
    private final int dueCents;

    public ShortCodeLease(JsonObject json) {
        this.monthlyCents = ShortCodeJson.integer(json, "monthlyCents");
        this.minimumTermMonths = ShortCodeJson.integer(json, "minimumTermMonths");
        this.startsAt = ShortCodeJson.string(json, "startsAt");
        this.state = ShortCodeJson.string(json, "state");
        this.billingEnabled = ShortCodeJson.bool(json, "billingEnabled");
        this.startedAt = ShortCodeJson.string(json, "startedAt");
        this.nextChargeAt = ShortCodeJson.string(json, "nextChargeAt");
        this.paidThrough = ShortCodeJson.string(json, "paidThrough");
        this.termEndsAt = ShortCodeJson.string(json, "termEndsAt");
        this.endsAt = ShortCodeJson.string(json, "endsAt");
        this.cancelRequestedAt = ShortCodeJson.string(json, "cancelRequestedAt");
        JsonObject pastDueJson = ShortCodeJson.object(json, "pastDue");
        this.pastDue = pastDueJson != null ? new ShortCodeLeasePastDue(pastDueJson) : null;
        this.dueMonths = ShortCodeJson.integer(json, "dueMonths");
        this.dueCents = ShortCodeJson.integer(json, "dueCents");
    }

    /** The monthly lease, in US cents. */
    public int getMonthlyCents() { return monthlyCents; }

    /** The lease runs at least this many months from go-live. */
    public int getMinimumTermMonths() { return minimumTermMonths; }

    /** {@code go_live} on the application's billing; null on a listed code. */
    public String getStartsAt() { return startsAt; }

    /** Where the lease stands, one of the {@code STATE_} constants; more values may be added. */
    public String getState() { return state; }

    /** False while Sendly is not billing leases yet. */
    public boolean isBillingEnabled() { return billingEnabled; }

    /** When the lease started (when the code went live), or null. */
    public String getStartedAt() { return startedAt; }

    /** When the next month is charged, or null when nothing more is billed. */
    public String getNextChargeAt() { return nextChargeAt; }

    /** The end of the last month paid for, or waived, or null. */
    public String getPaidThrough() { return paidThrough; }

    /** When the minimum term ends, or null. */
    public String getTermEndsAt() { return termEndsAt; }

    /** When a cancelled lease ends, or null. */
    public String getEndsAt() { return endsAt; }

    /** When the lease was cancelled, or null. */
    public String getCancelRequestedAt() { return cancelRequestedAt; }

    /** The oldest unpaid month, or null when nothing is unpaid. */
    public ShortCodeLeasePastDue getPastDue() { return pastDue; }

    /** Months that fell due and are charged in the next daily run. */
    public int getDueMonths() { return dueMonths; }

    /** What those months cost, in US cents. */
    public int getDueCents() { return dueCents; }
}
