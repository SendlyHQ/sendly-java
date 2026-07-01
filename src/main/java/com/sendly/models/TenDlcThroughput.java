package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Throughput detail for a campaign or use-case qualification.
 */
public class TenDlcThroughput {
    private String tier;
    private Integer carriersReady;

    public TenDlcThroughput() {}

    public TenDlcThroughput(JsonObject json) {
        if (json.has("tier") && !json.get("tier").isJsonNull()) {
            this.tier = json.get("tier").getAsString();
        }
        if (json.has("carriersReady") && !json.get("carriersReady").isJsonNull()) {
            this.carriersReady = json.get("carriersReady").getAsInt();
        }
    }

    /** Throughput tier granted by the carrier network: "High volume", "Standard", or "Low volume". */
    public String getTier() { return tier; }

    /** How many carriers have accepted the campaign so far. */
    public Integer getCarriersReady() { return carriersReady; }
}
