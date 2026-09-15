package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The workspace's AI agents, from {@code voice().agents().list()}.
 */
public class VoiceAgentListResponse {
    private final List<VoiceAgent> data;

    /**
     * Create a VoiceAgentListResponse from a JSON object.
     */
    public VoiceAgentListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("data");
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    data.add(new VoiceAgent(array.get(i).getAsJsonObject()));
                }
            }
        }
    }

    /** The workspace's agents. */
    public List<VoiceAgent> getData() {
        return data;
    }
}
