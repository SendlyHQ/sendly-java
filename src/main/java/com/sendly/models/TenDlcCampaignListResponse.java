package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code tenDlc().listCampaigns()}.
 */
public class TenDlcCampaignListResponse {
    private List<TenDlcCampaign> data;

    public TenDlcCampaignListResponse() {
        this.data = new ArrayList<>();
    }

    public TenDlcCampaignListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            json.get("data").getAsJsonArray().forEach(e ->
                data.add(new TenDlcCampaign(e.getAsJsonObject()))
            );
        }
    }

    /** Your messaging campaigns. */
    public List<TenDlcCampaign> getData() { return data; }
}
