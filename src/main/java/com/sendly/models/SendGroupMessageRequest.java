package com.sendly.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request object for sending a group MMS to 2-8 recipients (US/Canada only).
 */
public class SendGroupMessageRequest {
    private final List<String> to;
    private final String text;
    private final String messageType;

    @SerializedName("mediaUrls")
    private final List<String> mediaUrls;

    @SerializedName("from")
    private final String from;

    /**
     * Create a new group message request.
     *
     * @param to 2-8 recipient phone numbers in E.164 format (US/CA only)
     * @param text Message content (required unless mediaUrls is provided)
     */
    public SendGroupMessageRequest(List<String> to, String text) {
        this(to, text, null, null, null);
    }

    /**
     * Create a new group message request with all options.
     *
     * @param to          2-8 recipient phone numbers in E.164 format (US/CA only)
     * @param text        Message content (required unless mediaUrls is provided)
     * @param messageType Message type: "transactional" (default) or "marketing" (subject to quiet hours)
     * @param mediaUrls   Media URLs for the group MMS (required unless text is provided)
     * @param from        Sender phone number (E.164) or alphanumeric sender ID; defaults to account's number when omitted
     */
    public SendGroupMessageRequest(List<String> to, String text, String messageType, List<String> mediaUrls, String from) {
        this.to = to;
        this.text = text;
        this.messageType = messageType;
        this.mediaUrls = mediaUrls;
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public String getMessageType() {
        return messageType;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getFrom() {
        return from;
    }

    /**
     * Create a builder for SendGroupMessageRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SendGroupMessageRequest.
     */
    public static class Builder {
        private List<String> to;
        private String text;
        private String messageType;
        private List<String> mediaUrls;
        private String from;

        public Builder to(List<String> to) {
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
         * @param messageType "transactional" (default) or "marketing" (subject to quiet hours)
         */
        public Builder messageType(String messageType) {
            this.messageType = messageType;
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

        public SendGroupMessageRequest build() {
            return new SendGroupMessageRequest(to, text, messageType, mediaUrls, from);
        }
    }
}
