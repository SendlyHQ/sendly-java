package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A compliance problem with a short code application, by field.
 */
public class ShortCodeIssue {
    private final String path;
    private final String message;

    public ShortCodeIssue(JsonObject json) {
        this.path = ShortCodeJson.string(json, "path");
        this.message = ShortCodeJson.string(json, "message");
    }

    /** The field the issue is about, such as {@code helpResponse}. */
    public String getPath() { return path; }

    /** What to fix. */
    public String getMessage() { return message; }
}
