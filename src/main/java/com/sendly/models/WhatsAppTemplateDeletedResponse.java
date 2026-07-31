package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code whatsapp().templates().delete(id)}.
 */
public class WhatsAppTemplateDeletedResponse {
    private String id;
    private boolean deleted;

    public WhatsAppTemplateDeletedResponse() {}

    public WhatsAppTemplateDeletedResponse(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("deleted") && !json.get("deleted").isJsonNull()) {
            this.deleted = json.get("deleted").getAsBoolean();
        }
    }

    /** The deleted template's id. */
    public String getId() { return id; }

    /** Always true. */
    public boolean isDeleted() { return deleted; }
}
