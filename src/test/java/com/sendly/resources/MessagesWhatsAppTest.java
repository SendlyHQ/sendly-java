package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.SendWhatsAppMessageRequest;
import com.sendly.models.WhatsAppMessage;
import com.sendly.models.WhatsAppTemplateButtonVariables;
import com.sendly.models.WhatsAppTemplateSendParams;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Messages resource - WhatsApp channel sends (text, media, template).
 */
class MessagesWhatsAppTest {
    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(0);

        client = new Sendly("sk_live_123", builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private static String textMessageJson() {
        return "{\"id\":\"msg_wa_1\",\"channel\":\"whatsapp\",\"message_format\":\"whatsapp\"," +
            "\"to\":\"+15551234567\",\"from\":\"+15559876543\",\"text\":\"Your table is ready!\"," +
            "\"status\":\"queued\",\"segments\":1,\"creditsUsed\":1," +
            "\"whatsapp\":{\"kind\":\"text\",\"messageId\":null}," +
            "\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}";
    }

    // ==================== Text Send Tests ====================

    @Test
    void testSendWhatsApp_text_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+15551234567")
                .from("+15559876543")
                .text("Your table is ready!")
                .build());

        assertNotNull(message);
        assertEquals("msg_wa_1", message.getId());
        assertEquals("whatsapp", message.getChannel());
        assertEquals("whatsapp", message.getMessageFormat());
        assertEquals("+15551234567", message.getTo());
        assertEquals("+15559876543", message.getFrom());
        assertEquals("Your table is ready!", message.getText());
        assertEquals("queued", message.getStatus());
        assertEquals(1, message.getSegments());
        assertEquals(1, message.getCreditsUsed());
        assertNotNull(message.getWhatsapp());
        assertEquals("text", message.getWhatsapp().getKind());
        assertNull(message.getWhatsapp().getTemplate());
        assertNull(message.getWhatsapp().getMessageId());
        assertEquals("2026-07-31T10:00:00.000Z", message.getCreatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/messages"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"channel\":\"whatsapp\""));
        assertTrue(body.contains("\"to\":\"+15551234567\""));
        assertTrue(body.contains("\"from\":\"+15559876543\""));
        assertTrue(body.contains("\"text\":\"Your table is ready!\""));
        assertFalse(body.contains("mediaUrls"));
        assertFalse(body.contains("template"));
    }

    // ==================== Media Send Tests ====================

    @Test
    void testSendWhatsApp_mediaWithCaption() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_wa_2\",\"channel\":\"whatsapp\",\"message_format\":\"whatsapp\"," +
            "\"to\":\"+15551234567\",\"from\":\"+15559876543\",\"text\":\"Menu attached\"," +
            "\"status\":\"queued\",\"segments\":1,\"creditsUsed\":1," +
            "\"whatsapp\":{\"kind\":\"media\",\"messageId\":null}," +
            "\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}"
        ));

        WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+15551234567")
                .from("+15559876543")
                .text("Menu attached")
                .mediaUrls(List.of("https://example.com/menu.jpg"))
                .build());

        assertEquals("media", message.getWhatsapp().getKind());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"mediaUrls\":[\"https://example.com/menu.jpg\"]"));
        assertTrue(body.contains("\"text\":\"Menu attached\""));
    }

    @Test
    void testSendWhatsApp_mediaOnly_noText() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_wa_3\",\"channel\":\"whatsapp\",\"message_format\":\"whatsapp\"," +
            "\"to\":\"+15551234567\",\"from\":\"+15559876543\",\"text\":null," +
            "\"status\":\"queued\",\"segments\":1,\"creditsUsed\":1," +
            "\"whatsapp\":{\"kind\":\"media\",\"messageId\":null}," +
            "\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}"
        ));

        WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+15551234567")
                .from("+15559876543")
                .mediaUrls(List.of("https://example.com/menu.jpg"))
                .build());

        assertNull(message.getText());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("mediaUrls"));
        assertFalse(body.contains("\"text\""));
    }

    // ==================== Template Send Tests ====================

    @Test
    void testSendWhatsApp_template_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_wa_4\",\"channel\":\"whatsapp\",\"message_format\":\"whatsapp\"," +
            "\"to\":\"+15551234567\",\"from\":\"+15559876543\",\"text\":null," +
            "\"status\":\"queued\",\"segments\":1,\"creditsUsed\":2," +
            "\"whatsapp\":{\"kind\":\"template\",\"template\":{\"name\":\"order_shipped\"," +
            "\"language\":\"en_US\",\"category\":\"utility\"},\"messageId\":null}," +
            "\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}"
        ));

        WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+15551234567")
                .from("+15559876543")
                .template(new WhatsAppTemplateSendParams("order_shipped", "en_US",
                        Map.of("1", "Acme Inc", "2", "#4821")))
                .build());

        assertEquals("template", message.getWhatsapp().getKind());
        assertNull(message.getText());
        assertNotNull(message.getWhatsapp().getTemplate());
        assertEquals("order_shipped", message.getWhatsapp().getTemplate().getName());
        assertEquals("en_US", message.getWhatsapp().getTemplate().getLanguage());
        assertEquals("utility", message.getWhatsapp().getTemplate().getCategory());
        assertEquals(2, message.getCreditsUsed());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"channel\":\"whatsapp\""));
        assertTrue(body.contains("\"name\":\"order_shipped\""));
        assertTrue(body.contains("\"language\":\"en_US\""));
        assertTrue(body.contains("\"1\":\"Acme Inc\""));
    }

    @Test
    void testSendWhatsApp_templateWithButtonVariables_serializesButtons() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+15551234567")
                .from("+15559876543")
                .template(new WhatsAppTemplateSendParams("order_shipped", "en_US",
                        Map.of("1", "Acme Inc"),
                        List.of(new WhatsAppTemplateButtonVariables(0, Map.of("1", "4821")))))
                .build());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"index\":0"));
        assertTrue(body.contains("\"1\":\"4821\""));
    }

    // ==================== Validation Tests ====================

    @Test
    void testSendWhatsApp_noContent_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendWhatsAppMessageRequest.builder()
                    .to("+15551234567")
                    .from("+15559876543")
                    .build());
        });
    }

    @Test
    void testSendWhatsApp_missingFrom_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendWhatsAppMessageRequest.builder()
                    .to("+15551234567")
                    .text("Hello!")
                    .build());
        });
    }

    @Test
    void testSendWhatsApp_invalidTo_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendWhatsAppMessageRequest.builder()
                    .to("invalid")
                    .from("+15559876543")
                    .text("Hello!")
                    .build());
        });
    }

    // ==================== Error Mapping Tests ====================

    @Test
    void testSendWhatsApp_422WindowClosed_throwsValidationException() {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(422)
                .setBody("{\"error\":\"whatsapp_window_closed\"," +
                        "\"message\":\"No open 24-hour window. Send an approved template instead.\"}"));

        ValidationException e = assertThrows(ValidationException.class, () -> {
            client.messages().send(SendWhatsAppMessageRequest.builder()
                    .to("+15551234567")
                    .from("+15559876543")
                    .text("Hello!")
                    .build());
        });
        assertEquals("No open 24-hour window. Send an approved template instead.", e.getMessage());
    }

    @Test
    void testSendWhatsApp_402InsufficientCredits_throwsInsufficientCreditsException() {
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        assertThrows(InsufficientCreditsException.class, () -> {
            client.messages().send(SendWhatsAppMessageRequest.builder()
                    .to("+15551234567")
                    .from("+15559876543")
                    .text("Hello!")
                    .build());
        });
    }
}
