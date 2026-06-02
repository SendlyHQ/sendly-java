package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A phone number you own.
 */
public class OwnedNumber {
    private String id;
    private String phoneNumber;
    private String status;
    private String source;
    private String countryCode;
    private String phoneNumberType;
    private Integer monthlyCostCents;

    public OwnedNumber() {}

    public OwnedNumber(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("source") && !json.get("source").isJsonNull()) {
            this.source = json.get("source").getAsString();
        }
        if (json.has("countryCode") && !json.get("countryCode").isJsonNull()) {
            this.countryCode = json.get("countryCode").getAsString();
        }
        if (json.has("phoneNumberType") && !json.get("phoneNumberType").isJsonNull()) {
            this.phoneNumberType = json.get("phoneNumberType").getAsString();
        }
        if (json.has("monthlyCostCents") && !json.get("monthlyCostCents").isJsonNull()) {
            this.monthlyCostCents = json.get("monthlyCostCents").getAsInt();
        }
    }

    /** Unique number identifier. */
    public String getId() { return id; }

    /** Phone number in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /** Provisioning / lifecycle status. */
    public String getStatus() { return status; }

    /** How the number was acquired (e.g. "purchased", "ported"). */
    public String getSource() { return source; }

    /** ISO 3166-1 alpha-2 country code. */
    public String getCountryCode() { return countryCode; }

    /** Number type (e.g. "mobile", "local", "toll_free"). */
    public String getPhoneNumberType() { return phoneNumberType; }

    /** Monthly cost in cents. */
    public Integer getMonthlyCostCents() { return monthlyCostCents; }
}
