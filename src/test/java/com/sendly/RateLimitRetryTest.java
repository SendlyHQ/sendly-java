package com.sendly;

import com.google.gson.JsonObject;
import com.sendly.exceptions.RateLimitException;
import com.sendly.models.MediaFile;
import com.sendly.models.Message;
import com.sendly.models.SendVerificationRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Which 429s the client waits out and retries, and which it raises at once.
 */
class RateLimitRetryTest {
    private static final String MESSAGE =
        "{\"id\":\"msg_1\",\"to\":\"+15551234567\",\"text\":\"Hi\",\"status\":\"queued\",\"direction\":\"outbound\"}";

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

    private Sendly client(int maxRetries) {
        return new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(maxRetries));
    }

    private static MockResponse tooMany(String body, String retryAfterHeader) {
        MockResponse response = new MockResponse()
                .setResponseCode(429)
                .setBody(body)
                .addHeader("Content-Type", "application/json");
        return retryAfterHeader != null ? response.addHeader("Retry-After", retryAfterHeader) : response;
    }

    private static MockResponse keyLockout(String code, int retryAfter) {
        return tooMany("{\"error\":\"" + code + "\",\"message\":\"Too many failed API key attempts. Try again in "
                + retryAfter + " seconds.\",\"retryAfter\":" + retryAfter + "}", String.valueOf(retryAfter));
    }

    @Test
    void testFailedKeyLockout_isRaisedAtOnceWithItsCodeMessageAndRetryAfter() {
        Sendly client = client(3);
        mockServer.enqueue(keyLockout("too_many_failed_key_attempts", 300));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(RateLimitException.class, () -> client.messages().send("+15551234567", "Hi")));

        assertEquals("too_many_failed_key_attempts", e.getApiErrorCode());
        assertEquals("Too many failed API key attempts. Try again in 300 seconds.", e.getMessage());
        assertEquals(300, e.getRetryAfter());
        assertEquals(429, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testFailedKeyLockout_isNeverRetriedEvenWithAShortRetryAfter() {
        Sendly client = client(3);
        mockServer.enqueue(keyLockout("too_many_failed_key_attempts", 1));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(RateLimitException.class, () -> client.messages().get("msg_1")));

        assertEquals("too_many_failed_key_attempts", e.getApiErrorCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testLockoutBeforeTheNewCode_isRaisedAtOnceBecauseItsRetryAfterIsLong() {
        Sendly client = client(3);
        mockServer.enqueue(keyLockout("rate_limit_exceeded", 300));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(RateLimitException.class, () -> client.messages().get("msg_1")));

        assertEquals("rate_limit_exceeded", e.getApiErrorCode());
        assertEquals(300, e.getRetryAfter());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testConcurrentKeyCheck_isRetriedAfterItsRetryAfterWithTheSameIdempotencyKey() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(tooMany("{\"error\":\"too_many_concurrent_verifications\",\"message\":\"Too many API key "
                + "checks are already running for this account from this address. Try again in 1 second.\","
                + "\"retryAfter\":1}", "1"));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        long start = System.currentTimeMillis();
        Message message = client.messages().send("+15551234567", "Hi");
        long elapsed = System.currentTimeMillis() - start;

        assertEquals("msg_1", message.getId());
        assertTrue(elapsed >= 1000 && elapsed < 1900, "waited " + elapsed + " ms; expected only the 1 s retryAfter");
        RecordedRequest first = mockServer.takeRequest();
        RecordedRequest second = mockServer.takeRequest();
        assertNotNull(first.getHeader("Idempotency-Key"));
        assertEquals(first.getHeader("Idempotency-Key"), second.getHeader("Idempotency-Key"));
    }

    @Test
    void testOtpLimit_isRaisedAtOnceWithTheBodyRetryAfter() {
        Sendly client = client(3);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many OTPs sent to this phone "
                + "number. Max 5 per 10 minutes.\",\"retryAfter\":600}", null));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(
                RateLimitException.class,
                () -> client.verify().send(new SendVerificationRequest("+15551234567"))));

        assertEquals(600, e.getRetryAfter());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testDailyOtpLimit_isRaisedAtOnce() {
        Sendly client = client(3);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Daily OTP limit reached for this "
                + "phone number. Max 20 per day.\",\"retryAfter\":86400}", null));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(
                RateLimitException.class,
                () -> client.verify().send(new SendVerificationRequest("+15551234567"))));

        assertEquals(86400, e.getRetryAfter());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testOrdinaryRateLimit_isWaitedOutAndRetried() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded. Limit: 60 "
                + "requests per minute.\",\"retryAfter\":1}", "1"));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        long start = System.currentTimeMillis();
        Message message = client.messages().get("msg_1");
        long elapsed = System.currentTimeMillis() - start;

        assertEquals("msg_1", message.getId());
        assertTrue(elapsed >= 1000 && elapsed < 1900, "waited " + elapsed + " ms");
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testWaitedOutRateLimits_addNoBackoff() throws Exception {
        Sendly client = client(2);
        String body = "{\"error\":\"too_many_concurrent_verifications\",\"message\":\"Try again in 1 second.\","
                + "\"retryAfter\":1}";
        mockServer.enqueue(tooMany(body, "1"));
        mockServer.enqueue(tooMany(body, "1"));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        long start = System.currentTimeMillis();
        Message message = client.messages().get("msg_1");
        long elapsed = System.currentTimeMillis() - start;

        assertEquals("msg_1", message.getId());
        assertTrue(elapsed >= 2000 && elapsed < 2900, "waited " + elapsed + " ms; expected two 1 s retryAfters only");
        assertEquals(3, mockServer.getRequestCount());
    }

    @Test
    void testInterruptDuringRateLimitWait_throwsNetworkExceptionWithoutRetrying() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Slow down\",\"retryAfter\":30}",
                "30"));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        java.util.concurrent.atomic.AtomicReference<Throwable> thrown = new java.util.concurrent.atomic.AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                client.messages().get("msg_1");
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        caller.start();
        mockServer.takeRequest();
        Thread.sleep(300);
        caller.interrupt();
        caller.join(5000);

        assertFalse(caller.isAlive());
        assertInstanceOf(com.sendly.exceptions.NetworkException.class, thrown.get());
        assertEquals("Request interrupted", thrown.get().getMessage());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testRateLimitedCode_isRetried() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limited\",\"message\":\"Too many requests. Try again in a minute.\"}",
                null));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        Message message = client.messages().get("msg_1");

        assertEquals("msg_1", message.getId());
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testRateLimitWithoutACode_isWaitedOutAndRetried() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("<html><head><title>429 Too Many Requests</title></head><body>Rate limited</body></html>")
                .addHeader("Content-Type", "text/html")
                .addHeader("Retry-After", "1"));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        long start = System.currentTimeMillis();
        Message message = client.messages().get("msg_1");
        long elapsed = System.currentTimeMillis() - start;

        assertEquals("msg_1", message.getId());
        assertTrue(elapsed >= 1000 && elapsed < 1900, "waited " + elapsed + " ms");
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testRetryAfterComesFromTheBodyWhenTheHeaderIsMissing() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded. Limit: 60 "
                + "requests per minute.\",\"retryAfter\":2}", null));
        mockServer.enqueue(TestHelpers.mockSuccess(MESSAGE));

        long start = System.currentTimeMillis();
        client.messages().get("msg_1");
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed >= 2000 && elapsed < 2900, "waited " + elapsed + " ms; expected only the 2 s retryAfter");
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testRetryAfterHeaderThatIsADate_fallsBackToTheBody() {
        Sendly client = client(0);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Slow down\",\"retryAfter\":5}",
                "Wed, 21 Oct 2026 07:28:00 GMT"));

        RateLimitException e = assertThrows(RateLimitException.class, () -> client.messages().get("msg_1"));

        assertEquals(5, e.getRetryAfter());
    }

    @Test
    void testRateLimitWithNoRetriesLeft_isRaisedWithoutWaiting() {
        Sendly client = client(0);
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded. Limit: 60 "
                + "requests per minute.\",\"retryAfter\":30}", "30"));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(RateLimitException.class, () -> client.messages().get("msg_1")));

        assertEquals(30, e.getRetryAfter());
    }

    private static JsonObject workspace(String name) {
        JsonObject options = new JsonObject();
        options.addProperty("name", name);
        return options;
    }

    @Test
    void testProvisionPerMinuteLimit_isWaitedOutAndRetried() throws Exception {
        Sendly client = client(1);
        mockServer.enqueue(tooMany("{\"error\":\"provision_rate_limit\",\"message\":\"Max 120 provisions per minute.\","
                + "\"retryAfter\":1}", null));
        mockServer.enqueue(TestHelpers.mockSuccess("{\"workspace\":{\"id\":\"ws_1\",\"name\":\"Acme\",\"slug\":\"acme\"},"
                + "\"apiBaseUrl\":\"https://sendly.live\",\"dashboardUrl\":\"https://sendly.live/enterprise/workspaces/ws_1\"}")
                .setResponseCode(201));

        long start = System.currentTimeMillis();
        JsonObject result = client.enterprise().provision(workspace("Acme"));
        long elapsed = System.currentTimeMillis() - start;

        assertEquals("ws_1", result.getAsJsonObject("workspace").get("id").getAsString());
        assertTrue(elapsed >= 1000, "waited " + elapsed + " ms");
        RecordedRequest first = mockServer.takeRequest();
        RecordedRequest second = mockServer.takeRequest();
        assertEquals("/api/v1/enterprise/workspaces/provision", second.getPath());
        assertEquals(first.getHeader("Idempotency-Key"), second.getHeader("Idempotency-Key"));
    }

    @Test
    void testProvisionHourlyLimit_isRaisedAtOnce() {
        Sendly client = client(3);
        mockServer.enqueue(tooMany("{\"error\":\"provision_rate_limit\",\"message\":\"Max 1000 provisions per hour.\","
                + "\"retryAfter\":3600}", null));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(
                RateLimitException.class, () -> client.enterprise().provision(workspace("Acme"))));

        assertEquals("provision_rate_limit", e.getApiErrorCode());
        assertEquals(3600, e.getRetryAfter());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testMaxVerificationAttempts_isRaisedAtOnce() {
        Sendly client = client(3);
        mockServer.enqueue(tooMany("{\"error\":\"max_attempts_exceeded\",\"message\":\"Maximum verification attempts "
                + "exceeded\"}", null));

        RateLimitException e = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(
                RateLimitException.class, () -> client.verify().check("ver_1", "123456")));

        assertEquals("max_attempts_exceeded", e.getApiErrorCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testRetriedUploadResendsTheWholeFile() throws Exception {
        Sendly client = client(1);
        File file = Files.createTempFile("rate-limit-upload", ".jpg").toFile();
        file.deleteOnExit();
        Files.write(file.toPath(), "fake-image-bytes".getBytes());
        mockServer.enqueue(tooMany("{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded.\","
                + "\"retryAfter\":1}", "1"));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"med_1\",\"url\":\"https://cdn.example/x.jpg\",\"contentType\":\"image/jpeg\",\"sizeBytes\":16}"));

        MediaFile media = client.media().upload(file);

        assertEquals("med_1", media.getId());
        String first = mockServer.takeRequest().getBody().readUtf8();
        String second = mockServer.takeRequest().getBody().readUtf8();
        assertTrue(first.contains("fake-image-bytes"));
        assertEquals(first, second);
    }

    @Test
    void testRateLimitExceptionCarriesAnOptionalApiCode() {
        RateLimitException withCode = new RateLimitException("Locked", 300, "too_many_failed_key_attempts");
        RateLimitException withoutCode = new RateLimitException("Slow down", 5);

        assertEquals("too_many_failed_key_attempts", withCode.getApiErrorCode());
        assertEquals("RATE_LIMIT_EXCEEDED", withCode.getErrorCode());
        assertEquals(300, withCode.getRetryAfter());
        assertNull(withoutCode.getApiErrorCode());
    }
}
