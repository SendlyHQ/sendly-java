package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A number connected (or connecting) to WhatsApp.
 */
public class WhatsAppSender {
    private String phoneNumber;
    private String displayName;
    private String status;
    private String qualityRating;
    private String createdAt;

    public WhatsAppSender() {}

    public WhatsAppSender(JsonObject json) {
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("displayName") && !json.get("displayName").isJsonNull()) {
            this.displayName = json.get("displayName").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("qualityRating") && !json.get("qualityRating").isJsonNull()) {
            this.qualityRating = json.get("qualityRating").getAsString();
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
    }

    /** The sender, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * The name recipients see — chosen during the connect flow and reviewed
     * by Meta; null until set.
     */
    public String getDisplayName() { return displayName; }

    /**
     * Connection status: {@code pending}, {@code active}, or
     * {@code suspended}.
     */
    public String getStatus() { return status; }

    /** Meta quality rating (e.g. "GREEN"), or null before first rating. */
    public String getQualityRating() { return qualityRating; }

    /** ISO 8601 timestamp when the sender was connected. */
    public String getCreatedAt() { return createdAt; }
}
