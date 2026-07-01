package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code tenDlc().listBrands()}.
 */
public class TenDlcBrandListResponse {
    private List<TenDlcBrand> data;

    public TenDlcBrandListResponse() {
        this.data = new ArrayList<>();
    }

    public TenDlcBrandListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            json.get("data").getAsJsonArray().forEach(e ->
                data.add(new TenDlcBrand(e.getAsJsonObject()))
            );
        }
    }

    /** The brands registered for carrier review. */
    public List<TenDlcBrand> getData() { return data; }
}
