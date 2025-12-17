package com.sendly.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Represents an SMS message.
 */
public class Message {
    public static final String STATUS_QUEUED = "queued";
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_FAILED = "failed";

    private final String id;
    private final String to;
    private final String text;
    private final String status;

    @SerializedName("credits_used")
    private final int creditsUsed;

    @SerializedName("created_at")
    private final Instant createdAt;

    @SerializedName("updated_at")
    private final Instant updatedAt;

    @SerializedName("delivered_at")
    private final Instant deliveredAt;

    @SerializedName("error_code")
    private final String errorCode;

    @SerializedName("error_message")
    private final String errorMessage;

    /**
     * Create a Message from a JSON object.
     */
    public Message(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.to = getStringOrNull(json, "to");
        this.text = getStringOrNull(json, "text");
        this.status = getStringOrNull(json, "status");
        this.creditsUsed = json.has("credits_used") ? json.get("credits_used").getAsInt() : 0;
        this.createdAt = parseInstant(getStringOrNull(json, "created_at"));
        this.updatedAt = parseInstant(getStringOrNull(json, "updated_at"));
        this.deliveredAt = parseInstant(getStringOrNull(json, "delivered_at"));
        this.errorCode = getStringOrNull(json, "error_code");
        this.errorMessage = getStringOrNull(json, "error_message");
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

    // Getters

    public String getId() {
        return id;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Helper methods

    /**
     * Check if the message was delivered.
     */
    public boolean isDelivered() {
        return STATUS_DELIVERED.equals(status);
    }

    /**
     * Check if the message failed.
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    /**
     * Check if the message is pending.
     */
    public boolean isPending() {
        return STATUS_QUEUED.equals(status) ||
               STATUS_SENDING.equals(status) ||
               STATUS_SENT.equals(status);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", to='" + to + '\'' +
                ", status='" + status + '\'' +
                ", creditsUsed=" + creditsUsed +
                '}';
    }
}
