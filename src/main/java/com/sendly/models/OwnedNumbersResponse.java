package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code numbers().list()}.
 */
public class OwnedNumbersResponse {
    private List<OwnedNumber> numbers;

    public OwnedNumbersResponse() {
        this.numbers = new ArrayList<>();
    }

    public OwnedNumbersResponse(JsonObject json) {
        this.numbers = new ArrayList<>();
        if (json.has("numbers") && json.get("numbers").isJsonArray()) {
            json.get("numbers").getAsJsonArray().forEach(e ->
                numbers.add(new OwnedNumber(e.getAsJsonObject()))
            );
        }
    }

    /** The phone numbers you own. */
    public List<OwnedNumber> getNumbers() { return numbers; }
}
