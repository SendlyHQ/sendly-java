package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code rcs().agents().list()}.
 */
public class RcsAgentsResponse {
    private List<RcsAgent> agents;

    public RcsAgentsResponse() {
        this.agents = new ArrayList<>();
    }

    public RcsAgentsResponse(JsonObject json) {
        this.agents = new ArrayList<>();
        if (json.has("agents") && json.get("agents").isJsonArray()) {
            json.get("agents").getAsJsonArray().forEach(e ->
                agents.add(new RcsAgent(e.getAsJsonObject()))
            );
        }
    }

    /** The RCS agents on your workspace, newest first. */
    public List<RcsAgent> getAgents() { return agents; }
}
