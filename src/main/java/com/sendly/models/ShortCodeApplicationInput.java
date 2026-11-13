package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Answers to save on a short code application, or to check or submit with
 * it. Only the fields you set are sent; fields Sendly or the carriers own
 * are not settable.
 */
public class ShortCodeApplicationInput {
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
    private final String countryCode;
    private final String orderType;
    private final String codeType;
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
    private final Boolean contentProviderSameAsBrand;
    private final List<String> sampleMessages;
    private final Boolean acceptTerms;

    private ShortCodeApplicationInput(Builder builder) {
        this.useCase = builder.useCase;
        this.optInFlow = builder.optInFlow;
        this.optInConfirmation = builder.optInConfirmation;
        this.helpResponse = builder.helpResponse;
        this.stopConfirmation = builder.stopConfirmation;
        this.messageFrequency = builder.messageFrequency;
        this.campaignKeyword = builder.campaignKeyword;
        this.expectedMonthlyVolume = builder.expectedMonthlyVolume;
        this.expectedDailyVolume = builder.expectedDailyVolume;
        this.privacyPolicyUrl = builder.privacyPolicyUrl;
        this.termsUrl = builder.termsUrl;
        this.countryCode = builder.countryCode;
        this.orderType = builder.orderType;
        this.codeType = builder.codeType;
        this.requestedDigits = builder.requestedDigits;
        this.losingProvider = builder.losingProvider;
        this.brandContactName = builder.brandContactName;
        this.brandContactEmail = builder.brandContactEmail;
        this.brandContactPhone = builder.brandContactPhone;
        this.contentProviderLegalName = builder.contentProviderLegalName;
        this.contentProviderEin = builder.contentProviderEin;
        this.contentProviderContactName = builder.contentProviderContactName;
        this.contentProviderContactEmail = builder.contentProviderContactEmail;
        this.contentProviderContactPhone = builder.contentProviderContactPhone;
        this.contentProviderSameAsBrand = builder.contentProviderSameAsBrand;
        this.sampleMessages = builder.sampleMessages;
        this.acceptTerms = builder.acceptTerms;
    }

    /** Serialize to the JSON the API expects. Unset fields are omitted. */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (useCase != null) o.addProperty("useCase", useCase);
        if (optInFlow != null) o.addProperty("optInFlow", optInFlow);
        if (optInConfirmation != null) o.addProperty("optInConfirmation", optInConfirmation);
        if (helpResponse != null) o.addProperty("helpResponse", helpResponse);
        if (stopConfirmation != null) o.addProperty("stopConfirmation", stopConfirmation);
        if (messageFrequency != null) o.addProperty("messageFrequency", messageFrequency);
        if (campaignKeyword != null) o.addProperty("campaignKeyword", campaignKeyword);
        if (expectedMonthlyVolume != null) o.addProperty("expectedMonthlyVolume", expectedMonthlyVolume);
        if (expectedDailyVolume != null) o.addProperty("expectedDailyVolume", expectedDailyVolume);
        if (privacyPolicyUrl != null) o.addProperty("privacyPolicyUrl", privacyPolicyUrl);
        if (termsUrl != null) o.addProperty("termsUrl", termsUrl);
        if (countryCode != null) o.addProperty("countryCode", countryCode);
        if (orderType != null) o.addProperty("orderType", orderType);
        if (codeType != null) o.addProperty("codeType", codeType);
        if (requestedDigits != null) o.addProperty("requestedDigits", requestedDigits);
        if (losingProvider != null) o.addProperty("losingProvider", losingProvider);
        if (brandContactName != null) o.addProperty("brandContactName", brandContactName);
        if (brandContactEmail != null) o.addProperty("brandContactEmail", brandContactEmail);
        if (brandContactPhone != null) o.addProperty("brandContactPhone", brandContactPhone);
        if (contentProviderLegalName != null) o.addProperty("contentProviderLegalName", contentProviderLegalName);
        if (contentProviderEin != null) o.addProperty("contentProviderEin", contentProviderEin);
        if (contentProviderContactName != null) o.addProperty("contentProviderContactName", contentProviderContactName);
        if (contentProviderContactEmail != null) o.addProperty("contentProviderContactEmail", contentProviderContactEmail);
        if (contentProviderContactPhone != null) o.addProperty("contentProviderContactPhone", contentProviderContactPhone);
        if (contentProviderSameAsBrand != null) o.addProperty("contentProviderSameAsBrand", contentProviderSameAsBrand);
        if (sampleMessages != null) o.add("sampleMessages", ShortCodeJson.stringArray(sampleMessages));
        if (acceptTerms != null) o.addProperty("acceptTerms", acceptTerms);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String useCase;
        private String optInFlow;
        private String optInConfirmation;
        private String helpResponse;
        private String stopConfirmation;
        private String messageFrequency;
        private String campaignKeyword;
        private String expectedMonthlyVolume;
        private String expectedDailyVolume;
        private String privacyPolicyUrl;
        private String termsUrl;
        private String countryCode;
        private String orderType;
        private String codeType;
        private String requestedDigits;
        private String losingProvider;
        private String brandContactName;
        private String brandContactEmail;
        private String brandContactPhone;
        private String contentProviderLegalName;
        private String contentProviderEin;
        private String contentProviderContactName;
        private String contentProviderContactEmail;
        private String contentProviderContactPhone;
        private Boolean contentProviderSameAsBrand;
        private List<String> sampleMessages;
        private Boolean acceptTerms;

