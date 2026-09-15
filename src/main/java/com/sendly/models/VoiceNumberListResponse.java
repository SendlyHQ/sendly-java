package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The workspace's active numbers with their voice settings, from
 * {@code voice().numbers().list()}.
 */
public class VoiceNumberListResponse {
    private final List<VoiceNumber> data;

    /**
     * Create a VoiceNumberListResponse from a JSON object.
     */
    public VoiceNumberListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("data");
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    data.add(new VoiceNumber(array.get(i).getAsJsonObject()));
                }
            }
        }
    }

    /** Active numbers in the workspace, in the same order as the dashboard. */
    public List<VoiceNumber> getData() {
        return data;
    }
}
