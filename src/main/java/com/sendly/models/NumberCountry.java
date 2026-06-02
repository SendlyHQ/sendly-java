package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A country Sendly can provision phone numbers in, along with the number
 * types available there.
 */
public class NumberCountry {
    private String code;
    private String name;
    private List<String> numberTypes;

    public NumberCountry() {
        this.numberTypes = new ArrayList<>();
    }

    public NumberCountry(JsonObject json) {
        this.numberTypes = new ArrayList<>();
        if (json.has("code") && !json.get("code").isJsonNull()) {
            this.code = json.get("code").getAsString();
        }
        if (json.has("name") && !json.get("name").isJsonNull()) {
            this.name = json.get("name").getAsString();
        }
        if (json.has("numberTypes") && json.get("numberTypes").isJsonArray()) {
            json.get("numberTypes").getAsJsonArray().forEach(e -> {
                if (!e.isJsonNull()) numberTypes.add(e.getAsString());
            });
        }
    }

    /** ISO 3166-1 alpha-2 country code (e.g. "GB"). */
    public String getCode() { return code; }

    /** Human-readable country name. */
    public String getName() { return name; }

    /** Number types available in this country (e.g. ["mobile", "local"]). */
    public List<String> getNumberTypes() { return numberTypes; }
}
