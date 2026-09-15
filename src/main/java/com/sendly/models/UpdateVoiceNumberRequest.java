package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Request body for {@code voice().numbers().update()}: how a number answers
 * phone calls. Only the fields you set are sent.
 *
 * <pre>{@code
 * UpdateVoiceNumberRequest request = UpdateVoiceNumberRequest.builder()
 *     .voiceEnabled(true)
 *     .voiceMode(VoiceMode.AGENT)
 *     .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
 *     .build();
 * }</pre>
 */
public class UpdateVoiceNumberRequest {
    private final Boolean voiceEnabled;
    private final String voiceMode;
    private final String agentId;

    private UpdateVoiceNumberRequest(Builder builder) {
        this.voiceEnabled = builder.voiceEnabled;
        this.voiceMode = builder.voiceMode;
        this.agentId = builder.agentId;
    }

    public Boolean getVoiceEnabled() { return voiceEnabled; }
    public String getVoiceMode() { return voiceMode; }
    public String getAgentId() { return agentId; }

    /** Serialize to the JSON body the API expects (camelCase keys). Unset fields are omitted. */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (voiceEnabled != null) o.addProperty("voiceEnabled", voiceEnabled);
        if (voiceMode != null) o.addProperty("voiceMode", voiceMode);
        if (agentId != null) o.addProperty("agentId", agentId);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean voiceEnabled;
        private String voiceMode;
        private String agentId;

        /**
         * Switch voice on or off. Turning it on connects the number for phone
         * calls and answers in {@code ring_dashboard} mode unless
         * {@link #voiceMode(String)} is {@code agent}; {@code false} switches
         * voice off and sets the mode to {@code none} whatever
         * {@link #voiceMode(String)} says.
         */
        public Builder voiceEnabled(boolean voiceEnabled) {
            this.voiceEnabled = voiceEnabled;
            return this;
        }

        /**
         * How the number answers; see {@link VoiceMode}. On its own,
         * {@code ring_dashboard} or {@code agent} switches voice on, so it can
         * fail the way switching on does, and {@code none} switches it off.
         * {@code voiceEnabled(false)} wins over any mode, and {@code none}
         * with {@code voiceEnabled(true)} becomes {@code ring_dashboard}.
         */
        public Builder voiceMode(String voiceMode) {
            this.voiceMode = voiceMode;
            return this;
        }

        /**
         * The agent that answers in {@code agent} mode. Required (here or
         * already stored) when the mode is {@code agent}, and the agent must be
         * switched on. An empty string clears the stored agent.
         */
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public UpdateVoiceNumberRequest build() {
            return new UpdateVoiceNumberRequest(this);
        }
    }
}
