package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code whatsapp().signup().create()}.
 * <p>
 * Hand {@link #getConnectUrl() connectUrl} to a human — they open it in a
 * browser and log in with Facebook to link their WhatsApp Business Account.
 * Poll {@code whatsapp().signup().get(id)} until the status is {@code active}.
 * </p>
 */
public class WhatsAppSignupSession {
    private String id;
    private String connectUrl;
    private String status;

    public WhatsAppSignupSession() {}

    public WhatsAppSignupSession(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("connectUrl") && !json.get("connectUrl").isJsonNull()) {
            this.connectUrl = json.get("connectUrl").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
    }

    /** Unique signup identifier — use with {@code whatsapp().signup().get(id)}. */
    public String getId() { return id; }

    /** Hosted connect page URL. A person must open this in a browser. */
    public String getConnectUrl() { return connectUrl; }

    /**
     * Current signup status: {@code initiated}, {@code registering},
     * {@code active}, {@code failed}, or {@code expired}.
     */
    public String getStatus() { return status; }
}
