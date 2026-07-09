package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from sending a group MMS.
 */
public class GroupMessageResponse {
    private final String id;
    private final String status;
    private final List<String> to;
    private final String groupMessageId;
    private final boolean simulated;
    private final String message;

    /**
     * Create a GroupMessageResponse from a JSON object.
     */
    public GroupMessageResponse(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.status = getStringOrNull(json, "status");
        this.groupMessageId = getStringOrNull(json, "group_message_id", "groupMessageId");
        this.simulated = json.has("simulated") && !json.get("simulated").isJsonNull()
                && json.get("simulated").getAsBoolean();
        this.message = getStringOrNull(json, "message");

        this.to = new ArrayList<>();
        if (json.has("to") && json.get("to").isJsonArray()) {
            JsonArray toArray = json.getAsJsonArray("to");
            for (int i = 0; i < toArray.size(); i++) {
                if (!toArray.get(i).isJsonNull()) {
                    to.add(toArray.get(i).getAsString());
                }
            }
        }
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

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getTo() {
        return to;
    }

    public String getGroupMessageId() {
        return groupMessageId;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "GroupMessageResponse{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", to=" + to +
                ", groupMessageId='" + groupMessageId + '\'' +
                ", simulated=" + simulated +
                '}';
    }
}
