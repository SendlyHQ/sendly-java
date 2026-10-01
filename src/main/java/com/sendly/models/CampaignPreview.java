package com.sendly.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CampaignPreview {
    @SerializedName(value = "recipient_count", alternate = {"recipientCount", "totalRecipients"})
    private int recipientCount;
    @SerializedName(value = "estimated_credits", alternate = {"estimatedCredits"})
    private double estimatedCredits;
    @SerializedName("estimated_cost")
    private double estimatedCost;
    @SerializedName(value = "blocked_count", alternate = {"blockedCount"})
    private Integer blockedCount;
    @SerializedName(value = "sendable_count", alternate = {"sendableCount"})
    private Integer sendableCount;
    @SerializedName("warnings")
    private java.util.List<String> warnings;
    @SerializedName(value = "opted_out_count", alternate = {"optedOutCount"})
    private int optedOutCount;
    @SerializedName(value = "invalid_count", alternate = {"invalidCount"})
    private int invalidCount;
    @SerializedName(value = "current_balance", alternate = {"currentBalance"})
    private int currentBalance;
    @SerializedName(value = "has_enough_credits", alternate = {"hasEnoughCredits"})
    private boolean hasEnoughCredits;

    public CampaignPreview() {}

    public CampaignPreview(JsonObject json) {
        JsonElement recipients = present(json, "recipientCount", "totalRecipients", "recipient_count");
        if (recipients != null) this.recipientCount = recipients.getAsInt();
        JsonElement credits = present(json, "estimatedCredits", "estimated_credits");
        if (credits != null) this.estimatedCredits = credits.getAsDouble();
        JsonElement cost = present(json, "estimated_cost");
        if (cost != null) this.estimatedCost = cost.getAsDouble();
        JsonElement blocked = present(json, "blockedCount", "blocked_count");
        if (blocked != null) this.blockedCount = blocked.getAsInt();
        JsonElement sendable = present(json, "sendableCount", "sendable_count");
        if (sendable != null) this.sendableCount = sendable.getAsInt();
        JsonElement warningList = present(json, "warnings");
        if (warningList != null && warningList.isJsonArray()) {
            this.warnings = new java.util.ArrayList<>();
            warningList.getAsJsonArray().forEach(w -> {
                if (!w.isJsonNull()) this.warnings.add(w.getAsString());
            });
        }
        JsonElement optedOut = present(json, "optedOutCount");
        if (optedOut != null) this.optedOutCount = optedOut.getAsInt();
        JsonElement invalid = present(json, "invalidCount");
        if (invalid != null) this.invalidCount = invalid.getAsInt();
        JsonElement balance = present(json, "currentBalance");
        if (balance != null) this.currentBalance = (int) balance.getAsDouble();
        JsonElement enough = present(json, "hasEnoughCredits");
        if (enough != null) this.hasEnoughCredits = enough.getAsBoolean();
    }

    private static JsonElement present(JsonObject json, String... keys) {
        for (String key : keys) {
            JsonElement value = json.get(key);
            if (value != null && !value.isJsonNull()) {
                return value;
            }
        }
        return null;
    }

    /** Contacts the campaign targets, not counting those who opted out or whose number is flagged invalid. */
    public int getRecipientCount() { return recipientCount; }
    /** Credits the send is expected to use. */
    public double getEstimatedCredits() { return estimatedCredits; }
    /** Not sent by the API, so always 0; use {@link #getEstimatedCredits()}. */
    public double getEstimatedCost() { return estimatedCost; }
    /** Recipients your verification cannot reach. */
    public Integer getBlockedCount() { return blockedCount; }
    /** Recipients the send will go to. */
    public Integer getSendableCount() { return sendableCount; }
    public java.util.List<String> getWarnings() { return warnings; }
    /** Contacts on the list who have opted out. */
    public int getOptedOutCount() { return optedOutCount; }
    /** Contacts on the list with a missing or malformed phone number. */
    public int getInvalidCount() { return invalidCount; }
    /** The workspace's credit balance when the preview was made. */
    public int getCurrentBalance() { return currentBalance; }
    /** Whether the balance covers the estimate. Always true with a test key, whose sends are free. */
    public boolean hasEnoughCredits() { return hasEnoughCredits; }
}
