package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a paginated list of messages.
 */
public class MessageList implements Iterable<Message> {
    private final List<Message> messages;
    private final int total;
    private final int limit;
    private final int offset;
    private final boolean hasMore;

    /**
     * Create a MessageList from a JSON response.
     */
    public MessageList(JsonObject json) {
        this.messages = new ArrayList<>();

        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray data = json.getAsJsonArray("data");
            for (int i = 0; i < data.size(); i++) {
                messages.add(new Message(data.get(i).getAsJsonObject()));
            }
        }

        JsonObject pagination = json.has("pagination") ?
                json.getAsJsonObject("pagination") : new JsonObject();

        this.total = pagination.has("total") ? pagination.get("total").getAsInt() : messages.size();
        this.limit = pagination.has("limit") ? pagination.get("limit").getAsInt() : 20;
        this.offset = pagination.has("offset") ? pagination.get("offset").getAsInt() : 0;
        this.hasMore = pagination.has("has_more") && pagination.get("has_more").getAsBoolean();
    }

    /**
     * Get all messages.
     */
    public List<Message> getData() {
        return messages;
    }

    /**
     * Get total count.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Get limit.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Get offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Check if there are more pages.
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Get the number of messages in this page.
     */
    public int size() {
        return messages.size();
    }

    /**
     * Check if empty.
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Get first message.
     */
    public Message first() {
        return messages.isEmpty() ? null : messages.get(0);
    }

    /**
     * Get last message.
     */
    public Message last() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    /**
     * Get message at index.
     */
    public Message get(int index) {
        return messages.get(index);
    }

    @Override
    public Iterator<Message> iterator() {
        return messages.iterator();
    }
}
