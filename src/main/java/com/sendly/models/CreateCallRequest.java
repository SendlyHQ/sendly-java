package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request body for {@code calls().create()}: a phone call that one of your AI
 * agents places and handles.
 *
 * <pre>{@code
 * CreateCallRequest request = CreateCallRequest.builder()
 *     .to("+15555550123")
 *     .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
 *     .from("+15555550188")
 *     .context("You are calling Jordan to confirm the 3pm appointment on Tuesday.")
 *     .metadata("crmId", "lead_8812")
 *     .build();
 * }</pre>
 */
public class CreateCallRequest {
    private final String to;
    private final String agentId;
    private final String from;
    private final String context;
    private final Map<String, String> metadata;

    private CreateCallRequest(Builder builder) {
        this.to = builder.to;
        this.agentId = builder.agentId;
        this.from = builder.from;
        this.context = builder.context;
        this.metadata = builder.metadata.isEmpty() ? null : new LinkedHashMap<>(builder.metadata);
    }

    public String getTo() { return to; }
    public String getAgentId() { return agentId; }
    public String getFrom() { return from; }
    public String getContext() { return context; }
    public Map<String, String> getMetadata() { return metadata; }

    /** Serialize to the JSON body the API expects (camelCase keys). */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (to != null) o.addProperty("to", to);
        if (agentId != null) o.addProperty("agentId", agentId);
        if (from != null) o.addProperty("from", from);
        if (context != null) o.addProperty("context", context);
        if (metadata != null) {
            JsonObject meta = new JsonObject();
            metadata.forEach(meta::addProperty);
            o.add("metadata", meta);
        }
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String agentId;
        private String from;
        private String context;
        private final Map<String, String> metadata = new LinkedHashMap<>();

        /** Number to call, in E.164 format. US and Canada only. */
        public Builder to(String to) {
            this.to = to;
            return this;
        }

        /** The AI agent that talks on the call. Required: an API-placed call has no human on the line. */
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        /**
         * A voice-enabled number in your workspace to call from. Optional when
         * the workspace has exactly one voice-enabled number; required
         * otherwise ({@code from_number_required}). It must be a US or
         * Canadian number ({@code from_number_not_supported}).
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        /**
         * Up to 2000 characters appended to the agent's instructions for this
         * call only, so it knows why it is calling. Not echoed back.
         */
        public Builder context(String context) {
            this.context = context;
            return this;
        }

        /**
         * Attach one key/value pair. Up to 20 keys of 1 to 40 characters
         * matching {@code [A-Za-z0-9_.:-]}, values up to 500 characters. Echoed
         * on every read and in every {@code call.*} webhook.
         */
        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        /** Replace the metadata map wholesale; see {@link #metadata(String, String)} for the limits. */
        public Builder metadata(Map<String, String> metadata) {
            this.metadata.clear();
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        public CreateCallRequest build() {
            return new CreateCallRequest(this);
        }
    }
}
