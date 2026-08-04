package com.sendly.models;

import java.util.List;
import java.util.Map;

/**
 * Request object for sending an RCS message via
 * {@code messages().send(SendRcsMessageRequest)}.
 * <p>
 * Provide exactly one of:
 * </p>
 * <ul>
 *   <li>{@code text} — plain text, optionally with {@code suggestions}
 *       (tap-to-reply and open-URL chips)</li>
 *   <li>{@code card} — a rich card with title, description, optional image,
 *       and optional chips</li>
 * </ul>
 * <p>
 * Delivery picks the leg per recipient: RCS when the device supports it,
 * otherwise an automatic SMS fallback (billed as SMS) for text sends — the
 * default; disable it with {@code fallbackToSms(false)} to get a 422 instead.
 * Rich cards have no SMS form and never fall back. RCS sends require a live
 * API key and an RCS agent registered on your workspace; {@code agentId} is
 * only needed when the workspace has more than one agent.
 * </p>
 */
public class SendRcsMessageRequest {
    private final String channel = "rcs";
    private final String to;
    private final String agentId;
    private final String text;
    private final RcsCard card;
    private final List<RcsSuggestion> suggestions;
    private final Boolean fallbackToSms;
    private final Map<String, Object> metadata;

    private SendRcsMessageRequest(Builder builder) {
        this.to = builder.to;
        this.agentId = builder.agentId;
        this.text = builder.text;
        this.card = builder.card;
        this.suggestions = builder.suggestions;
        this.fallbackToSms = builder.fallbackToSms;
        this.metadata = builder.metadata;
    }

    /** Always "rcs". */
    public String getChannel() { return channel; }
    public String getTo() { return to; }
    public String getAgentId() { return agentId; }
    public String getText() { return text; }
    public RcsCard getCard() { return card; }
    public List<RcsSuggestion> getSuggestions() { return suggestions; }
    public Boolean getFallbackToSms() { return fallbackToSms; }
    public Map<String, Object> getMetadata() { return metadata; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String agentId;
        private String text;
        private RcsCard card;
        private List<RcsSuggestion> suggestions;
        private Boolean fallbackToSms;
        private Map<String, Object> metadata;

        /** Destination phone number in E.164 format (e.g., +15551234567). */
        public Builder to(String to) {
            this.to = to;
            return this;
        }

        /**
         * The RCS agent to send as. Optional when the workspace has exactly
         * one agent; required (400 {@code rcs_agent_ambiguous}) when it has
         * more.
         */
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        /**
         * Plain message text. Exactly one of {@code text} or {@code card}.
         * Text sends fall back to SMS for non-RCS recipients.
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Rich card to send. Exactly one of {@code text} or {@code card}.
         * Cards have no SMS form and never fall back.
         */
        public Builder card(RcsCard card) {
            this.card = card;
            return this;
        }

        /**
         * Suggestion chips shown under a text message. Only valid alongside
         * {@code text} — put card chips in {@code card.suggestions}. Dropped
         * (with {@code suggestionsDropped} disclosed) when the send falls back
         * to SMS.
         */
        public Builder suggestions(List<RcsSuggestion> suggestions) {
            this.suggestions = suggestions;
            return this;
        }

        /**
         * Whether a text send to a non-RCS recipient falls back to SMS
         * (billed as SMS). Defaults to true; set false to get a 422
         * {@code rcs_not_supported_for_recipient} instead.
         */
        public Builder fallbackToSms(Boolean fallbackToSms) {
            this.fallbackToSms = fallbackToSms;
            return this;
        }

        /** Custom JSON metadata to attach to the message (max 4KB). */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public SendRcsMessageRequest build() {
            return new SendRcsMessageRequest(this);
        }
    }
}
