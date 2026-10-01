package com.sendly.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Represents a scheduled SMS message.
 */
public class ScheduledMessage {
    public static final String STATUS_SCHEDULED = "scheduled";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_FAILED = "failed";

    private final String id;
    private final String to;
    private final String text;
    private final String from;
    private final String status;

    @SerializedName(value = "scheduledAt", alternate = {"scheduled_at"})
    private final Instant scheduledAt;

    @SerializedName(value = "creditsReserved", alternate = {"credits_reserved"})
    private final int creditsReserved;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private final Instant createdAt;

    @SerializedName(value = "cancelledAt", alternate = {"cancelled_at"})
    private final Instant cancelledAt;

    @SerializedName(value = "sentAt", alternate = {"sent_at"})
    private final Instant sentAt;

    private final String error;

    /**
     * Create a ScheduledMessage from a JSON object.
     */
    public ScheduledMessage(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.to = getStringOrNull(json, "to");
        this.text = getStringOrNull(json, "text");
        this.from = getStringOrNull(json, "from");
        this.status = getStringOrNull(json, "status");
        this.scheduledAt = parseInstant(getStringOrNull(json, "scheduledAt", "scheduled_at"));
        this.creditsReserved = getIntOrZero(json, "creditsReserved", "credits_reserved");
        this.createdAt = parseInstant(getStringOrNull(json, "createdAt", "created_at"));
        this.cancelledAt = parseInstant(getStringOrNull(json, "cancelledAt", "cancelled_at"));
        this.sentAt = parseInstant(getStringOrNull(json, "sentAt", "sent_at"));
        this.error = getStringOrNull(json, "error");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private String getStringOrNull(JsonObject json, String key, String fallbackKey) {
        String value = getStringOrNull(json, key);
        return value != null ? value : getStringOrNull(json, fallbackKey);
    }

    private int getIntOrZero(JsonObject json, String key, String fallbackKey) {
        for (String k : new String[] {key, fallbackKey}) {
            if (json.has(k) && !json.get(k).isJsonNull()) {
                return json.get(k).getAsInt();
            }
        }
        return 0;
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

    public String getFrom() {
        return from;
    }

    public String getStatus() {
        return status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public int getCreditsReserved() {
        return creditsReserved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getError() {
        return error;
    }

    // Helper methods

    /**
     * Check if the message is still scheduled (pending delivery).
     */
    public boolean isScheduled() {
        return STATUS_SCHEDULED.equals(status);
    }

    /**
     * Check if the message was sent.
     */
    public boolean isSent() {
        return STATUS_SENT.equals(status);
    }

    /**
     * Check if the message was cancelled.
     */
    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    /**
     * Check if the message failed.
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    @Override
    public String toString() {
        return "ScheduledMessage{" +
                "id='" + id + '\'' +
                ", to='" + to + '\'' +
                ", status='" + status + '\'' +
                ", scheduledAt=" + scheduledAt +
                ", creditsReserved=" + creditsReserved +
                '}';
    }
}
