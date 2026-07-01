package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code tenDlc().listAssignments()}.
 */
public class TenDlcAssignmentListResponse {
    private List<TenDlcAssignment> data;

    public TenDlcAssignmentListResponse() {
        this.data = new ArrayList<>();
    }

    public TenDlcAssignmentListResponse(JsonObject json) {
        this.data = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            json.get("data").getAsJsonArray().forEach(e ->
                data.add(new TenDlcAssignment(e.getAsJsonObject()))
            );
        }
    }

    /** Your number-to-campaign assignments. */
    public List<TenDlcAssignment> getData() { return data; }
}
