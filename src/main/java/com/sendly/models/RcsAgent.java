package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * An RCS agent — the brand identity your RCS messages are sent as.
 */
public class RcsAgent {
    private String id;
    private String name;
    private String status;
    private String useCase;
    private boolean sendable;
    private String createdAt;

    public RcsAgent() {}

    public RcsAgent(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("name") && !json.get("name").isJsonNull()) {
            this.name = json.get("name").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("useCase") && !json.get("useCase").isJsonNull()) {
            this.useCase = json.get("useCase").getAsString();
        }
        if (json.has("sendable") && !json.get("sendable").isJsonNull()) {
            this.sendable = json.get("sendable").getAsBoolean();
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
    }

    /** Unique agent identifier — pass as {@code agentId} on sends. */
    public String getId() { return id; }

    /** The display name recipients see. */
    public String getName() { return name; }

    /**
     * Agent status. {@code testing} reaches invited test numbers only;
     * {@code approved} reaches everyone.
     */
    public String getStatus() { return status; }

    /** The agent's registered use case, or null when not set. */
    public String getUseCase() { return useCase; }

    /** True when the agent can send right now. */
    public boolean isSendable() { return sendable; }

    /** ISO 8601 timestamp when the agent was registered. */
    public String getCreatedAt() { return createdAt; }
}
