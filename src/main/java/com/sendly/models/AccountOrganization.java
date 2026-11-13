package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * The workspace an API key belongs to.
 */
public class AccountOrganization {
    private final String id;
    private final String name;
    private final boolean personal;

    public AccountOrganization(JsonObject json) {
        this.id = json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString() : null;
        this.name = json.has("name") && !json.get("name").isJsonNull() ? json.get("name").getAsString() : null;
        this.personal = json.has("isPersonal") && !json.get("isPersonal").isJsonNull()
            && json.get("isPersonal").getAsBoolean();
    }

    /** The workspace's ID, the {@code organization_id} webhook payloads carry. */
    public String getId() { return id; }
    /** The workspace's name. */
    public String getName() { return name; }
    /** Whether this is the user's personal workspace. */
    public boolean isPersonal() { return personal; }

    @Override
    public String toString() {
        return "AccountOrganization{id='" + id + "', name='" + name + "', personal=" + personal + "}";
    }
}
