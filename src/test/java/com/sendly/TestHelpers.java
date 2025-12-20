package com.sendly;

import okhttp3.mockwebserver.MockResponse;

/**
 * Test helper utilities for Sendly SDK tests.
 */
public class TestHelpers {

    /**
     * Create a successful mock response with JSON body.
     */
    public static MockResponse mockSuccess(String jsonBody) {
        return new MockResponse()
                .setResponseCode(200)
                .setBody(jsonBody)
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Create a mock authentication error response (401).
     */
    public static MockResponse mockAuthError() {
        return new MockResponse()
                .setResponseCode(401)
                .setBody("{\"message\":\"Invalid API key\"}")
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Create a mock insufficient credits error response (402).
     */
    public static MockResponse mockInsufficientCredits() {
        return new MockResponse()
                .setResponseCode(402)
                .setBody("{\"message\":\"Insufficient credits\"}")
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Create a mock not found error response (404).
     */
    public static MockResponse mockNotFound() {
        return new MockResponse()
                .setResponseCode(404)
                .setBody("{\"message\":\"Resource not found\"}")
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Create a mock rate limit error response (429).
     */
    public static MockResponse mockRateLimit(int retryAfter) {
        return new MockResponse()
                .setResponseCode(429)
                .setBody("{\"message\":\"Rate limit exceeded\"}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Retry-After", String.valueOf(retryAfter));
    }

    /**
     * Create a mock validation error response (400).
     */
    public static MockResponse mockValidationError(String message) {
        return new MockResponse()
                .setResponseCode(400)
                .setBody("{\"message\":\"" + message + "\"}")
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Create a mock server error response (500).
     */
    public static MockResponse mockServerError() {
        return new MockResponse()
                .setResponseCode(500)
                .setBody("{\"message\":\"Internal server error\"}")
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Create a JSON response for a single message.
     */
    public static String messageJson(String id, String to, String text, String status) {
        return String.format(
            "{\"message\":{\"id\":\"%s\",\"to\":\"%s\",\"text\":\"%s\",\"status\":\"%s\",\"credits_used\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\",\"updated_at\":\"2025-01-15T10:00:00.000Z\"}}",
            id, to, text, status
        );
    }

    /**
     * Create a JSON response for a message list.
     */
    public static String messageListJson(int count, int offset, boolean hasMore) {
        StringBuilder json = new StringBuilder("{\"data\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"id\":\"msg_%d\",\"to\":\"+15551234567\",\"text\":\"Test %d\",\"status\":\"sent\",\"credits_used\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\",\"updated_at\":\"2025-01-15T10:00:00.000Z\"}",
                offset + i, i
            ));
        }
        json.append("],\"pagination\":{\"total\":100,\"limit\":20,\"offset\":").append(offset)
            .append(",\"has_more\":").append(hasMore).append("}}");
        return json.toString();
    }

    /**
     * Create a JSON response for a scheduled message.
     */
    public static String scheduledMessageJson(String id, String to, String text, String scheduledAt) {
        return String.format(
            "{\"data\":{\"id\":\"%s\",\"to\":\"%s\",\"text\":\"%s\",\"status\":\"scheduled\",\"scheduled_at\":\"%s\",\"credits_reserved\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\"}}",
            id, to, text, scheduledAt
        );
    }

    /**
     * Create a JSON response for a list of scheduled messages.
     */
    public static String scheduledMessageListJson(int count, int offset, boolean hasMore) {
        StringBuilder json = new StringBuilder("{\"data\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"id\":\"sch_%d\",\"to\":\"+15551234567\",\"text\":\"Test %d\",\"status\":\"scheduled\",\"scheduled_at\":\"2025-01-20T10:00:00.000Z\",\"credits_reserved\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\"}",
                offset + i, i
            ));
        }
        json.append("],\"total\":50,\"limit\":20,\"offset\":").append(offset)
            .append(",\"has_more\":").append(hasMore).append("}");
        return json.toString();
    }

    /**
     * Create a JSON response for cancelling a scheduled message.
     */
    public static String cancelScheduledJson(String id, int creditsRefunded) {
        return String.format(
            "{\"id\":\"%s\",\"status\":\"cancelled\",\"credits_refunded\":%d,\"cancelled_at\":\"2025-01-15T10:00:00.000Z\"}",
            id, creditsRefunded
        );
    }

    /**
     * Create a JSON response for a batch send.
     */
    public static String batchResponseJson(String batchId, int total, int queued, int failed) {
        // Determine status based on success/failure counts
        String status;
        if (queued == 0) {
            status = "failed";
        } else if (failed > 0) {
            status = "partial_failure";
        } else {
            status = "completed";
        }

        StringBuilder json = new StringBuilder(String.format(
            "{\"batch_id\":\"%s\",\"status\":\"%s\",\"total\":%d,\"queued\":%d,\"failed\":%d,\"credits_used\":%d,\"messages\":[",
            batchId, status, total, queued, failed, queued
        ));
        for (int i = 0; i < total; i++) {
            if (i > 0) json.append(",");
            String msgStatus = i < queued ? "queued" : "failed";
            String error = i < queued ? null : "Invalid phone number";
            json.append(String.format(
                "{\"id\":\"msg_%d\",\"to\":\"+155512345%02d\",\"status\":\"%s\"%s}",
                i, i, msgStatus, error != null ? ",\"error\":\"" + error + "\"" : ""
            ));
        }
        json.append("],\"created_at\":\"2025-01-15T10:00:00.000Z\"}");
        return json.toString();
    }

    /**
     * Create a JSON response for a batch list.
     */
    public static String batchListJson(int count, int offset, boolean hasMore) {
        StringBuilder json = new StringBuilder("{\"data\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"batch_id\":\"batch_%d\",\"status\":\"completed\",\"total\":10,\"queued\":10,\"failed\":0,\"credits_used\":10,\"created_at\":\"2025-01-15T10:00:00.000Z\"}",
                offset + i
            ));
        }
        json.append("],\"total\":30,\"limit\":20,\"offset\":").append(offset)
            .append(",\"has_more\":").append(hasMore).append("}");
        return json.toString();
    }
}
