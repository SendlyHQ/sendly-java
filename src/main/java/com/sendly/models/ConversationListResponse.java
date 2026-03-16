package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a paginated list of conversations.
 */
public class ConversationListResponse implements Iterable<Conversation> {
    private final List<Conversation> conversations;
    private final int total;
    private final int limit;
    private final int offset;
    private final boolean hasMore;

    /**
     * Create a ConversationListResponse from a JSON response.
     */
    public ConversationListResponse(JsonObject json) {
        this.conversations = new ArrayList<>();

        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray data = json.getAsJsonArray("data");
            for (int i = 0; i < data.size(); i++) {
                conversations.add(new Conversation(data.get(i).getAsJsonObject()));
            }
        }

        JsonObject pagination = json.has("pagination") ?
                json.getAsJsonObject("pagination") : new JsonObject();

        this.total = pagination.has("total") ? pagination.get("total").getAsInt() : conversations.size();
        this.limit = pagination.has("limit") ? pagination.get("limit").getAsInt() : 20;
        this.offset = pagination.has("offset") ? pagination.get("offset").getAsInt() : 0;
        this.hasMore = pagination.has("hasMore") && pagination.get("hasMore").getAsBoolean();
    }

    /**
     * Get all conversations.
     */
    public List<Conversation> getData() {
        return conversations;
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
     * Get the number of conversations in this page.
     */
    public int size() {
        return conversations.size();
    }

    /**
     * Check if empty.
     */
    public boolean isEmpty() {
        return conversations.isEmpty();
    }

    /**
     * Get first conversation.
     */
    public Conversation first() {
        return conversations.isEmpty() ? null : conversations.get(0);
    }

    /**
     * Get last conversation.
     */
    public Conversation last() {
        return conversations.isEmpty() ? null : conversations.get(conversations.size() - 1);
    }

    /**
     * Get conversation at index.
     */
    public Conversation get(int index) {
        return conversations.get(index);
    }

    @Override
    public Iterator<Conversation> iterator() {
        return conversations.iterator();
    }
}
