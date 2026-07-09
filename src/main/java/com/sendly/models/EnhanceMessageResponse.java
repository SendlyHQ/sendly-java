package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Result of an AI message enhancement.
 */
public class EnhanceMessageResponse {
    private final String enhanced;
    private final String explanation;
    private final String model;

    /**
     * Create an EnhanceMessageResponse from a JSON object.
     */
    public EnhanceMessageResponse(JsonObject json) {
        this.enhanced = getStringOrNull(json, "enhanced");
        this.explanation = getStringOrNull(json, "explanation");
        this.model = getStringOrNull(json, "model");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /**
     * The rewritten message, capped at 160 characters (one SMS segment). When AI
     * enhancement is unavailable, this falls back to the original text.
     */
    public String getEnhanced() {
        return enhanced;
    }

    /**
     * Short explanation of what changed. An empty string on the fallback path.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * The model that produced the enhancement, when available.
     */
    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "EnhanceMessageResponse{" +
                "enhanced='" + enhanced + '\'' +
                ", explanation='" + explanation + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
