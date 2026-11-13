package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * The short code application a workspace is filling in, or has filed.
 * Text answers are null until saved.
 */
public class ShortCodeApplication {
    public static final String ORDER_TYPE_NEW = "new";
    public static final String ORDER_TYPE_MIGRATION = "migration";
    public static final String CODE_TYPE_RANDOM = "random";
    public static final String CODE_TYPE_VANITY = "vanity";

    private final String id;
    private final String shortCode;
    private final String countryCode;
    private final String status;
    private final String reviewStatus;
    private final String reviewNote;
    private final String orderType;
    private final String codeType;
    private final boolean contentProviderSameAsBrand;
    private final List<String> sampleMessages;
    private final String useCase;
    private final String optInFlow;
    private final String optInConfirmation;
    private final String helpResponse;
    private final String stopConfirmation;
    private final String messageFrequency;
    private final String campaignKeyword;
    private final String expectedMonthlyVolume;
    private final String expectedDailyVolume;
    private final String privacyPolicyUrl;
    private final String termsUrl;
    private final String requestedDigits;
    private final String losingProvider;
    private final String brandContactName;
    private final String brandContactEmail;
    private final String brandContactPhone;
    private final String contentProviderLegalName;
    private final String contentProviderEin;
    private final String contentProviderContactName;
    private final String contentProviderContactEmail;
    private final String contentProviderContactPhone;
    private final String brandRegistrationStatus;
    private final String contentProviderRegistrationStatus;
    private final String submittedAt;
    private final String reviewedAt;
    private final String filedAt;
    private final String activatedAt;
    private final String registryRevettingDueAt;
    private final String createdAt;
    private final String updatedAt;

    public ShortCodeApplication(JsonObject json) {
        this.id = ShortCodeJson.string(json, "id");
        this.shortCode = ShortCodeJson.string(json, "shortCode");
        this.countryCode = ShortCodeJson.string(json, "countryCode");
        this.status = ShortCodeJson.string(json, "status");
        this.reviewStatus = ShortCodeJson.string(json, "reviewStatus");
        this.reviewNote = ShortCodeJson.string(json, "reviewNote");
        this.orderType = ShortCodeJson.string(json, "orderType");
        this.codeType = ShortCodeJson.string(json, "codeType");
        this.contentProviderSameAsBrand = ShortCodeJson.bool(json, "contentProviderSameAsBrand");
        this.sampleMessages = ShortCodeJson.strings(json, "sampleMessages");
        this.useCase = ShortCodeJson.string(json, "useCase");
        this.optInFlow = ShortCodeJson.string(json, "optInFlow");
        this.optInConfirmation = ShortCodeJson.string(json, "optInConfirmation");
        this.helpResponse = ShortCodeJson.string(json, "helpResponse");
        this.stopConfirmation = ShortCodeJson.string(json, "stopConfirmation");
        this.messageFrequency = ShortCodeJson.string(json, "messageFrequency");
        this.campaignKeyword = ShortCodeJson.string(json, "campaignKeyword");
        this.expectedMonthlyVolume = ShortCodeJson.string(json, "expectedMonthlyVolume");
        this.expectedDailyVolume = ShortCodeJson.string(json, "expectedDailyVolume");
        this.privacyPolicyUrl = ShortCodeJson.string(json, "privacyPolicyUrl");
        this.termsUrl = ShortCodeJson.string(json, "termsUrl");
        this.requestedDigits = ShortCodeJson.string(json, "requestedDigits");
        this.losingProvider = ShortCodeJson.string(json, "losingProvider");
        this.brandContactName = ShortCodeJson.string(json, "brandContactName");
        this.brandContactEmail = ShortCodeJson.string(json, "brandContactEmail");
        this.brandContactPhone = ShortCodeJson.string(json, "brandContactPhone");
        this.contentProviderLegalName = ShortCodeJson.string(json, "contentProviderLegalName");
        this.contentProviderEin = ShortCodeJson.string(json, "contentProviderEin");
        this.contentProviderContactName = ShortCodeJson.string(json, "contentProviderContactName");
        this.contentProviderContactEmail = ShortCodeJson.string(json, "contentProviderContactEmail");
        this.contentProviderContactPhone = ShortCodeJson.string(json, "contentProviderContactPhone");
        this.brandRegistrationStatus = ShortCodeJson.string(json, "brandRegistrationStatus");
        this.contentProviderRegistrationStatus = ShortCodeJson.string(json, "contentProviderRegistrationStatus");
        this.submittedAt = ShortCodeJson.string(json, "submittedAt");
        this.reviewedAt = ShortCodeJson.string(json, "reviewedAt");
        this.filedAt = ShortCodeJson.string(json, "filedAt");
        this.activatedAt = ShortCodeJson.string(json, "activatedAt");
        this.registryRevettingDueAt = ShortCodeJson.string(json, "registryRevettingDueAt");
        this.createdAt = ShortCodeJson.string(json, "createdAt");
        this.updatedAt = ShortCodeJson.string(json, "updatedAt");
    }

