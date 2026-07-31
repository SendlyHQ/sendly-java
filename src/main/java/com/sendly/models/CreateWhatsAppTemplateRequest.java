package com.sendly.models;

import java.util.List;
import java.util.Map;

/**
 * Request body for {@code whatsapp().templates().create()}.
 *
 * <pre>{@code
 * CreateWhatsAppTemplateRequest request = CreateWhatsAppTemplateRequest.builder()
 *     .sender("+15559876543")
 *     .name("order_shipped")
 *     .language("en_US")
 *     .category("UTILITY")
 *     .body("Hi {{1}}, your order {{2}} has shipped!")
 *     .examples(Map.of("1", "Sam", "2", "#4821"))
 *     .build();
 * }</pre>
 */
public class CreateWhatsAppTemplateRequest {
    private final String sender;
    private final String name;
    private final String language;
    private final String category;
    private final String body;
    private final String footer;
    private final String header;
    private final List<WhatsAppTemplateButton> buttons;
    private final Map<String, String> examples;

    private CreateWhatsAppTemplateRequest(Builder builder) {
        this.sender = builder.sender;
        this.name = builder.name;
        this.language = builder.language;
        this.category = builder.category;
        this.body = builder.body;
        this.footer = builder.footer;
        this.header = builder.header;
        this.buttons = builder.buttons;
        this.examples = builder.examples;
    }

    public String getSender() { return sender; }
    public String getName() { return name; }
    public String getLanguage() { return language; }
    public String getCategory() { return category; }
    public String getBody() { return body; }
    public String getFooter() { return footer; }
    public String getHeader() { return header; }
    public List<WhatsAppTemplateButton> getButtons() { return buttons; }
    public Map<String, String> getExamples() { return examples; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sender;
        private String name;
        private String language;
        private String category;
        private String body;
        private String footer;
        private String header;
        private List<WhatsAppTemplateButton> buttons;
        private Map<String, String> examples;

        /**
         * The WhatsApp-connected sending number this template belongs to, in
         * E.164 format.
         */
        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Template name: lowercase letters, digits, and underscores (e.g.
         * "order_shipped").
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Template language code (e.g. "en_US"). */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * Template category — drives Meta review rules and pricing:
         * "AUTHENTICATION", "UTILITY", or "MARKETING".
         */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /**
         * Body text. Use {@code {{1}}}, {@code {{2}}}, … for variables; every
         * placeholder needs an example value in {@code examples}.
         */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /** Optional footer line. */
        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        /** Optional text header. */
        public Builder header(String header) {
            this.header = header;
            return this;
        }

        /** Optional buttons. */
        public Builder buttons(List<WhatsAppTemplateButton> buttons) {
            this.buttons = buttons;
            return this;
        }

        /**
         * Example values for body placeholders, keyed by placeholder number:
         * {@code Map.of("1", "Acme Inc", "2", "#4821")}. Required when the
         * body has variables — Meta reviews templates with these examples
         * filled in.
         */
        public Builder examples(Map<String, String> examples) {
            this.examples = examples;
            return this;
        }

        public CreateWhatsAppTemplateRequest build() {
            return new CreateWhatsAppTemplateRequest(this);
        }
    }
}
