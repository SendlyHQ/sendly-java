package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A newly minted branded short link.
 */
public class ShortLink {
    private final String code;
    private final String shortUrl;
    private final String destinationUrl;

    /**
     * Create a ShortLink from a JSON object.
     */
    public ShortLink(JsonObject json) {
        this.code = getStringOrNull(json, "code");
        this.shortUrl = getStringOrNull(json, "shortUrl");
        this.destinationUrl = getStringOrNull(json, "destinationUrl");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /**
     * Short code (the segment after the domain, e.g. "Ab3xY7").
     */
    public String getCode() {
        return code;
    }

    /**
     * Full branded short URL to share (e.g. "https://sendly.live/l/Ab3xY7").
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

    @Override
    public String toString() {
        return "ShortLink{" +
                "code='" + code + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", destinationUrl='" + destinationUrl + '\'' +
                '}';
    }
}
