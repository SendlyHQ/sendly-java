package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A phone number assigned to a campaign.
 * <p>
 * Status is one of {@code Active}, {@code Under review}, or
 * {@code Action needed}; the number can send once {@code Active}.
 */
public class TenDlcAssignment {
    private String id;
    private String campaignId;
    private String phoneNumber;
    private String status;
    private String assignedAt;

    public TenDlcAssignment() {}

    public TenDlcAssignment(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("campaignId") && !json.get("campaignId").isJsonNull()) {
            this.campaignId = json.get("campaignId").getAsString();
        }
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("assignedAt") && !json.get("assignedAt").isJsonNull()) {
            this.assignedAt = json.get("assignedAt").getAsString();
        }
    }

    /** Unique assignment identifier. */
    public String getId() { return id; }

    /** The campaign the number is assigned to. */
    public String getCampaignId() { return campaignId; }

    /** The assigned phone number in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /** Assignment status; the number can send once "Active". */
    public String getStatus() { return status; }

    /** When the assignment completed (ISO-8601), or null while in progress. */
    public String getAssignedAt() { return assignedAt; }
}
