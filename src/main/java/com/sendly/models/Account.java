package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Represents account information.
 */
public class Account {
    private final String id;
    private final String email;
    private final String name;
    private final String createdAt;

    public Account(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.email = getStringOrNull(json, "email");
        this.name = getStringOrNull(json, "name");
        this.createdAt = getStringOrNull(json, "created_at", "createdAt");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private String getStringOrNull(JsonObject json, String key1, String key2) {
        if (json.has(key1) && !json.get(key1).isJsonNull()) return json.get(key1).getAsString();
        if (json.has(key2) && !json.get(key2).isJsonNull()) return json.get(key2).getAsString();
        return null;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    /** The account endpoint does not send a name, so this is null. */
    public String getName() { return name; }
    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Account{id='" + id + "', email='" + email + "'}";
    }
}
