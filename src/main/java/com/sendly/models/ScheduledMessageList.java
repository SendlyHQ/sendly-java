package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of scheduled messages with pagination metadata.
 * <p>
 * {@code GET /messages/scheduled} answers with the page and its
 * {@code count} only, so the limit and offset are the request's, as the API
 * applied them, and {@link #hasMore()} is worked out from whether the page
 * is full.
 * </p>
 */
public class ScheduledMessageList implements Iterable<ScheduledMessage> {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final List<ScheduledMessage> messages;
    private final int total;
    private final int limit;
    private final int offset;
    private final boolean hasMore;

    public ScheduledMessageList(JsonObject json) {
        this(json, null);
    }

    /**
     * Create a list from a {@code GET /messages/scheduled} response and the
     * request that fetched it.
     *
     * @param json    The response body
     * @param request The request's options, or null for the API's defaults
     */
    public ScheduledMessageList(JsonObject json, ListScheduledMessagesRequest request) {
        this.messages = new ArrayList<>();

        JsonArray data = json.has("data") ? json.getAsJsonArray("data") : new JsonArray();
        for (JsonElement element : data) {
            messages.add(new ScheduledMessage(element.getAsJsonObject()));
        }

        Integer requestedLimit = request != null ? request.getLimit() : null;
        Integer requestedOffset = request != null ? request.getOffset() : null;

        this.total = json.has("total") ? json.get("total").getAsInt()
                : json.has("count") ? json.get("count").getAsInt() : messages.size();
        this.limit = json.has("limit") ? json.get("limit").getAsInt() : limitOf(requestedLimit);
        this.offset = json.has("offset") ? json.get("offset").getAsInt()
                : requestedOffset != null ? Math.max(requestedOffset, 0) : 0;
        this.hasMore = json.has("has_more") ? json.get("has_more").getAsBoolean()
                : json.has("total") ? offset + messages.size() < total : messages.size() >= limit;
    }

    private static int limitOf(Integer requested) {
        if (requested == null || requested == 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    /**
     * Get all messages in this page.
     */
    public List<ScheduledMessage> getData() {
        return messages;
    }

    /**
     * Get the number of scheduled messages in this page, the API's
     * {@code count}. The API sends no total across pages.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Get the limit the API applied: the request's limit, at most 100, or 50
     * when the request set none.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Get the offset of this page: the request's offset, or 0.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Check if there may be more messages to fetch: true when this page is
     * full. The API sends no total, so when the last page is exactly full
     * the next page comes back empty.
     */
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public Iterator<ScheduledMessage> iterator() {
        return messages.iterator();
    }
}
