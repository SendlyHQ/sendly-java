package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code tenDlc().qualify()}.
 */
public class TenDlcQualifyResponse {
    private TenDlcQualifyResult data;

    public TenDlcQualifyResponse() {}

    public TenDlcQualifyResponse(JsonObject json) {
        if (json.has("data") && json.get("data").isJsonObject()) {
            this.data = new TenDlcQualifyResult(json.getAsJsonObject("data"));
        }
    }

    /** The qualification result. */
    public TenDlcQualifyResult getData() { return data; }
}
