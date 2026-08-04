package com.sendly.models;

/**
 * Request body for {@code whatsapp().senders().updateProfile()}. Supply only
 * the fields to change; omitted fields keep their current value. At least one
 * field is required.
 */
public class UpdateWhatsAppSenderProfileRequest {
    private final String displayName;
    private final String about;
    private final String description;
    private final String category;
    private final String email;
    private final String website;
    private final String address;

    private UpdateWhatsAppSenderProfileRequest(Builder builder) {
        this.displayName = builder.displayName;
        this.about = builder.about;
        this.description = builder.description;
        this.category = builder.category;
        this.email = builder.email;
        this.website = builder.website;
        this.address = builder.address;
    }

    public String getDisplayName() { return displayName; }
    public String getAbout() { return about; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getEmail() { return email; }
    public String getWebsite() { return website; }
    public String getAddress() { return address; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String displayName;
        private String about;
        private String description;
        private String category;
        private String email;
        private String website;
        private String address;

        /** Replacement display name. */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /** Replacement about line (max 139 characters). */
        public Builder about(String about) {
            this.about = about;
            return this;
        }

        /** Replacement business description (max 512 characters). */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /** Replacement business category. */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /** Replacement contact email. */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /** Replacement website URL. */
        public Builder website(String website) {
            this.website = website;
            return this;
        }

        /** Replacement street address. */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public UpdateWhatsAppSenderProfileRequest build() {
            return new UpdateWhatsAppSenderProfileRequest(this);
        }
    }
}
