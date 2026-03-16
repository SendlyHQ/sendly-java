package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a conversation thread.
 */
public class Conversation {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CLOSED = "closed";

    private final String id;

    @SerializedName("phone_number")
    private final String phoneNumber;

    private final String status;

    @SerializedName("unread_count")
    private final int unreadCount;

    @SerializedName("message_count")
    private final int messageCount;

    @SerializedName("last_message_text")
    private final String lastMessageText;

    @SerializedName("last_message_at")
    private final Instant lastMessageAt;

    @SerializedName("last_message_direction")
    private final String lastMessageDirection;

    private final Map<String, Object> metadata;

    private final List<String> tags;

    @SerializedName("contact_id")
    private final String contactId;

    @SerializedName("created_at")
    private final Instant createdAt;

    @SerializedName("updated_at")
    private final Instant updatedAt;

    private final List<Message> messages;
    private final JsonObject messagesPagination;

    /**
     * Create a Conversation from a JSON object.
     */
    public Conversation(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.phoneNumber = getStringOrNull(json, "phoneNumber", "phone_number");
        this.status = getStringOrNull(json, "status");
        this.unreadCount = getIntOrDefault(json, "unreadCount", "unread_count", 0);
        this.messageCount = getIntOrDefault(json, "messageCount", "message_count", 0);
        this.lastMessageText = getStringOrNull(json, "lastMessageText", "last_message_text");
        this.lastMessageAt = parseInstant(getStringOrNull(json, "lastMessageAt", "last_message_at"));
        this.lastMessageDirection = getStringOrNull(json, "lastMessageDirection", "last_message_direction");
        this.metadata = parseMetadata(json);
        this.tags = parseTags(json);
        this.contactId = getStringOrNull(json, "contactId", "contact_id");
        this.createdAt = parseInstant(getStringOrNull(json, "createdAt", "created_at"));
        this.updatedAt = parseInstant(getStringOrNull(json, "updatedAt", "updated_at"));

        if (json.has("messages") && json.get("messages").isJsonObject()) {
            JsonObject msgsObj = json.getAsJsonObject("messages");
            this.messages = new ArrayList<>();
            if (msgsObj.has("data") && msgsObj.get("data").isJsonArray()) {
                JsonArray data = msgsObj.getAsJsonArray("data");
                for (int i = 0; i < data.size(); i++) {
                    this.messages.add(new Message(data.get(i).getAsJsonObject()));
                }
            }
            this.messagesPagination = msgsObj.has("pagination") ?
                    msgsObj.getAsJsonObject("pagination") : null;
        } else {
            this.messages = null;
            this.messagesPagination = null;
        }
    }

    @SuppressWarnings("unchecked")
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
        return new HashMap<>();
    }

    private List<String> parseTags(JsonObject json) {
        List<String> result = new ArrayList<>();
        if (json.has("tags") && !json.get("tags").isJsonNull() && json.get("tags").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("tags");
            for (int i = 0; i < arr.size(); i++) {
                if (!arr.get(i).isJsonNull()) {
                    result.add(arr.get(i).getAsString());
                }
            }
        }
        return result;
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private String getStringOrNull(JsonObject json, String key1, String key2) {
        if (json.has(key1) && !json.get(key1).isJsonNull()) {
            return json.get(key1).getAsString();
        }
        if (json.has(key2) && !json.get(key2).isJsonNull()) {
            return json.get(key2).getAsString();
        }
        return null;
    }

    private int getIntOrDefault(JsonObject json, String key1, String key2, int defaultValue) {
        if (json.has(key1) && !json.get(key1).isJsonNull()) {
            return json.get(key1).getAsInt();
        }
        if (json.has(key2) && !json.get(key2).isJsonNull()) {
            return json.get(key2).getAsInt();
        }
        return defaultValue;
    }

    private Instant parseInstant(String value) {
        if (value == null) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public String getLastMessageDirection() {
        return lastMessageDirection;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getContactId() {
        return contactId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public JsonObject getMessagesPagination() {
        return messagesPagination;
    }

    // Helper methods

    /**
     * Check if the conversation is active.
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * Check if the conversation is closed.
     */
    public boolean isClosed() {
        return STATUS_CLOSED.equals(status);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id='" + id + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                ", unreadCount=" + unreadCount +
                ", messageCount=" + messageCount +
                '}';
    }
}
