package com.sendly.models;

import java.util.List;

/**
 * A button on a WhatsApp template.
 * <ul>
 *   <li>{@code url} — link button; {@code url} is required and may contain a
 *       {@code {{1}}} placeholder (supply {@code example} values for review)</li>
 *   <li>{@code quick_reply} — tap-to-reply button (e.g. a "Stop promotions"
 *       opt-out, recommended on marketing templates)</li>
 *   <li>{@code otp} — copy-code button; required on AUTHENTICATION templates</li>
 * </ul>
 */
public class WhatsAppTemplateButton {
    private final String type;
    private final String text;
    private final String url;
    private final List<String> example;

    /**
     * Create a template button.
     *
     * @param type Button type: "url", "quick_reply", or "otp"
     * @param text Button label
     */
    public WhatsAppTemplateButton(String type, String text) {
        this(type, text, null, null);
    }

    /**
     * Create a template button with all options.
     *
     * @param type    Button type: "url", "quick_reply", or "otp"
     * @param text    Button label
     * @param url     Link target (url buttons only); may contain a {@code {{1}}} placeholder
     * @param example Example values for a url placeholder, for Meta review
     */
    public WhatsAppTemplateButton(String type, String text, String url, List<String> example) {
        this.type = type;
        this.text = text;
        this.url = url;
        this.example = example;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getExample() {
        return example;
    }

    /**
     * Create a builder for WhatsAppTemplateButton.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for WhatsAppTemplateButton.
     */
    public static class Builder {
        private String type;
        private String text;
        private String url;
        private List<String> example;

        /**
         * Set the button type.
         *
         * @param type "url", "quick_reply", or "otp"
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Set the link target (url buttons only); may contain a {@code {{1}}}
         * placeholder.
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Set example values for a url placeholder, for Meta review.
         */
        public Builder example(List<String> example) {
            this.example = example;
            return this;
        }

        public WhatsAppTemplateButton build() {
            return new WhatsAppTemplateButton(type, text, url, example);
        }
    }
}
