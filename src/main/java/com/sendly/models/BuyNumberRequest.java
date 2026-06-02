package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Request body for {@code numbers().buy()}. Build it from a listing returned by
 * {@code numbers().listAvailable()}.
 *
 * <pre>{@code
 * BuyNumberRequest request = BuyNumberRequest.builder()
 *     .phoneNumber("+447700900123")
 *     .countryCode("GB")
 *     .phoneNumberType("mobile")
 *     .monthlyCost("1.50")
 *     .build();
 * }</pre>
 */
public class BuyNumberRequest {
    private final String phoneNumber;
    private final String countryCode;
    private final String phoneNumberType;
    private final String monthlyCost;
    private final String actionCode;

    private BuyNumberRequest(Builder builder) {
        this.phoneNumber = builder.phoneNumber;
        this.countryCode = builder.countryCode;
        this.phoneNumberType = builder.phoneNumberType;
        this.monthlyCost = builder.monthlyCost;
        this.actionCode = builder.actionCode;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getCountryCode() { return countryCode; }
    public String getPhoneNumberType() { return phoneNumberType; }
    public String getMonthlyCost() { return monthlyCost; }
    public String getActionCode() { return actionCode; }

    /** Serialize to the JSON body the API expects (camelCase keys). */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (phoneNumber != null) o.addProperty("phoneNumber", phoneNumber);
        if (countryCode != null) o.addProperty("countryCode", countryCode);
        if (phoneNumberType != null) o.addProperty("phoneNumberType", phoneNumberType);
        if (monthlyCost != null) o.addProperty("monthlyCost", monthlyCost);
        if (actionCode != null) o.addProperty("actionCode", actionCode);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String phoneNumber;
        private String countryCode;
        private String phoneNumberType;
        private String monthlyCost;
        private String actionCode;

        /** Phone number to buy, in E.164 format. */
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /** ISO 3166-1 alpha-2 country code. */
        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        /** Number type (e.g. "mobile", "local", "toll_free"). */
        public Builder phoneNumberType(String phoneNumberType) {
            this.phoneNumberType = phoneNumberType;
            return this;
        }

        /** Customer-facing monthly cost from the available-numbers listing. */
        public Builder monthlyCost(String monthlyCost) {
            this.monthlyCost = monthlyCost;
            return this;
        }

        /**
         * The code from a COMPLETED action. Set this only when re-calling
         * {@code buy()} after the user finished a {@code documents_required} /
         * {@code payment_required} hosted-page step.
         */
        public Builder actionCode(String actionCode) {
            this.actionCode = actionCode;
            return this;
        }

        public BuyNumberRequest build() {
            return new BuyNumberRequest(this);
        }
    }
}
