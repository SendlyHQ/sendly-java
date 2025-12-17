package com.sendly.models;

/**
 * Request object for scheduling an SMS message.
 */
public class ScheduleMessageRequest {
    private final String to;
    private final String text;
    private final String scheduledAt;
    private final String from;

    /**
     * Create a new schedule message request.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param scheduledAt ISO 8601 datetime for delivery (must be at least 1 minute in the future)
     */
    public ScheduleMessageRequest(String to, String text, String scheduledAt) {
        this(to, text, scheduledAt, null);
    }

    /**
     * Create a new schedule message request with sender ID.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param scheduledAt ISO 8601 datetime for delivery (must be at least 1 minute in the future)
     * @param from        Optional sender ID
     */
    public ScheduleMessageRequest(String to, String text, String scheduledAt, String from) {
        this.to = to;
        this.text = text;
        this.scheduledAt = scheduledAt;
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public String getFrom() {
        return from;
    }

    /**
     * Create a builder for ScheduleMessageRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ScheduleMessageRequest.
     */
    public static class Builder {
        private String to;
        private String text;
        private String scheduledAt;
        private String from;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder scheduledAt(String scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public ScheduleMessageRequest build() {
            return new ScheduleMessageRequest(to, text, scheduledAt, from);
        }
    }
}
