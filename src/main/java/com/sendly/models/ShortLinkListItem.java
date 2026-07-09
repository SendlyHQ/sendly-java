package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A short link with click analytics, as returned by {@code LinksResource.list}.
 */
public class ShortLinkListItem {
    private final String code;
    private final String shortUrl;
    private final String destinationUrl;
    private final String brandSlug;
    private final int clickCount;
    private final boolean disabled;
    private final String lastCountry;
    private final Instant lastClickedAt;
    private final Instant createdAt;
    private final List<Integer> spark;

    /**
     * Create a ShortLinkListItem from a JSON object.
     */
    public ShortLinkListItem(JsonObject json) {
        this.code = getStringOrNull(json, "code");
        this.shortUrl = getStringOrNull(json, "shortUrl");
        this.destinationUrl = getStringOrNull(json, "destinationUrl");
        this.brandSlug = getStringOrNull(json, "brandSlug");
        this.clickCount = json.has("clickCount") && !json.get("clickCount").isJsonNull()
                ? json.get("clickCount").getAsInt() : 0;
        this.disabled = json.has("disabled") && !json.get("disabled").isJsonNull()
                && json.get("disabled").getAsBoolean();
        this.lastCountry = getStringOrNull(json, "lastCountry");
        this.lastClickedAt = parseInstant(getStringOrNull(json, "lastClickedAt"));
        this.createdAt = parseInstant(getStringOrNull(json, "createdAt"));

        this.spark = new ArrayList<>();
        if (json.has("spark") && json.get("spark").isJsonArray()) {
            JsonArray sparkArray = json.getAsJsonArray("spark");
            for (int i = 0; i < sparkArray.size(); i++) {
                if (!sparkArray.get(i).isJsonNull()) {
                    spark.add(sparkArray.get(i).getAsInt());
                }
            }
        }
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Instant parseInstant(String value) {
        if (value == null) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Short code (the segment after the domain).
     */
    public String getCode() {
        return code;
    }

    /**
     * Full branded short URL.
     */
    public String getShortUrl() {
        return shortUrl;
    }

    /**
     * The destination the short link redirects to.
     */
    public String getDestinationUrl() {
        return destinationUrl;
    }

    /**
     * Workspace brand slug segment, or {@code null} when unbranded.
     */
    public String getBrandSlug() {
        return brandSlug;
    }

    /**
     * Total human clicks recorded (link-preview bots are excluded).
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * Whether the link is disabled (the redirect then returns 404).
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * ISO 3166-1 alpha-2 country of the most recent click, or {@code null}.
     */
    public String getLastCountry() {
        return lastCountry;
    }

    /**
     * When the link was last clicked, or {@code null}.
     */
    public Instant getLastClickedAt() {
        return lastClickedAt;
    }

    /**
     * When the link was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 14-day daily click histogram, oldest first (today last).
     */
    public List<Integer> getSpark() {
        return spark;
    }

    @Override
    public String toString() {
        return "ShortLinkListItem{" +
                "code='" + code + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", clickCount=" + clickCount +
                ", disabled=" + disabled +
                '}';
    }
}
