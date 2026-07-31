package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A WhatsApp message template.
 */
public class WhatsAppTemplate {
    private String id;
    private String name;
    private String language;
    private String category;
    private String status;
    private String qualityRating;
    private String rejectionReason;
    private String createdAt;
    private String updatedAt;
    private List<String> warnings;

    public WhatsAppTemplate() {}

    public WhatsAppTemplate(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("name") && !json.get("name").isJsonNull()) {
            this.name = json.get("name").getAsString();
        }
        if (json.has("language") && !json.get("language").isJsonNull()) {
            this.language = json.get("language").getAsString();
        }
        if (json.has("category") && !json.get("category").isJsonNull()) {
            this.category = json.get("category").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("qualityRating") && !json.get("qualityRating").isJsonNull()) {
            this.qualityRating = json.get("qualityRating").getAsString();
        }
        if (json.has("rejectionReason") && !json.get("rejectionReason").isJsonNull()) {
            this.rejectionReason = json.get("rejectionReason").getAsString();
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            this.updatedAt = json.get("updatedAt").getAsString();
        }
        if (json.has("warnings") && json.get("warnings").isJsonArray()) {
            this.warnings = new ArrayList<>();
            JsonArray arr = json.getAsJsonArray("warnings");
            for (int i = 0; i < arr.size(); i++) {
                if (!arr.get(i).isJsonNull()) {
                    this.warnings.add(arr.get(i).getAsString());
                }
            }
        }
    }

    /** Unique template identifier. */
    public String getId() { return id; }

    /** Template name. */
    public String getName() { return name; }

    /** Template language code (e.g. "en_US"). */
    public String getLanguage() { return language; }

    /**
     * Category: {@code AUTHENTICATION}, {@code UTILITY}, or {@code MARKETING}.
     * Meta may reclassify; this value drives pricing.
     */
    public String getCategory() { return category; }

    /**
     * Review status: {@code PENDING}, {@code APPROVED}, {@code REJECTED},
     * {@code PAUSED}, or {@code DISABLED}.
     */
    public String getStatus() { return status; }

    /** Meta quality rating (e.g. "GREEN"), or null before first rating. */
    public String getQualityRating() { return qualityRating; }

    /** Why Meta rejected the template, when status is {@code REJECTED}. */
    public String getRejectionReason() { return rejectionReason; }

    /** ISO 8601 timestamp when the template was created. */
    public String getCreatedAt() { return createdAt; }

    /** ISO 8601 timestamp when the template was last updated. */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Non-blocking submission warnings (e.g. an unapproved display name, or a
     * marketing template without an opt-out button). Present on create
     * responses when applicable; null otherwise.
     */
    public List<String> getWarnings() { return warnings; }
}
