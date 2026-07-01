package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Result of a use-case qualification pre-check.
 */
public class TenDlcQualifyResult {
    private String useCase;
    private boolean qualified;
    private String reason;
    private TenDlcThroughput throughput;

    public TenDlcQualifyResult() {}

    public TenDlcQualifyResult(JsonObject json) {
        if (json.has("useCase") && !json.get("useCase").isJsonNull()) {
            this.useCase = json.get("useCase").getAsString();
        }
        if (json.has("qualified") && !json.get("qualified").isJsonNull()) {
            this.qualified = json.get("qualified").getAsBoolean();
        }
        if (json.has("reason") && !json.get("reason").isJsonNull()) {
            this.reason = json.get("reason").getAsString();
        }
        if (json.has("throughput") && json.get("throughput").isJsonObject()) {
            this.throughput = new TenDlcThroughput(json.getAsJsonObject("throughput"));
        }
    }

    /** The use-case code that was checked (e.g. "MIXED", "MARKETING"). */
    public String getUseCase() { return useCase; }

    /** True if the use case qualifies for this brand. */
    public boolean isQualified() { return qualified; }

    /** Why the use case does not qualify, when not qualified; null otherwise. */
    public String getReason() { return reason; }

    /** Expected throughput, when the carrier network reports it; null otherwise. */
    public TenDlcThroughput getThroughput() { return throughput; }
}
