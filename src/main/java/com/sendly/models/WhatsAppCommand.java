package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A WhatsApp command: shown to the customer when they type "/" in the chat.
 */
public class WhatsAppCommand {
    private final String command;
    private final String description;

    /**
     * @param command     Letters, digits or underscores, 1-32 characters; a
     *                    leading "/" is stripped by the API
     * @param description What the command does, 1-256 characters
     */
    public WhatsAppCommand(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public WhatsAppCommand(JsonObject json) {
        this.command = json.has("command") && !json.get("command").isJsonNull()
                ? json.get("command").getAsString() : null;
        this.description = json.has("description") && !json.get("description").isJsonNull()
                ? json.get("description").getAsString() : null;
    }

    /** The command, without its leading "/". */
    public String getCommand() { return command; }

    /** What the command does. */
    public String getDescription() { return description; }
}
