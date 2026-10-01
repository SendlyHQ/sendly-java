package com.sendly.models;

import com.google.gson.annotations.SerializedName;

public class ScheduleCampaignRequest {
    @SerializedName("scheduledAt")
    private final String scheduledAt;
    private final String timezone;

    private ScheduleCampaignRequest(Builder builder) {
        this.scheduledAt = builder.scheduledAt;
        this.timezone = builder.timezone;
    }

    public String getScheduledAt() { return scheduledAt; }
    public String getTimezone() { return timezone; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String scheduledAt;
        private String timezone;

        public Builder scheduledAt(String scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public ScheduleCampaignRequest build() {
            return new ScheduleCampaignRequest(this);
        }
    }
}
