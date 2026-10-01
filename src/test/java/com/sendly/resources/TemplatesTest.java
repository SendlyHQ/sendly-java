package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.Template;
import com.sendly.models.TemplatePreview;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the templates resource against the shapes the API sends and accepts.
 */
class TemplatesTest {
    private static final String TEMPLATE =
        "{\"id\":\"tpl_1\",\"name\":\"Welcome\",\"text\":\"Hi {{name}}\",\"variables\":[],\"is_preset\":false,"
            + "\"status\":\"published\",\"version\":1,\"published_at\":\"2026-09-25T00:00:00.000Z\","
            + "\"created_at\":\"2026-09-25T00:00:00.000Z\",\"updated_at\":\"2026-09-25T00:00:00.000Z\"}";

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
    void testPublish_sendsAnEmptyJsonObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(TEMPLATE));

        Template template = client.templates().publish("tpl_1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/templates/tpl_1/publish", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
        assertEquals("published", template.getStatus());
    }

    @Test
    void testPreview_readsTheRenderedTextTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"template_id\":\"tpl_1\",\"original_text\":\"Hi {{name}}\",\"rendered_text\":\"Hi Ann\","
                + "\"character_count\":6,\"segment_count\":1}"
        ));

        TemplatePreview preview = client.templates().preview("tpl_1", Map.of("name", "Ann"));

        assertEquals("Hi Ann", preview.getPreviewText());
        assertEquals("tpl_1", preview.getId());
        assertEquals("Hi {{name}}", preview.getOriginalText());
        assertEquals(6, preview.getCharacterCount());
        assertEquals(1, preview.getSegmentCount());
        assertEquals("{\"variables\":{\"name\":\"Ann\"}}", mockServer.takeRequest().getBody().readUtf8());
    }

    @Test
    void testClone_sendsAnEmptyJsonObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(TEMPLATE.replace("tpl_1", "tpl_2")));

        Template template = client.templates().clone("tpl_1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/templates/tpl_1/clone", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
        assertEquals("tpl_2", template.getId());
    }
}
