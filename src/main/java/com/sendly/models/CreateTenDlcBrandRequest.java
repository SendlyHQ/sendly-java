package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Request body for {@code tenDlc().createBrand()}.
 *
 * <pre>{@code
 * CreateTenDlcBrandRequest request = CreateTenDlcBrandRequest.builder()
 *     .legalName("Acme Holdings LLC")
 *     .ein("12-3456789")
 *     .website("https://acme.example")
 *     .email("ops@acme.example")
 *     .build();
 * }</pre>
 */
public class CreateTenDlcBrandRequest {
    private final String legalName;
    private final String dba;
    private final String ein;
    private final String entityType;
    private final String vertical;
    private final String website;
    private final String email;
    private final String phone;
    private final String mobilePhone;
    private final String street;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;
    private final String verificationId;

    private CreateTenDlcBrandRequest(Builder builder) {
        this.legalName = builder.legalName;
        this.dba = builder.dba;
        this.ein = builder.ein;
        this.entityType = builder.entityType;
        this.vertical = builder.vertical;
        this.website = builder.website;
        this.email = builder.email;
        this.phone = builder.phone;
        this.mobilePhone = builder.mobilePhone;
        this.street = builder.street;
        this.city = builder.city;
        this.state = builder.state;
        this.postalCode = builder.postalCode;
        this.country = builder.country;
        this.verificationId = builder.verificationId;
    }

    public String getLegalName() { return legalName; }
    public String getDba() { return dba; }
    public String getEin() { return ein; }
    public String getEntityType() { return entityType; }
    public String getVertical() { return vertical; }
    public String getWebsite() { return website; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMobilePhone() { return mobilePhone; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public String getVerificationId() { return verificationId; }

    /** Serialize to the JSON body the API expects (camelCase keys). */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (legalName != null) o.addProperty("legalName", legalName);
        if (dba != null) o.addProperty("dba", dba);
        if (ein != null) o.addProperty("ein", ein);
        if (entityType != null) o.addProperty("entityType", entityType);
        if (vertical != null) o.addProperty("vertical", vertical);
        if (website != null) o.addProperty("website", website);
        if (email != null) o.addProperty("email", email);
        if (phone != null) o.addProperty("phone", phone);
        if (mobilePhone != null) o.addProperty("mobilePhone", mobilePhone);
        if (street != null) o.addProperty("street", street);
        if (city != null) o.addProperty("city", city);
        if (state != null) o.addProperty("state", state);
        if (postalCode != null) o.addProperty("postalCode", postalCode);
        if (country != null) o.addProperty("country", country);
        if (verificationId != null) o.addProperty("verificationId", verificationId);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String legalName;
        private String dba;
        private String ein;
        private String entityType;
        private String vertical;
        private String website;
        private String email;
        private String phone;
        private String mobilePhone;
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String verificationId;

        /** Legal business name (required). */
        public Builder legalName(String legalName) {
            this.legalName = legalName;
            return this;
        }

        /** "Doing business as" name, if different from the legal name. */
        public Builder dba(String dba) {
            this.dba = dba;
            return this;
        }

        /** Business registration number (e.g. EIN). */
        public Builder ein(String ein) {
            this.ein = ein;
            return this;
        }

        /** Business entity type (e.g. "PRIVATE_PROFIT", "SOLE_PROPRIETOR"); defaults to "PRIVATE_PROFIT". */
        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        /** Industry vertical. */
        public Builder vertical(String vertical) {
            this.vertical = vertical;
            return this;
        }

        /** Business website URL. */
        public Builder website(String website) {
            this.website = website;
            return this;
        }

        /** Business contact email. */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /** Business phone number. */
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        /** Business mobile phone number. */
        public Builder mobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
            return this;
        }

        /** Street address. */
        public Builder street(String street) {
            this.street = street;
            return this;
        }

        /** City. */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /** State or region. */
        public Builder state(String state) {
            this.state = state;
            return this;
        }

        /** Postal code. */
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /** ISO 3166-1 alpha-2 country code; defaults to "US". */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /** Existing Sendly verification to prefill business details from. */
        public Builder verificationId(String verificationId) {
            this.verificationId = verificationId;
            return this;
        }

        public CreateTenDlcBrandRequest build() {
            return new CreateTenDlcBrandRequest(this);
        }
    }
}
