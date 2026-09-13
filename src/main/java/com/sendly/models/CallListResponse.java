package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One page of calls from {@code calls().list()}, newest first.
 */
public class CallListResponse {
    private final List<Call> data;
    private final int total;
    private final int limit;
    private final int offset;
    private final boolean hasMore;

    /**
     * Create a CallListResponse from a JSON object.
     */
    public CallListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("data");
            for (int i = 0; i < array.size(); i++) {
                data.add(new Call(array.get(i).getAsJsonObject()));
            }
        }

        JsonObject pagination = json.has("pagination") && json.get("pagination").isJsonObject()
                ? json.getAsJsonObject("pagination") : new JsonObject();
        this.total = pagination.has("total") && !pagination.get("total").isJsonNull()
                ? pagination.get("total").getAsInt() : data.size();
        this.limit = pagination.has("limit") && !pagination.get("limit").isJsonNull()
                ? pagination.get("limit").getAsInt() : 50;
        this.offset = pagination.has("offset") && !pagination.get("offset").isJsonNull()
                ? pagination.get("offset").getAsInt() : 0;
        this.hasMore = pagination.has("hasMore") && !pagination.get("hasMore").isJsonNull()
                && pagination.get("hasMore").getAsBoolean();
    }

    /** The calls on this page, most recently started first. */
    public List<Call> getData() {
        return data;
    }

    /** Calls matching the filters across every page. */
    public int getTotal() {
        return total;
    }

    /** Page size that was applied. */
    public int getLimit() {
        return limit;
    }

    /** Number of calls skipped before this page. */
    public int getOffset() {
        return offset;
    }

    /** Whether another page follows this one. */
    public boolean hasMore() {
        return hasMore;
    }
}
