package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code whatsapp().senders().setCalling(phoneNumber, enabled)}.
 */
public class WhatsAppCallingSettings {
    private String phoneNumber;
    private boolean callingEnabled;
    private boolean outboundCallingAllowed;

    public WhatsAppCallingSettings() {}

    public WhatsAppCallingSettings(JsonObject json) {
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("callingEnabled") && !json.get("callingEnabled").isJsonNull()) {
            this.callingEnabled = json.get("callingEnabled").getAsBoolean();
        }
        if (json.has("outboundCallingAllowed") && !json.get("outboundCallingAllowed").isJsonNull()) {
            this.outboundCallingAllowed = json.get("outboundCallingAllowed").getAsBoolean();
        }
    }

    /** The sender, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /** Whether WhatsApp calling is now switched on for the number. */
    public boolean isCallingEnabled() { return callingEnabled; }

    /**
     * Whether WhatsApp lets the business place calls from this number.
     * False for every +1 number (the US, Canada and the rest of the North
     * American numbering plan), and for +20 (Egypt), +84 (Vietnam) and +234
     * (Nigeria) numbers.
     */
    public boolean isOutboundCallingAllowed() { return outboundCallingAllowed; }
}
