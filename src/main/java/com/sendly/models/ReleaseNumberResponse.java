package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code numbers().release()}.
 * <p>
 * A live paid purchase is cancelled at the end of the paid period — the
 * response then carries {@code scheduled == true} and a {@code scheduledReleaseAt}.
 * Everything else is released immediately ({@code scheduled == false}).
 */
public class ReleaseNumberResponse {
    private final boolean success;
    private final boolean scheduled;
    private final String scheduledReleaseAt;

    public ReleaseNumberResponse(JsonObject json) {
        this.success = json.has("success") && !json.get("success").isJsonNull()
                && json.get("success").getAsBoolean();
        this.scheduled = json.has("scheduled") && !json.get("scheduled").isJsonNull()
                && json.get("scheduled").getAsBoolean();
        this.scheduledReleaseAt = json.has("scheduledReleaseAt") && !json.get("scheduledReleaseAt").isJsonNull()
                ? json.get("scheduledReleaseAt").getAsString() : null;
    }

    /** True when the release (immediate or scheduled) succeeded. */
    public boolean isSuccess() { return success; }

    /** True when the number is scheduled to release at period end rather than immediately. */
    public boolean isScheduled() { return scheduled; }

    /** When the number is scheduled to be released (ISO-8601), or null for an immediate release. */
    public String getScheduledReleaseAt() { return scheduledReleaseAt; }

    @Override
    public String toString() {
        return "ReleaseNumberResponse{" +
                "success=" + success +
                ", scheduled=" + scheduled +
                ", scheduledReleaseAt='" + scheduledReleaseAt + '\'' +
                '}';
    }
}
