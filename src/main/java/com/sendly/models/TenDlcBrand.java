package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A business identity registered for carrier review.
 * <p>
 * Status is one of {@code pending}, {@code verified}, or {@code failed}
 * (see {@link #getFailureReasons()}).
 */
public class TenDlcBrand {
    private String id;
    private String legalName;
    private String dba;
    private String entityType;
    private String ein;
    private String vertical;
    private String website;
    private String status;
    private String identityStatus;
    private List<String> failureReasons;
    private String createdAt;
    private String updatedAt;

    public TenDlcBrand() {}

    public TenDlcBrand(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("legalName") && !json.get("legalName").isJsonNull()) {
            this.legalName = json.get("legalName").getAsString();
        }
        if (json.has("dba") && !json.get("dba").isJsonNull()) {
            this.dba = json.get("dba").getAsString();
        }
        if (json.has("entityType") && !json.get("entityType").isJsonNull()) {
            this.entityType = json.get("entityType").getAsString();
        }
        if (json.has("ein") && !json.get("ein").isJsonNull()) {
            this.ein = json.get("ein").getAsString();
        }
        if (json.has("vertical") && !json.get("vertical").isJsonNull()) {
            this.vertical = json.get("vertical").getAsString();
        }
        if (json.has("website") && !json.get("website").isJsonNull()) {
            this.website = json.get("website").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("identityStatus") && !json.get("identityStatus").isJsonNull()) {
            this.identityStatus = json.get("identityStatus").getAsString();
        }
        if (json.has("failureReasons") && json.get("failureReasons").isJsonArray()) {
            this.failureReasons = new ArrayList<>();
            json.get("failureReasons").getAsJsonArray().forEach(e ->
                failureReasons.add(e.getAsString())
            );
        }
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            this.createdAt = json.get("createdAt").getAsString();
        }
        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            this.updatedAt = json.get("updatedAt").getAsString();
        }
    }

    /** Unique brand identifier. */
    public String getId() { return id; }

    /** Legal business name. */
    public String getLegalName() { return legalName; }

    /** "Doing business as" name, if different from the legal name. */
    public String getDba() { return dba; }

    /** Business entity type (e.g. "PRIVATE_PROFIT", "SOLE_PROPRIETOR"). */
    public String getEntityType() { return entityType; }

    /** Business registration number (e.g. EIN). */
    public String getEin() { return ein; }

    /** Industry vertical. */
    public String getVertical() { return vertical; }

    /** Business website URL. */
    public String getWebsite() { return website; }

    /** Carrier-review status: "pending", "verified", or "failed". */
    public String getStatus() { return status; }

    /** Identity-verification detail from the carrier review, when available. */
    public String getIdentityStatus() { return identityStatus; }

    /** Why the review failed, when status is "failed"; null otherwise. */
    public List<String> getFailureReasons() { return failureReasons; }

    /** When the brand was created (ISO-8601). */
    public String getCreatedAt() { return createdAt; }

    /** When the brand was last updated (ISO-8601). */
    public String getUpdatedAt() { return updatedAt; }
}
