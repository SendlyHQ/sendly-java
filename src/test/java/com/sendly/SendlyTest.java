package com.sendly;

import com.sendly.exceptions.AuthenticationException;
import com.sendly.exceptions.NetworkException;
import com.sendly.exceptions.RateLimitException;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.Message;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sendly client initialization and configuration.
 */
class SendlyTest {
    private MockWebServer mockServer;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // ==================== Happy Path Tests ====================

    @Test
    void testClientInitialization_withApiKey() {
        Sendly client = new Sendly("sk_test_123");
        assertNotNull(client);
        assertNotNull(client.messages());
    }

    @Test
    void testClientInitialization_withBuilder() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl("https://api.test.com")
                .timeout(Duration.ofSeconds(60))
                .maxRetries(5);

        Sendly client = new Sendly("sk_test_123", builder);
        assertNotNull(client);
        assertNotNull(client.messages());
    }

    @Test
    void testClientInitialization_withCustomTimeouts() {
        Sendly.Builder builder = new Sendly.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(20))
                .writeTimeout(Duration.ofSeconds(20));

        Sendly client = new Sendly("sk_test_123", builder);
        assertNotNull(client);
    }

    @Test
    void testClientInitialization_withCustomBaseUrl() throws Exception {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString());

        Sendly client = new Sendly("sk_test_123", builder);

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Test", "sent")
        ));

        client.messages().send("+15551234567", "Test");

        RecordedRequest request = mockServer.takeRequest();
        assertTrue(request.getPath().contains("/api/v1/messages"));
    }

    @Test
    void testClientSetsCorrectHeaders() throws Exception {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString());

        Sendly client = new Sendly("sk_test_123", builder);

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Test", "sent")
        ));

        client.messages().send("+15551234567", "Test");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("Bearer sk_test_123", request.getHeader("Authorization"));
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"));
        assertTrue(request.getHeader("Accept").startsWith("application/json"));
        assertTrue(request.getHeader("User-Agent").startsWith("sendly-java/"));
    }

    // ==================== Validation Error Tests ====================

    @Test
    void testClientInitialization_nullApiKey_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class, () -> {
            new Sendly(null);
        });
    }

    @Test
    void testClientInitialization_emptyApiKey_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class, () -> {
            new Sendly("");
        });
    }

    // ==================== HTTP Error Tests ====================

    @Test
    void testClient_401Unauthorized_throwsAuthenticationException() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(0);

        Sendly client = new Sendly("sk_invalid", builder);
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });
    }

    @Test
    void testClient_429RateLimit_throwsRateLimitException() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(0);

        Sendly client = new Sendly("sk_test_123", builder);
        mockServer.enqueue(TestHelpers.mockRateLimit(60));

        RateLimitException exception = assertThrows(RateLimitException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });

        assertEquals(60, exception.getRetryAfter());
        assertEquals(429, exception.getStatusCode());
    }

    @Test
    void testClient_500ServerError_retriesAndFails() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(2);

        Sendly client = new Sendly("sk_test_123", builder);

        // Enqueue 3 server errors (initial + 2 retries)
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });

        // Verify it made 3 attempts
        assertEquals(3, mockServer.getRequestCount());
    }

    @Test
    void testClient_500ServerError_retriesAndSucceeds() throws Exception {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(2);

        Sendly client = new Sendly("sk_test_123", builder);

        // First attempt fails, second succeeds
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Test", "sent")
        ));

        assertDoesNotThrow(() -> {
            client.messages().send("+15551234567", "Test");
        });

        // Verify it made 2 attempts
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testClient_rateLimitWithRetry_waitsAndRetries() throws Exception {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(1);

        Sendly client = new Sendly("sk_test_123", builder);

        // First request hits rate limit with 1 second retry
        mockServer.enqueue(TestHelpers.mockRateLimit(1));
        // Second request succeeds
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Test", "sent")
        ));

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> {
            client.messages().send("+15551234567", "Test");
        });
        long duration = System.currentTimeMillis() - startTime;

        // Should have waited at least 1 second
        assertTrue(duration >= 1000, "Should wait for retry-after duration");
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testClient_authenticationError_doesNotRetry() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3);

        Sendly client = new Sendly("sk_invalid", builder);
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });

        // Should not retry authentication errors
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testClient_validationError_doesNotRetry() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3);

        Sendly client = new Sendly("sk_test_123", builder);
        mockServer.enqueue(TestHelpers.mockValidationError("Invalid phone number"));

        assertThrows(Exception.class, () -> {
            client.messages().send("+15551234567", "Test");
        });

        // Should not retry validation errors
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testClient_notFoundError_doesNotRetry() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3);

        Sendly client = new Sendly("sk_test_123", builder);
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(Exception.class, () -> {
            client.messages().get("msg_nonexistent");
        });

        // Should not retry not found errors
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testClient_insufficientCredits_doesNotRetry() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3);

        Sendly client = new Sendly("sk_test_123", builder);
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        assertThrows(Exception.class, () -> {
            client.messages().send("+15551234567", "Test");
        });

        // Should not retry insufficient credits errors
        assertEquals(1, mockServer.getRequestCount());
    }

    // ==================== Non-JSON Response Tests ====================

    private static MockResponse htmlResponse(int code, String html) {
        return new MockResponse()
                .setResponseCode(code)
                .setBody(html)
                .addHeader("Content-Type", "text/html");
    }

    @Test
    void testClient_422_keepsItsStatus() {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString()));

        mockServer.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":\"idempotency_key_mismatch\",\"message\":\"This idempotency key was already used with a different request body. Use a new key for different requests.\"}"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> client.messages().get("msg_1"));

        assertEquals(422, exception.getStatusCode());
        assertEquals("idempotency_key_mismatch", exception.getApiErrorCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testClient_400_keepsItsStatus() {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString()));

        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":\"invalid_request\",\"message\":\"to is required\"}"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> client.messages().get("msg_1"));

        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void testClient_htmlGatewayPage_isRetried() throws Exception {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(1));

        mockServer.enqueue(htmlResponse(502, "<html>Bad gateway</html>"));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_1\",\"to\":\"+15551234567\",\"text\":\"Hi\",\"status\":\"delivered\",\"direction\":\"outbound\"}"
        ));

        Message message = client.messages().get("msg_1");

        assertEquals("msg_1", message.getId());
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testClient_htmlGatewayPage_surfacesAsSendlyException() {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(0));

        mockServer.enqueue(htmlResponse(524, "<!DOCTYPE html>\n<html><title>A timeout occurred</title></html>"));

        SendlyException exception = assertThrows(SendlyException.class, () -> client.messages().get("msg_1"));

        assertEquals(524, exception.getStatusCode());
        assertTrue(exception.getMessage().startsWith("HTTP 524"), exception.getMessage());
    }

    @Test
    void testClient_htmlBlockPage_isThrownAtOnce() {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3));

        mockServer.enqueue(htmlResponse(403,
            "<!DOCTYPE html>\n<html><head><title>Attention Required! | Cloudflare</title></head>"
                + "<body><h1>Sorry, you have been blocked</h1></body></html>"));

        SendlyException exception = assertTimeoutPreemptively(Duration.ofSeconds(5),
            () -> assertThrows(SendlyException.class, () -> client.messages().get("msg_1")));

        assertEquals(403, exception.getStatusCode());
        assertTrue(exception.getMessage().startsWith("HTTP 403"), exception.getMessage());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testClient_htmlPayloadTooLargePage_isThrownAtOnceOnAPost() {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3));

        mockServer.enqueue(new MockResponse()
                .setResponseCode(413)
                .setBody("<html><head><title>413 Request Entity Too Large</title></head>"
                    + "<body><center><h1>413 Request Entity Too Large</h1></center></body></html>")
                .addHeader("Content-Type", "text/html"));

        SendlyException exception = assertTimeoutPreemptively(Duration.ofSeconds(5),
            () -> assertThrows(SendlyException.class, () -> client.messages().send("+15551234567", "Test")));

        assertEquals(413, exception.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testClient_nonJsonSuccessBody_throwsSendlyExceptionWithoutRetrying() {
        Sendly client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(2));

        mockServer.enqueue(htmlResponse(200, "<html>Welcome</html>"));

        SendlyException exception = assertThrows(SendlyException.class, () -> client.messages().get("msg_1"));

        assertEquals("Invalid JSON response from API", exception.getMessage());
        assertEquals(200, exception.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    // ==================== Null Request Body Tests ====================

    private Sendly apiClient() {
        return new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(0));
    }

    private static MockResponse strictJsonParserRefusal() {
        return new MockResponse()
                .setResponseCode(400)
                .setBody("{\"message\":\"Unexpected token 'n', \\\"null\\\" is not valid JSON\"}")
                .addHeader("Content-Type", "application/json");
    }

    @Test
    void testPost_nullBody_sendsAnEmptyObject() throws Exception {
        Sendly client = apiClient();
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"draft_1\",\"status\":\"approved\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"tpl_1\",\"status\":\"published\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess("{\"code\":\"abc123\",\"shortUrl\":\"https://sendly.live/l/abc123\"}"));

        client.post("/drafts/draft_1/approve", null);
        client.request("POST", "/templates/tpl_1/publish", null, com.google.gson.JsonObject.class);
        client.postUnversioned("/api/links", null);

        for (int i = 0; i < 3; i++) {
            RecordedRequest request = mockServer.takeRequest();
            assertEquals("POST", request.getMethod());
            assertEquals("{}", request.getBody().readUtf8(), request.getPath());
        }
    }

    @Test
    void testPutAndPatch_nullBody_isNotTurnedIntoAnEmptyObject() throws Exception {
        Sendly client = apiClient();
        mockServer.enqueue(strictJsonParserRefusal());
        mockServer.enqueue(strictJsonParserRefusal());
        mockServer.enqueue(strictJsonParserRefusal());
        mockServer.enqueue(strictJsonParserRefusal());

        assertThrows(ValidationException.class, () -> client.put("/enterprise/settings/auto-top-up", null));
        assertThrows(ValidationException.class,
                () -> client.request("PUT", "/enterprise/settings/auto-top-up", null, com.google.gson.JsonObject.class));
        assertThrows(ValidationException.class, () -> client.patch("/account/keys/key_1/revoke", null));
        assertThrows(ValidationException.class, () -> client.patchUnversioned("/api/links/abc123", null));

        for (int i = 0; i < 4; i++) {
            RecordedRequest request = mockServer.takeRequest();
            assertEquals("null", request.getBody().readUtf8(), request.getMethod() + " " + request.getPath());
        }
    }

    // ==================== Network Error Tests ====================

    @Test
    void testClient_networkFailure_throwsNetworkException() throws Exception {
        // Create client with invalid URL to simulate network failure
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl("http://localhost:1")  // Invalid port
                .timeout(Duration.ofMillis(100))
                .maxRetries(0);

        Sendly client = new Sendly("sk_test_123", builder);

        assertThrows(NetworkException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });
    }

    @Test
    void testClient_emptyResponseBody_handledGracefully() throws Exception {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString());

        Sendly client = new Sendly("sk_test_123", builder);

        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        // Should not throw, should return empty JsonObject
        assertDoesNotThrow(() -> {
            client.get("/test", null);
        });
    }
}
