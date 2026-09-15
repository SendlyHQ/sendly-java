package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Every voice an AI agent can use, from {@code voice().voices().list()}.
 */
public class VoiceListResponse {
    private final List<Voice> data;

    /**
     * Create a VoiceListResponse from a JSON object.
     */
    public VoiceListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("data");
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    data.add(new Voice(array.get(i).getAsJsonObject()));
                }
            }
        }
    }

    /** Every voice an agent can use. */
    public List<Voice> getData() {
        return data;
    }
}
