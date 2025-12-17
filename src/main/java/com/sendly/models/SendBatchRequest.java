package com.sendly.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object for sending a batch of SMS messages.
 */
public class SendBatchRequest {
    private final List<BatchMessageItem> messages;
    private final String from;

    /**
     * Create a new send batch request.
     *
     * @param messages List of messages to send
     */
    public SendBatchRequest(List<BatchMessageItem> messages) {
        this(messages, null);
    }

    /**
     * Create a new send batch request with sender ID.
     *
     * @param messages List of messages to send
     * @param from     Optional sender ID (applies to all messages)
     */
    public SendBatchRequest(List<BatchMessageItem> messages, String from) {
        this.messages = messages;
        this.from = from;
    }

    public List<BatchMessageItem> getMessages() {
        return messages;
    }

    public String getFrom() {
        return from;
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

        public SendBatchRequest build() {
            return new SendBatchRequest(messages, from);
        }
    }
}
