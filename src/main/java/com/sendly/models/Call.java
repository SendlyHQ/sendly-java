package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A phone call: placed over the API and handled by an AI agent, or taken on
 * one of the workspace's voice-enabled numbers.
 * <p>
 * Timestamps are ISO 8601 strings. {@link #getTranscript()} is populated only
 * by {@code calls().get(id)} and only for agent-handled calls; it is
 * {@code null} everywhere else.
 * </p>
 */
public class Call {
    private final String id;
    private final String object;
    private final String kind;
    private final String direction;
    private final String status;
    private final String handledBy;
    private final String agentId;
    private final String from;
    private final String to;
    private final String callerName;
    private final String calleeName;
    private final String startedAt;
    private final String answeredAt;
    private final String endedAt;
    private final int durationSecs;
    private final int creditsCharged;
    private final String billing;
    private final String hangupClass;
    private final String recordingStatus;
    private final Map<String, String> metadata;
    private final List<CallTranscriptLine> transcript;

    /**
     * Create a Call from a JSON object.
     */
    public Call(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.object = getStringOrNull(json, "object");
        this.kind = getStringOrNull(json, "kind");
        this.direction = getStringOrNull(json, "direction");
        this.status = getStringOrNull(json, "status");
        this.handledBy = getStringOrNull(json, "handledBy");
        this.agentId = getStringOrNull(json, "agentId");
        this.from = getStringOrNull(json, "from");
        this.to = getStringOrNull(json, "to");
        this.callerName = getStringOrNull(json, "callerName");
        this.calleeName = getStringOrNull(json, "calleeName");
        this.startedAt = getStringOrNull(json, "startedAt");
        this.answeredAt = getStringOrNull(json, "answeredAt");
        this.endedAt = getStringOrNull(json, "endedAt");
        this.durationSecs = getIntOrZero(json, "durationSecs");
        this.creditsCharged = getIntOrZero(json, "creditsCharged");
        this.billing = getStringOrNull(json, "billing");
        this.hangupClass = getStringOrNull(json, "hangupClass");
        this.recordingStatus = getStringOrNull(json, "recordingStatus");

        Map<String, String> meta = new LinkedHashMap<>();
        if (json.has("metadata") && json.get("metadata").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("metadata").entrySet()) {
                JsonElement value = entry.getValue();
                if (value != null && value.isJsonPrimitive()) {
                    meta.put(entry.getKey(), value.getAsString());
                }
            }
        }
        this.metadata = Collections.unmodifiableMap(meta);

        if (json.has("transcript") && json.get("transcript").isJsonArray()) {
            List<CallTranscriptLine> lines = new ArrayList<>();
            JsonArray array = json.getAsJsonArray("transcript");
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    lines.add(new CallTranscriptLine(array.get(i).getAsJsonObject()));
                }
            }
            this.transcript = Collections.unmodifiableList(lines);
        } else {
            this.transcript = null;
        }
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static int getIntOrZero(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }

    /** Unique call identifier. */
    public String getId() {
        return id;
    }

    /** Always {@code "call"}. */
    public String getObject() {
        return object;
    }

    /** {@code "pstn"} for a phone call, {@code "internal"} for a browser call between teammates; see {@link CallKind}. */
    public String getKind() {
        return kind;
    }

    /** {@code "inbound"} or {@code "outbound"}; see {@link CallDirection}. */
    public String getDirection() {
        return direction;
    }

    /** Where the call is in its life; see {@link CallStatus}. */
    public String getStatus() {
        return status;
    }

    /** {@code "agent"} when an AI agent is on the line, {@code "dashboard"} when the team answers; see {@link CallHandledBy}. */
    public String getHandledBy() {
        return handledBy;
    }

    /** The agent handling the call, or {@code null}. */
    public String getAgentId() {
        return agentId;
    }

    /** Calling number in E.164, or {@code null} on an internal call. */
    public String getFrom() {
        return from;
    }

    /** Called number in E.164, or {@code null} on an internal call. */
    public String getTo() {
        return to;
    }

    /** Display name of the calling party, or {@code null}. */
    public String getCallerName() {
        return callerName;
    }

    /** Display name of the called party, or {@code null}. */
    public String getCalleeName() {
        return calleeName;
    }

    /** When the call was created (ISO 8601). */
    public String getStartedAt() {
        return startedAt;
    }

    /** When the call was answered (ISO 8601), or {@code null} while ringing or if never answered. */
    public String getAnsweredAt() {
        return answeredAt;
    }

    /** When the call ended (ISO 8601), or {@code null} while live. */
    public String getEndedAt() {
        return endedAt;
    }

    /** Answered seconds; 0 until the call has ended. */
    public int getDurationSecs() {
        return durationSecs;
    }

    /** Credits charged so far (final once {@link #getBilling()} is {@code "settled"}). */
    public int getCreditsCharged() {
        return creditsCharged;
    }

    /** {@code "metered"} while a phone call runs, {@code "settled"} once final, {@code "unbilled"} when never charged; see {@link CallBilling}. */
    public String getBilling() {
        return billing;
    }

    /** Why the call ended, or {@code null} while live. Unrecognised reasons are reported as {@code "ended"}. */
    public String getHangupClass() {
        return hangupClass;
    }

    /** {@code null}, {@code "recording"}, {@code "ready"} or {@code "failed"}; see {@link CallRecordingStatus}. */
    public String getRecordingStatus() {
        return recordingStatus;
    }

    /** The key/value pairs attached when the call was created; empty when none. */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * What was said on an agent-handled call, in order. Only populated by
     * {@code calls().get(id)} for agent-handled calls; {@code null} otherwise
     * (an answered call where nothing was said returns an empty list).
     */
    public List<CallTranscriptLine> getTranscript() {
        return transcript;
    }

    @Override
    public String toString() {
        return "Call{" +
                "id='" + id + '\'' +
                ", direction='" + direction + '\'' +
                ", status='" + status + '\'' +
                ", handledBy='" + handledBy + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", durationSecs=" + durationSecs +
                ", creditsCharged=" + creditsCharged +
                ", hangupClass='" + hangupClass + '\'' +
                '}';
    }
}
