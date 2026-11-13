package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.NotFoundException;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.ListWorkspacesOptions;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

    @Test
    void testUploadVerificationDocument_withContentType_sendsItOnTheFilePart(@TempDir Path dir) throws Exception {
        File file = dir.resolve("registration").toFile();
        Files.write(file.toPath(), new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47});
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"url\":\"https://cdn.example.com/doc.png\",\"id\":\"upload_1\"}"));

        JsonObject result = client.enterprise().uploadVerificationDocument(file, null, "ver_1", "image/png");

        assertEquals("upload_1", result.get("id").getAsString());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/enterprise/verification-document/upload", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("filename=\"registration\""));
        assertTrue(body.contains("Content-Type: image/png"));
        assertFalse(body.contains("application/octet-stream"));
        assertTrue(body.contains("ver_1"));
    }

    @Test
    void testUploadVerificationDocument_withoutContentType_usesTheExtension(@TempDir Path dir) throws Exception {
        File file = dir.resolve("registration.pdf").toFile();
        Files.write(file.toPath(), "%PDF-1.4".getBytes());
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"url\":\"https://cdn.example.com/doc.pdf\",\"id\":\"upload_2\"}"));

        client.enterprise().uploadVerificationDocument(file);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertTrue(body.contains("Content-Type: application/pdf"));
    }

    @Test
    void testWorkspacesList_withOptionsSendsTheFilters() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"workspaces\":[],\"pagination\":{\"page\":2,\"limit\":25,\"total\":0,\"totalPages\":0}}"));

        client.enterprise().workspaces().list(ListWorkspacesOptions.builder()
            .page(2)
            .limit(25)
            .search("acme")
            .status("active")
            .verification("unverified")
            .sort("name_asc")
            .tags(Arrays.asList("tag_1", "tag_2"))
            .build());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v1/enterprise/workspaces", request.getRequestUrl().encodedPath());
        assertEquals("2", request.getRequestUrl().queryParameter("page"));
        assertEquals("25", request.getRequestUrl().queryParameter("limit"));
        assertEquals("acme", request.getRequestUrl().queryParameter("search"));
        assertEquals("active", request.getRequestUrl().queryParameter("status"));
        assertEquals("unverified", request.getRequestUrl().queryParameter("verification"));
        assertEquals("name_asc", request.getRequestUrl().queryParameter("sort"));
        assertEquals("tag_1,tag_2", request.getRequestUrl().queryParameter("tags"));
        assertNull(request.getRequestUrl().queryParameter("compact"));
    }

    @Test
    void testWorkspacesList_compactSendsOnlyCompact() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"workspaces\":[]}"));

        client.enterprise().workspaces().list(ListWorkspacesOptions.builder().compact(true).build());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("compact=true", request.getRequestUrl().encodedQuery());
    }

    private static final String FIXTURE_SUMMARY = "\"summary\":{\"totalCredits\":1500,"
        + "\"pendingVerifications\":[{\"id\":\"00000000-0000-4000-8000-000000000011\",\"name\":\"Bright Smiles Dental\"}],"
        + "\"verificationBreakdown\":{\"verified\":1,\"pending\":1,\"submitted\":0,\"rejected\":0,\"unverified\":0}},"
        + "\"maxWorkspaces\":25,\"workspacesUsed\":2";

    private static void assertFixtureSummary(JsonObject page) {
        JsonObject summary = page.getAsJsonObject("summary");
        assertEquals(1500, summary.get("totalCredits").getAsInt());
        JsonArray pending = summary.getAsJsonArray("pendingVerifications");
        assertEquals(1, pending.size());
        assertEquals("00000000-0000-4000-8000-000000000011", pending.get(0).getAsJsonObject().get("id").getAsString());
        assertEquals("Bright Smiles Dental", pending.get(0).getAsJsonObject().get("name").getAsString());
        JsonObject breakdown = summary.getAsJsonObject("verificationBreakdown");
        assertEquals(1, breakdown.get("verified").getAsInt());
        assertEquals(1, breakdown.get("pending").getAsInt());
        assertEquals(0, breakdown.get("unverified").getAsInt());
    }

    @Test
    void testWorkspacesList_readsTheFullListAndItsSummary() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"workspaces\":["
            + "{\"id\":\"00000000-0000-4000-8000-000000000011\",\"name\":\"Bright Smiles Dental\","
            + "\"slug\":\"bright-smiles-dental\",\"status\":\"active\",\"suspendedAt\":null,\"suspendReason\":null,"
            + "\"verificationStatus\":\"pending\",\"rejectionReason\":null,\"verificationType\":\"toll_free\","
            + "\"tollFreeNumber\":null,\"creditBalance\":0,\"keyCount\":1,\"messages30d\":0,\"delivered30d\":0,"
            + "\"failed30d\":0,\"monthlyMessageQuota\":null,\"messagesThisMonth\":0,\"quotaResetAt\":null,"
            + "\"createdAt\":\"2026-01-08T00:00:00.000Z\",\"tags\":[{\"id\":\"00000000-0000-4000-8000-000000000030\","
            + "\"enterpriseAccountId\":\"00000000-0000-4000-8000-000000000031\",\"name\":\"dental\","
            + "\"color\":\"#6366f1\",\"createdAt\":\"2026-01-01T00:00:00.000Z\"}]},"
            + "{\"id\":\"00000000-0000-4000-8000-000000000010\",\"name\":\"Acme Clinics\",\"slug\":\"acme-clinics\","
            + "\"status\":\"active\",\"suspendedAt\":null,\"suspendReason\":null,\"verificationStatus\":\"verified\","
            + "\"rejectionReason\":null,\"verificationType\":\"toll_free\",\"tollFreeNumber\":\"+15555550100\","
            + "\"creditBalance\":1500,\"keyCount\":1,\"messages30d\":120,\"delivered30d\":118,\"failed30d\":2,"
            + "\"monthlyMessageQuota\":null,\"messagesThisMonth\":40,\"quotaResetAt\":null,"
            + "\"createdAt\":\"2026-01-01T00:00:00.000Z\",\"tags\":[]}],"
            + "\"pagination\":{\"total\":2,\"limit\":50,\"page\":1,\"totalPages\":1,\"hasMore\":false},"
            + FIXTURE_SUMMARY + "}"));

        JsonObject page = client.enterprise().workspaces().list();

        assertEquals(2, page.getAsJsonArray("workspaces").size());
        assertEquals(2, page.getAsJsonObject("pagination").get("total").getAsInt());
        assertFixtureSummary(page);
    }

    @Test
    void testWorkspacesList_readsTheCompactListAndItsSummary() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"workspaces\":["
            + "{\"id\":\"00000000-0000-4000-8000-000000000011\",\"name\":\"Bright Smiles Dental\",\"creditBalance\":0},"
            + "{\"id\":\"00000000-0000-4000-8000-000000000010\",\"name\":\"Acme Clinics\",\"creditBalance\":1500}],"
            + FIXTURE_SUMMARY + "}"));

        JsonObject page = client.enterprise().workspaces().list(ListWorkspacesOptions.builder().compact(true).build());

        assertEquals(1500, page.getAsJsonArray("workspaces").get(1).getAsJsonObject().get("creditBalance").getAsInt());
        assertFalse(page.has("pagination"));
        assertFixtureSummary(page);
    }
}
