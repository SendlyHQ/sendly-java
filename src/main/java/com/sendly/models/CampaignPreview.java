package com.sendly.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CampaignPreview {
    @SerializedName("recipient_count")
    private int recipientCount;
    @SerializedName("estimated_credits")
    private double estimatedCredits;
    @SerializedName("estimated_cost")
    private double estimatedCost;
    @SerializedName("blocked_count")
    private Integer blockedCount;
    @SerializedName("sendable_count")
    private Integer sendableCount;
    @SerializedName("warnings")
    private java.util.List<String> warnings;

    public CampaignPreview() {}

    public CampaignPreview(JsonObject json) {
        if (json.has("recipient_count")) this.recipientCount = json.get("recipient_count").getAsInt();
        if (json.has("estimated_credits")) this.estimatedCredits = json.get("estimated_credits").getAsDouble();
        if (json.has("estimated_cost")) this.estimatedCost = json.get("estimated_cost").getAsDouble();
        if (json.has("blocked_count")) this.blockedCount = json.get("blocked_count").getAsInt();
        if (json.has("sendable_count")) this.sendableCount = json.get("sendable_count").getAsInt();
    }

    public int getRecipientCount() { return recipientCount; }
    public double getEstimatedCredits() { return estimatedCredits; }
    public double getEstimatedCost() { return estimatedCost; }
    public Integer getBlockedCount() { return blockedCount; }
    public Integer getSendableCount() { return sendableCount; }
    public java.util.List<String> getWarnings() { return warnings; }
}
