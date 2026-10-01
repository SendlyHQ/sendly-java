package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.NotFoundException;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the enterprise resource against the shapes the API sends.
 */
class EnterpriseTest {
    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(0);

        client = new Sendly("sk_live_123", builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testListKeys_readsTheBareArrayTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "[{\"id\":\"key_1\",\"name\":\"k\",\"keyPrefix\":\"sk_test_v1_ab\",\"type\":\"test\","
                + "\"scopes\":[\"sms:send\"],\"lastUsedAt\":null,\"createdAt\":\"2026-09-25T00:00:00.000Z\"}]"
        ));

        List<JsonObject> keys = client.enterprise().workspaces().listKeys("ws_1");

        assertEquals(1, keys.size());
        assertEquals("key_1", keys.get(0).get("id").getAsString());
        assertEquals("/api/v1/enterprise/workspaces/ws_1/keys", mockServer.takeRequest().getPath());
    }

    private void answerLikeTheCreateKeyRoute() {
        mockServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                JsonObject body = JsonParser.parseString(request.getBody().clone().readUtf8()).getAsJsonObject();
                if (!body.has("name") || body.get("name").getAsString().isEmpty()) {
                    return new MockResponse().setResponseCode(400).setBody("{\"error\":\"name is required\"}");
                }
                String type = body.has("type") ? body.get("type").getAsString() : "test";
                return new MockResponse().setResponseCode(201).setBody(
                    "{\"id\":\"key_1\",\"name\":" + body.get("name") + ",\"key\":\"sk_" + type + "_v1_x\","
                        + "\"keyPrefix\":\"sk_" + type + "_v1_x\",\"type\":\"" + type + "\"}");
            }
        });
    }

    @Test
    void testCreateKey_withoutANameSendsOne() throws Exception {
        answerLikeTheCreateKeyRoute();

        JsonObject key = client.enterprise().workspaces().createKey("ws_1");

        assertEquals("key_1", key.get("id").getAsString());
        JsonObject body = JsonParser.parseString(mockServer.takeRequest().getBody().readUtf8()).getAsJsonObject();
        assertEquals("API key", body.get("name").getAsString());
    }

    @Test
    void testCreateKey_emptyNameSendsTheDefaultAndKeepsTheType() throws Exception {
        answerLikeTheCreateKeyRoute();

        JsonObject key = client.enterprise().workspaces().createKey("ws_1", "", "live");

        assertEquals("live", key.get("type").getAsString());
        JsonObject body = JsonParser.parseString(mockServer.takeRequest().getBody().readUtf8()).getAsJsonObject();
        assertEquals("API key", body.get("name").getAsString());
        assertEquals("live", body.get("type").getAsString());
    }

    @Test
    void testCreateKey_sendsTheGivenName() throws Exception {
        answerLikeTheCreateKeyRoute();

        client.enterprise().workspaces().createKey("ws_1", "Production", null);

        JsonObject body = JsonParser.parseString(mockServer.takeRequest().getBody().readUtf8()).getAsJsonObject();
        assertEquals("Production", body.get("name").getAsString());
        assertFalse(body.has("type"));
    }

    @Test
    void testInheritVerification_canOrderANewNumber() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(201).setBody(
            "{\"verificationId\":\"bv_2\",\"status\":\"pending\",\"type\":\"toll_free\","
                + "\"tollFreeNumber\":\"+18885550100\",\"inheritedFrom\":\"org_src\",\"newNumber\":true}"));

        JsonObject result = client.enterprise().workspaces().inheritVerification("ws_1", "org_src", true);

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/enterprise/workspaces/ws_1/verification/inherit", request.getPath());
        assertEquals("{\"sourceWorkspaceId\":\"org_src\",\"purchaseNewNumber\":true}", request.getBody().readUtf8());
        assertTrue(result.get("newNumber").getAsBoolean());
        assertEquals("+18885550100", result.get("tollFreeNumber").getAsString());
    }

    @Test
    void testInheritVerification_withoutTheFlagOmitsIt() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(201).setBody(
            "{\"verificationId\":\"bv_2\",\"status\":\"approved\",\"type\":\"toll_free\",\"inheritedFrom\":\"org_src\"}"));
        mockServer.enqueue(new MockResponse().setResponseCode(201).setBody(
            "{\"verificationId\":\"bv_3\",\"status\":\"approved\",\"type\":\"toll_free\",\"inheritedFrom\":\"org_src\"}"));

        client.enterprise().workspaces().inheritVerification("ws_1", "org_src");
        client.enterprise().workspaces().inheritVerification("ws_1", "org_src", false);

        assertEquals("{\"sourceWorkspaceId\":\"org_src\"}", mockServer.takeRequest().getBody().readUtf8());
        assertEquals("{\"sourceWorkspaceId\":\"org_src\"}", mockServer.takeRequest().getBody().readUtf8());
    }

    @Test
    void testAnalyticsDelivery_emptyArrayIsWrappedInData() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("[]"));

        JsonObject delivery = client.enterprise().analytics().delivery();

        assertEquals(0, delivery.getAsJsonArray("data").size());
    }

    @Test
    void testErrorWithOnlyAnErrorSentence_isTheExceptionMessage() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"error\":\"sourceWorkspaceId is required\"}")
                .addHeader("Content-Type", "application/json"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> client.enterprise().workspaces().transferCredits("ws_1", "ws_2", 10));

        assertEquals("sourceWorkspaceId is required", exception.getMessage());
        assertEquals("sourceWorkspaceId is required", exception.getApiErrorCode());
    }

    @Test
    void testErrorWithNeitherMessageNorError_namesTheStatus() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"success\":false}")
                .addHeader("Content-Type", "application/json"));

        SendlyException exception = assertThrows(SendlyException.class,
                () -> client.enterprise().workspaces().get("ws_1"));

        assertEquals("HTTP 403", exception.getMessage());
        assertEquals(403, exception.getStatusCode());
    }

    @Test
    void testErrorWithMessage_keepsTheMessage() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"workspace_not_found\",\"message\":\"Workspace not found\"}")
                .addHeader("Content-Type", "application/json"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> client.enterprise().workspaces().get("ws_1"));

        assertEquals("Workspace not found", exception.getMessage());
        assertEquals("workspace_not_found", exception.getApiErrorCode());
    }

    @Test
    void testArrayListsAreWrappedInData() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "[{\"workspaceId\":\"ws_1\",\"workspaceName\":\"Acme\",\"totalMessages\":4,\"delivered\":3,\"deliveryRate\":75,"
                + "\"name\":\"Acme\",\"sent\":4,\"failed\":1,\"rate\":75}]"
        ));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "[{\"id\":\"page_1\",\"slug\":\"acme\",\"url\":\"https://sendly.live/opt-in/acme\",\"businessName\":\"Acme\"}]"
        ));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "[{\"id\":\"whk_1\",\"url\":\"https://example.com/h\",\"events\":[\"message.delivered\"],\"isActive\":true}]"
        ));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "[{\"id\":\"inv_1\",\"email\":\"dev@example.com\",\"role\":\"member\",\"status\":\"pending\"}]"
        ));

        JsonObject delivery = client.enterprise().analytics().delivery();
        JsonObject pages = client.enterprise().workspaces().listOptInPages("ws_1");
        JsonObject webhooks = client.enterprise().workspaces().listWebhooks("ws_1");
        JsonObject invitations = client.enterprise().workspaces().listInvitations("ws_1");

        assertEquals("ws_1", delivery.getAsJsonArray("data").get(0).getAsJsonObject().get("workspaceId").getAsString());
        assertEquals(4, delivery.getAsJsonArray("data").get(0).getAsJsonObject().get("totalMessages").getAsInt());
        assertEquals("page_1", pages.getAsJsonArray("data").get(0).getAsJsonObject().get("id").getAsString());
        assertEquals("whk_1", webhooks.getAsJsonArray("data").get(0).getAsJsonObject().get("id").getAsString());
        assertEquals("inv_1", invitations.getAsJsonArray("data").get(0).getAsJsonObject().get("id").getAsString());
    }

    private static JsonArray workspaces(int count) {
        JsonArray workspaces = new JsonArray();
        for (int i = 0; i < count; i++) {
            JsonObject workspace = new JsonObject();
            workspace.addProperty("name", "Workspace " + i);
            workspaces.add(workspace);
        }
        return workspaces;
    }

    @Test
    void testProvisionBulk_sendsUpToTheApiLimitOf100() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"jobId\":\"job_1\",\"status\":\"queued\"}").setResponseCode(202));

        JsonObject result = client.enterprise().workspaces().provisionBulk(workspaces(100));

        assertEquals("job_1", result.get("jobId").getAsString());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/enterprise/workspaces/provision/bulk", request.getPath());
        assertEquals(100, JsonParser.parseString(request.getBody().readUtf8())
                .getAsJsonObject().getAsJsonArray("workspaces").size());
    }

    @Test
    void testProvisionBulk_refusesMoreThan100BeforeSending() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> client.enterprise().workspaces().provisionBulk(workspaces(101)));

        assertEquals("Maximum 100 workspaces per bulk provision", e.getMessage());
        assertEquals(0, mockServer.getRequestCount());
    }
}
