package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * One step of a connection test, from {@code VoiceAgentTest.getSteps()}.
 */
public class VoiceAgentTestStep {
    private final String step;
    private final boolean ok;
    private final String detail;

    /**
     * Create a VoiceAgentTestStep from a JSON object.
     */
    public VoiceAgentTestStep(JsonObject json) {
        this.step = getStringOrNull(json, "step");
        this.ok = json.has("ok") && !json.get("ok").isJsonNull() && json.get("ok").getAsBoolean();
        this.detail = getStringOrNull(json, "detail");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /**
     * {@code "address"}, {@code "connect"}, {@code "login"} or
     * {@code "reply"}; see {@link VoiceAgentTestStepName}. More values may be
     * added.
     */
    public String getStep() {
        return step;
    }

    /** Whether this step worked. */
    public boolean isOk() {
        return ok;
    }

    /** What happened at this step, in plain words. */
    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "VoiceAgentTestStep{" +
                "step='" + step + '\'' +
                ", ok=" + ok +
                ", detail='" + detail + '\'' +
                '}';
    }
}
