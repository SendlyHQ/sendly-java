package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the conversations resource against the shapes the API sends.
 */
class ConversationsTest {
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
    void testSuggestReplies_postsAndReadsTheSuggestions() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"suggestions\":[{\"text\":\"Yes, we open at 9am.\",\"tone\":\"professional\"},"
                + "{\"text\":\"See you at 9!\",\"tone\":\"friendly\"}],"
                + "\"basedOnMessageId\":\"msg_in_1\",\"model\":\"gpt-4o-mini\"}"
        ));

        JsonObject result = client.conversations().suggestReplies("conv/1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/conversations/conv%2F1/suggest-replies", request.getPath());
        assertEquals(2, result.getAsJsonArray("suggestions").size());
        assertEquals("friendly",
            result.getAsJsonArray("suggestions").get(1).getAsJsonObject().get("tone").getAsString());
        assertEquals("msg_in_1", result.get("basedOnMessageId").getAsString());
        assertFalse(result.has("disabled"));
    }

    @Test
    void testSuggestReplies_readsDisabledAndReason() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"suggestions\":[],\"disabled\":true,\"reason\":\"enterprise_policy\"}"
        ));

        JsonObject result = client.conversations().suggestReplies("conv_1");

        assertEquals(0, result.getAsJsonArray("suggestions").size());
        assertTrue(result.get("disabled").getAsBoolean());
        assertEquals("enterprise_policy", result.get("reason").getAsString());
    }

    @Test
    void testList_withLabelSendsTheLabelFilter() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[],\"pagination\":{\"total\":0,\"limit\":20,\"offset\":0,\"hasMore\":false}}"));

        client.conversations().list("active", "lbl_vip", 20, 0);

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v1/conversations", request.getRequestUrl().encodedPath());
        assertEquals("lbl_vip", request.getRequestUrl().queryParameter("label"));
        assertEquals("active", request.getRequestUrl().queryParameter("status"));
    }

    @Test
    void testList_withoutLabelSendsNoLabel() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[],\"pagination\":{}}"));

        client.conversations().list();

        assertNull(mockServer.takeRequest().getRequestUrl().queryParameter("label"));
    }
}
