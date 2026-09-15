package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * The street address emergency services are sent to when someone calls them
 * from a number. Used both when registering one with
 * {@code voice().numbers().registerEmergencyAddress()} and when reading it
 * back from {@link VoiceNumberEmergencyAddress#getAddress()}.
 *
 * <pre>{@code
 * EmergencyAddress address = EmergencyAddress.builder()
 *     .street("500 Example Ave")
 *     .unit("Suite 2")
 *     .city("Austin")
 *     .state("TX")
 *     .zip("78701")
 *     .build();
 * }</pre>
 */
public class EmergencyAddress {
    private final String street;
    private final String unit;
    private final String city;
    private final String state;
    private final String zip;
    private final String country;

    private EmergencyAddress(Builder builder) {
        this.street = builder.street;
        this.unit = builder.unit;
        this.city = builder.city;
        this.state = builder.state;
        this.zip = builder.zip;
        this.country = builder.country;
    }

    /**
     * Create an EmergencyAddress from a JSON object.
     */
    public EmergencyAddress(JsonObject json) {
        this.street = getStringOrNull(json, "street");
        this.unit = getStringOrNull(json, "unit");
        this.city = getStringOrNull(json, "city");
        this.state = getStringOrNull(json, "state");
        this.zip = getStringOrNull(json, "zip");
        this.country = getStringOrNull(json, "country");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    /** Street address. */
    public String getStreet() {
        return street;
    }

    /** Apartment, suite or floor, or {@code null} when there is none. */
    public String getUnit() {
        return unit;
    }

    /** City. */
    public String getCity() {
        return city;
    }

    /** Two-letter state or province code. */
    public String getState() {
        return state;
    }

    /** ZIP code (US) or postal code (Canada). */
    public String getZip() {
        return zip;
    }

    /** {@code "US"} or {@code "CA"}. {@code null} on a request that leaves it to default to {@code "US"}. */
    public String getCountry() {
        return country;
    }

    /** Serialize to the JSON body the API expects. Unset fields are omitted. */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (street != null) o.addProperty("street", street);
        if (unit != null) o.addProperty("unit", unit);
        if (city != null) o.addProperty("city", city);
        if (state != null) o.addProperty("state", state);
        if (zip != null) o.addProperty("zip", zip);
        if (country != null) o.addProperty("country", country);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String street;
        private String unit;
        private String city;
        private String state;
        private String zip;
        private String country;

        /** Street address. Required. */
        public Builder street(String street) {
            this.street = street;
            return this;
        }

        /** Apartment, suite or floor. */
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        /** City. Required. */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /** Two-letter state or province code, e.g. {@code "TX"}. Required. */
        public Builder state(String state) {
            this.state = state;
            return this;
        }

        /** Five-digit ZIP (or ZIP+4) in the US, {@code "A1A 1A1"} in Canada. Required. */
        public Builder zip(String zip) {
            this.zip = zip;
            return this;
        }

        /** {@code "US"} or {@code "CA"}. Defaults to {@code "US"} when not set. */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public EmergencyAddress build() {
            return new EmergencyAddress(this);
        }
    }

    @Override
    public String toString() {
        return "EmergencyAddress{" +
                "street='" + street + '\'' +
                ", unit='" + unit + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
