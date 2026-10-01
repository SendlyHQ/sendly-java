package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * One recipient of a live group MMS send.
 */
public class GroupRecipient {
    private final String phoneNumber;
    private final String status;

    /**
     * Create a GroupRecipient from a JSON object.
     */
    public GroupRecipient(JsonObject json) {
        this.phoneNumber = json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()
                ? json.get("phoneNumber").getAsString() : null;
        this.status = json.has("status") && !json.get("status").isJsonNull()
                ? json.get("status").getAsString() : null;
    }

    /**
     * @return The recipient's phone number in E.164 format
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @return The recipient's status when the send was accepted, for example "queued"
     */
    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "GroupRecipient{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
