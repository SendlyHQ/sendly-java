package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code whatsapp().templates().list()}.
 */
public class WhatsAppTemplateListResponse {
    private List<WhatsAppTemplate> templates;

    public WhatsAppTemplateListResponse() {
        this.templates = new ArrayList<>();
    }

    public WhatsAppTemplateListResponse(JsonObject json) {
        this.templates = new ArrayList<>();
        if (json.has("templates") && json.get("templates").isJsonArray()) {
            json.get("templates").getAsJsonArray().forEach(e ->
                templates.add(new WhatsAppTemplate(e.getAsJsonObject()))
            );
        }
    }

    /** Your WhatsApp templates with review status and quality rating. */
    public List<WhatsAppTemplate> getTemplates() { return templates; }
}
