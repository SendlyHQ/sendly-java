package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Webhooks resource for managing webhook endpoints.
 */
public class WebhooksResource {
    private final Sendly client;

    public WebhooksResource(Sendly client) {
        this.client = client;
    }

    /**
     * Create a new webhook endpoint.
     *
     * @param url HTTPS endpoint URL
     * @param events Event types to subscribe to
     * @return Created webhook with signing secret
     */
    public WebhookCreatedResponse create(String url, List<String> events) throws SendlyException {
        return create(url, events, null, null);
    }

    /**
     * Create a new webhook endpoint with options.
     *
     * @param url HTTPS endpoint URL
     * @param events Event types to subscribe to
     * @param description Optional description
     * @param metadata Optional metadata
     * @return Created webhook with signing secret
     */
    public WebhookCreatedResponse create(String url, List<String> events, String description, Map<String, Object> metadata) throws SendlyException {
        if (url == null || !url.startsWith("https://")) {
            throw new ValidationException("Webhook URL must be HTTPS");
        }
        if (events == null || events.isEmpty()) {
            throw new ValidationException("At least one event type is required");
        }

        JsonObject body = new JsonObject();
        body.addProperty("url", url);
        JsonArray eventsArray = new JsonArray();
        events.forEach(eventsArray::add);
        body.add("events", eventsArray);
        if (description != null) body.addProperty("description", description);
        if (metadata != null) body.add("metadata", client.getGson().toJsonTree(metadata));

        JsonObject response = client.post("/webhooks", body);
        return new WebhookCreatedResponse(response);
    }

    /**
     * List all webhooks.
     */
    public List<Webhook> list() throws SendlyException {
        JsonObject response = client.get("/webhooks", null);
        List<Webhook> webhooks = new ArrayList<>();
        if (response.has("data") && response.get("data").isJsonArray()) {
            response.getAsJsonArray("data").forEach(e -> webhooks.add(new Webhook(e.getAsJsonObject())));
        } else if (response.isJsonArray()) {
            // Handle array response
            JsonArray array = client.getGson().fromJson(response.toString(), JsonArray.class);
            array.forEach(e -> webhooks.add(new Webhook(e.getAsJsonObject())));
        }
        return webhooks;
    }

    /**
     * Get a specific webhook by ID.
     */
    public Webhook get(String webhookId) throws SendlyException {
        validateWebhookId(webhookId);
        JsonObject response = client.get("/webhooks/" + webhookId, null);
        return new Webhook(response);
    }

    /**
     * Update a webhook configuration.
     */
    public Webhook update(String webhookId, String url, List<String> events, String description, Boolean isActive) throws SendlyException {
        validateWebhookId(webhookId);
        if (url != null && !url.startsWith("https://")) {
            throw new ValidationException("Webhook URL must be HTTPS");
        }

        JsonObject body = new JsonObject();
        if (url != null) body.addProperty("url", url);
        if (events != null) {
            JsonArray eventsArray = new JsonArray();
            events.forEach(eventsArray::add);
            body.add("events", eventsArray);
        }
        if (description != null) body.addProperty("description", description);
        if (isActive != null) body.addProperty("is_active", isActive);

        JsonObject response = client.patch("/webhooks/" + webhookId, body);
        return new Webhook(response);
    }

    /**
     * Delete a webhook.
     */
    public void delete(String webhookId) throws SendlyException {
        validateWebhookId(webhookId);
        client.delete("/webhooks/" + webhookId);
    }

    /**
     * Test a webhook endpoint.
     */
    public WebhookTestResult test(String webhookId) throws SendlyException {
        validateWebhookId(webhookId);
        JsonObject response = client.post("/webhooks/" + webhookId + "/test", new JsonObject());
        return new WebhookTestResult(response);
    }

    /**
     * Reset the circuit breaker for a webhook.
     */
    public JsonObject resetCircuit(String webhookId) throws SendlyException {
        validateWebhookId(webhookId);
        return client.post("/webhooks/" + webhookId + "/reset-circuit", new JsonObject());
    }

    /**
     * Replay failed or cancelled webhook deliveries from the audit log.
     *
     * <p>Use after a customer endpoint has recovered from an outage to
     * re-fire deliveries we recorded but couldn't deliver. Each replay
     * creates a new delivery row preserving the original event_id so
     * customers can dedupe. Rejects with HTTP 409 if the circuit is
     * currently open — call {@link #resetCircuit(String)} first.
     *
     * @param webhookId Webhook ID
     * @param options   Optional window and filter options; pass {@code null}
     *                  for server defaults
     * @return Counts of requeued deliveries plus delivery IDs
     */
    public JsonObject redeliver(String webhookId, RedeliverOptions options) throws SendlyException {
        validateWebhookId(webhookId);
        JsonObject body = options != null ? options.toJson() : new JsonObject();
        return client.post("/webhooks/" + webhookId + "/redeliver", body);
    }

    public JsonObject redeliver(String webhookId) throws SendlyException {
        return redeliver(webhookId, null);
    }

