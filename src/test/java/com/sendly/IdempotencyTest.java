package com.sendly;

import com.sendly.exceptions.ValidationException;
import com.sendly.models.BatchMessageItem;
import com.sendly.models.BatchMessageResponse;
import com.sendly.models.GroupMessageResponse;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.MediaFile;
import com.sendly.models.Message;
import com.sendly.models.RcsMessage;
import com.sendly.models.ScheduleMessageRequest;
import com.sendly.models.ScheduledMessage;
import com.sendly.models.SendBatchRequest;
import com.sendly.models.SendGroupMessageRequest;
import com.sendly.models.SendMessageRequest;
import com.sendly.models.SendRcsMessageRequest;
import com.sendly.models.SendWhatsAppMessageRequest;
import com.sendly.models.WhatsAppMessage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for automatic idempotency keys - generation, retry reuse, rotation.
 */
class IdempotencyTest {
    private static final String AUTO_KEY_REGEX =
        "^sendly-java-retry-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String AUTO_KEY_PREFIX = "sendly-java-retry-";

    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(0);

        client = new Sendly("sk_test_123", builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private Sendly retryingClient(int maxRetries) {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .readTimeout(Duration.ofMillis(500))
                .maxRetries(maxRetries);

        return new Sendly("sk_test_123", builder);
    }

    private static MockResponse mockTimeout() {
        return new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE);
    }

    private static String keyOf(RecordedRequest request) {
        return request.getHeader("Idempotency-Key");
    }

    private static String mediaFileJson() {
        return "{\"id\":\"med_x\",\"url\":\"https://cdn.example/x.jpg\",\"contentType\":\"image/jpeg\",\"sizeBytes\":16}";
    }

    private static String groupMessageJson() {
        return "{\"message\":{\"id\":\"msg_x\",\"group_message_id\":\"grp_x\",\"status\":\"queued\",\"to\":[\"+14155551234\",\"+14155555678\"]}}";
    }

    private static String whatsAppMessageJson() {
        return "{\"id\":\"msg_wa\",\"channel\":\"whatsapp\",\"to\":\"+15551234567\",\"from\":\"+15559876543\"," +
            "\"status\":\"queued\",\"whatsapp\":{\"kind\":\"text\"}}";
    }

    private static String rcsMessageJson() {
        return "{\"id\":\"msg_rcs\",\"channel\":\"rcs\",\"to\":\"+15551234567\",\"status\":\"queued\"}";
    }

    private File tempMediaFile() throws IOException {
        File file = Files.createTempFile("idempotency-test", ".jpg").toFile();
        file.deleteOnExit();
        Files.write(file.toPath(), "fake-image-bytes".getBytes());
        return file;
    }

    // ==================== Automatic Key Generation Tests ====================

    @Test
    void testSend_attachesAutoKeyToPost() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        client.messages().send(new SendMessageRequest("+15551234567", "Hello!"));

