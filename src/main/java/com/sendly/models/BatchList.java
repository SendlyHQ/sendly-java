package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of message batches with pagination metadata.
 */
public class BatchList implements Iterable<BatchMessageResponse> {
    private final List<BatchMessageResponse> batches;
    private final int total;
    private final int limit;
    private final int offset;
    private final boolean hasMore;

    public BatchList(JsonObject json) {
        this.batches = new ArrayList<>();

        JsonArray data = json.has("data") ? json.getAsJsonArray("data") : new JsonArray();
        for (JsonElement element : data) {
            batches.add(new BatchMessageResponse(element.getAsJsonObject()));
        }

        this.total = json.has("total") ? json.get("total").getAsInt() : batches.size();
        this.limit = json.has("limit") ? json.get("limit").getAsInt() : 20;
        this.offset = json.has("offset") ? json.get("offset").getAsInt() : 0;
        this.hasMore = json.has("has_more") ? json.get("has_more").getAsBoolean() : (offset + batches.size() < total);
    }

    /**
     * Get all batches in this page.
     */
    public List<BatchMessageResponse> getData() {
        return batches;
    }

    /**
     * Get the total number of batches.
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
     * Check if there are more batches to fetch.
     */
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public Iterator<BatchMessageResponse> iterator() {
        return batches.iterator();
    }
}
