package com.sendly.models;

/**
 * Represents a single message in a batch send request.
 */
public class BatchMessageItem {
    private final String to;
    private final String text;

    /**
     * Create a new batch message item.
     *
     * @param to   Recipient phone number in E.164 format
     * @param text Message content
     */
    public BatchMessageItem(String to, String text) {
        this.to = to;
        this.text = text;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }
}
