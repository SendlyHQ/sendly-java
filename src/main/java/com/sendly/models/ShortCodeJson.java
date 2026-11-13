package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ShortCodeJson {
    private ShortCodeJson() {}

    static String string(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    }

    static boolean bool(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsBoolean();
    }

    static Boolean optionalBool(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : null;
    }

    static int integer(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsInt() : 0;
    }

    static JsonObject object(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
    }

    static List<String> strings(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        if (e == null || !e.isJsonArray()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (JsonElement item : e.getAsJsonArray()) {
            if (item.isJsonPrimitive()) {
                out.add(item.getAsString());
            }
        }
        return out;
    }

    static List<JsonObject> objects(JsonObject json, String key) {
        JsonElement e = json == null ? null : json.get(key);
        if (e == null || !e.isJsonArray()) {
            return Collections.emptyList();
        }
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement item : e.getAsJsonArray()) {
            if (item.isJsonObject()) {
                out.add(item.getAsJsonObject());
            }
        }
        return out;
    }

    static JsonArray stringArray(List<String> values) {
        JsonArray arr = new JsonArray();
        for (String v : values) {
            arr.add(v);
        }
        return arr;
    }
}
