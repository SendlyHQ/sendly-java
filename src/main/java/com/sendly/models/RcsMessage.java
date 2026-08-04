package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A sent RCS message — or its SMS fallback.
 * <p>
 * Check {@link #getChannel()} (or {@link #getFellBackTo()}) to see which leg
 * delivered: {@code channel} is {@code rcs} for a native RCS send and
 * {@code sms} when the recipient's device doesn't support RCS and the message
 * fell back to SMS (billed as SMS). {@link #getRcs()} carries the leg-specific
 * details either way.
 * </p>
 */
public class RcsMessage {
    private String id;
    private String channel;
    private String fellBackTo;
    private String messageFormat;
    private String to;
    private String from;
    private String text;
    private String status;
    private int segments;
    private int creditsUsed;
    private RcsDetails rcs;
    private String createdAt;
    private Map<String, Object> metadata;

    public RcsMessage() {}

    public RcsMessage(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("channel") && !json.get("channel").isJsonNull()) {
            this.channel = json.get("channel").getAsString();
        }
        if (json.has("fellBackTo") && !json.get("fellBackTo").isJsonNull()) {
            this.fellBackTo = json.get("fellBackTo").getAsString();
        }
        if (json.has("message_format") && !json.get("message_format").isJsonNull()) {
            this.messageFormat = json.get("message_format").getAsString();
        }
        if (json.has("to") && !json.get("to").isJsonNull()) {
            this.to = json.get("to").getAsString();
        }
        if (json.has("from") && !json.get("from").isJsonNull()) {
            this.from = json.get("from").getAsString();
        }
        if (json.has("text") && !json.get("text").isJsonNull()) {
            this.text = json.get("text").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        this.segments = json.has("segments") && !json.get("segments").isJsonNull() ?
                json.get("segments").getAsInt() : 1;
        if (json.has("creditsUsed") && !json.get("creditsUsed").isJsonNull()) {
            this.creditsUsed = json.get("creditsUsed").getAsInt();
        }
        if (json.has("rcs") && json.get("rcs").isJsonObject()) {
            this.rcs = new RcsDetails(json.getAsJsonObject("rcs"));
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
        this.metadata = parseMetadata(json);
    }

    private Map<String, Object> parseMetadata(JsonObject json) {
        if (json.has("metadata") && !json.get("metadata").isJsonNull() && json.get("metadata").isJsonObject()) {
            Map<String, Object> result = new HashMap<>();
            JsonObject metaObj = json.getAsJsonObject("metadata");
            for (String key : metaObj.keySet()) {
                if (!metaObj.get(key).isJsonNull()) {
                    if (metaObj.get(key).isJsonPrimitive()) {
                        var prim = metaObj.get(key).getAsJsonPrimitive();
                        if (prim.isString()) {
                            result.put(key, prim.getAsString());
                        } else if (prim.isNumber()) {
                            result.put(key, prim.getAsNumber());
                        } else if (prim.isBoolean()) {
                            result.put(key, prim.getAsBoolean());
                        }
                    } else {
                        result.put(key, metaObj.get(key).toString());
                    }
                }
            }
            return result;
        }
        return null;
    }

    /** Unique message identifier. */
    public String getId() { return id; }

    /**
     * The channel that delivered: {@code rcs}, or {@code sms} when the send
     * fell back.
     */
    public String getChannel() { return channel; }

    /**
     * {@code "sms"} when this send fell back to SMS; null on a native RCS
     * send.
     */
    public String getFellBackTo() { return fellBackTo; }

    /** {@code rcs}, or {@code sms} when the send fell back. */
    public String getMessageFormat() { return messageFormat; }

    /** Destination phone number. */
    public String getTo() { return to; }

    /**
     * The sender the recipient sees: the RCS agent name, or the SMS sender on
     * a fallback.
     */
    public String getFrom() { return from; }

    /** Body text for text sends; null for card sends. */
    public String getText() { return text; }

    /** Current delivery status. */
    public String getStatus() { return status; }

    /** Always 1 for native RCS; SMS segment count on a fallback. */
    public int getSegments() { return segments; }

    /**
     * Credits charged for this message — RCS pricing natively, SMS pricing on
     * a fallback.
     */
    public int getCreditsUsed() { return creditsUsed; }

    /** RCS-specific details for either leg. */
    public RcsDetails getRcs() { return rcs; }

    /** ISO 8601 timestamp when the message was created. */
    public String getCreatedAt() { return createdAt; }

    /** Custom JSON metadata attached to the message. */
    public Map<String, Object> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return "RcsMessage{" +
                "id='" + id + '\'' +
                ", channel='" + channel + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", status='" + status + '\'' +
                ", creditsUsed=" + creditsUsed +
                '}';
    }

    /**
     * RCS-specific details on a sent message.
     */
    public static class RcsDetails {
        private String kind;
        private String agentId;
        private String agentName;
        private String requestedChannel;
        private Boolean suggestionsDropped;

        public RcsDetails() {}

        public RcsDetails(JsonObject json) {
            if (json.has("kind") && !json.get("kind").isJsonNull()) {
                this.kind = json.get("kind").getAsString();
            }
            if (json.has("agentId") && !json.get("agentId").isJsonNull()) {
                this.agentId = json.get("agentId").getAsString();
            }
            if (json.has("agentName") && !json.get("agentName").isJsonNull()) {
                this.agentName = json.get("agentName").getAsString();
            }
            if (json.has("requestedChannel") && !json.get("requestedChannel").isJsonNull()) {
                this.requestedChannel = json.get("requestedChannel").getAsString();
            }
            if (json.has("suggestionsDropped") && !json.get("suggestionsDropped").isJsonNull()) {
                this.suggestionsDropped = json.get("suggestionsDropped").getAsBoolean();
            }
        }

        /**
         * What was sent natively: {@code text} or {@code card}; null on a
         * fallback.
         */
        public String getKind() { return kind; }

        /** The agent the message was sent as. */
        public String getAgentId() { return agentId; }

        /** The agent's display name (native sends only); null on a fallback. */
        public String getAgentName() { return agentName; }

        /**
         * {@code "rcs"} on a fallback — the channel that was asked for; null
         * on a native send.
         */
        public String getRequestedChannel() { return requestedChannel; }

        /**
         * True when suggestion chips were dropped because the send fell back
         * to SMS (suggestions have no SMS form); null otherwise.
         */
        public Boolean getSuggestionsDropped() { return suggestionsDropped; }
    }
}
