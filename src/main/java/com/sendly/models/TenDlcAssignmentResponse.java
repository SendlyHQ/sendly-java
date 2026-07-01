package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code tenDlc().assignNumber()}.
 */
public class TenDlcAssignmentResponse {
    private TenDlcAssignment data;

    public TenDlcAssignmentResponse() {}

    public TenDlcAssignmentResponse(JsonObject json) {
        if (json.has("data") && json.get("data").isJsonObject()) {
            this.data = new TenDlcAssignment(json.getAsJsonObject("data"));
        }
    }

    /** The assignment; the number can send once its status is "Active". */
    public TenDlcAssignment getData() { return data; }
}
