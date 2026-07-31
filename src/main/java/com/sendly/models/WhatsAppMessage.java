package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A sent WhatsApp message.
 */
public class WhatsAppMessage {
    private String id;
    private String channel;
    private String messageFormat;
    private String to;
    private String from;
    private String text;
    private String status;
    private int segments;
    private int creditsUsed;
    private WhatsAppMessageDetails whatsapp;
    private String createdAt;
    private Map<String, Object> metadata;

    public WhatsAppMessage() {}

    public WhatsAppMessage(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("channel") && !json.get("channel").isJsonNull()) {
            this.channel = json.get("channel").getAsString();
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
        if (json.has("whatsapp") && json.get("whatsapp").isJsonObject()) {
            this.whatsapp = new WhatsAppMessageDetails(json.getAsJsonObject("whatsapp"));
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

    /** Always "whatsapp". */
    public String getChannel() { return channel; }

    /** Always "whatsapp". */
    public String getMessageFormat() { return messageFormat; }

    /** Destination phone number. */
    public String getTo() { return to; }

    /** Sending number. */
    public String getFrom() { return from; }

    /**
     * Body text for free-form text sends; null for template and media sends.
     */
    public String getText() { return text; }

    /** Current delivery status. */
    public String getStatus() { return status; }

    /** Always 1 — WhatsApp has no segment concept. */
    public int getSegments() { return segments; }

    /**
     * Credits charged for this message (priced by destination country and
     * category).
     */
    public int getCreditsUsed() { return creditsUsed; }

    /** WhatsApp-specific details. */
    public WhatsAppMessageDetails getWhatsapp() { return whatsapp; }

    /** ISO 8601 timestamp when the message was created. */
    public String getCreatedAt() { return createdAt; }

    /** Custom JSON metadata attached to the message. */
    public Map<String, Object> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return "WhatsAppMessage{" +
                "id='" + id + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", status='" + status + '\'' +
                ", creditsUsed=" + creditsUsed +
                '}';
    }
}
