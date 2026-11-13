package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * The business verification reported with the account. It decides where the
 * account can send.
 */
public class AccountVerification {
    private final String status;
    private final String type;
    private final String region;
    private final String submittedAt;
    private final String updatedAt;

    public AccountVerification(JsonObject json) {
        this.status = getStringOrNull(json, "status");
        this.type = getStringOrNull(json, "type");
        this.region = getStringOrNull(json, "region");
        this.submittedAt = getStringOrNull(json, "submittedAt");
        this.updatedAt = getStringOrNull(json, "updatedAt");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /**
     * Where the verification is, such as {@code pending}, {@code verified},
     * {@code approved} or {@code rejected}. Values the API adds later come
     * through unchanged.
     */
    public String getStatus() { return status; }
    /** What the verification covers: {@code toll_free}, {@code international} or {@code both}. */
    public String getType() { return type; }
    /** The verification's region: {@code us}, {@code intl} or {@code both}; null when it has none. */
    public String getRegion() { return region; }
    /** When the verification was created. */
    public String getSubmittedAt() { return submittedAt; }
    /** When the verification last changed. */
    public String getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "AccountVerification{status='" + status + "', type='" + type + "', region='" + region + "'}";
    }
}