    /** The application's id, or null before the first save. */
    public String getId() { return id; }

    /** The code's digits, or null until Sendly assigns a code. */
    public String getShortCode() { return shortCode; }

    public String getCountryCode() { return countryCode; }

    /** Where the code stands, one of the {@code ShortCode.STATUS_} constants. */
    public String getStatus() { return status; }

    /** Where Sendly's review stands, one of the {@code ShortCode.REVIEW_} constants. */
    public String getReviewStatus() { return reviewStatus; }

    /** Sendly's note from the review, such as what to change. */
    public String getReviewNote() { return reviewNote; }

    /** {@link #ORDER_TYPE_NEW} or {@link #ORDER_TYPE_MIGRATION}. */
    public String getOrderType() { return orderType; }

    /** {@link #CODE_TYPE_RANDOM} or {@link #CODE_TYPE_VANITY}. */
    public String getCodeType() { return codeType; }

    public boolean isContentProviderSameAsBrand() { return contentProviderSameAsBrand; }

    /** Example messages; empty before the first save. */
    public List<String> getSampleMessages() { return sampleMessages; }

    public String getUseCase() { return useCase; }

    public String getOptInFlow() { return optInFlow; }

    public String getOptInConfirmation() { return optInConfirmation; }

    public String getHelpResponse() { return helpResponse; }

    public String getStopConfirmation() { return stopConfirmation; }

    public String getMessageFrequency() { return messageFrequency; }

    public String getCampaignKeyword() { return campaignKeyword; }

    public String getExpectedMonthlyVolume() { return expectedMonthlyVolume; }

    public String getExpectedDailyVolume() { return expectedDailyVolume; }

    public String getPrivacyPolicyUrl() { return privacyPolicyUrl; }

    public String getTermsUrl() { return termsUrl; }

    /** The digits asked for: a vanity code, or the code being migrated. */
    public String getRequestedDigits() { return requestedDigits; }

    /** The provider a migrated code is moving from. */
    public String getLosingProvider() { return losingProvider; }

    public String getBrandContactName() { return brandContactName; }

    public String getBrandContactEmail() { return brandContactEmail; }

    public String getBrandContactPhone() { return brandContactPhone; }

    public String getContentProviderLegalName() { return contentProviderLegalName; }

    public String getContentProviderEin() { return contentProviderEin; }

    public String getContentProviderContactName() { return contentProviderContactName; }

    public String getContentProviderContactEmail() { return contentProviderContactEmail; }

    public String getContentProviderContactPhone() { return contentProviderContactPhone; }

    public String getBrandRegistrationStatus() { return brandRegistrationStatus; }

    public String getContentProviderRegistrationStatus() { return contentProviderRegistrationStatus; }

    public String getSubmittedAt() { return submittedAt; }

    /** When Sendly finished reviewing the application. */
    public String getReviewedAt() { return reviewedAt; }

    public String getFiledAt() { return filedAt; }

    public String getActivatedAt() { return activatedAt; }

    /** When the registry registration is next due to be re-vetted. */
    public String getRegistryRevettingDueAt() { return registryRevettingDueAt; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }
}
