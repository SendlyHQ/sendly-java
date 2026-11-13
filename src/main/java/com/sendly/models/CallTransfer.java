package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * The latest transfer on a call, from {@code Call.getTransfer()}, which is
 * {@code null} when the call was never transferred.
 */
public class CallTransfer {
    private final String to;
    private final String status;
    private final String failureReason;

    /**
     * Create a CallTransfer from a JSON object.
     */
    public CallTransfer(JsonObject json) {
        this.to = getStringOrNull(json, "to");
        this.status = getStringOrNull(json, "status");
        this.failureReason = getStringOrNull(json, "failureReason");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /** The number the caller is transferred to, in E.164. */
    public String getTo() {
        return to;
    }

    /**
     * {@code "pending"} while the number rings, {@code "completed"} once the
     * caller was handed over, {@code "failed"} when the transfer did not go
     * through and the caller is still on the call; see
     * {@link CallTransferStatus}.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Why the transfer failed ({@code "busy"}, {@code "no_answer"},
     * {@code "declined"}, {@code "unavailable"} or {@code "unknown"}; see
     * {@link CallTransferFailureReason}), or {@code null} unless the status is
     * {@code failed}.
     */
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String toString() {
        return "CallTransfer{" +
                "to='" + to + '\'' +
                ", status='" + status + '\'' +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
