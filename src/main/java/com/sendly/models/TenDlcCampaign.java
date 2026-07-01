package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A messaging campaign registered for carrier review.
 * <p>
 * Status is one of {@code pending}, {@code active}, {@code failed},
 * {@code suspended}, or {@code expired}.
 */
public class TenDlcCampaign {
    private String id;
    private String brandId;
    private String useCase;
    private List<String> subUseCases;
    private String description;
    private String status;
    private List<String> sampleMessages;
    private TenDlcThroughput throughput;
    private List<String> failureReasons;
    private String createdAt;
    private String updatedAt;

    public TenDlcCampaign() {
        this.subUseCases = new ArrayList<>();
        this.sampleMessages = new ArrayList<>();
    }

    public TenDlcCampaign(JsonObject json) {
        this.subUseCases = new ArrayList<>();
        this.sampleMessages = new ArrayList<>();
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("brandId") && !json.get("brandId").isJsonNull()) {
            this.brandId = json.get("brandId").getAsString();
        }
        if (json.has("useCase") && !json.get("useCase").isJsonNull()) {
            this.useCase = json.get("useCase").getAsString();
        }
        if (json.has("subUseCases") && json.get("subUseCases").isJsonArray()) {
            json.get("subUseCases").getAsJsonArray().forEach(e ->
                subUseCases.add(e.getAsString())
            );
        }
        if (json.has("description") && !json.get("description").isJsonNull()) {
            this.description = json.get("description").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("sampleMessages") && json.get("sampleMessages").isJsonArray()) {
            json.get("sampleMessages").getAsJsonArray().forEach(e ->
                sampleMessages.add(e.getAsString())
            );
        }
        if (json.has("throughput") && json.get("throughput").isJsonObject()) {
            this.throughput = new TenDlcThroughput(json.getAsJsonObject("throughput"));
        }
        if (json.has("failureReasons") && json.get("failureReasons").isJsonArray()) {
            this.failureReasons = new ArrayList<>();
            json.get("failureReasons").getAsJsonArray().forEach(e ->
                failureReasons.add(e.getAsString())
            );
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            this.updatedAt = json.get("updatedAt").getAsString();
        }
    }

    /** Unique campaign identifier. */
    public String getId() { return id; }

    /** The brand this campaign belongs to. */
    public String getBrandId() { return brandId; }

    /** Primary use-case code (e.g. "MIXED", "MARKETING"). */
    public String getUseCase() { return useCase; }

    /** Sub-use-case codes. */
    public List<String> getSubUseCases() { return subUseCases; }

    /** What the campaign sends and why. */
    public String getDescription() { return description; }

    /** Carrier-review status: "pending", "active", "failed", "suspended", or "expired". */
    public String getStatus() { return status; }

    /** Example messages the campaign sends. */
    public List<String> getSampleMessages() { return sampleMessages; }

    /** Granted throughput, once carriers approve; null until then. */
    public TenDlcThroughput getThroughput() { return throughput; }

    /** Why the review failed, when status is "failed"; null otherwise. */
    public List<String> getFailureReasons() { return failureReasons; }

    /** When the campaign was created (ISO-8601). */
    public String getCreatedAt() { return createdAt; }

    /** When the campaign was last updated (ISO-8601). */
    public String getUpdatedAt() { return updatedAt; }
}
