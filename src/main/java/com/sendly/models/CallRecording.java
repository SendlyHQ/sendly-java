package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Where a call's recording stands, with a short-lived download URL once it is
 * ready.
 */
public class CallRecording {
    private final String callId;
    private final String status;
    private final String url;
    private final String expiresAt;
    private final String contentType;

    /**
     * Create a CallRecording from a JSON object.
     */
    public CallRecording(JsonObject json) {
        this.callId = getStringOrNull(json, "callId");
        this.status = getStringOrNull(json, "status");
        this.url = getStringOrNull(json, "url");
        this.expiresAt = getStringOrNull(json, "expiresAt");
        this.contentType = getStringOrNull(json, "contentType");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /** The call the recording belongs to. */
    public String getCallId() {
        return callId;
    }

    /** {@code "none"}, {@code "recording"}, {@code "ready"} or {@code "failed"}; see {@link CallRecordingStatus}. */
    public String getStatus() {
        return status;
    }

    /** Signed download URL, valid for five minutes. {@code null} unless the status is {@code "ready"}. */
    public String getUrl() {
        return url;
    }

    /** When {@link #getUrl()} stops working (ISO 8601), or {@code null}. */
    public String getExpiresAt() {
        return expiresAt;
    }

    /** {@code "audio/ogg"} when ready, else {@code null}. */
    public String getContentType() {
        return contentType;
    }

    /** True when the recording can be downloaded right now. */
    public boolean isReady() {
        return CallRecordingStatus.READY.equals(status) && url != null;
    }

    @Override
    public String toString() {
        return "CallRecording{" +
                "callId='" + callId + '\'' +
                ", status='" + status + '\'' +
                ", expiresAt='" + expiresAt + '\'' +
                '}';
    }
}