        RecordedRequest request = mockServer.takeRequest();
        String key = keyOf(request);
        assertNotNull(key);
        assertTrue(key.matches(AUTO_KEY_REGEX));
        assertTrue(key.length() <= 255);
    }

    @Test
    void testList_noKeyOnGet() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(2, 0, false)
        ));

        client.messages().list();

        RecordedRequest request = mockServer.takeRequest();
        assertNull(keyOf(request));
    }

    @Test
    void testCancelScheduled_noKeyOnDelete() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.cancelScheduledJson("sch_123", 1)
        ));

        client.messages().cancelScheduled("sch_123");

        RecordedRequest request = mockServer.takeRequest();
        assertNull(keyOf(request));
    }

    @Test
    void testSendBatch_noAutoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchResponseJson("batch_123", 1, 1, 0)
        ));

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Hi!")
        );
        client.messages().sendBatch(new SendBatchRequest(messages));

        RecordedRequest request = mockServer.takeRequest();
        assertNull(keyOf(request));
    }

    @Test
    void testMediaUpload_attachesAutoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(mediaFileJson()));

        MediaFile media = client.media().upload(tempMediaFile(), "image/jpeg");

        assertEquals("med_x", media.getId());
        RecordedRequest request = mockServer.takeRequest();
        String key = keyOf(request);
        assertNotNull(key);
        assertTrue(key.startsWith(AUTO_KEY_PREFIX));
    }

    @Test
    void testSend_distinctKeysAcrossLogicalRequests() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_1", "+15551234567", "First", "queued")
        ));
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_2", "+15551234567", "Second", "queued")
        ));

        client.messages().send(new SendMessageRequest("+15551234567", "First"));
        client.messages().send(new SendMessageRequest("+15551234567", "Second"));

        String first = keyOf(mockServer.takeRequest());
        String second = keyOf(mockServer.takeRequest());
        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }

    // ==================== Retry Behavior Tests ====================

    @Test
    void testSend_timeoutRetry_reusesKey() throws Exception {
        mockServer.enqueue(mockTimeout());
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        Sendly retrying = retryingClient(1);
        Message message = retrying.messages().send(new SendMessageRequest("+15551234567", "Hello!"));

        assertEquals("msg_123", message.getId());
        assertEquals(2, mockServer.getRequestCount());
        String first = keyOf(mockServer.takeRequest());
        String second = keyOf(mockServer.takeRequest());
        assertNotNull(first);
        assertEquals(first, second);
    }

    @Test
    void testSend_serverErrorRetry_rotatesAutoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        Sendly retrying = retryingClient(1);
        Message message = retrying.messages().send(new SendMessageRequest("+15551234567", "Hello!"));

        assertEquals("msg_123", message.getId());
        assertEquals(2, mockServer.getRequestCount());
        String first = keyOf(mockServer.takeRequest());
        String second = keyOf(mockServer.takeRequest());
        assertTrue(first.startsWith(AUTO_KEY_PREFIX));
        assertTrue(second.startsWith(AUTO_KEY_PREFIX));
        assertNotEquals(first, second);
    }

    @Test
    void testSend_serverErrorThenTimeout_keepsRotatedKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(mockTimeout());
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        Sendly retrying = retryingClient(2);
        Message message = retrying.messages().send(new SendMessageRequest("+15551234567", "Hello!"));

        assertEquals("msg_123", message.getId());
        assertEquals(3, mockServer.getRequestCount());
        String first = keyOf(mockServer.takeRequest());
        String second = keyOf(mockServer.takeRequest());
        String third = keyOf(mockServer.takeRequest());
        assertNotEquals(first, second);
        assertEquals(second, third);
    }

    @Test
    void testSend_non5xxRetry_keepsKey() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(409)
                .setBody("{\"message\":\"Resource busy\"}")
                .addHeader("Content-Type", "application/json"));
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        Sendly retrying = retryingClient(1);
        Message message = retrying.messages().send(new SendMessageRequest("+15551234567", "Hello!"));

        assertEquals("msg_123", message.getId());
        assertEquals(2, mockServer.getRequestCount());
        String first = keyOf(mockServer.takeRequest());
        String second = keyOf(mockServer.takeRequest());
        assertNotNull(first);
        assertEquals(first, second);
    }

    @Test
    void testMediaUpload_serverErrorRetry_rotatesAutoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(TestHelpers.mockSuccess(mediaFileJson()));

        Sendly retrying = retryingClient(1);
        retrying.media().upload(tempMediaFile(), "image/jpeg");

        assertEquals(2, mockServer.getRequestCount());
        String first = keyOf(mockServer.takeRequest());
        String second = keyOf(mockServer.takeRequest());
        assertTrue(first.startsWith(AUTO_KEY_PREFIX));
        assertTrue(second.startsWith(AUTO_KEY_PREFIX));
        assertNotEquals(first, second);
    }

    // ==================== Caller-Supplied Key Tests ====================

    @Test
    void testSend_callerKeySentVerbatim() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        client.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions("order-4821-shipped"));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("order-4821-shipped", keyOf(request));
    }

    @Test
    void testSend_callerKeyNeverRotatedAcross5xxRetry() throws Exception {
        mockServer.enqueue(TestHelpers.mockServerError());
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        Sendly retrying = retryingClient(1);
        retrying.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions("order-4821-shipped"));

        assertEquals(2, mockServer.getRequestCount());
        assertEquals("order-4821-shipped", keyOf(mockServer.takeRequest()));
        assertEquals("order-4821-shipped", keyOf(mockServer.takeRequest()));
    }

    @Test
    void testSend_callerKeyReusedAcrossTimeoutRetry() throws Exception {
        mockServer.enqueue(mockTimeout());
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        Sendly retrying = retryingClient(1);
        retrying.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions("signup-otp-user-99"));

        assertEquals(2, mockServer.getRequestCount());
        assertEquals("signup-otp-user-99", keyOf(mockServer.takeRequest()));
        assertEquals("signup-otp-user-99", keyOf(mockServer.takeRequest()));
    }

    @Test
    void testSendBatch_callerKeySent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchResponseJson("batch_123", 1, 1, 0)
        ));

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Hi!")
        );
        BatchMessageResponse response = client.messages()
                .sendBatch(new SendBatchRequest(messages), new IdempotentRequestOptions("campaign-77-wave-1"));

        assertEquals("batch_123", response.getBatchId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("campaign-77-wave-1", keyOf(request));
    }

    @Test
    void testSchedule_callerKeySent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageJson("sch_123", "+15551234567", "Reminder!", "2030-01-20T10:00:00.000Z")
        ));

        ScheduledMessage scheduled = client.messages().schedule(
            new ScheduleMessageRequest("+15551234567", "Reminder!", "2030-01-20T10:00:00Z"),
            new IdempotentRequestOptions("reminder-visit-31")
        );

        assertEquals("sch_123", scheduled.getId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("reminder-visit-31", keyOf(request));
    }

    @Test
    void testSendGroup_callerKeySent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(groupMessageJson()));

        GroupMessageResponse response = client.messages().sendGroup(
            new SendGroupMessageRequest(Arrays.asList("+14155551234", "+14155555678"), "Team sync at noon"),
            new IdempotentRequestOptions("standup-ping-0823")
        );

        assertEquals("grp_x", response.getGroupMessageId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("standup-ping-0823", keyOf(request));
    }

    @Test
    void testSendWhatsApp_callerKeySent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(whatsAppMessageJson()));

        WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+15551234567")
                .from("+15559876543")
                .text("Hello!")
                .build(), new IdempotentRequestOptions("wa-hello-1"));

        assertEquals("msg_wa", message.getId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("wa-hello-1", keyOf(request));
    }

    @Test
    void testSendRcs_callerKeySent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(rcsMessageJson()));

        RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
                .to("+15551234567")
                .text("Hello!")
                .build(), new IdempotentRequestOptions("rcs-hello-1"));

        assertEquals("msg_rcs", message.getId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("rcs-hello-1", keyOf(request));
    }

    @Test
    void testSend_emptyCallerKey_fallsBackToAutoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        client.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions(""));

        RecordedRequest request = mockServer.takeRequest();
        String key = keyOf(request);
        assertNotNull(key);
        assertTrue(key.startsWith(AUTO_KEY_PREFIX));
    }

    @Test
    void testSend_whitespaceCallerKey_fallsBackToAutoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello!", "queued")
        ));

        client.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions("   "));

        RecordedRequest request = mockServer.takeRequest();
        String key = keyOf(request);
        assertNotNull(key);
        assertTrue(key.startsWith(AUTO_KEY_PREFIX));
    }

    @Test
    void testSend_nonAsciiCallerKey_throwsImmediatelyWithoutNetworkCall() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions("Заказ-42"));
        });

        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testSend_tooLongCallerKey_throwsImmediatelyWithoutNetworkCall() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(new SendMessageRequest("+15551234567", "Hello!"), new IdempotentRequestOptions("k".repeat(256)));
        });

        assertEquals(0, mockServer.getRequestCount());
    }
}
