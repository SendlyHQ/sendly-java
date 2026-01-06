package com.sendly.models;

import java.util.Map;

public class CreateSessionRequest {
    private String successUrl;
    private String cancelUrl;
    private String brandName;
    private String brandColor;
    private Map<String, Object> metadata;

    public CreateSessionRequest(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getSuccessUrl() { return successUrl; }
    public CreateSessionRequest setSuccessUrl(String successUrl) { this.successUrl = successUrl; return this; }

    public String getCancelUrl() { return cancelUrl; }
    public CreateSessionRequest setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; return this; }

    public String getBrandName() { return brandName; }
    public CreateSessionRequest setBrandName(String brandName) { this.brandName = brandName; return this; }

    public String getBrandColor() { return brandColor; }
    public CreateSessionRequest setBrandColor(String brandColor) { this.brandColor = brandColor; return this; }

    public Map<String, Object> getMetadata() { return metadata; }
    public CreateSessionRequest setMetadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String successUrl;
        private String cancelUrl;
        private String brandName;
        private String brandColor;
        private Map<String, Object> metadata;

        public Builder successUrl(String successUrl) { this.successUrl = successUrl; return this; }
        public Builder cancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; return this; }
        public Builder brandName(String brandName) { this.brandName = brandName; return this; }
        public Builder brandColor(String brandColor) { this.brandColor = brandColor; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

        public CreateSessionRequest build() {
            CreateSessionRequest req = new CreateSessionRequest(successUrl);
            req.cancelUrl = cancelUrl;
            req.brandName = brandName;
            req.brandColor = brandColor;
            req.metadata = metadata;
            return req;
        }
    }
}
