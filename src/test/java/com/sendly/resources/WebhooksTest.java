package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.models.Webhook;
import com.sendly.models.WebhookCreatedResponse;
import com.sendly.models.WebhookDelivery;
import com.sendly.models.WebhookRecoveryPreview;
import com.sendly.models.WebhookTestResult;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the webhooks resource against the shapes the API sends.
 */
class WebhooksTest {
    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(0);

        client = new Sendly("sk_test_123", builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testList_readsTheBareArrayTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "[{\"id\":\"whk_1\",\"url\":\"https://example.com/h\",\"events\":[\"message.delivered\"],"
                + "\"is_active\":true,\"created_at\":\"2026-09-25T00:00:00.000Z\","
                + "\"updated_at\":\"2026-09-25T00:00:00.000Z\"}]"
        ));

        List<Webhook> webhooks = client.webhooks().list();

        assertEquals(1, webhooks.size());
        assertEquals("whk_1", webhooks.get(0).getId());
        assertTrue(webhooks.get(0).isActive());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v1/webhooks", request.getPath());
    }

    private static final String DELIVERY =
        "{\"id\":\"del_1\",\"webhook_id\":\"whk_1\",\"event_id\":\"evt_1\",\"event_type\":\"message.delivered\","
            + "\"status\":\"delivered\",\"success\":true,\"response_status_code\":200,\"http_status\":200,"
            + "\"response_time\":120,\"response_time_ms\":120,\"response_body\":\"ok\",\"error_message\":null,"
            + "\"error_code\":null,\"attempt_number\":1,\"max_attempts\":6,\"next_retry_at\":null,"
            + "\"created_at\":\"2026-09-25T00:00:00.000Z\",\"delivered_at\":\"2026-09-25T00:00:01.000Z\"}";

    @Test
    void testGetDeliveries_readsTheDeliveriesEnvelope() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"deliveries\":[" + DELIVERY + "],\"pagination\":{\"limit\":50,\"offset\":0}}"
        ));

        List<WebhookDelivery> deliveries = client.webhooks().getDeliveries("whk_1");

        assertEquals(1, deliveries.size());
        WebhookDelivery delivery = deliveries.get(0);
        assertEquals("delivered", delivery.getStatus());
        assertEquals("message.delivered", delivery.getEventType());
        assertEquals("evt_1", delivery.getEventId());
        assertEquals(1, delivery.getAttemptNumber());
        assertEquals(200, delivery.getResponseStatusCode());
        assertEquals(120, delivery.getResponseTimeMs());
        assertEquals("/api/v1/webhooks/whk_1/deliveries", mockServer.takeRequest().getPath());
    }

    @Test
    void testGetDeliveries_sendsLimitOffsetAndStatus() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"deliveries\":[],\"pagination\":{\"limit\":10,\"offset\":20}}"
        ));

        List<WebhookDelivery> deliveries = client.webhooks().getDeliveries("whk_1", 10, 20, "failed");

        assertTrue(deliveries.isEmpty());
        okhttp3.HttpUrl url = mockServer.takeRequest().getRequestUrl();
        assertEquals("/api/v1/webhooks/whk_1/deliveries", url.encodedPath());
        assertEquals("10", url.queryParameter("limit"));
        assertEquals("20", url.queryParameter("offset"));
        assertEquals("failed", url.queryParameter("status"));
    }

    @Test
    void testGetDeliveries_stillReadsData() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + DELIVERY + "]}"));

        assertEquals(1, client.webhooks().getDeliveries("whk_1").size());
    }

    @Test
    void testTest_readsTheDeliveryTheApiNests() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"success\":true,\"message\":\"Test webhook delivered successfully in 120ms\","
                + "\"delivery\":{\"id\":\"del_1\",\"delivery_id\":\"del_1\",\"webhook_url\":\"https://example.com/h\","
                + "\"event_type\":\"webhook.test\",\"status\":\"delivered\",\"response_time\":120,\"status_code\":200,"
                + "\"response_body\":\"ok\",\"delivered_at\":\"2026-09-25T00:00:00.000Z\"}}"
        ));

        WebhookTestResult result = client.webhooks().test("whk_1");

        assertTrue(result.isSuccess());
        assertEquals(200, result.getStatusCode());
        assertEquals(120, result.getResponseTimeMs());
        assertNull(result.getError());
        assertEquals("Test webhook delivered successfully in 120ms", result.getMessage());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/webhooks/whk_1/test", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
    }

    @Test
    void testTest_failedDeliveryThrowsWithTheApiMessage() {
        mockServer.enqueue(new okhttp3.mockwebserver.MockResponse()
                .setResponseCode(400)
                .setBody("{\"success\":false,\"message\":\"Test webhook failed: Connection refused\"}"));

        com.sendly.exceptions.ValidationException exception = assertThrows(
                com.sendly.exceptions.ValidationException.class, () -> client.webhooks().test("whk_1"));

        assertEquals("Test webhook failed: Connection refused", exception.getMessage());
    }

    @Test
    void testClientSideChecks_throwTheDocumentedValidationException() {
        List<String> events = List.of("message.delivered");

        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().create("http://example.com/h", events));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().create(null, events));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().create("https://example.com/h", List.of()));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().create("https://example.com/h", null));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().update("whk_1", "http://example.com/h", null, null, null));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().get("hook_1"));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().retryDelivery("whk_1", "1"));
        assertThrows(com.sendly.exceptions.ValidationException.class,
                () -> client.webhooks().recoveryPreview("hook_1"));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testListEventTypes_readsTheEventsTheApiLists() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"events\":[{\"type\":\"message.sent\",\"description\":\"Message sent\"},"
                + "{\"type\":\"message.delivered\",\"description\":\"Message delivered\"}]}"
        ));

        assertEquals(List.of("message.sent", "message.delivered"), client.webhooks().listEventTypes());
        assertEquals("/api/v1/webhooks/event-types", mockServer.takeRequest().getPath());
    }

    @Test
    void testUpdate_sendsIsActive() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"whk_1\",\"url\":\"https://example.com/h\",\"events\":[\"message.delivered\"],"
                + "\"is_active\":false,\"created_at\":\"2026-09-25T00:00:00.000Z\","
                + "\"updated_at\":\"2026-09-25T00:00:01.000Z\"}"
        ));

        Webhook webhook = client.webhooks().update("whk_1", null, null, null, false);

        assertFalse(webhook.isActive());
        assertEquals("{\"is_active\":false}", mockServer.takeRequest().getBody().readUtf8());
    }

    @Test
    void testList_emptyArrayIsAnEmptyList() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("[]"));

        List<Webhook> webhooks = client.webhooks().list();

        assertTrue(webhooks.isEmpty());
    }

    private static final String RECOVERY_PREVIEW =
        "{\"circuitState\":\"open\",\"circuitOpenedAt\":\"2026-09-25T08:00:00.000Z\",\"failureCount\":12,"
            + "\"auditFailures\":{\"total\":3,\"byStatus\":{\"failed\":2,\"cancelled\":1},"
            + "\"byEventType\":{\"message.delivered\":2,\"message.opt_out\":1}},"
            + "\"missingFromMessages\":{\"total\":4,\"byEventType\":{\"message.sent\":4}},"
            + "\"histogram\":[{\"bucket\":\"2026-09-25T08:00:00.000Z\",\"missing\":4,\"failed\":3}],"
            + "\"samples\":[{\"id\":\"msg_1\",\"toMasked\":\"+1555***4567\",\"eventType\":\"message.delivered\","
            + "\"occurredAt\":\"2026-09-25T08:30:00.000Z\"}],"
            + "\"windowSuggestion\":{\"since\":\"2026-09-25T08:00:00.000Z\",\"until\":\"2026-09-25T10:00:00.000Z\"},"
            + "\"since\":\"2026-09-24T10:00:00.000Z\",\"until\":\"2026-09-25T10:00:00.000Z\"}";

    @Test
    void testRecoveryPreview_sendsTheWindowAndReadsTheCamelCasePreview() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(RECOVERY_PREVIEW));

        WebhookRecoveryPreview preview = client.webhooks().recoveryPreview("whk_1",
            new WebhooksResource.RecoveryPreviewOptions()
                .since("2026-09-24T10:00:00.000Z")
                .until("2026-09-25T10:00:00.000Z")
                .eventTypes(Arrays.asList("message.delivered", "message.failed")));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v1/webhooks/whk_1/recovery-preview", request.getRequestUrl().encodedPath());
        assertEquals("2026-09-24T10:00:00.000Z", request.getRequestUrl().queryParameter("since"));
        assertEquals("2026-09-25T10:00:00.000Z", request.getRequestUrl().queryParameter("until"));
        assertEquals("message.delivered,message.failed", request.getRequestUrl().queryParameter("event_types"));

        assertEquals("open", preview.getCircuitState());
        assertEquals("2026-09-25T08:00:00.000Z", preview.getCircuitOpenedAt());
        assertEquals(12, preview.getFailureCount());
        assertEquals(3, preview.getAuditFailures().getTotal());
        assertEquals(1, preview.getAuditFailures().getByStatus().get("cancelled"));
        assertEquals(1, preview.getAuditFailures().getByEventType().get("message.opt_out"));
        assertEquals(4, preview.getMissingFromMessages().getTotal());
        assertEquals(4, preview.getMissingFromMessages().getByEventType().get("message.sent"));
        assertEquals(1, preview.getHistogram().size());
        assertEquals(3, preview.getHistogram().get(0).getFailed());
        assertEquals("+1555***4567", preview.getSamples().get(0).getToMasked());
        assertEquals("2026-09-25T08:30:00.000Z", preview.getSamples().get(0).getOccurredAt());
        assertNotNull(preview.getWindowSuggestion());
        assertEquals("2026-09-25T08:00:00.000Z", preview.getWindowSuggestion().getSince());
        assertEquals("2026-09-25T10:00:00.000Z", preview.getUntil());
    }

    @Test
    void testRecoveryPreview_withoutOptionsSendsNoQuery() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(RECOVERY_PREVIEW
            .replace("\"circuitOpenedAt\":\"2026-09-25T08:00:00.000Z\"", "\"circuitOpenedAt\":null")
            .replaceAll("\"windowSuggestion\":\\{[^}]*\\}", "\"windowSuggestion\":null")));

        WebhookRecoveryPreview preview = client.webhooks().recoveryPreview("whk_1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/webhooks/whk_1/recovery-preview", request.getPath());
        assertNull(preview.getCircuitOpenedAt());
        assertNull(preview.getWindowSuggestion());
    }

    @Test
    void testCreate_sendsModeDescriptionAndMetadata() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"whk_1\",\"url\":\"https://example.com/h\",\"events\":[\"message.delivered\"],"
                + "\"description\":\"Orders\",\"mode\":\"test\",\"is_active\":true,"
                + "\"secret\":\"whsec_abc\",\"created_at\":\"2026-09-25T00:00:00.000Z\","
                + "\"updated_at\":\"2026-09-25T00:00:00.000Z\"}"
        ).setResponseCode(201));

        WebhookCreatedResponse webhook = client.webhooks().create("https://example.com/h",
            Arrays.asList("message.delivered"), "Orders", Webhook.MODE_TEST, Map.of("team", "ops"));

        assertEquals("whk_1", webhook.getId());
        assertEquals("test", webhook.getMode());
        assertEquals("whsec_abc", webhook.getSecret());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/webhooks", request.getPath());
        JsonObject body = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals("https://example.com/h", body.get("url").getAsString());
        assertEquals("Orders", body.get("description").getAsString());
        assertEquals("test", body.get("mode").getAsString());
        assertEquals("ops", body.getAsJsonObject("metadata").get("team").getAsString());
    }

    @Test
    void testCreate_withoutModeLeavesItOut() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"whk_1\",\"url\":\"https://example.com/h\",\"events\":[\"message.delivered\"],"
                + "\"is_active\":true,\"secret\":\"whsec_abc\"}"
        ).setResponseCode(201));

        client.webhooks().create("https://example.com/h", Arrays.asList("message.delivered"));

        JsonObject body = JsonParser.parseString(mockServer.takeRequest().getBody().readUtf8()).getAsJsonObject();
        assertFalse(body.has("mode"));
        assertFalse(body.has("description"));
        assertFalse(body.has("metadata"));
    }

    @Test
    void testUpdate_sendsModeAndMetadata() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"whk_1\",\"url\":\"https://example.com/h\",\"events\":[\"message.delivered\"],"
                + "\"mode\":\"live\",\"is_active\":true,\"created_at\":\"2026-09-25T00:00:00.000Z\","
                + "\"updated_at\":\"2026-09-25T00:00:01.000Z\"}"
        ));

        Webhook webhook = client.webhooks().update("whk_1", null, null, null, null,
            Webhook.MODE_LIVE, Map.of("team", "ops"));

        assertEquals("live", webhook.getMode());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertEquals("/api/v1/webhooks/whk_1", request.getPath());
        JsonObject body = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals(2, body.size());
        assertEquals("live", body.get("mode").getAsString());
        assertEquals("ops", body.getAsJsonObject("metadata").get("team").getAsString());
    }

    private static String storedWebhook(String metadata) {
        return "{\"id\":\"whk_placeholder1\",\"url\":\"https://localhost/sendly-contract-capture\","
            + "\"events\":[\"message.received\"],\"description\":\"Contract capture\",\"mode\":\"test\","
            + "\"is_active\":true,\"failure_count\":0,\"circuit_state\":\"closed\",\"api_version\":\"2024-01\","
            + "\"metadata\":" + metadata + ",\"created_at\":\"2026-01-01T00:00:00.000Z\","
            + "\"updated_at\":\"2026-01-01T00:00:00.000Z\",\"total_deliveries\":0,"
            + "\"successful_deliveries\":0,\"success_rate\":0,\"last_delivery_at\":null}";
    }

    @Test
    void testGet_readsObjectMetadata() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(storedWebhook("{\"purpose\":\"contract-capture\"}")));

        Webhook webhook = client.webhooks().get("whk_placeholder1");

        assertEquals("contract-capture", webhook.getMetadata().get("purpose"));
    }

    @Test
    void testList_readsMetadataThatIsNotAnObjectAsEmpty() throws Exception {
        for (String stored : Arrays.asList("[\"a\",1]", "\"note\"", "7", "true", "null")) {
            mockServer.enqueue(TestHelpers.mockSuccess("[" + storedWebhook(stored) + "]"));

            List<Webhook> webhooks = client.webhooks().list();

            assertEquals(1, webhooks.size(), stored);
            assertEquals("whk_placeholder1", webhooks.get(0).getId(), stored);
            assertTrue(webhooks.get(0).getMetadata().isEmpty(), stored);
        }
    }
}
