package com.sendly.models;

import java.util.List;
import java.util.Map;

/**
 * Request object for sending a WhatsApp message via
 * {@code messages().send(SendWhatsAppMessageRequest)}.
 * <p>
 * Provide exactly one of:
 * </p>
 * <ul>
 *   <li>{@code text} — free-form text; only deliverable inside an open
 *       24-hour customer-service window (the recipient messaged you in the
 *       last 24h)</li>
 *   <li>{@code mediaUrls} — a single media attachment (optional {@code text}
 *       becomes its caption); also window-bound</li>
 *   <li>{@code template} — an approved template; works regardless of the
 *       window</li>
 * </ul>
 * <p>
 * WhatsApp sends require a live API key and a {@code from} number that has
 * been connected to WhatsApp (see {@code whatsapp().signup()}).
 * </p>
 */
public class SendWhatsAppMessageRequest {
    private final String channel = "whatsapp";
    private final String to;
    private final String from;
    private final String text;
    private final List<String> mediaUrls;
    private final WhatsAppTemplateSendParams template;
    private final Map<String, Object> metadata;

    private SendWhatsAppMessageRequest(Builder builder) {
        this.to = builder.to;
        this.from = builder.from;
        this.text = builder.text;
        this.mediaUrls = builder.mediaUrls;
        this.template = builder.template;
        this.metadata = builder.metadata;
    }

    /** Always "whatsapp". */
    public String getChannel() { return channel; }
    public String getTo() { return to; }
    public String getFrom() { return from; }
    public String getText() { return text; }
    public List<String> getMediaUrls() { return mediaUrls; }
    public WhatsAppTemplateSendParams getTemplate() { return template; }
    public Map<String, Object> getMetadata() { return metadata; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String from;
        private String text;
        private List<String> mediaUrls;
        private WhatsAppTemplateSendParams template;
        private Map<String, Object> metadata;

        /** Destination phone number in E.164 format (e.g., +15551234567). */
        public Builder to(String to) {
            this.to = to;
            return this;
        }

        /**
         * Sending number in E.164 format. Required — must be one of your
         * numbers with an active WhatsApp connection.
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        /**
         * Free-form message text (max 4096 bytes), or the caption when
         * {@code mediaUrls} is provided (max 1024 bytes). Requires an open
         * 24-hour window — outside it the API responds 422
         * {@code whatsapp_window_closed}; send a {@code template} instead.
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Media attachment URL. WhatsApp accepts exactly one per message.
         * Must be a publicly accessible HTTPS URL.
         */
        public Builder mediaUrls(List<String> mediaUrls) {
            this.mediaUrls = mediaUrls;
            return this;
        }

        /** Approved template to send. Works regardless of the 24-hour window. */
        public Builder template(WhatsAppTemplateSendParams template) {
            this.template = template;
            return this;
        }

        /** Custom JSON metadata to attach to the message (max 4KB). */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public SendWhatsAppMessageRequest build() {
            return new SendWhatsAppMessageRequest(this);
        }
    }
}
