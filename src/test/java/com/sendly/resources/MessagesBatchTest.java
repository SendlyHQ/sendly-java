package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.*;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Messages resource - batch operations methods.
 */
class MessagesBatchTest {
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

    // ==================== sendBatch() Method Tests ====================

    @Test
    void testSendBatch_happyPath_allSucceed() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchResponseJson("batch_123", 3, 3, 0)
        ));

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Message 1"),
            new BatchMessageItem("+15551234568", "Message 2"),
            new BatchMessageItem("+15551234569", "Message 3")
        );

        SendBatchRequest request = new SendBatchRequest(messages);
        BatchMessageResponse response = client.messages().sendBatch(request);

        assertNotNull(response);
        assertEquals("batch_123", response.getBatchId());
        assertEquals("completed", response.getStatus());
        assertEquals(3, response.getTotal());
        assertEquals(3, response.getSent());
        assertEquals(0, response.getFailed());
        assertEquals(3, response.getCreditsUsed());
        // The send payload carries no queued count and no timestamps.
        assertEquals(0, response.getQueued());
        assertNull(response.getCreatedAt());
        assertTrue(response.isCompleted());
        assertFalse(response.isPartialFailure());
        assertFalse(response.isFailed());

        RecordedRequest req = mockServer.takeRequest();
        assertEquals("POST", req.getMethod());
        assertTrue(req.getPath().contains("/messages/batch"));
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("Message 1"));
        assertTrue(body.contains("Message 2"));
        assertTrue(body.contains("Message 3"));
    }

    @Test
    void testSendBatch_happyPath_partialSuccess() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchResponseJson("batch_456", 5, 3, 2)
        ));

        // Use valid phone numbers - the mock server response simulates server-side failures
        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Message 1"),
            new BatchMessageItem("+15551234568", "Message 2"),
            new BatchMessageItem("+15551234569", "Message 3"),
            new BatchMessageItem("+15551234570", "Message 4"),
            new BatchMessageItem("+15551234571", "Message 5")
        );

        SendBatchRequest request = new SendBatchRequest(messages);
        BatchMessageResponse response = client.messages().sendBatch(request);

        assertNotNull(response);
        assertEquals("batch_456", response.getBatchId());
        assertEquals(5, response.getTotal());
        assertEquals(3, response.getSent());
        assertEquals(2, response.getFailed());
        assertEquals(5, response.getMessages().size());
        assertTrue(response.isPartialFailure());

        // Check individual message results - first 3 succeed, last 2 fail (per mock)
        List<BatchMessageResult> results = response.getMessages();
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertTrue(results.get(2).isSuccess());
        assertTrue(results.get(3).isFailed());
        assertNotNull(results.get(3).getError());
    }

    @Test
    void testSendBatch_emptyMessages_throwsValidationException() {
        List<BatchMessageItem> emptyList = Arrays.asList();
        SendBatchRequest request = new SendBatchRequest(emptyList);

        assertThrows(ValidationException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_nullMessages_throwsValidationException() {
        SendBatchRequest request = new SendBatchRequest(null);

        assertThrows(ValidationException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_invalidPhoneInBatch_throwsValidationException() {
        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Valid"),
            new BatchMessageItem("invalid", "Invalid phone")
        );

        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(ValidationException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_emptyTextInBatch_throwsValidationException() {
        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Valid"),
            new BatchMessageItem("+15551234568", "")
        );

        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(ValidationException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_tooLongTextInBatch_throwsValidationException() {
        String longText = "a".repeat(1601);
        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Valid"),
            new BatchMessageItem("+15551234568", longText)
        );

        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(ValidationException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Test")
        );
        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(AuthenticationException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_402InsufficientCredits_throwsInsufficientCreditsException() {
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Test")
        );
        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(InsufficientCreditsException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(30));

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Test")
        );
        SendBatchRequest request = new SendBatchRequest(messages);

        RateLimitException exception = assertThrows(RateLimitException.class, () -> {
            client.messages().sendBatch(request);
        });

        assertEquals(30, exception.getRetryAfter());
    }

    @Test
    void testSendBatch_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Test")
        );
        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(SendlyException.class, () -> {
            client.messages().sendBatch(request);
        });
    }

    @Test
    void testSendBatch_networkError_throwsNetworkException() {
        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl("http://localhost:1")
                .maxRetries(0);

        Sendly badClient = new Sendly("sk_test_123", builder);

        List<BatchMessageItem> messages = Arrays.asList(
            new BatchMessageItem("+15551234567", "Test")
        );
        SendBatchRequest request = new SendBatchRequest(messages);

        assertThrows(NetworkException.class, () -> {
            badClient.messages().sendBatch(request);
        });
    }

    // ==================== getBatch() Method Tests ====================

    @Test
    void testGetBatch_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchStatusJson("batch_123", 10, 10, 0)
        ));

        BatchMessageResponse response = client.messages().getBatch("batch_123");

        assertNotNull(response);
        // The fetch payload identifies the batch as "id", not "batchId".
        assertEquals("batch_123", response.getBatchId());
        assertEquals("completed", response.getStatus());
        assertEquals(10, response.getTotal());
        assertEquals(10, response.getQueued());
        assertEquals(0, response.getFailed());
        assertEquals(10, response.getCreditsUsed());
        assertNotNull(response.getCreatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/messages/batch/batch_123"));
    }

    @Test
    void testGetBatch_nullId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().getBatch(null);
        });
    }

    @Test
    void testGetBatch_emptyId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().getBatch("");
        });
    }

    @Test
    void testGetBatch_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().getBatch("batch_123");
        });
    }

    @Test
    void testGetBatch_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().getBatch("batch_nonexistent");
        });
    }

    @Test
    void testGetBatch_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(45));

        assertThrows(RateLimitException.class, () -> {
            client.messages().getBatch("batch_123");
        });
    }

    @Test
    void testGetBatch_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().getBatch("batch_123");
        });
    }

    // ==================== listBatches() Method Tests ====================

    @Test
    void testListBatches_happyPath_defaultParams() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchListJson(5, 0, true)
        ));

        BatchList list = client.messages().listBatches();

        assertNotNull(list);
        assertEquals(5, list.getData().size());
        assertEquals(30, list.getTotal());
        assertEquals(20, list.getLimit());
        assertEquals(0, list.getOffset());
        assertTrue(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/messages/batches"));
    }

    @Test
    void testListBatches_happyPath_withPagination() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchListJson(10, 10, false)
        ));

        ListBatchesRequest req = ListBatchesRequest.builder()
                .limit(10)
                .offset(10)
                .build();

        BatchList list = client.messages().listBatches(req);

        assertNotNull(list);
        assertEquals(10, list.getData().size());
        assertEquals(10, list.getOffset());
        assertFalse(list.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertTrue(path.contains("limit=10"));
        assertTrue(path.contains("offset=10"));
    }

    @Test
    void testListBatches_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchListJson(0, 0, false)
        ));

        BatchList list = client.messages().listBatches();

        assertNotNull(list);
        assertEquals(0, list.getData().size());
        assertTrue(list.getData().isEmpty());
        assertFalse(list.hasMore());
    }

    @Test
    void testListBatches_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.messages().listBatches();
        });
    }

    @Test
    void testListBatches_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.messages().listBatches();
        });
    }

    @Test
    void testListBatches_429RateLimit_throwsRateLimitException() {
        mockServer.enqueue(TestHelpers.mockRateLimit(60));

        assertThrows(RateLimitException.class, () -> {
            client.messages().listBatches();
        });
    }

    @Test
    void testListBatches_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.messages().listBatches();
        });
    }

    // ==================== BatchMessageResponse Model Helper Tests ====================

    @Test
    void testBatchResponse_statusHelpers() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"batch_1\",\"status\":\"processing\",\"total\":5,\"queued\":5,\"sent\":0,\"delivered\":0,\"failed\":0,"
                + "\"creditsReserved\":5,\"creditsUsed\":0,\"creditsRefunded\":0,\"messages\":[],"
                + "\"createdAt\":\"2025-01-15T10:00:00.000Z\",\"completedAt\":null}"
        ));
        BatchMessageResponse processing = client.messages().getBatch("batch_1");
        assertEquals("batch_1", processing.getBatchId());
        assertTrue(processing.isProcessing());
        assertFalse(processing.isCompleted());
        assertFalse(processing.isPartialFailure());
        assertFalse(processing.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchStatusJson("batch_2", 5, 5, 0)
        ));
        BatchMessageResponse completed = client.messages().getBatch("batch_2");
        assertTrue(completed.isCompleted());
        assertFalse(completed.isProcessing());
        assertFalse(completed.isPartialFailure());
        assertFalse(completed.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchStatusJson("batch_3", 5, 3, 2)
        ));
        BatchMessageResponse partial = client.messages().getBatch("batch_3");
        assertTrue(partial.isPartialFailure());
        assertFalse(partial.isCompleted());
        assertFalse(partial.isProcessing());
        assertFalse(partial.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchStatusJson("batch_4", 5, 0, 5)
        ));
        BatchMessageResponse failed = client.messages().getBatch("batch_4");
        assertTrue(failed.isFailed());
        assertFalse(failed.isCompleted());
        assertFalse(failed.isProcessing());
        assertFalse(failed.isPartialFailure());
    }

    @Test
    void testBatchMessageResult_statusHelpers() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchStatusJson("batch_123", 2, 1, 1)
        ));

        BatchMessageResponse response = client.messages().getBatch("batch_123");
        List<BatchMessageResult> results = response.getMessages();

        assertTrue(results.get(0).isSuccess());
        assertFalse(results.get(0).isFailed());

        assertTrue(results.get(1).isFailed());
        assertFalse(results.get(1).isSuccess());
        assertNotNull(results.get(1).getError());
    }

    @Test
    void testBatchList_accessMethods() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchListJson(5, 0, false)
        ));

        BatchList list = client.messages().listBatches();

        assertNotNull(list.getData().get(0));
        assertNotNull(list.getData().get(4));
        assertEquals("batch_0", list.getData().get(0).getBatchId());
        assertEquals("batch_4", list.getData().get(4).getBatchId());
        assertEquals("batch_2", list.getData().get(2).getBatchId());
    }

    // ==================== previewBatch() Wire Shape Tests ====================

    private static SendBatchRequest twoMessages() {
        return new SendBatchRequest(Arrays.asList(
            new BatchMessageItem("+15551234567", "Message 1"),
            new BatchMessageItem("+15551234568", "Message 2")
        ));
    }

    private static String previewJson(String overrides) {
        return "{\"total\":2,\"sendable\":2,\"blocked\":0,\"duplicates\":0,\"creditsNeeded\":4,\"creditBalance\":100,"
            + "\"hasSufficientCredits\":true,\"pooled\":false,\"keyType\":\"live\",\"keyScopes\":[\"sms:send\"],"
            + "\"hasWriteScope\":true,\"messagingProfile\":{\"id\":\"mp_1\",\"canSendDomestic\":true,"
            + "\"canSendInternational\":false,\"verificationStatus\":\"verified\",\"verificationType\":\"toll_free\"},"
            + "\"byCountry\":{\"US\":{\"count\":2,\"credits\":4,\"tier\":\"domestic\",\"allowed\":true}},"
            + "\"blockedMessages\":[],\"compliance\":{\"messageType\":\"marketing\",\"optedOutBlocked\":0,"
            + "\"shaftBlocked\":0,\"quietHoursBlocked\":0,\"quietHoursRescheduled\":0,\"shaftBlockedMessages\":[],"
            + "\"quietHoursBlockedMessages\":[]},\"warnings\":[]" + overrides + "}";
    }

    @Test
    void testPreviewBatch_readsTheKeysTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"total\":2,\"sendable\":2,\"blocked\":0,\"duplicates\":0,\"creditsNeeded\":4,\"creditBalance\":100,"
                + "\"hasSufficientCredits\":true,\"hasWriteScope\":true,\"blockedMessages\":[],\"warnings\":[]}"
        ));

        BatchPreviewResponse preview = client.messages().previewBatch(twoMessages());

        assertTrue(preview.canSend());
        assertEquals(2, preview.getTotalMessages());
        assertEquals(2, preview.getWillSend());
        assertEquals(0, preview.getBlocked());
        assertEquals(4, preview.getCreditsNeeded());
        assertEquals(100, preview.getCurrentBalance());
        assertTrue(preview.hasEnoughCredits());
        assertEquals(0, preview.getDuplicates());
        assertTrue(preview.getWarnings().isEmpty());
    }

    @Test
    void testPreviewBatch_readsDuplicatesAndWarnings() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("").replace(
            "\"duplicates\":0", "\"duplicates\":1").replace(
            "\"warnings\":[]", "\"warnings\":[\"1 duplicate number will be removed\"]")));

        BatchPreviewResponse preview = client.messages().previewBatch(twoMessages());

        assertEquals(1, preview.getDuplicates());
        assertEquals(List.of("1 duplicate number will be removed"), preview.getWarnings());
    }

    @Test
    void testPreviewBatch_cannotSendWhenAMessageIsBlockedForMoreThanAnOptOut() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("").replace(
            "\"sendable\":2,\"blocked\":0", "\"sendable\":1,\"blocked\":1").replace(
            "\"blockedMessages\":[]",
            "\"blockedMessages\":[{\"index\":1,\"to\":\"+447700900123\",\"reason\":\"International messaging is not enabled\"}]")));

        BatchPreviewResponse preview = client.messages().previewBatch(twoMessages());

        assertFalse(preview.canSend());
        assertEquals(1, preview.getWillSend());
        assertEquals(1, preview.getBlockReasons().get("International messaging is not enabled"));
    }

    @Test
    void testPreviewBatch_canSendWhenOnlyOptOutsAreBlocked() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("").replace(
            "\"sendable\":2,\"blocked\":0", "\"sendable\":1,\"blocked\":1").replace(
            "\"optedOutBlocked\":0", "\"optedOutBlocked\":1").replace(
            "\"blockedMessages\":[]",
            "\"blockedMessages\":[{\"index\":1,\"to\":\"+15551234568\",\"reason\":\"Contact has opted out (texted STOP)\"}]")));

        BatchPreviewResponse preview = client.messages().previewBatch(twoMessages());

        assertTrue(preview.canSend());
        assertEquals(1, preview.getBlockReasons().get("Contact has opted out (texted STOP)"));
    }

    @Test
    void testPreviewBatch_needsABalanceOnlyWithALiveKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("").replace(
            "\"hasSufficientCredits\":true", "\"hasSufficientCredits\":false")));
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("").replace(
            "\"hasSufficientCredits\":true", "\"hasSufficientCredits\":false").replace(
            "\"keyType\":\"live\"", "\"keyType\":\"test\"")));

        BatchPreviewResponse live = client.messages().previewBatch(twoMessages());
        BatchPreviewResponse test = client.messages().previewBatch(twoMessages());

        assertFalse(live.canSend());
        assertFalse(live.hasEnoughCredits());
        assertTrue(test.canSend());
    }

    @Test
    void testPreviewBatch_cannotSendWithoutTheSendScope() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("").replace(
            "\"hasWriteScope\":true", "\"hasWriteScope\":false")));

        assertFalse(client.messages().previewBatch(twoMessages()).canSend());
    }

    @Test
    void testPreviewBatch_cannotSendMoreThanTenThousandMessages() throws Exception {
        List<BatchMessageItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 10001; i++) {
            items.add(new BatchMessageItem(String.format("+1555%07d", i), "Hi"));
        }
        mockServer.enqueue(TestHelpers.mockSuccess(previewJson("")
            .replace("\"total\":2,\"sendable\":2", "\"total\":10001,\"sendable\":10001")
            .replace("\"creditsNeeded\":4,\"creditBalance\":100", "\"creditsNeeded\":20002,\"creditBalance\":50000")
            .replace("\"count\":2,\"credits\":4", "\"count\":10001,\"credits\":20002")
            .replace("\"warnings\":[]", "\"warnings\":[\"Batch size exceeds 10,000 limit - sending it will be rejected, "
                + "split it into batches of 10,000 or fewer\"]")));

        BatchPreviewResponse preview = client.messages().previewBatch(new SendBatchRequest(items));

        assertEquals(10001, preview.getTotalMessages());
        assertEquals(10001, preview.getWillSend());
        assertTrue(preview.hasEnoughCredits());
        assertFalse(preview.canSend());
    }

    @Test
    void testPreviewBatch_testKeyOnAnUnverifiedWorkspace_readsTheLiveChecks() throws Exception {
        String noProfile = "No messaging profile - complete verification first";
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"total\":2,\"sendable\":0,\"blocked\":2,\"duplicates\":0,\"creditsNeeded\":0,\"creditBalance\":0,"
                + "\"hasSufficientCredits\":true,\"pooled\":false,\"keyType\":\"test\",\"keyScopes\":[\"sms:send\"],"
                + "\"hasWriteScope\":true,\"messagingProfile\":{\"id\":null,\"canSendDomestic\":false,"
                + "\"canSendInternational\":false,\"verificationStatus\":null,\"verificationType\":null},"
                + "\"byCountry\":{\"US\":{\"count\":2,\"credits\":0,\"tier\":\"domestic\",\"allowed\":false,"
                + "\"blockedReason\":\"" + noProfile + "\"}},"
                + "\"blockedMessages\":[{\"index\":0,\"to\":\"+15551234567\",\"reason\":\"" + noProfile + "\"},"
                + "{\"index\":1,\"to\":\"+15551234568\",\"reason\":\"" + noProfile + "\"}],"
                + "\"compliance\":{\"messageType\":\"marketing\",\"optedOutBlocked\":0,\"shaftBlocked\":0,"
                + "\"quietHoursBlocked\":0,\"quietHoursRescheduled\":0,\"shaftBlockedMessages\":[],"
                + "\"quietHoursBlockedMessages\":[]},"
                + "\"warnings\":[\"Using TEST key - messages will be simulated in sandbox mode\","
                + "\"No messaging profile - complete verification to send messages\","
                + "\"Marketing messages are subject to quiet hours enforcement (8pm-8am recipient local time)\"]}"
        ));

        BatchPreviewResponse preview = client.messages().previewBatch(twoMessages());

        assertFalse(preview.canSend());
        assertEquals(0, preview.getWillSend());
        assertEquals(2, preview.getBlockReasons().get(noProfile));
        assertTrue(preview.getWarnings().contains("Using TEST key - messages will be simulated in sandbox mode"));
    }

    @Test
    void testBatchList_iteration() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchListJson(3, 0, false)
        ));

        BatchList list = client.messages().listBatches();

        int count = 0;
        for (BatchMessageResponse batch : list) {
            assertNotNull(batch);
            assertNotNull(batch.getBatchId());
            count++;
        }

        assertEquals(3, count);
    }
}