    /**
     * Backfill missed webhook events from the underlying message log.
     *
     * <p>Use when a circuit-breaker outage left events with no audit row
     * (the case {@link #redeliver(String, RedeliverOptions)} cannot
     * recover). Synthesized events have fresh IDs; clients should dedupe
     * by event.data.object.id (the message ID). Rejects with HTTP 409 if
     * the circuit is currently open — call
     * {@link #resetCircuit(String)} first.
     *
     * @param webhookId Webhook ID
     * @param options   Optional window and filter options; pass {@code null}
     *                  for server defaults
     * @return Counts grouped by event type plus delivery IDs
     */
    public JsonObject backfill(String webhookId, BackfillOptions options) throws SendlyException {
        validateWebhookId(webhookId);
        JsonObject body = options != null ? options.toJson() : new JsonObject();
        return client.post("/webhooks/" + webhookId + "/backfill", body);
    }

    public JsonObject backfill(String webhookId) throws SendlyException {
        return backfill(webhookId, null);
    }

    /** Options for {@link #redeliver(String, RedeliverOptions)}. */
    public static final class RedeliverOptions {
        private String since;
        private String until;
        private java.util.List<String> eventTypes;
        private java.util.List<String> statuses;
        private Integer limit;

        public RedeliverOptions since(String since) { this.since = since; return this; }
        public RedeliverOptions until(String until) { this.until = until; return this; }
        public RedeliverOptions eventTypes(java.util.List<String> eventTypes) { this.eventTypes = eventTypes; return this; }
        public RedeliverOptions statuses(java.util.List<String> statuses) { this.statuses = statuses; return this; }
        public RedeliverOptions limit(int limit) { this.limit = limit; return this; }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (since != null) json.addProperty("since", since);
            if (until != null) json.addProperty("until", until);
            if (eventTypes != null) {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                for (String t : eventTypes) arr.add(t);
                json.add("event_types", arr);
            }
            if (statuses != null) {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                for (String s : statuses) arr.add(s);
                json.add("statuses", arr);
            }
            if (limit != null) json.addProperty("limit", limit);
            return json;
        }
    }

    /** Options for {@link #backfill(String, BackfillOptions)}. */
    public static final class BackfillOptions {
        private String since;
        private String until;
        private java.util.List<String> eventTypes;
        private Integer limit;

        public BackfillOptions since(String since) { this.since = since; return this; }
        public BackfillOptions until(String until) { this.until = until; return this; }
        public BackfillOptions eventTypes(java.util.List<String> eventTypes) { this.eventTypes = eventTypes; return this; }
        public BackfillOptions limit(int limit) { this.limit = limit; return this; }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (since != null) json.addProperty("since", since);
            if (until != null) json.addProperty("until", until);
            if (eventTypes != null) {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                for (String t : eventTypes) arr.add(t);
                json.add("event_types", arr);
            }
            if (limit != null) json.addProperty("limit", limit);
            return json;
        }
    }

    /**
     * Rotate the webhook signing secret.
     */
    public WebhookCreatedResponse rotateSecret(String webhookId) throws SendlyException {
        validateWebhookId(webhookId);
        JsonObject response = client.post("/webhooks/" + webhookId + "/rotate-secret", new JsonObject());
        // The response contains webhook and newSecret
        JsonObject webhookJson = response.has("webhook") ? response.getAsJsonObject("webhook") : response;
        if (response.has("new_secret") || response.has("newSecret")) {
            webhookJson.addProperty("secret",
                response.has("new_secret") ? response.get("new_secret").getAsString() : response.get("newSecret").getAsString());
        }
        return new WebhookCreatedResponse(webhookJson);
    }

    /**
     * Get delivery history for a webhook.
     */
    public List<WebhookDelivery> getDeliveries(String webhookId) throws SendlyException {
        validateWebhookId(webhookId);
        JsonObject response = client.get("/webhooks/" + webhookId + "/deliveries", null);
        List<WebhookDelivery> deliveries = new ArrayList<>();
        if (response.has("data") && response.get("data").isJsonArray()) {
            response.getAsJsonArray("data").forEach(e -> deliveries.add(new WebhookDelivery(e.getAsJsonObject())));
        }
        return deliveries;
    }

    /**
     * Retry a failed delivery.
     */
    public void retryDelivery(String webhookId, String deliveryId) throws SendlyException {
        validateWebhookId(webhookId);
        if (deliveryId == null || !deliveryId.startsWith("del_")) {
            throw new ValidationException("Invalid delivery ID format");
        }
        client.post("/webhooks/" + webhookId + "/deliveries/" + deliveryId + "/retry", new JsonObject());
    }

    /**
     * List available webhook event types.
     *
     * @return List of event type strings
     * @throws SendlyException if the request fails
     */
    public List<String> listEventTypes() throws SendlyException {
        JsonObject response = client.get("/webhooks/event-types", null);
        List<String> eventTypes = new ArrayList<>();

        if (response.has("events") && response.get("events").isJsonArray()) {
            JsonArray events = response.getAsJsonArray("events");
            for (int i = 0; i < events.size(); i++) {
                JsonObject event = events.get(i).getAsJsonObject();
                if (event.has("type")) {
                    eventTypes.add(event.get("type").getAsString());
                }
            }
        }

        return eventTypes;
    }

    private void validateWebhookId(String webhookId) {
        if (webhookId == null || !webhookId.startsWith("whk_")) {
            throw new ValidationException("Invalid webhook ID format");
        }
    }
}
