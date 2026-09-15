package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A voice an AI agent can speak with, from {@code voice().voices().list()}.
 */
public class Voice {
    private final String id;
    private final String label;
    private final String language;

    /**
     * Create a Voice from a JSON object.
     */
    public Voice(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.label = getStringOrNull(json, "label");
        this.language = getStringOrNull(json, "language");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /** Voice id to pass as {@code voice} when creating or updating an agent. */
    public String getId() {
        return id;
    }

    /** Human-readable name, e.g. {@code "Ashley (US, warm)"}. */
    public String getLabel() {
        return label;
    }

    /** Language the voice speaks, e.g. {@code "en"}. */
    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "Voice{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
