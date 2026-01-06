package com.sendly.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ValidateSessionResponse {
    private boolean valid;

    @SerializedName("session_id")
    private String sessionId;

    private String phone;

    @SerializedName("verified_at")
    private String verifiedAt;

    private Map<String, Object> metadata;

    public boolean isValid() { return valid; }
    public String getSessionId() { return sessionId; }
    public String getPhone() { return phone; }
    public String getVerifiedAt() { return verifiedAt; }
    public Map<String, Object> getMetadata() { return metadata; }
}
