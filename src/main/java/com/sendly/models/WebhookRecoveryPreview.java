package com.sendly.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a webhook recovery would do for a window, without sending anything,
 * from {@code webhooks().recoveryPreview()}. The API sends its keys in
 * camelCase, unlike the other webhook responses.
 */
public class WebhookRecoveryPreview {
    private final String circuitState;
    private final String circuitOpenedAt;
    private final int failureCount;
    private final AuditFailures auditFailures;
    private final MissingFromMessages missingFromMessages;
    private final List<HistogramBucket> histogram;
    private final List<Sample> samples;
    private final Window windowSuggestion;
    private final String since;
    private final String until;

    /**
     * Create a WebhookRecoveryPreview from a JSON object.
     */
    public WebhookRecoveryPreview(JsonObject json) {
        this.circuitState = getStringOrNull(json, "circuitState");
        this.circuitOpenedAt = getStringOrNull(json, "circuitOpenedAt");
        this.failureCount = getInt(json, "failureCount");
        this.auditFailures = new AuditFailures(getObject(json, "auditFailures"));
        this.missingFromMessages = new MissingFromMessages(getObject(json, "missingFromMessages"));
        List<HistogramBucket> buckets = new ArrayList<>();
        if (json.has("histogram") && json.get("histogram").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("histogram")) {
                if (element.isJsonObject()) buckets.add(new HistogramBucket(element.getAsJsonObject()));
            }
        }
        this.histogram = Collections.unmodifiableList(buckets);
        List<Sample> parsedSamples = new ArrayList<>();
        if (json.has("samples") && json.get("samples").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("samples")) {
                if (element.isJsonObject()) parsedSamples.add(new Sample(element.getAsJsonObject()));
            }
        }
        this.samples = Collections.unmodifiableList(parsedSamples);
        this.windowSuggestion = json.has("windowSuggestion") && json.get("windowSuggestion").isJsonObject()
                ? new Window(json.getAsJsonObject("windowSuggestion")) : null;
        this.since = getStringOrNull(json, "since");
        this.until = getStringOrNull(json, "until");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static int getInt(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }

    private static JsonObject getObject(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonObject() ? json.getAsJsonObject(key) : new JsonObject();
    }

    private static Map<String, Integer> getCounts(JsonObject json, String key) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (json.has(key) && json.get(key).isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(key).entrySet()) {
                if (!entry.getValue().isJsonNull()) counts.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
        return Collections.unmodifiableMap(counts);
    }

    /** {@code "closed"}, {@code "open"} or {@code "half_open"}. More values may be added. */
    public String getCircuitState() { return circuitState; }

    /** When the circuit opened (ISO 8601), or {@code null}. */
    public String getCircuitOpenedAt() { return circuitOpenedAt; }

    /** Failed deliveries in the current streak. */
    public int getFailureCount() { return failureCount; }

    /** Failed and cancelled deliveries in the window: what {@code redeliver} would send. */
    public AuditFailures getAuditFailures() { return auditFailures; }

    /** Message events with no delivery in the window: what {@code backfill} would send. */
    public MissingFromMessages getMissingFromMessages() { return missingFromMessages; }

    /** The window in up to 24 equal buckets, oldest first. */
    public List<HistogramBucket> getHistogram() { return histogram; }

    /** Up to 5 affected events: failed deliveries first, then missing message events. */
    public List<Sample> getSamples() { return samples; }

    /** The outage window, when the circuit has an opening time, or {@code null}. */
    public Window getWindowSuggestion() { return windowSuggestion; }

    /** The window's start (ISO 8601). */
    public String getSince() { return since; }

    /** The window's end (ISO 8601). */
    public String getUntil() { return until; }

    @Override
    public String toString() {
        return "WebhookRecoveryPreview{" +
                "circuitState='" + circuitState + '\'' +
                ", failureCount=" + failureCount +
                ", auditFailures=" + auditFailures.getTotal() +
                ", missingFromMessages=" + missingFromMessages.getTotal() +
                ", since='" + since + '\'' +
                ", until='" + until + '\'' +
                '}';
    }

    /** Failed and cancelled deliveries in the window. */
    public static final class AuditFailures {
        private final int total;
        private final Map<String, Integer> byStatus;
        private final Map<String, Integer> byEventType;

        AuditFailures(JsonObject json) {
            this.total = getInt(json, "total");
            this.byStatus = getCounts(json, "byStatus");
            this.byEventType = getCounts(json, "byEventType");
        }

        public int getTotal() { return total; }
        /** Counts by delivery status, such as {@code failed} and {@code cancelled}. */
        public Map<String, Integer> getByStatus() { return byStatus; }
        /** Counts by event type, such as {@code message.delivered}. */
        public Map<String, Integer> getByEventType() { return byEventType; }
    }

    /** Message events with no delivery in the window. */
    public static final class MissingFromMessages {
        private final int total;
        private final Map<String, Integer> byEventType;

        MissingFromMessages(JsonObject json) {
            this.total = getInt(json, "total");
            this.byEventType = getCounts(json, "byEventType");
        }

        public int getTotal() { return total; }
        /** Counts by event type, such as {@code message.sent}. */
        public Map<String, Integer> getByEventType() { return byEventType; }
    }

    /** One bucket of the histogram. */
    public static final class HistogramBucket {
        private final String bucket;
        private final int missing;
        private final int failed;

        HistogramBucket(JsonObject json) {
            this.bucket = getStringOrNull(json, "bucket");
            this.missing = getInt(json, "missing");
            this.failed = getInt(json, "failed");
        }

        /** The bucket's start (ISO 8601). */
        public String getBucket() { return bucket; }
        public int getMissing() { return missing; }
        public int getFailed() { return failed; }
    }

    /** One affected event. */
    public static final class Sample {
        private final String id;
        private final String toMasked;
        private final String eventType;
        private final String occurredAt;

        Sample(JsonObject json) {
            this.id = getStringOrNull(json, "id");
            this.toMasked = getStringOrNull(json, "toMasked");
            this.eventType = getStringOrNull(json, "eventType");
            this.occurredAt = getStringOrNull(json, "occurredAt");
        }

        /** The event's object id, such as a message id. */
        public String getId() { return id; }
        /** The recipient, masked, or {@code ***} when there is none. */
        public String getToMasked() { return toMasked; }
        public String getEventType() { return eventType; }
        /** When the delivery or message was created (ISO 8601). */
        public String getOccurredAt() { return occurredAt; }
    }

    /** A time window. */
    public static final class Window {
        private final String since;
        private final String until;

        Window(JsonObject json) {
            this.since = getStringOrNull(json, "since");
            this.until = getStringOrNull(json, "until");
        }

        public String getSince() { return since; }
        public String getUntil() { return until; }
    }
}
