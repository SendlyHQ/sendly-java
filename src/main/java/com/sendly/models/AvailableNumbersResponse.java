package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code numbers().listAvailable()}.
 */
public class AvailableNumbersResponse {
    private List<AvailableNumber> numbers;

    public AvailableNumbersResponse() {
        this.numbers = new ArrayList<>();
    }

    public AvailableNumbersResponse(JsonObject json) {
        this.numbers = new ArrayList<>();
        if (json.has("numbers") && json.get("numbers").isJsonArray()) {
            json.get("numbers").getAsJsonArray().forEach(e ->
                numbers.add(new AvailableNumber(e.getAsJsonObject()))
            );
        }
    }

    /** Available numbers with pricing, already priced for your account. */
    public List<AvailableNumber> getNumbers() { return numbers; }
}
