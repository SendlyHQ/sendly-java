package com.sendly.models;

/**
 * Request object for AI-enhancing a draft message. Provide {@code text},
 * {@code messageType}, or both — at least one is required.
 */
public class EnhanceMessageRequest {
    private final String text;
    private final String messageType;

    /**
     * Create a new enhance request.
     *
     * @param text        Draft message text to rewrite (optional if messageType is provided)
     * @param messageType Message type hint to steer the rewrite, e.g. "marketing" or "transactional"
     */
    public EnhanceMessageRequest(String text, String messageType) {
        this.text = text;
        this.messageType = messageType;
    }

    public String getText() {
        return text;
    }

    public String getMessageType() {
        return messageType;
    }

    /**
     * Create a builder for EnhanceMessageRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for EnhanceMessageRequest.
     */
    public static class Builder {
        private String text;
        private String messageType;

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Set the message type hint.
         *
         * @param messageType "marketing" or "transactional" to steer the rewrite
         */
        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public EnhanceMessageRequest build() {
            return new EnhanceMessageRequest(text, messageType);
        }
    }
}
