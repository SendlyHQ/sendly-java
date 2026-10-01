package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.Label;
import com.sendly.models.LabelListResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for labels against the shapes the API sends.
 */
class LabelsTest {
    private static final String LABEL =
        "{\"id\":\"lbl_1\",\"userId\":\"user_1\",\"organizationId\":null,\"name\":\"VIP\",\"color\":\"#f00\","
            + "\"description\":null,\"createdAt\":\"2026-09-25T00:00:00.000Z\"}";

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
    void testList_readsTheCreatedAtTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + LABEL + "]}"));

        Label label = client.labels().list().getData().get(0);

        assertEquals("VIP", label.getName());
        assertEquals("2026-09-25T00:00:00.000Z", label.getCreatedAt());
    }

    @Test
    void testRemoveLabel_acceptsTheEmpty204() throws Exception {
        mockServer.enqueue(new okhttp3.mockwebserver.MockResponse().setResponseCode(204));

        client.conversations().removeLabel("conv_1", "lbl_1");

        okhttp3.mockwebserver.RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/api/v1/conversations/conv_1/labels/lbl_1", request.getPath());
    }

    @Test
    void testAddLabels_returnsTheConversationsLabels() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + LABEL + "]}"));

        LabelListResponse labels = client.conversations().addLabels("conv_1", List.of("lbl_1"));

        assertEquals("lbl_1", labels.getData().get(0).getId());
        assertEquals("2026-09-25T00:00:00.000Z", labels.getData().get(0).getCreatedAt());
        assertEquals("{\"labelIds\":[\"lbl_1\"]}", mockServer.takeRequest().getBody().readUtf8());
    }
}
