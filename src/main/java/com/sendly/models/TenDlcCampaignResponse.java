package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code tenDlc().createCampaign()} and {@code tenDlc().getCampaign()}.
 */
public class TenDlcCampaignResponse {
    private TenDlcCampaign data;

    public TenDlcCampaignResponse() {}

    public TenDlcCampaignResponse(JsonObject json) {
        if (json.has("data") && json.get("data").isJsonObject()) {
            this.data = new TenDlcCampaign(json.getAsJsonObject("data"));
        }
    }

    /** The campaign with its current review status. */
    public TenDlcCampaign getData() { return data; }
}
