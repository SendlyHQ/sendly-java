package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Result of testing a webhook: a successful test delivery. A delivery that
 * fails makes {@code webhooks().test(id)} throw a {@code ValidationException}
 * carrying the API's message instead.
 */
public class WebhookTestResult {
    private final boolean success;
    private final Integer statusCode;
    private final Integer responseTimeMs;
    private final String error;
    private final String message;

    public WebhookTestResult(JsonObject json) {
        JsonObject delivery = json.has("delivery") && json.get("delivery").isJsonObject()
                ? json.getAsJsonObject("delivery") : new JsonObject();
        this.success = json.has("success") && json.get("success").getAsBoolean();
        this.statusCode = firstInteger(delivery, json, "status_code", "statusCode");
        this.responseTimeMs = firstInteger(delivery, json, "response_time_ms", "response_time", "responseTimeMs");
        this.error = getStringOrNull(delivery.has("error") ? delivery : json, "error");
        this.message = getStringOrNull(json, "message");
    }

    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Integer firstInteger(JsonObject delivery, JsonObject root, String... keys) {
        for (JsonObject json : new JsonObject[] {delivery, root}) {
            for (String key : keys) {
                if (json.has(key) && !json.get(key).isJsonNull()) return json.get(key).getAsInt();
            }
        }
        return null;
    }

    public boolean isSuccess() { return success; }
    /** The HTTP status your endpoint answered the test delivery with. */
    public Integer getStatusCode() { return statusCode; }
    /** How long your endpoint took to answer, in milliseconds. */
    public Integer getResponseTimeMs() { return responseTimeMs; }
    public String getError() { return error; }
    /** The API's summary of the test, such as "Test webhook delivered successfully in 120ms". */
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "WebhookTestResult{success=" + success + ", statusCode=" + statusCode + "}";
    }
}
