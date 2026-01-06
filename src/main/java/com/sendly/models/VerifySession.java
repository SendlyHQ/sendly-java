package com.sendly.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class VerifySession {
    private String id;
    private String url;
    private String status;

    @SerializedName("success_url")
    private String successUrl;

    @SerializedName("cancel_url")
    private String cancelUrl;

    @SerializedName("brand_name")
    private String brandName;

    @SerializedName("brand_color")
    private String brandColor;

    private String phone;

    @SerializedName("verification_id")
    private String verificationId;

    private String token;
    private Map<String, Object> metadata;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getStatus() { return status; }
    public String getSuccessUrl() { return successUrl; }
    public String getCancelUrl() { return cancelUrl; }
    public String getBrandName() { return brandName; }
    public String getBrandColor() { return brandColor; }
    public String getPhone() { return phone; }
    public String getVerificationId() { return verificationId; }
    public String getToken() { return token; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getExpiresAt() { return expiresAt; }
    public String getCreatedAt() { return createdAt; }
}
