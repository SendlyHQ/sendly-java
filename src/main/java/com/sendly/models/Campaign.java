package com.sendly.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

public class Campaign {
    private String id;
    private String name;
    @SerializedName(value = "text", alternate = {"messageText"})
    private String text;
    @SerializedName(value = "template_id", alternate = {"templateId"})
    private String templateId;
    @SerializedName(value = "contact_list_ids", alternate = {"contactListIds"})
    private List<String> contactListIds;
    private String status;
    @SerializedName(value = "recipient_count", alternate = {"totalRecipients", "recipientCount"})
    private Integer recipientCount;
    @SerializedName(value = "sent_count", alternate = {"sentCount"})
    private Integer sentCount;
    @SerializedName(value = "delivered_count", alternate = {"deliveredCount"})
    private Integer deliveredCount;
    @SerializedName(value = "failed_count", alternate = {"failedCount"})
    private Integer failedCount;
    @SerializedName(value = "estimated_credits", alternate = {"estimatedCredits"})
    private Double estimatedCredits;
    @SerializedName(value = "credits_used", alternate = {"creditsUsed"})
    private Double creditsUsed;
    @SerializedName(value = "scheduled_at", alternate = {"scheduledAt"})
    private String scheduledAt;
    private String timezone;
    @SerializedName(value = "started_at", alternate = {"sentAt", "startedAt"})
    private String startedAt;
    @SerializedName(value = "completed_at", alternate = {"completedAt"})
    private String completedAt;
    @SerializedName(value = "created_at", alternate = {"createdAt"})
    private String createdAt;
    @SerializedName(value = "updated_at", alternate = {"updatedAt"})
    private String updatedAt;
    @SerializedName(value = "batch_id", alternate = {"batchId"})
    private String batchId;

    public Campaign() {}

    public Campaign(JsonObject json) {
        this.id = string(json, "id");
        this.name = string(json, "name");
        this.text = string(json, "text", "messageText");
        this.templateId = string(json, "template_id", "templateId");
        this.contactListIds = contactListIdsOf(json);
        this.status = string(json, "status");
        this.recipientCount = integer(json, "totalRecipients", "recipient_count", "recipientCount");
        this.sentCount = integer(json, "sentCount", "sent_count");
        this.deliveredCount = integer(json, "deliveredCount", "delivered_count");
        this.failedCount = integer(json, "failedCount", "failed_count");
        this.estimatedCredits = decimal(json, "estimatedCredits", "estimated_credits");
        this.creditsUsed = decimal(json, "creditsUsed", "credits_used");
        this.scheduledAt = string(json, "scheduledAt", "scheduled_at");
        this.timezone = string(json, "timezone");
        this.startedAt = string(json, "sentAt", "started_at", "startedAt");
        this.completedAt = string(json, "completedAt", "completed_at");
        this.createdAt = string(json, "created_at", "createdAt");
        this.updatedAt = string(json, "updated_at", "updatedAt");
        this.batchId = string(json, "batchId", "batch_id");
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

    private static String string(JsonObject json, String... keys) {
        JsonElement value = present(json, keys);
        return value != null ? value.getAsString() : null;
    }

    private static Integer integer(JsonObject json, String... keys) {
        JsonElement value = present(json, keys);
        return value != null ? value.getAsInt() : null;
    }

    private static Double decimal(JsonObject json, String... keys) {
        JsonElement value = present(json, keys);
        return value != null ? value.getAsDouble() : null;
    }

    private static List<String> contactListIdsOf(JsonObject json) {
        JsonElement ids = present(json, "contact_list_ids", "contactListIds");
        if (ids != null && ids.isJsonArray()) {
            List<String> out = new ArrayList<>();
            ids.getAsJsonArray().forEach(e -> {
                if (!e.isJsonNull()) out.add(e.getAsString());
            });
            return out;
        }
        String targetListId = string(json, "targetListId");
        if (targetListId != null) {
            List<String> out = new ArrayList<>();
            out.add(targetListId);
            return out;
        }
        return null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getText() { return text; }
    public String getTemplateId() { return templateId; }
    public List<String> getContactListIds() { return contactListIds; }
    public String getStatus() { return status; }
    public Integer getRecipientCount() { return recipientCount; }
    public Integer getSentCount() { return sentCount; }
    public Integer getDeliveredCount() { return deliveredCount; }
    public Integer getFailedCount() { return failedCount; }
    public Double getEstimatedCredits() { return estimatedCredits; }
    public Double getCreditsUsed() { return creditsUsed; }
    public String getScheduledAt() { return scheduledAt; }
    public String getTimezone() { return timezone; }
    public String getStartedAt() { return startedAt; }
    public String getCompletedAt() { return completedAt; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    /** The batch the campaign was sent as, or null before it is sent. */
    public String getBatchId() { return batchId; }
}
