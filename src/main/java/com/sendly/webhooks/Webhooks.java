package com.sendly.webhooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Webhook utilities for verifying and parsing Sendly webhook events.
 *
 * <pre>{@code
 * // In your webhook handler (e.g., Spring Boot)
 * @PostMapping("/webhooks/sendly")
 * public ResponseEntity<String> handleWebhook(
 *     @RequestBody String payload,
 *     @RequestHeader("X-Sendly-Signature") String signature
 * ) {
 *     try {
 *         WebhookEvent event = Webhooks.parseEvent(payload, signature, webhookSecret);
 *         System.out.println("Received event: " + event.getType());
 *
 *         switch (event.getType()) {
 *             case "message.delivered":
 *                 System.out.println("Message delivered: " + event.getData().getMessageId());
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

    /**
     * Verify webhook signature from Sendly.
     *
     * @param payload   Raw request body as string
     * @param signature X-Sendly-Signature header value
     * @param secret    Your webhook secret from dashboard
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifySignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null ||
            payload.isEmpty() || signature.isEmpty() || secret.isEmpty()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String expected = "sha256=" + hexString.toString();

            // Timing-safe comparison
            return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    /**
     * Parse and validate a webhook event.
     *
     * @param payload   Raw request body as string
     * @param signature X-Sendly-Signature header value
     * @param secret    Your webhook secret from dashboard
     * @return Parsed and validated WebhookEvent
     * @throws WebhookSignatureException if signature is invalid or payload is malformed
     */
    public static WebhookEvent parseEvent(String payload, String signature, String secret)
            throws WebhookSignatureException {
        if (!verifySignature(payload, signature, secret)) {
            throw new WebhookSignatureException("Invalid webhook signature");
        }

        try {
            WebhookEvent event = gson.fromJson(payload, WebhookEvent.class);

            // Basic validation
            if (event.getId() == null || event.getType() == null || event.getCreatedAt() == null) {
                throw new WebhookSignatureException("Invalid event structure");
            }

            return event;
        } catch (JsonSyntaxException e) {
            throw new WebhookSignatureException("Failed to parse webhook payload: " + e.getMessage());
        }
    }

    /**
     * Generate a webhook signature for testing purposes.
     *
     * @param payload The payload to sign
     * @param secret  The secret to use for signing
     * @return The signature in the format "sha256=..."
     */
    public static String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

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

    /**
     * Webhook event data containing message details.
     */
    public static class WebhookMessageData {
        @SerializedName("message_id")
        private String messageId;
        private String status;
        private String to;
        private String from;
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

        public String getMessageId() { return messageId; }
        public String getStatus() { return status; }
        public String getTo() { return to; }
        public String getFrom() { return from; }
        public String getError() { return error; }
        public String getErrorCode() { return errorCode; }
        public String getDeliveredAt() { return deliveredAt; }
        public String getFailedAt() { return failedAt; }
        public int getSegments() { return segments; }
        public int getCreditsUsed() { return creditsUsed; }
    }

    /**
     * Webhook event from Sendly.
     */
    public static class WebhookEvent {
        private String id;
        private String type;
        private WebhookMessageData data;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("api_version")
        private String apiVersion;

        public String getId() { return id; }
        public String getType() { return type; }
        public WebhookMessageData getData() { return data; }
        public String getCreatedAt() { return createdAt; }
        public String getApiVersion() { return apiVersion; }
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
