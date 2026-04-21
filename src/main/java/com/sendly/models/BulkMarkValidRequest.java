package com.sendly.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request for {@code contacts.bulkMarkValid}. Pass either {@code ids} (up to
 * 10,000 per call) OR {@code listId} — not both. Foreign ids silently no-op
 * via the per-organization filter.
 */
public class BulkMarkValidRequest {
    private List<String> ids;
    @SerializedName("listId")
    private String listId;

    public BulkMarkValidRequest() {}

    public BulkMarkValidRequest(List<String> ids, String listId) {
        this.ids = ids;
        this.listId = listId;
    }

    /** Clear this explicit set of contact ids. Pass {@code null} when scoping by listId. */
    public static BulkMarkValidRequest ofIds(List<String> ids) {
        return new BulkMarkValidRequest(ids, null);
    }

    /** Clear every flagged member of this list. Pass {@code null} when scoping by ids. */
    public static BulkMarkValidRequest ofListId(String listId) {
        return new BulkMarkValidRequest(null, listId);
    }

    public List<String> getIds() { return ids; }
    public void setIds(List<String> ids) { this.ids = ids; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }
}
