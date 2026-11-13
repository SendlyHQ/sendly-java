package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * One of the workspace's short code applications, with its code once Sendly
 * assigns one. {@link #getShortCode()} is null until then, and drafts,
 * rejected and cancelled applications are listed too.
 */
public class ShortCode {
    public static final String STATUS_REQUESTED = "requested";
    public static final String STATUS_PROVISIONING = "provisioning";
    public static final String STATUS_PARKED = "parked";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_SUSPENDED = "suspended";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String REVIEW_DRAFT = "draft";
    public static final String REVIEW_AWAITING_REVIEW = "awaiting_review";
    public static final String REVIEW_CHANGES_REQUESTED = "changes_requested";
    public static final String REVIEW_APPROVED_FOR_FILING = "approved_for_filing";
    public static final String REVIEW_FILED = "filed";
    public static final String REVIEW_REJECTED = "rejected";

    private final String id;
    private final String shortCode;
    private final String countryCode;
    private final String status;
    private final String reviewStatus;
    private final String useCase;
    private final String createdAt;
    private final ShortCodeBilling billing;

    public ShortCode(JsonObject json) {
        this.id = ShortCodeJson.string(json, "id");
        this.shortCode = ShortCodeJson.string(json, "shortCode");
        this.countryCode = ShortCodeJson.string(json, "countryCode");
        this.status = ShortCodeJson.string(json, "status");
        this.reviewStatus = ShortCodeJson.string(json, "reviewStatus");
        this.useCase = ShortCodeJson.string(json, "useCase");
        this.createdAt = ShortCodeJson.string(json, "createdAt");
        JsonObject billingJson = ShortCodeJson.object(json, "billing");
        this.billing = billingJson != null ? new ShortCodeBilling(billingJson) : null;
    }

    /** The application's id. */
    public String getId() { return id; }

    /** The code's digits, or null until Sendly assigns a code. */
    public String getShortCode() { return shortCode; }

    /** Always {@code US} today. */
    public String getCountryCode() { return countryCode; }

    /**
     * Where the code stands, one of the {@code STATUS_} constants; more
     * values may be added. Only {@link #STATUS_ACTIVE} can send.
     */
    public String getStatus() { return status; }

    /** Where Sendly's review stands, one of the {@code REVIEW_} constants; more values may be added. */
    public String getReviewStatus() { return reviewStatus; }

    /** What the code is for, as the customer wrote it. */
    public String getUseCase() { return useCase; }

    /** When the application was started. */
    public String getCreatedAt() { return createdAt; }

    /** Where the code's setup fee and lease stand. */
    public ShortCodeBilling getBilling() { return billing; }

    /** Whether the code can send. */
    public boolean isActive() { return STATUS_ACTIVE.equals(status); }
}
