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

    /** When the window closes (ISO 8601), or null when no window is open. */
    public String getExpiresAt() { return expiresAt; }
}