        /** What the code will be used for. */
        public Builder useCase(String useCase) {
            this.useCase = useCase;
            return this;
        }

        /** How subscribers opt in. */
        public Builder optInFlow(String optInFlow) {
            this.optInFlow = optInFlow;
            return this;
        }

        /** The message sent when someone opts in. */
        public Builder optInConfirmation(String optInConfirmation) {
            this.optInConfirmation = optInConfirmation;
            return this;
        }

        /** The reply to HELP. */
        public Builder helpResponse(String helpResponse) {
            this.helpResponse = helpResponse;
            return this;
        }

        /** The reply to STOP. */
        public Builder stopConfirmation(String stopConfirmation) {
            this.stopConfirmation = stopConfirmation;
            return this;
        }

        /** How often subscribers get messages, such as {@code 4 messages per month}. */
        public Builder messageFrequency(String messageFrequency) {
            this.messageFrequency = messageFrequency;
            return this;
        }

        /** The keyword subscribers text to join. */
        public Builder campaignKeyword(String campaignKeyword) {
            this.campaignKeyword = campaignKeyword;
            return this;
        }

        /** Expected messages a month. */
        public Builder expectedMonthlyVolume(String expectedMonthlyVolume) {
            this.expectedMonthlyVolume = expectedMonthlyVolume;
            return this;
        }

        /** Expected messages a day. */
        public Builder expectedDailyVolume(String expectedDailyVolume) {
            this.expectedDailyVolume = expectedDailyVolume;
            return this;
        }

        /** The privacy policy URL. */
        public Builder privacyPolicyUrl(String privacyPolicyUrl) {
            this.privacyPolicyUrl = privacyPolicyUrl;
            return this;
        }

        /** The terms and conditions URL. */
        public Builder termsUrl(String termsUrl) {
            this.termsUrl = termsUrl;
            return this;
        }

        /** Only {@code US}. */
        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        /** {@code new} for a new code or {@code migration} to move one from another provider. */
        public Builder orderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        /** {@code random} for any available code or {@code vanity} for chosen digits. */
        public Builder codeType(String codeType) {
            this.codeType = codeType;
            return this;
        }

        /** 5 or 6 digits: the vanity code, or the code being migrated. */
        public Builder requestedDigits(String requestedDigits) {
            this.requestedDigits = requestedDigits;
            return this;
        }

        /** For a migration, the provider the code is moving from. */
        public Builder losingProvider(String losingProvider) {
            this.losingProvider = losingProvider;
            return this;
        }

        /** The brand's contact for the Short Code Registry. */
        public Builder brandContactName(String brandContactName) {
            this.brandContactName = brandContactName;
            return this;
        }

        /** The brand contact's email. */
        public Builder brandContactEmail(String brandContactEmail) {
            this.brandContactEmail = brandContactEmail;
            return this;
        }

        /** The brand contact's phone number. */
        public Builder brandContactPhone(String brandContactPhone) {
            this.brandContactPhone = brandContactPhone;
            return this;
        }

        /** The content provider's legal name. */
        public Builder contentProviderLegalName(String contentProviderLegalName) {
            this.contentProviderLegalName = contentProviderLegalName;
            return this;
        }

        /** The content provider's EIN. */
        public Builder contentProviderEin(String contentProviderEin) {
            this.contentProviderEin = contentProviderEin;
            return this;
        }

        /** The content provider's contact. */
        public Builder contentProviderContactName(String contentProviderContactName) {
            this.contentProviderContactName = contentProviderContactName;
            return this;
        }

        /** The content provider contact's email. */
        public Builder contentProviderContactEmail(String contentProviderContactEmail) {
            this.contentProviderContactEmail = contentProviderContactEmail;
            return this;
        }

        /** The content provider contact's phone number. */
        public Builder contentProviderContactPhone(String contentProviderContactPhone) {
            this.contentProviderContactPhone = contentProviderContactPhone;
            return this;
        }

        /** Whether the content provider is the brand itself. */
        public Builder contentProviderSameAsBrand(boolean contentProviderSameAsBrand) {
            this.contentProviderSameAsBrand = contentProviderSameAsBrand;
            return this;
        }

        /** Real examples of the messages. */
        public Builder sampleMessages(List<String> sampleMessages) {
            this.sampleMessages = sampleMessages == null ? null : new ArrayList<>(sampleMessages);
            return this;
        }

        /**
         * For submit only: accept the one-time setup fee, the monthly lease
         * from go-live and the minimum term. Submit needs it to be true.
         */
        public Builder acceptTerms(boolean acceptTerms) {
            this.acceptTerms = acceptTerms;
            return this;
        }

        public ShortCodeApplicationInput build() {
            return new ShortCodeApplicationInput(this);
        }
    }
}
