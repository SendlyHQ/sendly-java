package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.RcsCard;
import com.sendly.models.RcsMessage;
import com.sendly.models.RcsSuggestion;
import com.sendly.models.SendRcsMessageRequest;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Messages resource - RCS channel sends (text, suggestions, card,
 * SMS fallback).
 */
class MessagesRcsTest {
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
        return "{\"id\":\"msg_rcs_1\",\"channel\":\"rcs\",\"message_format\":\"rcs\"," +
            "\"to\":\"+15551234567\",\"from\":\"Acme Inc\",\"text\":\"Your order has shipped!\"," +
            "\"status\":\"sent\",\"segments\":1,\"creditsUsed\":2," +
            "\"rcs\":{\"kind\":\"text\",\"agentId\":\"rag_1\",\"agentName\":\"Acme Inc\"}," +
            "\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}";
    }

    // ==================== Text Send Tests ====================

    @Test
    void testSendRcs_text_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
                .to("+15551234567")
                .text("Your order has shipped!")
                .build());

        assertNotNull(message);
        assertEquals("msg_rcs_1", message.getId());
        assertEquals("rcs", message.getChannel());
        assertNull(message.getFellBackTo());
        assertEquals("rcs", message.getMessageFormat());
        assertEquals("+15551234567", message.getTo());
        assertEquals("Acme Inc", message.getFrom());
        assertEquals("Your order has shipped!", message.getText());
        assertEquals("sent", message.getStatus());
        assertEquals(1, message.getSegments());
        assertEquals(2, message.getCreditsUsed());
        assertNotNull(message.getRcs());
        assertEquals("text", message.getRcs().getKind());
        assertEquals("rag_1", message.getRcs().getAgentId());
        assertEquals("Acme Inc", message.getRcs().getAgentName());
        assertNull(message.getRcs().getRequestedChannel());
        assertNull(message.getRcs().getSuggestionsDropped());
        assertEquals("2026-07-31T10:00:00.000Z", message.getCreatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/messages"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"channel\":\"rcs\""));
        assertTrue(body.contains("\"to\":\"+15551234567\""));
        assertTrue(body.contains("\"text\":\"Your order has shipped!\""));
        assertFalse(body.contains("card"));
        assertFalse(body.contains("agentId"));
        assertFalse(body.contains("fallbackToSms"));
    }

    @Test
    void testSendRcs_textWithSuggestions_serializesChips() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        client.messages().send(SendRcsMessageRequest.builder()
                .to("+15551234567")
                .agentId("rag_1")
                .text("Your order has shipped!")
                .suggestions(List.of(
                    RcsSuggestion.reply("Track it", "track_order"),
                    RcsSuggestion.action("View receipt", "view_receipt", "https://example.com/receipt/4821")))
                .build());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"agentId\":\"rag_1\""));
        assertTrue(body.contains("\"reply\":{\"text\":\"Track it\",\"postbackData\":\"track_order\"}"));
        assertTrue(body.contains("\"action\":{\"text\":\"View receipt\",\"postbackData\":\"view_receipt\"," +
                "\"url\":\"https://example.com/receipt/4821\"}"));
    }

    // ==================== Card Send Tests ====================

    @Test
    void testSendRcs_card_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_rcs_2\",\"channel\":\"rcs\",\"message_format\":\"rcs\"," +
            "\"to\":\"+15551234567\",\"from\":\"Acme Inc\",\"text\":null," +
            "\"status\":\"sent\",\"segments\":1,\"creditsUsed\":2," +
            "\"rcs\":{\"kind\":\"card\",\"agentId\":\"rag_1\",\"agentName\":\"Acme Inc\"}," +
            "\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}"
        ));

        RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
                .to("+15551234567")
                .card(RcsCard.builder()
                    .title("Order #4821 shipped")
                    .description("Arriving Thursday")
                    .mediaUrl("https://example.com/package.jpg")
                    .orientation("horizontal")
                    .suggestions(List.of(RcsSuggestion.reply("Track it", "track_order")))
                    .build())
                .build());

        assertEquals("card", message.getRcs().getKind());
        assertNull(message.getText());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"title\":\"Order #4821 shipped\""));
        assertTrue(body.contains("\"description\":\"Arriving Thursday\""));
        assertTrue(body.contains("\"mediaUrl\":\"https://example.com/package.jpg\""));
        assertTrue(body.contains("\"orientation\":\"horizontal\""));
        assertTrue(body.contains("\"reply\":{\"text\":\"Track it\",\"postbackData\":\"track_order\"}"));
        assertFalse(body.contains("\"text\":\"Order"));
    }

    // ==================== SMS Fallback Tests ====================

    @Test
    void testSendRcs_smsFallback_parsesFallbackShape() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_rcs_3\",\"channel\":\"sms\",\"fellBackTo\":\"sms\"," +
            "\"message_format\":\"sms\",\"to\":\"+15551234567\",\"from\":\"+18005550142\"," +
            "\"text\":\"Your order has shipped!\",\"status\":\"sent\",\"segments\":1," +
            "\"creditsUsed\":2,\"rcs\":{\"requestedChannel\":\"rcs\",\"agentId\":\"rag_1\"," +
            "\"suggestionsDropped\":true},\"createdAt\":\"2026-07-31T10:00:00.000Z\",\"metadata\":{}}"
        ));

        RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
                .to("+15551234567")
                .text("Your order has shipped!")
                .suggestions(List.of(RcsSuggestion.reply("Track it", "track_order")))
                .build());

        assertEquals("sms", message.getChannel());
        assertEquals("sms", message.getFellBackTo());
        assertEquals("sms", message.getMessageFormat());
        assertEquals("+18005550142", message.getFrom());
        assertEquals("Your order has shipped!", message.getText());
        assertNotNull(message.getRcs());
        assertNull(message.getRcs().getKind());
        assertEquals("rcs", message.getRcs().getRequestedChannel());
        assertEquals("rag_1", message.getRcs().getAgentId());
        assertNull(message.getRcs().getAgentName());
        assertEquals(Boolean.TRUE, message.getRcs().getSuggestionsDropped());
    }

    @Test
    void testSendRcs_fallbackDisabled_serializesFlagAnd422Throws() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(422)
                .setBody("{\"error\":\"rcs_not_supported_for_recipient\"," +
                        "\"message\":\"This recipient's device or network doesn't support RCS.\"}"));

        ValidationException e = assertThrows(ValidationException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .text("Your order has shipped!")
                    .fallbackToSms(false)
                    .build());
        });
        assertEquals("This recipient's device or network doesn't support RCS.", e.getMessage());

        RecordedRequest request = mockServer.takeRequest();
        assertTrue(request.getBody().readUtf8().contains("\"fallbackToSms\":false"));
    }

    // ==================== Validation Tests ====================

    @Test
    void testSendRcs_noContent_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .build());
        });
    }

    @Test
    void testSendRcs_textAndCard_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .text("Hello!")
                    .card(new RcsCard("Order #4821 shipped", "Arriving Thursday"))
                    .build());
        });
    }

    @Test
    void testSendRcs_suggestionsWithCard_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .card(new RcsCard("Order #4821 shipped", "Arriving Thursday"))
                    .suggestions(List.of(RcsSuggestion.reply("Track it", "track_order")))
                    .build());
        });
    }

    @Test
    void testSendRcs_invalidTo_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("invalid")
                    .text("Hello!")
                    .build());
        });
    }

    // ==================== Error Mapping Tests ====================

    @Test
    void testSendRcs_404NotEnabled_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(404)
                .setBody("{\"error\":\"rcs_not_enabled\"," +
                        "\"message\":\"RCS sending isn't enabled for this account yet.\"}"));

        assertThrows(NotFoundException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .text("Hello!")
                    .build());
        });
    }

    @Test
    void testSendRcs_402InsufficientCredits_throwsInsufficientCreditsException() {
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        assertThrows(InsufficientCreditsException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .text("Hello!")
                    .build());
        });
    }

    @Test
    void testSendRcs_403AgentNotReady_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(403)
                .setBody("{\"error\":\"rcs_agent_not_ready\",\"agentStatus\":\"pending\"," +
                        "\"message\":\"This workspace's RCS agent isn't approved for sending yet.\"}"));

        SendlyException e = assertThrows(SendlyException.class, () -> {
            client.messages().send(SendRcsMessageRequest.builder()
                    .to("+15551234567")
                    .text("Hello!")
                    .build());
        });
        assertEquals("This workspace's RCS agent isn't approved for sending yet.", e.getMessage());
    }
}
