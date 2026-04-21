package com.sendly.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import java.util.HashMap;

public class Contact {
    private String id;
    @SerializedName("phone_number")
    private String phoneNumber;
    private String name;
    private String email;
    private Map<String, Object> metadata;
    @SerializedName("opted_out")
    private Boolean optedOut;
    @SerializedName("line_type")
    private String lineType;
    @SerializedName("carrier_name")
    private String carrierName;
    @SerializedName("line_type_checked_at")
    private String lineTypeCheckedAt;
    @SerializedName("invalid_reason")
    private String invalidReason;
    @SerializedName("invalidated_at")
    private String invalidatedAt;
    @SerializedName("user_marked_valid_at")
    private String userMarkedValidAt;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    public Contact() {}

    public Contact(JsonObject json) {
        if (json.has("id")) this.id = json.get("id").getAsString();
        // Accept either snake_case or camelCase from the server.
        String phoneKey = json.has("phone_number") ? "phone_number" : (json.has("phoneNumber") ? "phoneNumber" : null);
        if (phoneKey != null) this.phoneNumber = json.get(phoneKey).getAsString();
        if (json.has("name") && !json.get("name").isJsonNull()) {
            this.name = json.get("name").getAsString();
        }
        if (json.has("email") && !json.get("email").isJsonNull()) {
            this.email = json.get("email").getAsString();
        }
        if (json.has("metadata") && json.get("metadata").isJsonObject()) {
            this.metadata = new HashMap<>();
            json.get("metadata").getAsJsonObject().entrySet().forEach(e -> {
                if (e.getValue().isJsonPrimitive()) {
                    if (e.getValue().getAsJsonPrimitive().isString()) {
                        metadata.put(e.getKey(), e.getValue().getAsString());
                    } else if (e.getValue().getAsJsonPrimitive().isNumber()) {
                        metadata.put(e.getKey(), e.getValue().getAsNumber());
                    } else if (e.getValue().getAsJsonPrimitive().isBoolean()) {
                        metadata.put(e.getKey(), e.getValue().getAsBoolean());
                    }
                }
            });
        }
        this.optedOut = readBool(json, "opted_out", "optedOut");
        this.lineType = readStr(json, "line_type", "lineType");
        this.carrierName = readStr(json, "carrier_name", "carrierName");
        this.lineTypeCheckedAt = readStr(json, "line_type_checked_at", "lineTypeCheckedAt");
        this.invalidReason = readStr(json, "invalid_reason", "invalidReason");
        this.invalidatedAt = readStr(json, "invalidated_at", "invalidatedAt");
        this.userMarkedValidAt = readStr(json, "user_marked_valid_at", "userMarkedValidAt");
        this.createdAt = readStr(json, "created_at", "createdAt");
        this.updatedAt = readStr(json, "updated_at", "updatedAt");
    }

    private static String readStr(JsonObject j, String snake, String camel) {
        String key = j.has(snake) ? snake : (j.has(camel) ? camel : null);
        if (key == null || j.get(key).isJsonNull()) return null;
        return j.get(key).getAsString();
    }

    private static Boolean readBool(JsonObject j, String snake, String camel) {
        String key = j.has(snake) ? snake : (j.has(camel) ? camel : null);
        if (key == null || j.get(key).isJsonNull()) return null;
        return j.get(key).getAsBoolean();
    }

    public String getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Boolean getOptedOut() { return optedOut; }
    public String getLineType() { return lineType; }
    public String getCarrierName() { return carrierName; }
    public String getLineTypeCheckedAt() { return lineTypeCheckedAt; }
    public String getInvalidReason() { return invalidReason; }
    public String getInvalidatedAt() { return invalidatedAt; }
    public String getUserMarkedValidAt() { return userMarkedValidAt; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
