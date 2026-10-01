package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code whatsapp().window(from, to)}.
 */
public class WhatsAppWindow {
    private boolean open;
    private String expiresAt;

    public WhatsAppWindow() {}

    public WhatsAppWindow(JsonObject json) {
        if (json.has("open") && !json.get("open").isJsonNull()) {
            this.open = json.get("open").getAsBoolean();
        }
        if (json.has("expiresAt") && !json.get("expiresAt").isJsonNull()) {
            this.expiresAt = json.get("expiresAt").getAsString();
        }
    }

    /** True when a 24-hour customer-service window is currently open. */
    public boolean isOpen() { return open; }

    /**
     * When the window closes (ISO 8601). After it closes this is the past
     * expiry, with {@link #isOpen()} false. Null when Sendly has no window on
     * record for the pair; a free-form send may still go through then if
     * WhatsApp reports an open window, and otherwise fails with
     * {@code whatsapp_window_closed}.
     */
    public String getExpiresAt() { return expiresAt; }
}
