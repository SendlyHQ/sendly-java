package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.GroupMessageResponse;
import com.sendly.models.Message;
import com.sendly.models.MessageList;
import com.sendly.models.ListMessagesRequest;
import com.sendly.models.SendGroupMessageRequest;
import com.sendly.models.SendMessageRequest;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Messages resource - send, list, get, and iteration methods.
 */
class MessagesTest {
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

    // ==================== send() Method Tests ====================

    @Test
    void testSend_happyPath_withStrings() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", "Hello World", "sent")
        ));

        Message message = client.messages().send("+15551234567", "Hello World");

        assertNotNull(message);
        assertEquals("msg_123", message.getId());
        assertEquals("+15551234567", message.getTo());
        assertEquals("Hello World", message.getText());
        assertEquals("sent", message.getStatus());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/messages"));
        assertTrue(request.getBody().readUtf8().contains("Hello World"));
    }

    @Test
    void testSend_happyPath_withRequest() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_456", "+15559876543", "Test message", "queued")
        ));

        SendMessageRequest request = new SendMessageRequest("+15559876543", "Test message");
        Message message = client.messages().send(request);

        assertNotNull(message);
        assertEquals("msg_456", message.getId());
        assertEquals("+15559876543", message.getTo());
        assertEquals("Test message", message.getText());
        assertEquals("queued", message.getStatus());
    }

    @Test
    void testSend_invalidPhoneFormat_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send("1234567890", "Test");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().send("invalid", "Test");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().send(null, "Test");
        });
    }

    @Test
    void testSend_emptyText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send("+15551234567", "");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().send("+15551234567", null);
        });
    }

    @Test
    void testSend_tooLongText_throwsValidationException() {
        String longText = "a".repeat(1601);
        assertThrows(ValidationException.class, () -> {
            client.messages().send("+15551234567", longText);
        });
    }

    @Test
    void testSend_maxLengthText_succeeds() {
        String maxText = "a".repeat(1600);
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_123", "+15551234567", maxText, "sent")
        ));

        assertDoesNotThrow(() -> {
            client.messages().send("+15551234567", maxText);
        });
    }

    @Test
    void testSend_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });
    }

    @Test
    void testSend_402InsufficientCredits_throwsInsufficientCreditsException() {
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        assertThrows(InsufficientCreditsException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });
    }

    @Test
    void testSend_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(30));

        RateLimitException exception = assertThrows(RateLimitException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });

        assertEquals(30, exception.getRetryAfter());
    }

    @Test
    void testSend_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().send("+15551234567", "Test");
        });
    }

    @Test
    void testSend_networkError_throwsNetworkException() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl("http://localhost:1")
                .maxRetries(0);

        Sendly badClient = new Sendly("sk_test_123", builder);

        assertThrows(NetworkException.class, () -> {
            badClient.messages().send("+15551234567", "Test");
        });
    }

    // ==================== list() Method Tests ====================

    @Test
    void testList_happyPath_defaultParams() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(5, 0, true)
        ));

        MessageList list = client.messages().list();

        assertNotNull(list);
        assertEquals(5, list.size());
        assertEquals(100, list.getTotal());
        assertEquals(20, list.getLimit());
        assertEquals(0, list.getOffset());
        assertTrue(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/messages"));
    }

    @Test
    void testList_happyPath_withPagination() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(10, 20, true)
        ));

        ListMessagesRequest req = ListMessagesRequest.builder()
                .limit(10)
                .offset(20)
                .build();

        MessageList list = client.messages().list(req);

        assertNotNull(list);
        assertEquals(10, list.size());
        assertEquals(20, list.getOffset());
        assertTrue(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertTrue(path.contains("limit=10"));
        assertTrue(path.contains("offset=20"));
    }

    @Test
    void testList_happyPath_withFilters() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(3, 0, false)
        ));

        ListMessagesRequest req = ListMessagesRequest.builder()
                .status("sent")
                .to("+15551234567")
                .build();

        MessageList list = client.messages().list(req);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertFalse(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertTrue(path.contains("status=sent"));
        assertTrue(path.contains("to=%2B15551234567"));
    }

    @Test
    void testList_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(0, 0, false)
        ));

        MessageList list = client.messages().list();

        assertNotNull(list);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.hasMore());
        assertNull(list.first());
        assertNull(list.last());
    }

    @Test
    void testList_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().list();
        });
    }

    @Test
    void testList_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().list();
        });
    }

    @Test
    void testList_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(60));

        assertThrows(RateLimitException.class, () -> {
            client.messages().list();
        });
    }

    @Test
    void testList_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().list();
        });
    }

    // ==================== get() Method Tests ====================

    @Test
    void testGet_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"msg_123\",\"to\":\"+15551234567\",\"text\":\"Test\",\"status\":\"delivered\",\"credits_used\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\",\"updated_at\":\"2025-01-15T10:00:00.000Z\",\"delivered_at\":\"2025-01-15T10:01:00.000Z\"}}"
        ));

        Message message = client.messages().get("msg_123");

        assertNotNull(message);
        assertEquals("msg_123", message.getId());
        assertEquals("+15551234567", message.getTo());
        assertEquals("Test", message.getText());
        assertEquals("delivered", message.getStatus());
        assertTrue(message.isDelivered());
        assertNotNull(message.getDeliveredAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/messages/msg_123"));
    }

    @Test
    void testGet_nullId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().get(null);
        });
    }

    @Test
    void testGet_emptyId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().get("");
        });
    }

    @Test
    void testGet_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().get("msg_123");
        });
    }

    @Test
    void testGet_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().get("msg_nonexistent");
        });
    }

    @Test
    void testGet_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(45));

        assertThrows(RateLimitException.class, () -> {
            client.messages().get("msg_123");
        });
    }

    @Test
    void testGet_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().get("msg_123");
        });
    }

    // ==================== Wire Shape Tests ====================

    private static String v1MessagePage(int count, int offset, int total, int limit, boolean hasMore) {
        StringBuilder json = new StringBuilder("{\"data\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"id\":\"msg_%d\",\"to\":\"+15551234567\",\"from\":\"+15559876543\",\"text\":\"Test %d\","
                    + "\"status\":\"delivered\",\"direction\":\"outbound\",\"error\":null,\"errorCode\":null,"
                    + "\"retryCount\":0,\"segments\":1,\"creditsUsed\":2,\"isSandbox\":true,"
                    + "\"createdAt\":\"2026-09-25T00:00:00.000Z\",\"deliveredAt\":\"2026-09-25T00:00:02.000Z\","
                    + "\"message_format\":\"sms\",\"messageFormat\":\"sms\"}",
                offset + i, offset + i
            ));
        }
        json.append("],\"pagination\":{\"total\":").append(total)
            .append(",\"limit\":").append(limit)
            .append(",\"offset\":").append(offset)
            .append(",\"page\":").append(offset / limit + 1)
            .append(",\"totalPages\":").append((total + limit - 1) / limit)
            .append(",\"hasMore\":").append(hasMore)
            .append("},\"count\":").append(count).append("}");
        return json.toString();
    }

    @Test
    void testList_readsHasMoreTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(v1MessagePage(100, 0, 150, 100, true)));

        MessageList list = client.messages().list(ListMessagesRequest.builder().limit(100).build());

        assertTrue(list.hasMore());
        assertEquals(150, list.getTotal());
    }

    @Test
    void testEach_walksEveryPageTheApiReports() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(v1MessagePage(100, 0, 150, 100, true)));
        mockServer.enqueue(TestHelpers.mockSuccess(v1MessagePage(50, 100, 150, 100, false)));

        List<Message> messages = new ArrayList<>();
        for (Message message : client.messages().each()) {
            messages.add(message);
        }

        assertEquals(150, messages.size());
        assertEquals(2, mockServer.getRequestCount());
        assertTrue(mockServer.takeRequest().getPath().contains("offset=0"));
        assertTrue(mockServer.takeRequest().getPath().contains("offset=100"));
    }

    @Test
    void testEach_sendsTheRequestFiltersOnEveryPage() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(v1MessagePage(100, 0, 101, 100, true)));
        mockServer.enqueue(TestHelpers.mockSuccess(v1MessagePage(1, 100, 101, 100, false)));

        int seen = 0;
        for (Message ignored : client.messages().each(
                ListMessagesRequest.builder().status("failed").to("+15551234567").direction("inbound").build())) {
            seen++;
        }

        assertEquals(101, seen);
        for (int page = 0; page < 2; page++) {
            RecordedRequest request = mockServer.takeRequest();
            okhttp3.HttpUrl url = request.getRequestUrl();
            assertEquals("failed", url.queryParameter("status"));
            assertEquals("+15551234567", url.queryParameter("to"));
            assertTrue(request.getPath().contains("to=%2B15551234567"), request.getPath());
            assertEquals("inbound", url.queryParameter("direction"));
            assertEquals(String.valueOf(page * 100), url.queryParameter("offset"));
            assertEquals("100", url.queryParameter("limit"));
        }
    }

    @Test
    void testList_sendsTheDirectionFilter() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(v1MessagePage(0, 0, 0, 50, false)));

        client.messages().list(ListMessagesRequest.builder().direction("outbound").build());

        assertEquals("outbound", mockServer.takeRequest().getRequestUrl().queryParameter("direction"));
    }

    @Test
    void testGet_readsTheDirectionTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_in\",\"to\":\"+15559876543\",\"from\":\"+15551234567\",\"text\":\"STOP\","
                + "\"status\":\"received\",\"direction\":\"inbound\",\"error\":null}"
        ));

        assertEquals("inbound", client.messages().get("msg_in").getDirection());
    }

    @Test
    void testGet_readsTheFailureTextFromError() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_1\",\"to\":\"+15551234567\",\"status\":\"failed\",\"direction\":\"outbound\","
                + "\"error\":\"Carrier rejected the message\",\"errorCode\":\"30007\"}"
        ));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_2\",\"to\":\"+15551234567\",\"status\":\"delivered\",\"direction\":\"outbound\","
                + "\"error\":null,\"errorCode\":null}"
        ));

        Message failed = client.messages().get("msg_1");
        Message delivered = client.messages().get("msg_2");

        assertEquals("Carrier rejected the message", failed.getErrorMessage());
        assertEquals("30007", failed.getErrorCode());
        assertNull(delivered.getErrorMessage());
    }

    @Test
    void testList_derivesHasMoreWhenThePageOmitsIt() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[{\"id\":\"msg_1\"}],\"pagination\":{\"total\":2,\"limit\":1,\"offset\":0}}"
        ));

        assertTrue(client.messages().list().hasMore());
    }

    // ==================== each() Method Tests ====================

    @Test
    void testEach_happyPath_singlePage() {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(5, 0, false)
        ));

        List<Message> messages = new ArrayList<>();
        for (Message message : client.messages().each()) {
            messages.add(message);
        }

        assertEquals(5, messages.size());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testEach_happyPath_multiplePages() {
        // Page 1: 100 messages, has more
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(100, 0, true)
        ));
        // Page 2: 100 messages, has more
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(100, 100, true)
        ));
        // Page 3: 50 messages, no more
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(50, 200, false)
        ));

        List<Message> messages = new ArrayList<>();
        for (Message message : client.messages().each()) {
            messages.add(message);
        }

        assertEquals(250, messages.size());
        assertEquals(3, mockServer.getRequestCount());
    }

    @Test
    void testEach_happyPath_emptyResults() {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(0, 0, false)
        ));

        List<Message> messages = new ArrayList<>();
        for (Message message : client.messages().each()) {
            messages.add(message);
        }

        assertEquals(0, messages.size());
    }

    @Test
    void testEach_withRequest_usesFilters() {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(3, 0, false)
        ));

        ListMessagesRequest req = ListMessagesRequest.builder()
                .status("sent")
                .build();

        List<Message> messages = new ArrayList<>();
        for (Message message : client.messages().each(req)) {
            messages.add(message);
        }

        assertEquals(3, messages.size());
    }

    @Test
    void testEach_errorOnSecondPage_throwsException() {
        // First page succeeds
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(100, 0, true)
        ));
        // Second page fails
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(RuntimeException.class, () -> {
            List<Message> messages = new ArrayList<>();
            for (Message message : client.messages().each()) {
                messages.add(message);
            }
        });
    }

    @Test
    void testEach_401Unauthorized_throwsException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(RuntimeException.class, () -> {
            for (Message message : client.messages().each()) {
                // Should throw before iterating
            }
        });
    }

    @Test
    void testEach_429RateLimit_throwsException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(30));

        assertThrows(RuntimeException.class, () -> {
            for (Message message : client.messages().each()) {
                // Should throw before iterating
            }
        });
    }

    @Test
    void testEach_500ServerError_throwsException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(RuntimeException.class, () -> {
            for (Message message : client.messages().each()) {
                // Should throw before iterating
            }
        });
    }

    // ==================== Message Model Helper Tests ====================

    @Test
    void testMessage_statusHelpers() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_1", "+15551234567", "Test", "delivered")
        ));
        Message delivered = client.messages().send("+15551234567", "Test");
        assertTrue(delivered.isDelivered());
        assertFalse(delivered.isFailed());
        assertFalse(delivered.isPending());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_2", "+15551234567", "Test", "failed")
        ));
        Message failed = client.messages().send("+15551234567", "Test");
        assertTrue(failed.isFailed());
        assertFalse(failed.isDelivered());
        assertFalse(failed.isPending());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageJson("msg_3", "+15551234567", "Test", "queued")
        ));
        Message queued = client.messages().send("+15551234567", "Test");
        assertTrue(queued.isPending());
        assertFalse(queued.isDelivered());
        assertFalse(queued.isFailed());
    }

    @Test
    void testMessageList_firstLastMethods() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(5, 0, false)
        ));

        MessageList list = client.messages().list();

        assertNotNull(list.first());
        assertNotNull(list.last());
        assertEquals("msg_0", list.first().getId());
        assertEquals("msg_4", list.last().getId());
    }

    @Test
    void testMessageList_getMethods() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.messageListJson(5, 0, false)
        ));

        MessageList list = client.messages().list();

        assertEquals("msg_0", list.get(0).getId());
        assertEquals("msg_2", list.get(2).getId());
        assertEquals("msg_4", list.get(4).getId());
    }

    @Test
    void testSendGroup_liveSendListsRecipientsAsObjects() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"msg_g1\",\"status\":\"sent\",\"to\":["
            + "{\"phoneNumber\":\"+14155551234\",\"status\":\"queued\"},"
            + "{\"phoneNumber\":\"+14155555678\",\"status\":\"queued\"}],"
            + "\"group_message_id\":\"grp_1\"}").setResponseCode(201));

        GroupMessageResponse response = client.messages().sendGroup(
            new SendGroupMessageRequest(Arrays.asList("+14155551234", "+14155555678"), "Team sync at noon"));

        assertEquals("msg_g1", response.getId());
        assertEquals("grp_1", response.getGroupMessageId());
        assertEquals(Arrays.asList("+14155551234", "+14155555678"), response.getTo());
        assertEquals(2, response.getRecipients().size());
        assertEquals("+14155555678", response.getRecipients().get(1).getPhoneNumber());
        assertEquals("queued", response.getRecipients().get(1).getStatus());
    }

    @Test
    void testSendGroup_simulatedSendListsRecipientsAsNumbers() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"msg_g2\",\"status\":\"delivered\","
            + "\"to\":[\"+14155551234\",\"+14155555678\"],\"simulated\":true,"
            + "\"message\":\"Group message simulated (test key or verification pending).\"}").setResponseCode(201));

        GroupMessageResponse response = client.messages().sendGroup(
            new SendGroupMessageRequest(Arrays.asList("+14155551234", "+14155555678"), "Team sync at noon"));

        assertTrue(response.isSimulated());
        assertEquals(Arrays.asList("+14155551234", "+14155555678"), response.getTo());
        assertTrue(response.getRecipients().isEmpty());
    }
}
