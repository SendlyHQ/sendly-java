package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A short code application plus everything needed to act on it: the
 * documents, the quote, the billing, the business verification and each
 * carrier's certification. Returned by get, update and submit; the
 * submit-only and save-only fields are null or empty otherwise.
 */
public class ShortCodeApplicationView {
    private final boolean enabled;
    private final ShortCodeApplication application;
    private final JsonObject prefill;
    private final boolean editable;
    private final String lockMessage;
    private final boolean canStartNewApplication;
    private final JsonObject generatedDefaults;
    private final List<String> requiredDocuments;
    private final List<String> missingDocuments;
    private final List<ShortCodeDocument> documents;
    private final JsonObject quote;
    private final ShortCodeBilling billing;
    private final boolean verified;
    private final String verificationState;
    private final String verificationHref;
    private final JsonObject carriers;
    private final Boolean submitted;
    private final Boolean alreadySubmitted;
    private final Boolean notified;
    private final String paymentStatus;
    private final Boolean paymentCharged;
    private final List<String> ignoredFields;

    public ShortCodeApplicationView(JsonObject json) {
        this.enabled = ShortCodeJson.bool(json, "enabled");
        JsonObject applicationJson = ShortCodeJson.object(json, "application");
        this.application = applicationJson != null ? new ShortCodeApplication(applicationJson) : null;
        this.prefill = ShortCodeJson.object(json, "prefill");
        this.editable = ShortCodeJson.bool(json, "editable");
        this.lockMessage = ShortCodeJson.string(json, "lockMessage");
        this.canStartNewApplication = ShortCodeJson.bool(json, "canStartNewApplication");
        this.generatedDefaults = ShortCodeJson.object(json, "generatedDefaults");
        this.requiredDocuments = ShortCodeJson.strings(json, "requiredDocuments");
        this.missingDocuments = ShortCodeJson.strings(json, "missingDocuments");
        this.documents = documentsOf(json);
        this.quote = ShortCodeJson.object(json, "quote");
        JsonObject billingJson = ShortCodeJson.object(json, "billing");
        this.billing = billingJson != null ? new ShortCodeBilling(billingJson) : null;
        JsonObject verification = ShortCodeJson.object(json, "verification");
        this.verified = ShortCodeJson.bool(verification, "verified");
        this.verificationState = ShortCodeJson.string(verification, "state");
        this.verificationHref = ShortCodeJson.string(verification, "href");
        this.carriers = ShortCodeJson.object(json, "carriers");
        this.submitted = ShortCodeJson.optionalBool(json, "submitted");
        this.alreadySubmitted = ShortCodeJson.optionalBool(json, "alreadySubmitted");
        this.notified = ShortCodeJson.optionalBool(json, "notified");
        JsonObject payment = ShortCodeJson.object(json, "payment");
        this.paymentStatus = ShortCodeJson.string(payment, "status");
        this.paymentCharged = ShortCodeJson.optionalBool(payment, "charged");
        this.ignoredFields = ShortCodeJson.strings(json, "ignoredFields");
    }

    static List<ShortCodeDocument> documentsOf(JsonObject json) {
        List<ShortCodeDocument> list = new ArrayList<>();
        for (JsonObject document : ShortCodeJson.objects(json, "documents")) {
            list.add(new ShortCodeDocument(document));
        }
        return Collections.unmodifiableList(list);
    }

    /** Always true: short codes not enabled for the account answer 404 {@code short_codes_not_enabled}. */
    public boolean isEnabled() { return enabled; }

    /** The application. */
    public ShortCodeApplication getApplication() { return application; }

    /**
     * Suggestions from the workspace's 10DLC brand or toll-free verification
     * ({@code source}, {@code legalName}, {@code ein}, {@code address} and
     * so on). Never saved unless sent back with a save.
     */
    public JsonObject getPrefill() { return prefill; }

    /** Whether a save can change the application. */
    public boolean isEditable() { return editable; }

    /** Why the application can't be changed, or null when it can. */
    public String getLockMessage() { return lockMessage; }

    public boolean canStartNewApplication() { return canStartNewApplication; }

    /**
     * Wording Sendly suggested ({@code useCase}, {@code optInFlow}), or
     * null. Submit refuses it with {@code short_code_ai_draft_unedited}
     * until a person has edited it.
     */
    public JsonObject getGeneratedDefaults() { return generatedDefaults; }

    /** The forms this order type needs. */
    public List<String> getRequiredDocuments() { return requiredDocuments; }

    /** The required forms not signed yet. */
    public List<String> getMissingDocuments() { return missingDocuments; }

    /** The forms Sendly has prepared for this application. */
    public List<ShortCodeDocument> getDocuments() { return documents; }

    /**
     * The price: {@code setupCents}, {@code monthlyCents},
     * {@code minimumTermMonths}, {@code setupUsd}, {@code monthlyUsd} and
     * {@code codeType}.
     */
    public JsonObject getQuote() { return quote; }

    /** Where the setup fee and the lease stand, and every charge. */
    public ShortCodeBilling getBilling() { return billing; }

    /** Whether the workspace's business is verified, which submitting needs. */
    public boolean isVerified() { return verified; }

    /** {@code verified}, {@code in_review}, {@code needs_attention} or {@code not_started}. */
    public String getVerificationState() { return verificationState; }

    /** The dashboard page for the verification. */
    public String getVerificationHref() { return verificationHref; }

    /**
     * Each carrier's certification: {@code approved}, {@code total},
     * {@code overall} and {@code byCarrier}.
     */
    public JsonObject getCarriers() { return carriers; }

    /** On submit: always true. Null otherwise. */
    public Boolean getSubmitted() { return submitted; }

    /** On submit: true when it was already with Sendly, and nothing was charged. Null otherwise. */
    public Boolean getAlreadySubmitted() { return alreadySubmitted; }

    /** On submit: whether Sendly's review team was told. Null otherwise. */
    public Boolean getNotified() { return notified; }

    /** On submit: the setup fee's status ({@code ShortCodeSetupFee.STATUS_}). Null otherwise. */
    public String getPaymentStatus() { return paymentStatus; }

    /** On submit: whether this call charged the card. Null otherwise. */
    public Boolean getPaymentCharged() { return paymentCharged; }

    /** On a save: fields Sendly or the carriers own, which were ignored; empty when none. */
    public List<String> getIgnoredFields() { return ignoredFields; }
}
