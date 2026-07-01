package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code tenDlc().createBrand()} and {@code tenDlc().getBrand()}.
 */
public class TenDlcBrandResponse {
    private TenDlcBrand data;

    public TenDlcBrandResponse() {}

    public TenDlcBrandResponse(JsonObject json) {
        if (json.has("data") && json.get("data").isJsonObject()) {
            this.data = new TenDlcBrand(json.getAsJsonObject("data"));
        }
    }

    /** The brand with its current review status. */
    public TenDlcBrand getData() { return data; }
}
