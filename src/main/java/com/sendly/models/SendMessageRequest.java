package com.sendly.models;

/**
 * Request object for sending an SMS message.
 */
public class SendMessageRequest {
    private final String to;
    private final String text;
    private final String messageType;

    /**
     * Create a new send message request.
     *
     * @param to   Recipient phone number in E.164 format
     * @param text Message content
     */
    public SendMessageRequest(String to, String text) {
        this(to, text, null);
    }

    /**
     * Create a new send message request with message type.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     */
    public SendMessageRequest(String to, String text, String messageType) {
        this.to = to;
        this.text = text;
        this.messageType = messageType;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public String getMessageType() {
        return messageType;
    }

    /**
     * Create a builder for SendMessageRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SendMessageRequest.
     */
    public static class Builder {
        private String to;
        private String text;
        private String messageType;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
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

        public SendMessageRequest build() {
            return new SendMessageRequest(to, text, messageType);
        }
    }
}
