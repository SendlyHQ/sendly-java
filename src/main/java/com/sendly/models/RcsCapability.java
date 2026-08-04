package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code rcs().capability(to)} — whether a recipient's device
 * can receive RCS from your agent.
 */
public class RcsCapability {
    private String to;
    private String agentId;
    private boolean capable;
    private List<String> features;

    public RcsCapability() {
        this.features = new ArrayList<>();
    }

    public RcsCapability(JsonObject json) {
        this.features = new ArrayList<>();
        if (json.has("to") && !json.get("to").isJsonNull()) {
            this.to = json.get("to").getAsString();
        }
        if (json.has("agentId") && !json.get("agentId").isJsonNull()) {
            this.agentId = json.get("agentId").getAsString();
        }
        if (json.has("capable") && !json.get("capable").isJsonNull()) {
            this.capable = json.get("capable").getAsBoolean();
        }
        if (json.has("features") && json.get("features").isJsonArray()) {
            json.get("features").getAsJsonArray().forEach(e -> {
                if (!e.isJsonNull()) {
                    features.add(e.getAsString());
                }
            });
        }
    }

    /** The checked number, in E.164 format. */
    public String getTo() { return to; }

    /** The agent the check ran against. */
    public String getAgentId() { return agentId; }

    /**
     * True when the recipient can receive RCS from this agent. When false, a
     * text send would take the SMS fallback and a card send would 422.
     */
    public boolean isCapable() { return capable; }

    /** RCS features the recipient's device reports; empty when not capable. */
    public List<String> getFeatures() { return features; }
}
