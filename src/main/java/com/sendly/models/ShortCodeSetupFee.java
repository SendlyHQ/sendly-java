package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Where a short code's one-time setup fee stands.
 */
public class ShortCodeSetupFee {
    public static final String STATUS_UNPAID = "unpaid";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_REQUIRES_ACTION = "requires_action";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_NO_PAYMENT_METHOD = "no_payment_method";
    public static final String STATUS_PAID = "paid";
    public static final String STATUS_WAIVED = "waived";
    public static final String STATUS_REFUND_PENDING = "refund_pending";
    public static final String STATUS_REFUNDED = "refunded";

    private final int amountCents;
    private final String status;
    private final String chargedAt;
    private final String paidAt;
    private final int refundedCents;
    private final String refundedAt;

    public ShortCodeSetupFee(JsonObject json) {
        this.amountCents = ShortCodeJson.integer(json, "amountCents");
        this.status = ShortCodeJson.string(json, "status");
        this.chargedAt = ShortCodeJson.string(json, "chargedAt");
        this.paidAt = ShortCodeJson.string(json, "paidAt");
        this.refundedCents = ShortCodeJson.integer(json, "refundedCents");
        this.refundedAt = ShortCodeJson.string(json, "refundedAt");
    }

    /** The setup fee, in US cents. */
    public int getAmountCents() { return amountCents; }

    /** One of the {@code STATUS_} constants; more values may be added. */
    public String getStatus() { return status; }

    /** {@code submit} on the application's billing; null on a listed code. */
    public String getChargedAt() { return chargedAt; }

    /** When the fee was paid, or null (always null on a listed code). */
    public String getPaidAt() { return paidAt; }

    /** How much was refunded, in US cents (0 on a listed code). */
    public int getRefundedCents() { return refundedCents; }

    /** When the fee was refunded, or null (always null on a listed code). */
    public String getRefundedAt() { return refundedAt; }
}
