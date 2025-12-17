package com.sendly;

import com.sendly.exceptions.AuthenticationException;
import com.sendly.exceptions.NetworkException;
import com.sendly.exceptions.RateLimitException;
import com.sendly.exceptions.SendlyException;
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
        assertEquals("application/json", request.getHeader("Content-Type"));
        assertEquals("application/json", request.getHeader("Accept"));
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
