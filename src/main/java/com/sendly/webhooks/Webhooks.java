package com.sendly.webhooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Webhook utilities for verifying and parsing Sendly webhook events.
 *
 * <pre>{@code
 * // In your webhook handler (e.g., Spring Boot)
 * @PostMapping("/webhooks/sendly")
 * public ResponseEntity<String> handleWebhook(
 *     @RequestBody String payload,
 *     @RequestHeader("X-Sendly-Signature") String signature,
 *     @RequestHeader(value = "X-Sendly-Timestamp", required = false) String timestamp
 * ) {
 *     try {
 *         WebhookEvent event = Webhooks.parseEvent(payload, signature, webhookSecret, timestamp);
 *         System.out.println("Received event: " + event.getType());
 *
 *         switch (event.getType()) {
 *             case "message.delivered":
 *                 System.out.println("Message delivered: " + event.getData().getId());
 *                 break;
 *             case "message.failed":
 *                 System.out.println("Message failed: " + event.getData().getError());
 *                 break;
 *         }
 *
 *         return ResponseEntity.ok("OK");
 *     } catch (WebhookSignatureException e) {
 *         return ResponseEntity.status(401).body("Invalid signature");
 *     }
 * }
 * }</pre>
 */
public class Webhooks {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    private static final int SIGNATURE_TOLERANCE_SECONDS = 300;

    /**
     * Verify webhook signature from Sendly.
     *
     * @param payload   Raw request body as string
     * @param signature X-Sendly-Signature header value
     * @param secret    Your webhook secret from dashboard
     * @param timestamp X-Sendly-Timestamp header value (null to skip timestamp check)
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifySignature(String payload, String signature, String secret, String timestamp) {
        if (payload == null || signature == null || secret == null ||
            payload.isEmpty() || signature.isEmpty() || secret.isEmpty()) {
            return false;
        }

        try {
            String signedPayload;
            if (timestamp != null && !timestamp.isEmpty()) {
                signedPayload = timestamp + "." + payload;
                long ts = Long.parseLong(timestamp);
                if (Math.abs(System.currentTimeMillis() / 1000 - ts) > SIGNATURE_TOLERANCE_SECONDS) {
                    return false;
                }
            } else {
                signedPayload = payload;
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String expected = "sha256=" + hexString.toString();

            return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException | NumberFormatException e) {
            return false;
        }
    }

    /** @deprecated Use {@link #verifySignature(String, String, String, String)} instead */
    public static boolean verifySignature(String payload, String signature, String secret) {
        return verifySignature(payload, signature, secret, null);
    }

    /**
     * Parse and validate a webhook event.
     *
     * @param payload   Raw request body as string
     * @param signature X-Sendly-Signature header value
     * @param secret    Your webhook secret from dashboard
     * @param timestamp X-Sendly-Timestamp header value (null to skip timestamp check)
     * @return Parsed and validated WebhookEvent
     * @throws WebhookSignatureException if signature is invalid or payload is malformed
     */
    public static WebhookEvent parseEvent(String payload, String signature, String secret, String timestamp)
            throws WebhookSignatureException {
        if (!verifySignature(payload, signature, secret, timestamp)) {
            throw new WebhookSignatureException("Invalid webhook signature");
        }

        try {
            JsonObject raw = gson.fromJson(payload, JsonObject.class);

            if (!raw.has("id") || !raw.has("type") || !raw.has("data")) {
                throw new WebhookSignatureException("Invalid event structure");
            }

            JsonObject dataObj = raw.getAsJsonObject("data");
            JsonObject msgObj = dataObj.has("object") ? dataObj.getAsJsonObject("object") : dataObj;

            WebhookMessageData data = new WebhookMessageData();
            data.id = getStringOr(msgObj, "id", getStringOr(msgObj, "message_id", ""));
            data.status = getStringOr(msgObj, "status", "");
            data.to = getStringOr(msgObj, "to", "");
            data.from = getStringOr(msgObj, "from", "");
            data.direction = getStringOr(msgObj, "direction", "outbound");
            data.organizationId = getStringOr(msgObj, "organization_id", null);
            data.text = getStringOr(msgObj, "text", null);
            data.error = getStringOr(msgObj, "error", null);
            data.errorCode = getStringOr(msgObj, "error_code", null);
            data.deliveredAt = getStringOr(msgObj, "delivered_at", null);
            data.failedAt = getStringOr(msgObj, "failed_at", null);
            data.segments = getIntOr(msgObj, "segments", 1);
            data.creditsUsed = getIntOr(msgObj, "credits_used", 0);
            data.messageFormat = getStringOr(msgObj, "message_format", null);
            data.batchId = getStringOr(msgObj, "batch_id", null);

            WebhookEvent event = new WebhookEvent();
            event.id = raw.get("id").getAsString();
            event.type = raw.get("type").getAsString();
            event.data = data;
            event.apiVersion = getStringOr(raw, "api_version", "2024-01");
            event.livemode = raw.has("livemode") && raw.get("livemode").getAsBoolean();

            if (raw.has("created")) {
                event.created = raw.get("created");
            } else if (raw.has("created_at")) {
                event.created = raw.get("created_at");
            }

            return event;
        } catch (JsonSyntaxException e) {
            throw new WebhookSignatureException("Failed to parse webhook payload: " + e.getMessage());
        }
    }

