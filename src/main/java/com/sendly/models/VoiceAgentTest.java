package com.sendly.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What a connection test found, from {@code voice().agents().test()}. A
 * failed test is still a successful request: check {@link #isOk()}.
 */
public class VoiceAgentTest {
    private final boolean ok;
    private final String outcome;
    private final String message;
    private final Integer status;
    private final Integer latencyMs;
    private final boolean signed;
    private final List<VoiceAgentTestStep> steps;

    /**
     * Create a VoiceAgentTest from a JSON object.
     */
    public VoiceAgentTest(JsonObject json) {
        this.ok = getBoolean(json, "ok");
        this.outcome = getStringOrNull(json, "outcome");
        this.message = getStringOrNull(json, "message");
        this.status = getIntOrNull(json, "status");
        this.latencyMs = getIntOrNull(json, "latencyMs");
        this.signed = getBoolean(json, "signed");
        List<VoiceAgentTestStep> parsed = new ArrayList<>();
        if (json.has("steps") && json.get("steps").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("steps")) {
                if (element.isJsonObject()) {
                    parsed.add(new VoiceAgentTestStep(element.getAsJsonObject()));
                }
            }
        }
        this.steps = Collections.unmodifiableList(parsed);
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static boolean getBoolean(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).getAsBoolean();
    }

    private static Integer getIntOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
    }

    /** Whether the agent answered the way a call needs. */
    public boolean isOk() {
        return ok;
    }

    /**
     * {@code "ok"}, or why not; see {@link VoiceAgentTestOutcome}. More
     * values may be added.
     */
    public String getOutcome() {
        return outcome;
    }

    /** One or two sentences to show a person: what happened and what to change. */
    public String getMessage() {
        return message;
    }

    /** The HTTP or SIP status the agent answered with, or {@code null}. */
    public Integer getStatus() {
        return status;
    }

    /** How long the agent took to answer, or {@code null}. */
    public Integer getLatencyMs() {
        return latencyMs;
    }

    /**
     * Whether the request carried {@code X-Sendly-Signature}, as calls do.
     * Always {@code false} for {@code sip}.
     */
    public boolean isSigned() {
        return signed;
    }

    /** Each step tried, in order, up to the first that failed. */
    public List<VoiceAgentTestStep> getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        return "VoiceAgentTest{" +
                "ok=" + ok +
                ", outcome='" + outcome + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", latencyMs=" + latencyMs +
                ", signed=" + signed +
                ", steps=" + steps +
                '}';
    }
}
