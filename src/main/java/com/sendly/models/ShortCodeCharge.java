package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * One short code charge: the setup fee or a lease month.
 */
public class ShortCodeCharge {
    public static final String KIND_SETUP = "setup";
    public static final String KIND_LEASE = "lease";

    private final String id;
    private final String kind;
    private final int amountCents;
    private final String status;
    private final String paidAt;
    private final int refundedCents;
    private final String refundedAt;
    private final String periodStart;
    private final String periodEnd;
    private final String createdAt;

    public ShortCodeCharge(JsonObject json) {
        this.id = ShortCodeJson.string(json, "id");
        this.kind = ShortCodeJson.string(json, "kind");
        this.amountCents = ShortCodeJson.integer(json, "amountCents");
        this.status = ShortCodeJson.string(json, "status");
        this.paidAt = ShortCodeJson.string(json, "paidAt");
        this.refundedCents = ShortCodeJson.integer(json, "refundedCents");
        this.refundedAt = ShortCodeJson.string(json, "refundedAt");
        this.periodStart = ShortCodeJson.string(json, "periodStart");
        this.periodEnd = ShortCodeJson.string(json, "periodEnd");
        this.createdAt = ShortCodeJson.string(json, "createdAt");
    }

    public String getId() { return id; }

    /** {@link #KIND_SETUP} or {@link #KIND_LEASE}; more values may be added. */
    public String getKind() { return kind; }

    /** The amount, in US cents. */
    public int getAmountCents() { return amountCents; }

    /** {@code paid}, {@code refunded}, {@code waived}, {@code not_charged}, or a payment still being taken. */
    public String getStatus() { return status; }

    /** When it was paid, or null. */
    public String getPaidAt() { return paidAt; }

    /** How much was refunded, in US cents. */
    public int getRefundedCents() { return refundedCents; }

    /** When it was refunded, or null. */
    public String getRefundedAt() { return refundedAt; }

    /** For a lease charge, the start of the month it pays for; otherwise null. */
    public String getPeriodStart() { return periodStart; }

    /** For a lease charge, the end of the month it pays for; otherwise null. */
    public String getPeriodEnd() { return periodEnd; }

    /** When the charge was recorded, or null. */
    public String getCreatedAt() { return createdAt; }
}
