package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * A request body sent exactly as built, JSON {@code null} values included.
 *
 * <p>The client's serializer drops {@code null} members, which would turn a
 * field set to {@code null} to remove it into a field left out, which keeps
 * it.
 */
@JsonAdapter(JsonBodyWithNulls.Adapter.class)
final class JsonBodyWithNulls {
    private final JsonObject json;

    JsonBodyWithNulls(JsonObject json) {
        this.json = json;
    }

    static final class Adapter extends TypeAdapter<JsonBodyWithNulls> {
        @Override
        public void write(JsonWriter out, JsonBodyWithNulls value) throws IOException {
            out.jsonValue(value.json.toString());
        }

        @Override
        public JsonBodyWithNulls read(JsonReader in) {
            throw new UnsupportedOperationException();
        }
    }
}
