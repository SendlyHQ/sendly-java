package com.sendly.models;

import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Response from cancelling a scheduled message.
 */
public class CancelScheduledMessageResponse {
    private final String id;
    private final String status;
    private final int creditsRefunded;
    private final Instant cancelledAt;

    /**
     * Create a CancelScheduledMessageResponse from a JSON object.
     */
    public CancelScheduledMessageResponse(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.status = getStringOrNull(json, "status");
        this.creditsRefunded = json.has("credits_refunded") ? json.get("credits_refunded").getAsInt() : 0;
        this.cancelledAt = parseInstant(getStringOrNull(json, "cancelled_at"));
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Instant parseInstant(String value) {
        if (value == null) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public int getCreditsRefunded() {
        return creditsRefunded;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    @Override
    public String toString() {
        return "CancelScheduledMessageResponse{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", creditsRefunded=" + creditsRefunded +
                '}';
    }
}
