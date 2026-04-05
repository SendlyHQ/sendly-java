package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Represents the result of a single message in a batch.
 */
public class BatchMessageResult {
    private final String id;
    private final String to;
    private final String status;
    private final String error;
    private final String createdAt;
    private final String deliveredAt;

    /**
     * Create a BatchMessageResult from a JSON object.
     */
    public BatchMessageResult(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.to = getStringOrNull(json, "to");
        this.status = getStringOrNull(json, "status");
        this.error = getStringOrNull(json, "error");
        this.createdAt = getStringOrNull(json, "createdAt");
        this.deliveredAt = getStringOrNull(json, "deliveredAt");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    public String getId() {
        return id;
    }

    public String getTo() {
        return to;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }

    /**
     * Check if this message was queued successfully.
     */
    public boolean isSuccess() {
        return "queued".equals(status) || "sent".equals(status) || "delivered".equals(status);
    }

    /**
     * Check if this message failed.
     */
    public boolean isFailed() {
        return "failed".equals(status) || "bounced".equals(status);
    }

    @Override
    public String toString() {
        return "BatchMessageResult{" +
                "id='" + id + '\'' +
                ", to='" + to + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
