package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A phone number available to buy. {@code monthlyCost} is already priced for
 * your account (marked up) and returned as a string.
 */
public class AvailableNumber {
    private String phoneNumber;
    private String country;
    private String numberType;
    private String monthlyCost;
    private String currency;

    public AvailableNumber() {}

    public AvailableNumber(JsonObject json) {
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("country") && !json.get("country").isJsonNull()) {
            this.country = json.get("country").getAsString();
        }
        if (json.has("numberType") && !json.get("numberType").isJsonNull()) {
            this.numberType = json.get("numberType").getAsString();
        }
        if (json.has("monthlyCost") && !json.get("monthlyCost").isJsonNull()) {
            this.monthlyCost = json.get("monthlyCost").getAsString();
        }
        if (json.has("currency") && !json.get("currency").isJsonNull()) {
            this.currency = json.get("currency").getAsString();
        }
    }

    /** Phone number in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /** ISO 3166-1 alpha-2 country code. */
    public String getCountry() { return country; }

    /** Number type (e.g. "mobile", "local", "toll_free"). */
    public String getNumberType() { return numberType; }

    /** Customer-facing monthly cost (already marked up), as a string. */
    public String getMonthlyCost() { return monthlyCost; }

    /** ISO 4217 currency code for {@link #getMonthlyCost()} (e.g. "USD"). */
    public String getCurrency() { return currency; }
}
