package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Request body for {@code numbers().update()}. Supply at least one supported
 * mutation:
 *
 * <ul>
 *   <li>{@code isDefault(true)} — make this the workspace's default sending
 *       number (the number must be {@code active}).</li>
 *   <li>{@code pendingCancellation(false)} — cancel a previously scheduled
 *       release and keep the number.</li>
 * </ul>
 *
 * <pre>{@code
 * // Make a number the default sender
 * UpdateNumberRequest request = UpdateNumberRequest.builder()
 *     .isDefault(true)
 *     .build();
 *
 * // Cancel a scheduled release ("keep this number")
 * UpdateNumberRequest keep = UpdateNumberRequest.builder()
 *     .pendingCancellation(false)
 *     .build();
 * }</pre>
 */
public class UpdateNumberRequest {
    private final Boolean isDefault;
    private final Boolean pendingCancellation;

    private UpdateNumberRequest(Builder builder) {
        this.isDefault = builder.isDefault;
        this.pendingCancellation = builder.pendingCancellation;
    }

    /** Whether the update makes this number the default sender, or null if unset. */
    public Boolean getIsDefault() { return isDefault; }

    /** Whether the update cancels a scheduled release, or null if unset. */
    public Boolean getPendingCancellation() { return pendingCancellation; }

    /** True when neither supported mutation was set. */
    public boolean isEmpty() {
        return isDefault == null && pendingCancellation == null;
    }

    /** Serialize to the JSON body the API expects (camelCase keys, set fields only). */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (isDefault != null) o.addProperty("isDefault", isDefault);
        if (pendingCancellation != null) o.addProperty("pendingCancellation", pendingCancellation);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean isDefault;
        private Boolean pendingCancellation;

        /** Make this number the workspace's default sender (only {@code true} is supported). */
        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        /** Cancel a scheduled period-end release (only {@code false} is supported). */
        public Builder pendingCancellation(boolean pendingCancellation) {
            this.pendingCancellation = pendingCancellation;
            return this;
        }

        public UpdateNumberRequest build() {
            return new UpdateNumberRequest(this);
        }
    }
}