    /** @deprecated Use {@link #parseEvent(String, String, String, String)} instead */
    public static WebhookEvent parseEvent(String payload, String signature, String secret)
            throws WebhookSignatureException {
        return parseEvent(payload, signature, secret, null);
    }

    /**
     * Generate a webhook signature for testing purposes.
     *
     * @param payload   The payload to sign
     * @param secret    The secret to use for signing
     * @param timestamp Optional timestamp (null to skip)
     * @return The signature in the format "sha256=..."
     */
    public static String generateSignature(String payload, String secret, String timestamp) {
        try {
            String signedPayload = (timestamp != null && !timestamp.isEmpty())
                    ? timestamp + "." + payload : payload;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return "sha256=" + hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /** @deprecated Use {@link #generateSignature(String, String, String)} instead */
    public static String generateSignature(String payload, String secret) {
        return generateSignature(payload, secret, null);
    }

    private static String getStringOr(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    private static int getIntOr(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }

    /**
     * Webhook event data containing message details.
     */
    public static class WebhookMessageData {
        private String id;
        private String status;
        private String to;
        private String from;
        private String direction;
        @SerializedName("organization_id")
        private String organizationId;
        private String text;
        private String error;
        @SerializedName("error_code")
        private String errorCode;
        @SerializedName("delivered_at")
        private String deliveredAt;
        @SerializedName("failed_at")
        private String failedAt;
        private int segments;
        @SerializedName("credits_used")
        private int creditsUsed;
        @SerializedName("message_format")
        private String messageFormat;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("retry_count")
        private int retryCount;
        @SerializedName("media_urls")
        private String[] mediaUrls;
        private transient Map<String, Object> metadata;
        @SerializedName("batch_id")
        private String batchId;

        public String getId() { return id; }
        /** @deprecated Use {@link #getId()} instead */
        public String getMessageId() { return id; }
        public String getStatus() { return status; }
        public String getTo() { return to; }
        public String getFrom() { return from; }
        public String getDirection() { return direction; }
        public String getOrganizationId() { return organizationId; }
        public String getText() { return text; }
        public String getError() { return error; }
        public String getErrorCode() { return errorCode; }
        public String getDeliveredAt() { return deliveredAt; }
        public String getFailedAt() { return failedAt; }
        public int getSegments() { return segments; }
        public int getCreditsUsed() { return creditsUsed; }
        public String getMessageFormat() { return messageFormat; }
        public String getCreatedAt() { return createdAt; }
        public int getRetryCount() { return retryCount; }
        public String[] getMediaUrls() { return mediaUrls; }
        public Map<String, Object> getMetadata() { return metadata; }
        public String getBatchId() { return batchId; }
    }

    public static class WebhookVerificationData {
        private String id;
        @SerializedName("organization_id") private String organizationId;
        private String phone;
        private String status;
        @SerializedName("delivery_status") private String deliveryStatus;
        private int attempts;
        @SerializedName("max_attempts") private int maxAttempts;
        @SerializedName("expires_at") private transient com.google.gson.JsonElement expiresAt;
        @SerializedName("verified_at") private transient com.google.gson.JsonElement verifiedAt;
        @SerializedName("created_at") private transient com.google.gson.JsonElement createdAt;
        @SerializedName("app_name") private String appName;
        @SerializedName("template_id") private String templateId;
        @SerializedName("profile_id") private String profileId;

        public String getId() { return id; }
        public String getOrganizationId() { return organizationId; }
        public String getPhone() { return phone; }
        public String getStatus() { return status; }
        public String getDeliveryStatus() { return deliveryStatus; }
        public int getAttempts() { return attempts; }
        public int getMaxAttempts() { return maxAttempts; }
        public com.google.gson.JsonElement getExpiresAt() { return expiresAt; }
        public com.google.gson.JsonElement getVerifiedAt() { return verifiedAt; }
        public com.google.gson.JsonElement getCreatedAt() { return createdAt; }
        public String getAppName() { return appName; }
        public String getTemplateId() { return templateId; }
        public String getProfileId() { return profileId; }
    }

    /**
     * Webhook event from Sendly.
     */
    public static class WebhookEvent {
        private String id;
        private String type;
        private WebhookMessageData data;
        private transient JsonElement created;
        @SerializedName("api_version")
        private String apiVersion;
        private boolean livemode;

        public String getId() { return id; }
        public String getType() { return type; }
        public WebhookMessageData getData() { return data; }
        public JsonElement getCreated() { return created; }
        /** @deprecated Use {@link #getCreated()} instead */
        public String getCreatedAt() {
            return created != null ? created.getAsString() : null;
        }
        public String getApiVersion() { return apiVersion; }
        public boolean isLivemode() { return livemode; }
    }

    /**
     * Exception thrown when webhook signature verification fails.
     */
    public static class WebhookSignatureException extends Exception {
        public WebhookSignatureException(String message) {
            super(message);
        }
    }
}
