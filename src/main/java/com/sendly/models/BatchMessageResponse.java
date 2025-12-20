package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Response from a batch send operation.
 */
public class BatchMessageResponse {
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_PARTIAL_FAILURE = "partial_failure";
    public static final String STATUS_FAILED = "failed";

    private final String batchId;
    private final String status;
    private final int total;
    private final int queued;
    private final int failed;
    private final int creditsUsed;
    private final List<BatchMessageResult> messages;
    private final Instant createdAt;
    private final Instant completedAt;

    /**
     * Create a BatchMessageResponse from a JSON object.
     */
    public BatchMessageResponse(JsonObject json) {
        this.batchId = getStringOrNull(json, "batch_id");
        this.status = getStringOrNull(json, "status");
        this.total = json.has("total") ? json.get("total").getAsInt() : 0;
        this.queued = json.has("queued") ? json.get("queued").getAsInt() : 0;
        this.failed = json.has("failed") ? json.get("failed").getAsInt() : 0;
        this.creditsUsed = json.has("credits_used") ? json.get("credits_used").getAsInt() : 0;
        this.createdAt = parseInstant(getStringOrNull(json, "created_at"));
        this.completedAt = parseInstant(getStringOrNull(json, "completed_at"));

        this.messages = new ArrayList<>();
        if (json.has("messages") && json.get("messages").isJsonArray()) {
            JsonArray messagesArray = json.getAsJsonArray("messages");
            for (JsonElement element : messagesArray) {
                messages.add(new BatchMessageResult(element.getAsJsonObject()));
            }
        }
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

    public String getBatchId() {
        return batchId;
    }

    public String getStatus() {
        return status;
    }

    public int getTotal() {
        return total;
    }

    public int getQueued() {
        return queued;
    }

    public int getFailed() {
        return failed;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public List<BatchMessageResult> getMessages() {
        return messages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    // Helper methods

    /**
     * Check if the batch is still processing.
     */
    public boolean isProcessing() {
        return STATUS_PROCESSING.equals(status);
    }

    /**
     * Check if the batch completed successfully.
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    /**
     * Check if the batch completed with some failures.
     */
    public boolean isPartialFailure() {
        return STATUS_PARTIAL_FAILURE.equals(status);
    }

    /**
     * Check if all messages in the batch failed.
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    @Override
    public String toString() {
        return "BatchMessageResponse{" +
                "batchId='" + batchId + '\'' +
                ", status='" + status + '\'' +
                ", total=" + total +
                ", queued=" + queued +
                ", failed=" + failed +
                ", creditsUsed=" + creditsUsed +
                '}';
    }
}
