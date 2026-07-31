package com.sendly.models;

import java.util.List;
import java.util.Map;

/**
 * Request body for {@code whatsapp().templates().update()}. Supply only the
 * fields to change; omitted fields keep their current value.
 */
public class UpdateWhatsAppTemplateRequest {
    private final String body;
    private final String footer;
    private final String header;
    private final List<WhatsAppTemplateButton> buttons;
    private final Map<String, String> examples;

    private UpdateWhatsAppTemplateRequest(Builder builder) {
        this.body = builder.body;
        this.footer = builder.footer;
        this.header = builder.header;
        this.buttons = builder.buttons;
        this.examples = builder.examples;
    }

    public String getBody() { return body; }
    public String getFooter() { return footer; }
    public String getHeader() { return header; }
    public List<WhatsAppTemplateButton> getButtons() { return buttons; }
    public Map<String, String> getExamples() { return examples; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String body;
        private String footer;
        private String header;
        private List<WhatsAppTemplateButton> buttons;
        private Map<String, String> examples;

        /** Replacement body text. */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /** Replacement footer. */
        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        /** Replacement text header. */
        public Builder header(String header) {
            this.header = header;
            return this;
        }

        /** Replacement buttons. */
        public Builder buttons(List<WhatsAppTemplateButton> buttons) {
            this.buttons = buttons;
            return this;
        }

        /** Replacement example values for body placeholders. */
        public Builder examples(Map<String, String> examples) {
            this.examples = examples;
            return this;
        }

        public UpdateWhatsAppTemplateRequest build() {
            return new UpdateWhatsAppTemplateRequest(this);
        }
    }
}
