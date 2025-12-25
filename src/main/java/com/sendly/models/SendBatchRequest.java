package com.sendly.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object for sending a batch of SMS messages.
 */
public class SendBatchRequest {
    private final List<BatchMessageItem> messages;
    private final String from;
    private final String messageType;

    /**
     * Create a new send batch request.
     *
     * @param messages List of messages to send
     */
    public SendBatchRequest(List<BatchMessageItem> messages) {
        this(messages, null, null);
    }

    /**
     * Create a new send batch request with sender ID.
     *
     * @param messages List of messages to send
     * @param from     Optional sender ID (applies to all messages)
     */
    public SendBatchRequest(List<BatchMessageItem> messages, String from) {
        this(messages, from, null);
    }

    /**
     * Create a new send batch request with sender ID and message type.
     *
     * @param messages    List of messages to send
     * @param from        Optional sender ID (applies to all messages)
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     */
    public SendBatchRequest(List<BatchMessageItem> messages, String from, String messageType) {
        this.messages = messages;
        this.from = from;
        this.messageType = messageType;
    }

    public List<BatchMessageItem> getMessages() {
        return messages;
    }

    public String getFrom() {
        return from;
    }

    public String getMessageType() {
        return messageType;
    }

    /**
     * Create a builder for SendBatchRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SendBatchRequest.
     */
    public static class Builder {
        private List<BatchMessageItem> messages = new ArrayList<>();
        private String from;
        private String messageType;

        public Builder addMessage(String to, String text) {
            this.messages.add(new BatchMessageItem(to, text));
            return this;
        }

        public Builder addMessage(BatchMessageItem item) {
            this.messages.add(item);
            return this;
        }

        public Builder messages(List<BatchMessageItem> messages) {
            this.messages = messages;
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

        public SendBatchRequest build() {
            return new SendBatchRequest(messages, from, messageType);
        }
    }
}
