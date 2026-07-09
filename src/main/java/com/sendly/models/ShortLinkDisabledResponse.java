package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from enabling or disabling a short link.
 */
public class ShortLinkDisabledResponse {
    private final String code;
    private final boolean disabled;

    /**
     * Create a ShortLinkDisabledResponse from a JSON object.
     */
    public ShortLinkDisabledResponse(JsonObject json) {
        this.code = json.has("code") && !json.get("code").isJsonNull() ? json.get("code").getAsString() : null;
        this.disabled = json.has("disabled") && !json.get("disabled").isJsonNull()
                && json.get("disabled").getAsBoolean();
    }

    /**
     * Short code that was updated.
     */
    public String getCode() {
        return code;
    }

    /**
     * New disabled state.
     */
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String toString() {
        return "ShortLinkDisabledResponse{" +
                "code='" + code + '\'' +
                ", disabled=" + disabled +
                '}';
    }
}
