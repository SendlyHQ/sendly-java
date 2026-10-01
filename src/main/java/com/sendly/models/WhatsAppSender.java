package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A number connected (or connecting) to WhatsApp.
 */
public class WhatsAppSender {
    private String phoneNumber;
    private String displayName;
    private String status;
    private String qualityRating;
    private String businessAccountId;
    private String businessName;
    private boolean callingEnabled;
    private boolean outboundCallingAllowed;
    private String createdAt;

    public WhatsAppSender() {}

    public WhatsAppSender(JsonObject json) {
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("displayName") && !json.get("displayName").isJsonNull()) {
            this.displayName = json.get("displayName").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("qualityRating") && !json.get("qualityRating").isJsonNull()) {
            this.qualityRating = json.get("qualityRating").getAsString();
        }
        if (json.has("businessAccountId") && !json.get("businessAccountId").isJsonNull()) {
            this.businessAccountId = json.get("businessAccountId").getAsString();
        }
        if (json.has("businessName") && !json.get("businessName").isJsonNull()) {
            this.businessName = json.get("businessName").getAsString();
        }
        if (json.has("callingEnabled") && !json.get("callingEnabled").isJsonNull()) {
            this.callingEnabled = json.get("callingEnabled").getAsBoolean();
        }
        if (json.has("outboundCallingAllowed") && !json.get("outboundCallingAllowed").isJsonNull()) {
            this.outboundCallingAllowed = json.get("outboundCallingAllowed").getAsBoolean();
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
    }

    /** The sender, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * The name recipients see — chosen during the connect flow and reviewed
     * by Meta; null until set.
     */
    public String getDisplayName() { return displayName; }

    /**
     * Connection status: {@code pending}, {@code active}, or
     * {@code suspended}.
     */
    public String getStatus() { return status; }

    /** Meta quality rating (e.g. "GREEN"), or null before first rating. */
    public String getQualityRating() { return qualityRating; }

    /**
     * The WhatsApp Business Account id the number belongs to; null while
     * the sender is {@code pending}. Pass it to
     * {@code CreateWhatsAppSignupRequest.Builder.businessAccountId(String)}
     * to add another number to the same account.
     */
    public String getBusinessAccountId() { return businessAccountId; }

    /** The WhatsApp Business Account's business name; null while pending or unknown. */
    public String getBusinessName() { return businessName; }

    /**
     * Whether WhatsApp calling is switched on for this number; change it
     * with {@code whatsapp().senders().setCalling(phoneNumber, enabled)}.
     */
    public boolean isCallingEnabled() { return callingEnabled; }

    /**
     * Whether WhatsApp lets the business place calls from this number.
     * False for every +1 number (the US, Canada and the rest of the North
     * American numbering plan), and for +20 (Egypt), +84 (Vietnam) and +234
     * (Nigeria) numbers, where Meta forbids business-initiated calls.
     */
    public boolean isOutboundCallingAllowed() { return outboundCallingAllowed; }

    /** ISO 8601 timestamp when the sender was connected. */
    public String getCreatedAt() { return createdAt; }
}
