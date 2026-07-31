package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code whatsapp().signup().get(id)}.
 */
public class WhatsAppSignup {
    private String id;
    private String status;
    private String phoneNumber;
    private String businessAccountId;
    private List<String> failureReasons;
    private String updatedAt;

    public WhatsAppSignup() {}

    public WhatsAppSignup(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("businessAccountId") && !json.get("businessAccountId").isJsonNull()) {
            this.businessAccountId = json.get("businessAccountId").getAsString();
        }
        if (json.has("failureReasons") && json.get("failureReasons").isJsonArray()) {
            this.failureReasons = new ArrayList<>();
            JsonArray reasons = json.getAsJsonArray("failureReasons");
            for (int i = 0; i < reasons.size(); i++) {
                if (!reasons.get(i).isJsonNull()) {
                    this.failureReasons.add(reasons.get(i).getAsString());
                }
            }
        }
        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            this.updatedAt = json.get("updatedAt").getAsString();
        }
    }

    /** Unique signup identifier. */
    public String getId() { return id; }

    /**
     * Current signup status: {@code initiated}, {@code registering},
     * {@code active}, {@code failed}, or {@code expired}.
     */
    public String getStatus() { return status; }

    /** The number being connected, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * The customer's WhatsApp Business Account id, once linked; null before
     * the human completes the connect step.
     */
    public String getBusinessAccountId() { return businessAccountId; }

    /** Why the signup failed, when status is {@code failed}; null otherwise. */
    public List<String> getFailureReasons() { return failureReasons; }

    /** ISO 8601 timestamp of the last status change. */
    public String getUpdatedAt() { return updatedAt; }
}
