package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * WhatsApp-specific details on a sent message.
 */
public class WhatsAppMessageDetails {
    private String kind;
    private SentTemplate template;
    private String messageId;

    public WhatsAppMessageDetails() {}

    public WhatsAppMessageDetails(JsonObject json) {
        if (json.has("kind") && !json.get("kind").isJsonNull()) {
            this.kind = json.get("kind").getAsString();
        }
        if (json.has("template") && json.get("template").isJsonObject()) {
            this.template = new SentTemplate(json.getAsJsonObject("template"));
        }
        if (json.has("messageId") && !json.get("messageId").isJsonNull()) {
            this.messageId = json.get("messageId").getAsString();
        }
    }

    /**
     * What was sent: {@code text}, {@code media}, or {@code template}.
     */
    public String getKind() { return kind; }

    /** The template that was sent (template sends only); null otherwise. */
    public SentTemplate getTemplate() { return template; }

    /**
     * WhatsApp message id — null until the first delivery report lands;
     * populated on the message record afterwards.
     */
    public String getMessageId() { return messageId; }

    /**
     * The template a WhatsApp message was sent with.
     */
    public static class SentTemplate {
        private String name;
        private String language;
        private String category;

        public SentTemplate() {}

        public SentTemplate(JsonObject json) {
            if (json.has("name") && !json.get("name").isJsonNull()) {
                this.name = json.get("name").getAsString();
            }
            if (json.has("language") && !json.get("language").isJsonNull()) {
                this.language = json.get("language").getAsString();
            }
            if (json.has("category") && !json.get("category").isJsonNull()) {
                this.category = json.get("category").getAsString();
            }
        }

        /** Template name. */
        public String getName() { return name; }

        /** Template language code. */
        public String getLanguage() { return language; }

        /**
         * Billed category: {@code marketing}, {@code utility}, or
         * {@code authentication}. Meta reviews and may reclassify templates;
         * the category on the send response is what was billed.
         */
        public String getCategory() { return category; }
    }
}
