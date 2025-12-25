package com.sendly.models;

/**
 * Request object for scheduling an SMS message.
 */
public class ScheduleMessageRequest {
    private final String to;
    private final String text;
    private final String scheduledAt;
    private final String from;
    private final String messageType;

    /**
     * Create a new schedule message request.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param scheduledAt ISO 8601 datetime for delivery (must be at least 1 minute in the future)
     */
    public ScheduleMessageRequest(String to, String text, String scheduledAt) {
        this(to, text, scheduledAt, null, null);
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
        this(to, text, scheduledAt, from, null);
    }

    /**
     * Create a new schedule message request with sender ID and message type.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param scheduledAt ISO 8601 datetime for delivery (must be at least 1 minute in the future)
     * @param from        Optional sender ID
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     */
    public ScheduleMessageRequest(String to, String text, String scheduledAt, String from, String messageType) {
        this.to = to;
        this.text = text;
        this.scheduledAt = scheduledAt;
        this.from = from;
        this.messageType = messageType;
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

    public String getMessageType() {
        return messageType;
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
        private String messageType;

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

        /**
         * Set the message type.
         *
         * @param messageType "marketing" (default, subject to quiet hours) or "transactional" (24/7)
         */
        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public ScheduleMessageRequest build() {
            return new ScheduleMessageRequest(to, text, scheduledAt, from, messageType);
        }
    }
}
