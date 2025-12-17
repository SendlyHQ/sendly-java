package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.ScheduledMessage;
import com.sendly.models.ScheduledMessageList;
import com.sendly.models.ListScheduledMessagesRequest;
import com.sendly.models.ScheduleMessageRequest;
import com.sendly.models.CancelScheduledMessageResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Messages resource - scheduling methods.
 */
class MessagesScheduleTest {
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

    // ==================== schedule() Method Tests ====================

    @Test
    void testSchedule_happyPath_withStrings() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageJson("sch_123", "+15551234567", "Future message", "2025-01-20T10:00:00.000Z")
        ));

        ScheduledMessage message = client.messages().schedule(
            "+15551234567",
            "Future message",
            "2025-01-20T10:00:00Z"
        );

        assertNotNull(message);
        assertEquals("sch_123", message.getId());
        assertEquals("+15551234567", message.getTo());
        assertEquals("Future message", message.getText());
        assertEquals("scheduled", message.getStatus());
        assertTrue(message.isScheduled());
        assertNotNull(message.getScheduledAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/messages/schedule"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("Future message"));
        assertTrue(body.contains("2025-01-20T10:00:00Z"));
    }

    @Test
    void testSchedule_happyPath_withRequest() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageJson("sch_456", "+15559876543", "Scheduled test", "2025-02-01T15:30:00.000Z")
        ));

        ScheduleMessageRequest request = new ScheduleMessageRequest(
            "+15559876543",
            "Scheduled test",
            "2025-02-01T15:30:00Z"
        );
        ScheduledMessage message = client.messages().schedule(request);

        assertNotNull(message);
        assertEquals("sch_456", message.getId());
        assertEquals("+15559876543", message.getTo());
        assertEquals("Scheduled test", message.getText());
        assertEquals("scheduled", message.getStatus());
    }

    @Test
    void testSchedule_invalidPhoneFormat_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("1234567890", "Test", "2025-01-20T10:00:00Z");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("invalid", "Test", "2025-01-20T10:00:00Z");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().schedule(null, "Test", "2025-01-20T10:00:00Z");
        });
    }

    @Test
    void testSchedule_emptyText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", "", "2025-01-20T10:00:00Z");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", null, "2025-01-20T10:00:00Z");
        });
    }

    @Test
    void testSchedule_tooLongText_throwsValidationException() {
        String longText = "a".repeat(1601);
        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", longText, "2025-01-20T10:00:00Z");
        });
    }

    @Test
    void testSchedule_invalidScheduledAtFormat_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "invalid-date");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "2025/01/20 10:00:00");
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", "Test", null);
        });

        assertThrows(ValidationException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "");
        });
    }

    @Test
    void testSchedule_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "2025-01-20T10:00:00Z");
        });
    }

    @Test
    void testSchedule_402InsufficientCredits_throwsInsufficientCreditsException() {
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        assertThrows(InsufficientCreditsException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "2025-01-20T10:00:00Z");
        });
    }

    @Test
    void testSchedule_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(30));

        RateLimitException exception = assertThrows(RateLimitException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "2025-01-20T10:00:00Z");
        });

        assertEquals(30, exception.getRetryAfter());
    }

    @Test
    void testSchedule_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().schedule("+15551234567", "Test", "2025-01-20T10:00:00Z");
        });
    }

    @Test
    void testSchedule_networkError_throwsNetworkException() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl("http://localhost:1")
                .maxRetries(0);

        Sendly badClient = new Sendly("sk_test_123", builder);

        assertThrows(NetworkException.class, () -> {
            badClient.messages().schedule("+15551234567", "Test", "2025-01-20T10:00:00Z");
        });
    }

    // ==================== listScheduled() Method Tests ====================

    @Test
    void testListScheduled_happyPath_defaultParams() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageListJson(5, 0, true)
        ));

        ScheduledMessageList list = client.messages().listScheduled();

        assertNotNull(list);
        assertEquals(5, list.getData().size());
        assertEquals(50, list.getTotal());
        assertEquals(20, list.getLimit());
        assertEquals(0, list.getOffset());
        assertTrue(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/messages/scheduled"));
    }

    @Test
    void testListScheduled_happyPath_withPagination() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageListJson(10, 20, false)
        ));

        ListScheduledMessagesRequest req = ListScheduledMessagesRequest.builder()
                .limit(10)
                .offset(20)
                .build();

        ScheduledMessageList list = client.messages().listScheduled(req);

        assertNotNull(list);
        assertEquals(10, list.getData().size());
        assertEquals(20, list.getOffset());
        assertFalse(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertTrue(path.contains("limit=10"));
        assertTrue(path.contains("offset=20"));
    }

    @Test
    void testListScheduled_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageListJson(0, 0, false)
        ));

        ScheduledMessageList list = client.messages().listScheduled();

        assertNotNull(list);
        assertEquals(0, list.getData().size());
        assertTrue(list.getData().isEmpty());
        assertFalse(list.hasMore());
    }

    @Test
    void testListScheduled_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().listScheduled();
        });
    }

    @Test
    void testListScheduled_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().listScheduled();
        });
    }

    @Test
    void testListScheduled_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(60));

        assertThrows(RateLimitException.class, () -> {
            client.messages().listScheduled();
        });
    }

    @Test
    void testListScheduled_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().listScheduled();
        });
    }

    // ==================== getScheduled() Method Tests ====================

    @Test
    void testGetScheduled_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.scheduledMessageJson("sch_123", "+15551234567", "Future test", "2025-01-20T10:00:00.000Z")
        ));

        ScheduledMessage message = client.messages().getScheduled("sch_123");

        assertNotNull(message);
        assertEquals("sch_123", message.getId());
        assertEquals("+15551234567", message.getTo());
        assertEquals("Future test", message.getText());
        assertEquals("scheduled", message.getStatus());
        assertTrue(message.isScheduled());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/messages/scheduled/sch_123"));
    }

    @Test
    void testGetScheduled_nullId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().getScheduled(null);
        });
    }

    @Test
    void testGetScheduled_emptyId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().getScheduled("");
        });
    }

    @Test
    void testGetScheduled_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().getScheduled("sch_123");
        });
    }

    @Test
    void testGetScheduled_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().getScheduled("sch_nonexistent");
        });
    }

    @Test
    void testGetScheduled_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(45));

        assertThrows(RateLimitException.class, () -> {
            client.messages().getScheduled("sch_123");
        });
    }

    @Test
    void testGetScheduled_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().getScheduled("sch_123");
        });
    }

    // ==================== cancelScheduled() Method Tests ====================

    @Test
    void testCancelScheduled_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.cancelScheduledJson("sch_123", 1)
        ));

        CancelScheduledMessageResponse response = client.messages().cancelScheduled("sch_123");

        assertNotNull(response);
        assertEquals("sch_123", response.getId());
        assertEquals("cancelled", response.getStatus());
        assertEquals(1, response.getCreditsRefunded());
        assertNotNull(response.getCancelledAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertTrue(request.getPath().contains("/messages/scheduled/sch_123"));
    }

    @Test
    void testCancelScheduled_nullId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().cancelScheduled(null);
        });
    }

    @Test
    void testCancelScheduled_emptyId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().cancelScheduled("");
        });
    }

    @Test
    void testCancelScheduled_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().cancelScheduled("sch_123");
        });
    }

    @Test
    void testCancelScheduled_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().cancelScheduled("sch_nonexistent");
        });
    }

    @Test
    void testCancelScheduled_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(30));

        assertThrows(RateLimitException.class, () -> {
            client.messages().cancelScheduled("sch_123");
        });
    }

    @Test
    void testCancelScheduled_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().cancelScheduled("sch_123");
        });
    }

    @Test
    void testCancelScheduled_networkError_throwsNetworkException() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl("http://localhost:1")
                .maxRetries(0);

        Sendly badClient = new Sendly("sk_test_123", builder);

        assertThrows(NetworkException.class, () -> {
            badClient.messages().cancelScheduled("sch_123");
        });
    }

    // ==================== ScheduledMessage Model Helper Tests ====================

    @Test
    void testScheduledMessage_statusHelpers() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"sch_1\",\"to\":\"+15551234567\",\"text\":\"Test\",\"status\":\"scheduled\",\"scheduled_at\":\"2025-01-20T10:00:00.000Z\",\"credits_reserved\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\"}}"
        ));
        ScheduledMessage scheduled = client.messages().schedule("+15551234567", "Test", "2025-01-20T10:00:00Z");
        assertTrue(scheduled.isScheduled());
        assertFalse(scheduled.isSent());
        assertFalse(scheduled.isCancelled());
        assertFalse(scheduled.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"sch_2\",\"to\":\"+15551234567\",\"text\":\"Test\",\"status\":\"sent\",\"scheduled_at\":\"2025-01-20T10:00:00.000Z\",\"credits_reserved\":1,\"created_at\":\"2025-01-15T10:00:00.000Z\",\"sent_at\":\"2025-01-20T10:00:00.000Z\"}}"
        ));
        ScheduledMessage sent = client.messages().getScheduled("sch_2");
        assertTrue(sent.isSent());
        assertFalse(sent.isScheduled());
        assertFalse(sent.isCancelled());
        assertFalse(sent.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.cancelScheduledJson("sch_3", 1)
        ));
        CancelScheduledMessageResponse cancelled = client.messages().cancelScheduled("sch_3");
        assertEquals("cancelled", cancelled.getStatus());
    }
}
