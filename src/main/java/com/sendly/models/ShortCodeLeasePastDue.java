package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * The oldest lease month of a short code that has not been paid.
 */
public class ShortCodeLeasePastDue {
    private final String chargeId;
    private final int amountCents;
    private final String status;
    private final String periodStart;
    private final String periodEnd;
    private final String firstFailedAt;
    private final String pauseAt;
    private final String noticeStage;

    public ShortCodeLeasePastDue(JsonObject json) {
        this.chargeId = ShortCodeJson.string(json, "chargeId");
        this.amountCents = ShortCodeJson.integer(json, "amountCents");
        this.status = ShortCodeJson.string(json, "status");
        this.periodStart = ShortCodeJson.string(json, "periodStart");
        this.periodEnd = ShortCodeJson.string(json, "periodEnd");
        this.firstFailedAt = ShortCodeJson.string(json, "firstFailedAt");
        this.pauseAt = ShortCodeJson.string(json, "pauseAt");
        this.noticeStage = ShortCodeJson.string(json, "noticeStage");
    }

    /** The unpaid lease charge. */
    public String getChargeId() { return chargeId; }

    /** What the month costs, in US cents. */
    public int getAmountCents() { return amountCents; }

    /** The charge's status. */
    public String getStatus() { return status; }

    /** The start of the month it pays for, or null. */
    public String getPeriodStart() { return periodStart; }

    /** The end of the month it pays for, or null. */
    public String getPeriodEnd() { return periodEnd; }

    /** When charging it first failed, or null. */
    public String getFirstFailedAt() { return firstFailedAt; }

    /** When sending from the code pauses if the month is still unpaid, or null. */
    public String getPauseAt() { return pauseAt; }

    /** The last payment notice sent, or null. */
    public String getNoticeStage() { return noticeStage; }
}
