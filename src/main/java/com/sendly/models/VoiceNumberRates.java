package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Credits charged per started minute on a number, from
 * {@code VoiceNumber.getRatePerMinute()}. One credit is one US cent.
 */
public class VoiceNumberRates {
    private final int inbound;
    private final int outbound;
    private final int agent;

    /**
     * Create a VoiceNumberRates from a JSON object.
     */
    public VoiceNumberRates(JsonObject json) {
        this.inbound = getIntOrZero(json, "inbound");
        this.outbound = getIntOrZero(json, "outbound");
        this.agent = getIntOrZero(json, "agent");
    }

    private static int getIntOrZero(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }

    /** An inbound call the team answers in the dashboard. */
    public int getInbound() {
        return inbound;
    }

    /** An outbound call; an agent on the call adds its own per-minute charge. */
    public int getOutbound() {
        return outbound;
    }

    /** An inbound call an AI agent answers, agent included. */
    public int getAgent() {
        return agent;
    }

    @Override
    public String toString() {
        return "VoiceNumberRates{" +
                "inbound=" + inbound +
                ", outbound=" + outbound +
                ", agent=" + agent +
                '}';
    }
}
