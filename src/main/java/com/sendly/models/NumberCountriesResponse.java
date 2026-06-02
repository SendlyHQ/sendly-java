package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code numbers().listCountries()}.
 */
public class NumberCountriesResponse {
    private List<NumberCountry> countries;

    public NumberCountriesResponse() {
        this.countries = new ArrayList<>();
    }

    public NumberCountriesResponse(JsonObject json) {
        this.countries = new ArrayList<>();
        if (json.has("countries") && json.get("countries").isJsonArray()) {
            json.get("countries").getAsJsonArray().forEach(e ->
                countries.add(new NumberCountry(e.getAsJsonObject()))
            );
        }
    }

    /** The countries Sendly can provision numbers in. */
    public List<NumberCountry> getCountries() { return countries; }
}
