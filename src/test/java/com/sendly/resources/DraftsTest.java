package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.Draft;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the drafts resource against the shapes the API sends and accepts.
 */
class DraftsTest {
    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(0);

        client = new Sendly("sk_test_123", builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testApprove_sendsAnEmptyJsonObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"drf_1\",\"conversationId\":\"conv_1\",\"text\":\"Hi\",\"status\":\"approved\","
                + "\"messageId\":\"msg_1\",\"createdAt\":\"2026-09-25T00:00:00.000Z\","
                + "\"updatedAt\":\"2026-09-25T00:00:01.000Z\",\"message\":{\"id\":\"msg_1\",\"status\":\"queued\"}}"
        ));

        Draft draft = client.drafts().approve("drf_1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/drafts/drf_1/approve", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
        assertEquals("approved", draft.getStatus());
        assertEquals("msg_1", draft.getMessageId());
    }

    @Test
    void testApprove_passesExpressJsonStrictMode() throws Exception {
        mockServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String body = request.getBody().readUtf8().trim();
                if (!body.isEmpty() && body.charAt(0) != '{' && body.charAt(0) != '[') {
                    return new MockResponse().setResponseCode(400)
                            .setBody("{\"message\":\"Unexpected token n in JSON at position 0\"}");
                }
                return TestHelpers.mockSuccess(
                    "{\"id\":\"drf_1\",\"conversationId\":\"conv_1\",\"text\":\"Hi\",\"status\":\"approved\"}"
                );
            }
        });

        Draft draft = client.drafts().approve("drf_1");

        assertEquals("approved", draft.getStatus());
    }
}
