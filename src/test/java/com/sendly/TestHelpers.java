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
     * Create a JSON response for a batch send (POST /messages/batch).
     *
     * <p>Mirrors the live payload: the batch is identified by {@code batchId},
     * progress is reported as {@code sent}, and there is no {@code queued}
     * count and no {@code createdAt}.</p>
     */
    public static String batchResponseJson(String batchId, int total, int sent, int failed) {
        StringBuilder json = new StringBuilder(String.format(
            "{\"batchId\":\"%s\",\"status\":\"%s\",\"total\":%d,\"sent\":%d,\"failed\":%d,"
                + "\"optedOutSkipped\":0,\"invalidSkipped\":0,\"creditsUsed\":%d,\"creditsRefunded\":0,\"messages\":[",
            batchId, batchStatus(sent, failed), total, sent, failed, sent
        ));
        appendBatchMessages(json, total, sent);
        json.append("]}");
        return json.toString();
    }

    /**
     * Create a JSON response for a batch fetch (GET /messages/batch/:id).
     *
     * <p>Mirrors the live payload: the batch is identified by {@code id} and
     * carries the {@code queued} count plus timestamps the send response omits.</p>
     */
    public static String batchStatusJson(String batchId, int total, int queued, int failed) {
        StringBuilder json = new StringBuilder(String.format(
            "{\"id\":\"%s\",\"status\":\"%s\",\"total\":%d,\"queued\":%d,\"sent\":%d,\"delivered\":0,\"failed\":%d,"
                + "\"creditsReserved\":%d,\"creditsUsed\":%d,\"creditsRefunded\":0,"
                + "\"createdAt\":\"2025-01-15T10:00:00.000Z\",\"completedAt\":null,\"messages\":[",
            batchId, batchStatus(queued, failed), total, queued, queued, failed, queued, queued
        ));
        appendBatchMessages(json, total, queued);
        json.append("]}");
        return json.toString();
    }

    /** Derive the batch status the server would report from the two counts. */
    private static String batchStatus(int succeeded, int failed) {
        if (succeeded == 0) return "failed";
        return failed > 0 ? "partial_failure" : "completed";
    }

    /** Append per-message results: the first {@code succeeded} queued, the rest failed. */
    private static void appendBatchMessages(StringBuilder json, int total, int succeeded) {
        for (int i = 0; i < total; i++) {
            if (i > 0) json.append(",");
            boolean ok = i < succeeded;
            json.append(String.format(
                "{\"id\":\"msg_%d\",\"to\":\"+155512345%02d\",\"status\":\"%s\"%s}",
                i, i, ok ? "queued" : "failed", ok ? "" : ",\"error\":\"Invalid phone number\""
            ));
        }
    }

    /**
     * Create a JSON response for a batch list.
     */
    public static String batchListJson(int count, int offset, boolean hasMore) {
        StringBuilder json = new StringBuilder("{\"data\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"id\":\"batch_%d\",\"status\":\"completed\",\"total\":10,\"queued\":10,\"sent\":10,\"delivered\":10,"
                    + "\"failed\":0,\"creditsReserved\":10,\"creditsUsed\":10,\"creditsRefunded\":0,"
                    + "\"createdAt\":\"2025-01-15T10:00:00.000Z\",\"completedAt\":null}",
                offset + i
            ));
        }
        json.append("],\"total\":30,\"limit\":20,\"offset\":").append(offset)
            .append(",\"has_more\":").append(hasMore).append("}");
        return json.toString();
    }
}
