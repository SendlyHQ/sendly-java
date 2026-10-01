package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an API key.
 */
public class ApiKey {
    private final String id;
    private final String name;
    private final String type;
    private final String prefix;
    private final String lastFour;
    private final List<String> permissions;
    private final String createdAt;
    private final String lastUsedAt;
    private final String expiresAt;
    private final String revokedAt;
    private final boolean isRevoked;

    public ApiKey(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.name = getStringOrNull(json, "name");
        this.type = getStringOrNull(json, "type");
        this.prefix = getStringOrNull(json, "prefix");
        this.lastFour = getStringOrNull(json, "last_four", "lastFour");
        this.permissions = json.has("permissions") && json.get("permissions").isJsonArray()
            ? getStringList(json, "permissions") : getStringList(json, "scopes");
        this.createdAt = getStringOrNull(json, "created_at", "createdAt");
        this.lastUsedAt = getStringOrNull(json, "last_used_at", "lastUsedAt");
        this.expiresAt = getStringOrNull(json, "expires_at", "expiresAt");
        this.revokedAt = getStringOrNull(json, "revoked_at", "revokedAt");
        this.isRevoked = isRevokedOf(json);
    }

    private boolean isRevokedOf(JsonObject json) {
        for (String key : new String[] {"is_revoked", "isRevoked", "revoked"}) {
            if (json.has(key) && !json.get(key).isJsonNull()) return json.get(key).getAsBoolean();
        }
        for (String key : new String[] {"is_active", "isActive"}) {
            if (json.has(key) && !json.get(key).isJsonNull()) return !json.get(key).getAsBoolean();
        }
        return false;
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private String getStringOrNull(JsonObject json, String key1, String key2) {
        if (json.has(key1) && !json.get(key1).isJsonNull()) return json.get(key1).getAsString();
        if (json.has(key2) && !json.get(key2).isJsonNull()) return json.get(key2).getAsString();
        return null;
    }

    private List<String> getStringList(JsonObject json, String key) {
        List<String> list = new ArrayList<>();
        if (json.has(key) && json.get(key).isJsonArray()) {
            JsonArray arr = json.getAsJsonArray(key);
            for (int i = 0; i < arr.size(); i++) {
                if (!arr.get(i).isJsonNull()) {
                    list.add(arr.get(i).getAsString());
                }
            }
        }
        return list;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getPrefix() { return prefix; }
    public String getLastFour() { return lastFour; }
    public List<String> getPermissions() { return permissions; }
    public String getCreatedAt() { return createdAt; }
    public String getLastUsedAt() { return lastUsedAt; }
    public String getExpiresAt() { return expiresAt; }
    /**
     * When the key was revoked, or null while it is active. It is also null on
     * the keys {@link com.sendly.resources.AccountResource#listApiKeys()}
     * returns, revoked ones included, because the list does not send it;
     * {@code getApiKey(id)} and {@code revokeApiKey(id)} do.
     */
    public String getRevokedAt() { return revokedAt; }
    public boolean isRevoked() { return isRevoked; }

    @Override
    public String toString() {
        return "ApiKey{id='" + id + "', name='" + name + "', type='" + type + "'}";
    }
}
