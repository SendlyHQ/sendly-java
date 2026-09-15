package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Confirmation from {@code voice().agents().delete()}.
 */
public class DeletedVoiceAgent {
    private final String id;
    private final String object;
    private final boolean deleted;

    /**
     * Create a DeletedVoiceAgent from a JSON object.
     */
    public DeletedVoiceAgent(JsonObject json) {
        this.id = json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString() : null;
        this.object = json.has("object") && !json.get("object").isJsonNull() ? json.get("object").getAsString() : null;
        this.deleted = json.has("deleted") && !json.get("deleted").isJsonNull() && json.get("deleted").getAsBoolean();
    }

    /** The deleted agent's id. */
    public String getId() {
        return id;
    }

    /** Always {@code "voice_agent"}. */
    public String getObject() {
        return object;
    }

    /** Always true. */
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "DeletedVoiceAgent{" +
                "id='" + id + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
