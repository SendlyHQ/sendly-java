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
    private final AccountOrganization organization;
    private final AccountVerification verification;

    public Account(JsonObject json) {
        this(json, null, null);
    }

    public Account(JsonObject json, JsonObject organization) {
        this(json, organization, null);
    }

    /**
     * @param json the account's user fields
     * @param organization the workspace the key belongs to, or null for a key
     *     that belongs to no workspace
     * @param verification the business verification, or null when there is
     *     none
     */
    public Account(JsonObject json, JsonObject organization, JsonObject verification) {
        this.id = getStringOrNull(json, "id");
        this.email = getStringOrNull(json, "email");
        this.name = getStringOrNull(json, "name");
        this.createdAt = getStringOrNull(json, "created_at", "createdAt");
        this.organization = organization != null ? new AccountOrganization(organization) : null;
        this.verification = verification != null ? new AccountVerification(verification) : null;
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
    /**
     * The workspace the API key belongs to, or null for a key that belongs to
     * no workspace. Its ID is the {@code organization_id} webhook payloads
     * carry.
     */
    public AccountOrganization getOrganization() { return organization; }
    /**
     * The business verification, which decides where the account can send,
     * or null when there is none.
     */
    public AccountVerification getVerification() { return verification; }

    @Override
    public String toString() {
        return "Account{id='" + id + "', email='" + email + "'}";
    }
}
