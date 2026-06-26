package com.sendly.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Request object for sending an SMS message.
 */
public class SendMessageRequest {
    private final String to;
    private final String text;
    private final String messageType;
    private final Map<String, Object> metadata;

    @SerializedName("mediaUrls")
    private final List<String> mediaUrls;

    @SerializedName("from")
    private final String from;

    /**
     * Create a new send message request.
     *
     * @param to   Recipient phone number in E.164 format
     * @param text Message content
     */
    public SendMessageRequest(String to, String text) {
        this(to, text, null, null, null, null);
    }

    /**
     * Create a new send message request with message type.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     */
    public SendMessageRequest(String to, String text, String messageType) {
        this(to, text, messageType, null, null, null);
    }

    /**
     * Create a new send message request with message type and metadata.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     * @param metadata    Custom metadata to attach to the message (max 4KB)
     */
    public SendMessageRequest(String to, String text, String messageType, Map<String, Object> metadata) {
        this(to, text, messageType, metadata, null, null);
    }

    /**
     * Create a new send message request with all options.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     * @param metadata    Custom metadata to attach to the message (max 4KB)
     * @param mediaUrls   List of media URLs for MMS
     */
    public SendMessageRequest(String to, String text, String messageType, Map<String, Object> metadata, List<String> mediaUrls) {
        this(to, text, messageType, metadata, mediaUrls, null);
    }

    /**
     * Create a new send message request with all options including sender.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param messageType Message type: "marketing" (default, subject to quiet hours) or "transactional" (24/7)
     * @param metadata    Custom metadata to attach to the message (max 4KB)
     * @param mediaUrls   List of media URLs for MMS
     * @param from        Sender phone number (E.164) or alphanumeric sender ID; defaults to account's number when omitted
     */
    public SendMessageRequest(String to, String text, String messageType, Map<String, Object> metadata, List<String> mediaUrls, String from) {
        this.to = to;
        this.text = text;
        this.messageType = messageType;
        this.metadata = metadata;
        this.mediaUrls = mediaUrls;
        this.from = from;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getFrom() {
        return from;
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
        private Map<String, Object> metadata;
        private List<String> mediaUrls;
        private String from;

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

        /**
         * Set custom metadata.
         *
         * @param metadata Custom metadata to attach to the message (max 4KB)
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder mediaUrls(List<String> mediaUrls) {
            this.mediaUrls = mediaUrls;
            return this;
        }

        /**
         * Set the sender.
         *
         * @param from Sender phone number (E.164) or alphanumeric sender ID; defaults to account's number when omitted
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public SendMessageRequest build() {
            return new SendMessageRequest(to, text, messageType, metadata, mediaUrls, from);
        }
    }
}
