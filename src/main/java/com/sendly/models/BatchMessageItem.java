package com.sendly.models;

import java.util.Map;

/**
 * Represents a single message in a batch send request.
 */
public class BatchMessageItem {
    private final String to;
    private final String text;
    private final Map<String, Object> metadata;

    /**
     * Create a new batch message item.
     *
     * @param to   Recipient phone number in E.164 format
     * @param text Message content
     */
    public BatchMessageItem(String to, String text) {
        this(to, text, null);
    }

    /**
     * Create a new batch message item with metadata.
     *
     * @param to       Recipient phone number in E.164 format
     * @param text     Message content
     * @param metadata Per-message metadata (max 4KB, merged with batch metadata)
     */
    public BatchMessageItem(String to, String text, Map<String, Object> metadata) {
        this.to = to;
        this.text = text;
        this.metadata = metadata;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
