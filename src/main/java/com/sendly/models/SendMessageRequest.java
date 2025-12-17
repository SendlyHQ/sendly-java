package com.sendly.models;

/**
 * Request object for sending an SMS message.
 */
public class SendMessageRequest {
    private final String to;
    private final String text;

    /**
     * Create a new send message request.
     *
     * @param to   Recipient phone number in E.164 format
     * @param text Message content
     */
    public SendMessageRequest(String to, String text) {
        this.to = to;
        this.text = text;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
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

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public SendMessageRequest build() {
            return new SendMessageRequest(to, text);
        }
    }
}
