package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class ImportContactsResponse {
    private final int imported;
    private final int skippedDuplicates;
    private final List<ImportError> errors;
    private final int totalErrors;

    public ImportContactsResponse(JsonObject json) {
        this.imported = json.has("imported") ? json.get("imported").getAsInt() : 0;
        this.skippedDuplicates = json.has("skippedDuplicates") ? json.get("skippedDuplicates").getAsInt() : 0;
        this.totalErrors = json.has("totalErrors") ? json.get("totalErrors").getAsInt() : 0;
        this.errors = new ArrayList<>();
        if (json.has("errors") && json.get("errors").isJsonArray()) {
            JsonArray errArray = json.getAsJsonArray("errors");
            for (int i = 0; i < errArray.size(); i++) {
                JsonObject err = errArray.get(i).getAsJsonObject();
                this.errors.add(new ImportError(
                    err.has("index") ? err.get("index").getAsInt() : 0,
                    err.has("phone") ? err.get("phone").getAsString() : "",
                    err.has("error") ? err.get("error").getAsString() : ""
                ));
            }
        }
    }

    public int getImported() { return imported; }
    public int getSkippedDuplicates() { return skippedDuplicates; }
    public List<ImportError> getErrors() { return errors; }
    public int getTotalErrors() { return totalErrors; }

    public static class ImportError {
        private final int index;
        private final String phone;
        private final String error;

        public ImportError(int index, String phone, String error) {
            this.index = index;
            this.phone = phone;
            this.error = error;
        }

        public int getIndex() { return index; }
        public String getPhone() { return phone; }
        public String getError() { return error; }
    }
}
