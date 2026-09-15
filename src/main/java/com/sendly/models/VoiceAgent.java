package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * An AI agent that answers and places phone calls, from
 * {@code voice().agents()}.
 * <p>
 * Timestamps are ISO 8601 strings.
 * </p>
 */
public class VoiceAgent {
    private final String id;
    private final String object;
    private final String name;
    private final boolean enabled;
    private final String voice;
    private final String voiceLabel;
    private final String language;
    private final String greeting;
    private final String instructions;
    private final VoiceAgentTools tools;
    private final boolean canSendSms;
    private final int callsHandled;
    private final int avgDurationSecs;
    private final String createdAt;
    private final String updatedAt;

    /**
     * Create a VoiceAgent from a JSON object.
     */
    public VoiceAgent(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.object = getStringOrNull(json, "object");
        this.name = getStringOrNull(json, "name");
        this.enabled = getBoolean(json, "enabled");
        this.voice = getStringOrNull(json, "voice");
        this.voiceLabel = getStringOrNull(json, "voiceLabel");
        this.language = getStringOrNull(json, "language");
        this.greeting = getStringOrNull(json, "greeting");
        this.instructions = getStringOrNull(json, "instructions");
        this.tools = json.has("tools") && json.get("tools").isJsonObject()
                ? new VoiceAgentTools(json.getAsJsonObject("tools")) : null;
        this.canSendSms = getBoolean(json, "canSendSms");
        this.callsHandled = getIntOrZero(json, "callsHandled");
        this.avgDurationSecs = getIntOrZero(json, "avgDurationSecs");
        this.createdAt = getStringOrNull(json, "createdAt");
        this.updatedAt = getStringOrNull(json, "updatedAt");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static boolean getBoolean(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).getAsBoolean();
    }

    private static int getIntOrZero(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }

    /** Unique agent identifier. */
    public String getId() {
        return id;
    }

    /** Always {@code "voice_agent"}. */
    public String getObject() {
        return object;
    }

    /** The agent's name. */
    public String getName() {
        return name;
    }

    /** False when the agent is switched off; a switched-off agent can't be pointed at a number or put on a call. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Voice id, one of {@code voice().voices().list()}. */
    public String getVoice() {
        return voice;
    }

    /** Human-readable voice name, e.g. {@code "Ashley (US, warm)"}. */
    public String getVoiceLabel() {
        return voiceLabel;
    }

    /** Language tag, e.g. {@code "en-US"}. */
    public String getLanguage() {
        return language;
    }

    /** What the agent says when it picks up ({@code ""} when unset). */
    public String getGreeting() {
        return greeting;
    }

    /** Business instructions the agent follows ({@code ""} when unset). */
    public String getInstructions() {
        return instructions;
    }

    /** What the agent may do on a call. */
    public VoiceAgentTools getTools() {
        return tools;
    }

    /** True when the agent holds its own scoped sending key, so {@code tools.sendSms} can send. */
    public boolean canSendSms() {
        return canSendSms;
    }

    /** Calls this agent has handled. */
    public int getCallsHandled() {
        return callsHandled;
    }

    /** Average answered duration of those calls, in seconds. */
    public int getAvgDurationSecs() {
        return avgDurationSecs;
    }

    /** When the agent was created (ISO 8601). */
    public String getCreatedAt() {
        return createdAt;
    }

    /** When the agent last changed (ISO 8601). */
    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "VoiceAgent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", voice='" + voice + '\'' +
                '}';
    }
}
