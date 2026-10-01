package com.sendly.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A sender's conversational components: the ice breakers shown when someone
 * opens a chat with the business for the first time, and the commands shown
 * when the customer types "/".
 */
public class WhatsAppConversationalComponents {
    private String phoneNumber;
    private final List<String> iceBreakers = new ArrayList<>();
    private final List<WhatsAppCommand> commands = new ArrayList<>();

    public WhatsAppConversationalComponents() {}

    public WhatsAppConversationalComponents(JsonObject json) {
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("iceBreakers") && json.get("iceBreakers").isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("iceBreakers")) {
                if (!e.isJsonNull()) {
                    iceBreakers.add(e.getAsString());
                }
            }
        }
        if (json.has("commands") && json.get("commands").isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("commands")) {
                if (e.isJsonObject()) {
                    commands.add(new WhatsAppCommand(e.getAsJsonObject()));
                }
            }
        }
    }

    /** The sender, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /** The ice breakers, in order; empty when none are set. */
    public List<String> getIceBreakers() { return iceBreakers; }

    /** The commands, in order; empty when none are set. */
    public List<WhatsAppCommand> getCommands() { return commands; }
}
