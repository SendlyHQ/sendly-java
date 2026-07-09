package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from listing short links.
 */
public class ShortLinkListResponse {
    private final List<ShortLinkListItem> links;
    private final int total;

    /**
     * Create a ShortLinkListResponse from a JSON object.
     */
    public ShortLinkListResponse(JsonObject json) {
        this.links = new ArrayList<>();
        if (json.has("links") && json.get("links").isJsonArray()) {
            JsonArray linksArray = json.getAsJsonArray("links");
            for (int i = 0; i < linksArray.size(); i++) {
                links.add(new ShortLinkListItem(linksArray.get(i).getAsJsonObject()));
            }
        }
        this.total = json.has("total") && !json.get("total").isJsonNull()
                ? json.get("total").getAsInt() : links.size();
    }

    /**
     * The workspace's short links, newest first.
     */
    public List<ShortLinkListItem> getLinks() {
        return links;
    }

    /**
     * Total number of short links in the workspace.
     */
    public int getTotal() {
        return total;
    }
}
