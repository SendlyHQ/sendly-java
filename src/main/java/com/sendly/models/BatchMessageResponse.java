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
 *
 * <p>Two API payloads deserialize into this type and they differ:</p>
 * <ul>
 *   <li>{@code POST /messages/batch} identifies the batch as {@code batchId} and
 *       reports {@code sent}; it carries no {@code queued} count and no timestamps.</li>
 *   <li>{@code GET /messages/batch/:id} (and each entry of
 *       {@code GET /messages/batches}) identifies the batch as {@code id} and adds
 *       {@code queued}, {@code createdAt} and {@code completedAt}.</li>
 * </ul>
 *
 * <p>{@link #getBatchId()} is populated from whichever of the two identifier
 * fields the payload carries, so it is safe to feed straight back into
 * {@code getBatch(...)}. Counts absent from a payload read as {@code 0} and
 * absent timestamps read as {@code null}.</p>
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
    private final int sent;
    private final int failed;
    private final int creditsUsed;
    private final List<BatchMessageResult> messages;
    private final Instant createdAt;
    private final Instant completedAt;

    /**
     * Create a BatchMessageResponse from a JSON object.
     */
    public BatchMessageResponse(JsonObject json) {
        String id = getStringOrNull(json, "batchId");
        this.batchId = id != null ? id : getStringOrNull(json, "id");
        this.status = getStringOrNull(json, "status");
        this.total = getIntOrZero(json, "total");
        this.queued = getIntOrZero(json, "queued");
        this.sent = getIntOrZero(json, "sent");
        this.failed = getIntOrZero(json, "failed");
        this.creditsUsed = getIntOrZero(json, "creditsUsed");
        this.createdAt = parseInstant(getStringOrNull(json, "createdAt"));
        this.completedAt = parseInstant(getStringOrNull(json, "completedAt"));

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

    private int getIntOrZero(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
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

    /**
     * The batch identifier, read from {@code batchId} on a send response and
     * from {@code id} on a fetched or listed batch.
     */
    public String getBatchId() {
        return batchId;
    }

    public String getStatus() {
        return status;
    }

    public int getTotal() {
        return total;
    }

    /**
     * Messages still waiting to go out. Only a fetched or listed batch reports
     * this; a send response has no queued count and returns {@code 0}.
     */
    public int getQueued() {
        return queued;
    }

    /**
     * Messages handed to the network so far.
     */
    public int getSent() {
        return sent;
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
                ", sent=" + sent +
                ", failed=" + failed +
                ", creditsUsed=" + creditsUsed +
                '}';
    }
}
