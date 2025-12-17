package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of scheduled messages with pagination metadata.
 */
public class ScheduledMessageList implements Iterable<ScheduledMessage> {
    private final List<ScheduledMessage> messages;
    private final int total;
    private final int limit;
    private final int offset;
    private final boolean hasMore;

    public ScheduledMessageList(JsonObject json) {
        this.messages = new ArrayList<>();

        JsonArray data = json.has("data") ? json.getAsJsonArray("data") : new JsonArray();
        for (JsonElement element : data) {
            messages.add(new ScheduledMessage(element.getAsJsonObject()));
        }

        this.total = json.has("total") ? json.get("total").getAsInt() : messages.size();
        this.limit = json.has("limit") ? json.get("limit").getAsInt() : 20;
        this.offset = json.has("offset") ? json.get("offset").getAsInt() : 0;
        this.hasMore = json.has("has_more") ? json.get("has_more").getAsBoolean() : (offset + messages.size() < total);
    }

    /**
     * Get all messages in this page.
     */
    public List<ScheduledMessage> getData() {
        return messages;
    }

    /**
     * Get the total number of scheduled messages.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Get the limit used for this request.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Get the offset used for this request.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Check if there are more messages to fetch.
     */
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public Iterator<ScheduledMessage> iterator() {
        return messages.iterator();
    }
}
