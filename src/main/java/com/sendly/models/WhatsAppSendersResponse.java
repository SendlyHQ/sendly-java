package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code whatsapp().senders().list()}.
 */
public class WhatsAppSendersResponse {
    private List<WhatsAppSender> senders;

    public WhatsAppSendersResponse() {
        this.senders = new ArrayList<>();
    }

    public WhatsAppSendersResponse(JsonObject json) {
        this.senders = new ArrayList<>();
        if (json.has("senders") && json.get("senders").isJsonArray()) {
            json.get("senders").getAsJsonArray().forEach(e ->
                senders.add(new WhatsAppSender(e.getAsJsonObject()))
            );
        }
    }

    /** The numbers connected (or connecting) to WhatsApp, newest first. */
    public List<WhatsAppSender> getSenders() { return senders; }
}
