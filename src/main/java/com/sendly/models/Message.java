package com.sendly.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an SMS message.
 */
public class Message {
    // Message status constants (sending removed - doesn't exist in database)
    public static final String STATUS_QUEUED = "queued";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_BOUNCED = "bounced";

    // Sender type constants
    public static final String SENDER_TYPE_NUMBER_POOL = "number_pool";
    public static final String SENDER_TYPE_ALPHANUMERIC = "alphanumeric";
    public static final String SENDER_TYPE_SANDBOX = "sandbox";

    private final String id;
    private final String to;
    private final String from;
    private final String text;
    private final String status;
    private final String direction;
    private final int segments;

    @SerializedName("credits_used")
    private final int creditsUsed;

    @SerializedName("is_sandbox")
    private final boolean isSandbox;

    @SerializedName("sender_type")
    private final String senderType;

    @SerializedName("telnyx_message_id")
    private final String telnyxMessageId;

    private final String warning;

    @SerializedName("sender_note")
    private final String senderNote;

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

    private final Map<String, Object> metadata;

    /**
     * Create a Message from a JSON object.
     */
    public Message(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.to = getStringOrNull(json, "to");
        this.from = getStringOrNull(json, "from");
        this.text = getStringOrNull(json, "text");
        this.status = getStringOrNull(json, "status");
        this.direction = json.has("direction") ? json.get("direction").getAsString() : "outbound";
        this.segments = json.has("segments") ? json.get("segments").getAsInt() : 1;
        this.creditsUsed = json.has("credits_used") || json.has("creditsUsed") ?
            (json.has("credits_used") ? json.get("credits_used").getAsInt() : json.get("creditsUsed").getAsInt()) : 0;
        this.isSandbox = json.has("is_sandbox") || json.has("isSandbox") ?
            (json.has("is_sandbox") ? json.get("is_sandbox").getAsBoolean() : json.get("isSandbox").getAsBoolean()) : false;
        this.senderType = getStringOrNull(json, "sender_type", "senderType");
        this.telnyxMessageId = getStringOrNull(json, "telnyx_message_id", "telnyxMessageId");
        this.warning = getStringOrNull(json, "warning");
        this.senderNote = getStringOrNull(json, "sender_note", "senderNote");
        this.createdAt = parseInstant(getStringOrNull(json, "created_at", "createdAt"));
        this.updatedAt = parseInstant(getStringOrNull(json, "updated_at", "updatedAt"));
        this.deliveredAt = parseInstant(getStringOrNull(json, "delivered_at", "deliveredAt"));
        this.errorCode = getStringOrNull(json, "error_code", "errorCode");
        this.errorMessage = getStringOrNull(json, "error_message", "errorMessage");
        this.metadata = parseMetadata(json);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMetadata(JsonObject json) {
        if (json.has("metadata") && !json.get("metadata").isJsonNull() && json.get("metadata").isJsonObject()) {
            Map<String, Object> result = new HashMap<>();
            JsonObject metaObj = json.getAsJsonObject("metadata");
            for (String key : metaObj.keySet()) {
                if (!metaObj.get(key).isJsonNull()) {
                    if (metaObj.get(key).isJsonPrimitive()) {
                        var prim = metaObj.get(key).getAsJsonPrimitive();
                        if (prim.isString()) {
                            result.put(key, prim.getAsString());
                        } else if (prim.isNumber()) {
                            result.put(key, prim.getAsNumber());
                        } else if (prim.isBoolean()) {
                            result.put(key, prim.getAsBoolean());
                        }
                    } else {
                        result.put(key, metaObj.get(key).toString());
                    }
                }
            }
            return result;
        }
        return null;
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private String getStringOrNull(JsonObject json, String key1, String key2) {
        if (json.has(key1) && !json.get(key1).isJsonNull()) {
            return json.get(key1).getAsString();
        }
        if (json.has(key2) && !json.get(key2).isJsonNull()) {
            return json.get(key2).getAsString();
        }
        return null;
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

    public String getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    public String getDirection() {
        return direction;
    }

    public int getSegments() {
        return segments;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public boolean isSandbox() {
        return isSandbox;
    }

    public String getSenderType() {
        return senderType;
    }

    public String getTelnyxMessageId() {
        return telnyxMessageId;
    }

    public String getWarning() {
        return warning;
    }

    public String getSenderNote() {
        return senderNote;
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

    public Map<String, Object> getMetadata() {
        return metadata;
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
     * Check if the message bounced (carrier rejected).
     */
    public boolean isBounced() {
        return STATUS_BOUNCED.equals(status);
    }

    /**
     * Check if the message is pending.
     */
    public boolean isPending() {
        return STATUS_QUEUED.equals(status) || STATUS_SENT.equals(status);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", status='" + status + '\'' +
                ", direction='" + direction + '\'' +
                ", creditsUsed=" + creditsUsed +
                '}';
    }
}
