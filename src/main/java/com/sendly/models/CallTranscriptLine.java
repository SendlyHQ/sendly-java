package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * One line of an agent-handled call's transcript.
 */
public class CallTranscriptLine {
    private final String speaker;
    private final String text;
    private final long atMs;

    /**
     * Create a CallTranscriptLine from a JSON object.
     */
    public CallTranscriptLine(JsonObject json) {
        this.speaker = json.has("speaker") && !json.get("speaker").isJsonNull() ? json.get("speaker").getAsString() : null;
        this.text = json.has("text") && !json.get("text").isJsonNull() ? json.get("text").getAsString() : null;
        this.atMs = json.has("atMs") && !json.get("atMs").isJsonNull() ? json.get("atMs").getAsLong() : 0L;
    }

    /**
     * Who spoke: {@code "caller"} or {@code "agent"}.
     */
    public String getSpeaker() {
        return speaker;
    }

    /**
     * What was said.
     */
    public String getText() {
        return text;
    }

    /**
     * Milliseconds from the moment the call was answered.
     */
    public long getAtMs() {
        return atMs;
    }

    @Override
    public String toString() {
        return "CallTranscriptLine{" +
                "speaker='" + speaker + '\'' +
                ", text='" + text + '\'' +
                ", atMs=" + atMs +
                '}';
    }
}
