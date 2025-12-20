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
        assertEquals(3, response.getQueued());
        assertEquals(0, response.getFailed());
        assertEquals(3, response.getCreditsUsed());
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
        assertEquals(3, response.getQueued());
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
            TestHelpers.batchResponseJson("batch_123", 10, 10, 0)
        ));

        BatchMessageResponse response = client.messages().getBatch("batch_123");

        assertNotNull(response);
        assertEquals("batch_123", response.getBatchId());
        assertEquals("completed", response.getStatus());
        assertEquals(10, response.getTotal());
        assertEquals(10, response.getQueued());
        assertEquals(0, response.getFailed());

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
            "{\"batch_id\":\"batch_1\",\"status\":\"processing\",\"total\":5,\"queued\":0,\"failed\":0,\"credits_used\":0,\"messages\":[],\"created_at\":\"2025-01-15T10:00:00.000Z\"}"
        ));
        BatchMessageResponse processing = client.messages().getBatch("batch_1");
        assertTrue(processing.isProcessing());
        assertFalse(processing.isCompleted());
        assertFalse(processing.isPartialFailure());
        assertFalse(processing.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            TestHelpers.batchResponseJson("batch_2", 5, 5, 0)
        ));
        BatchMessageResponse completed = client.messages().getBatch("batch_2");
        assertTrue(completed.isCompleted());
        assertFalse(completed.isProcessing());
        assertFalse(completed.isPartialFailure());
        assertFalse(completed.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"batch_id\":\"batch_3\",\"status\":\"partial_failure\",\"total\":5,\"queued\":3,\"failed\":2,\"credits_used\":3,\"messages\":[],\"created_at\":\"2025-01-15T10:00:00.000Z\"}"
        ));
        BatchMessageResponse partial = client.messages().getBatch("batch_3");
        assertTrue(partial.isPartialFailure());
        assertFalse(partial.isCompleted());
        assertFalse(partial.isProcessing());
        assertFalse(partial.isFailed());

        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"batch_id\":\"batch_4\",\"status\":\"failed\",\"total\":5,\"queued\":0,\"failed\":5,\"credits_used\":0,\"messages\":[],\"created_at\":\"2025-01-15T10:00:00.000Z\"}"
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
            TestHelpers.batchResponseJson("batch_123", 2, 1, 1)
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
