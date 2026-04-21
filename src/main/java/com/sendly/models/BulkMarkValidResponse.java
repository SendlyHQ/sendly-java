package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code contacts.bulkMarkValid} — reports how many contacts
 * actually had their invalid flag cleared. Already-clean contacts and foreign
 * ids don't count.
 */
public class BulkMarkValidResponse {
    private final int cleared;

    public BulkMarkValidResponse(JsonObject json) {
        this.cleared = json.has("cleared") && !json.get("cleared").isJsonNull()
                ? json.get("cleared").getAsInt()
                : 0;
    }

    public int getCleared() {
        return cleared;
    }
}
