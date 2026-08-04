package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A WhatsApp sender's business profile — what recipients see when they open
 * the sender's contact card.
 */
public class WhatsAppSenderProfile {
    private String phoneNumber;
    private String displayName;
    private String profilePhotoUrl;
    private String category;
    private String about;
    private String description;
    private String email;
    private String website;
    private String address;

    public WhatsAppSenderProfile() {}

    public WhatsAppSenderProfile(JsonObject json) {
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("displayName") && !json.get("displayName").isJsonNull()) {
            this.displayName = json.get("displayName").getAsString();
        }
        if (json.has("profilePhotoUrl") && !json.get("profilePhotoUrl").isJsonNull()) {
            this.profilePhotoUrl = json.get("profilePhotoUrl").getAsString();
        }
        if (json.has("category") && !json.get("category").isJsonNull()) {
            this.category = json.get("category").getAsString();
        }
        if (json.has("about") && !json.get("about").isJsonNull()) {
            this.about = json.get("about").getAsString();
        }
        if (json.has("description") && !json.get("description").isJsonNull()) {
            this.description = json.get("description").getAsString();
        }
        if (json.has("email") && !json.get("email").isJsonNull()) {
            this.email = json.get("email").getAsString();
        }
        if (json.has("website") && !json.get("website").isJsonNull()) {
            this.website = json.get("website").getAsString();
        }
        if (json.has("address") && !json.get("address").isJsonNull()) {
            this.address = json.get("address").getAsString();
        }
    }

    /** The sender, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /** The name recipients see; null until set. */
    public String getDisplayName() { return displayName; }

    /** Profile photo URL, or null when none is set. */
    public String getProfilePhotoUrl() { return profilePhotoUrl; }

    /** Business category (e.g. "Retail"), or null when not set. */
    public String getCategory() { return category; }

    /** Short about line (max 139 characters), or null when not set. */
    public String getAbout() { return about; }

    /** Longer business description (max 512 characters), or null when not set. */
    public String getDescription() { return description; }

    /** Contact email, or null when not set. */
    public String getEmail() { return email; }

    /** Website URL, or null when not set. */
    public String getWebsite() { return website; }

    /** Street address, or null when not set. */
    public String getAddress() { return address; }
}
