package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.Message;
import com.sendly.models.SendWhatsAppMessageRequest;
import com.sendly.models.WhatsAppMessage;
import com.sendly.models.WhatsAppSmsFallback;
import com.sendly.models.WhatsAppTemplateButtonVariables;
import com.sendly.models.WhatsAppTemplateSendParams;
import okhttp3.mockwebserver.MockResponse;
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

    // ==================== send outcome unknown ====================

    @Test
    void testSendWhatsApp_unconfirmed_isThrownOnFirstAttempt() {
        Sendly retrying = new Sendly("sk_live_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(3));
        mockServer.enqueue(new MockResponse().setResponseCode(409).addHeader("Content-Type", "application/json").setBody(
            "{\"error\":\"whatsapp_send_unconfirmed\",\"errorCode\":\"E024\"," +
            "\"message\":\"We couldn't confirm whether WhatsApp accepted this message. It has been marked failed and refunded, " +
            "but it may still be delivered. Check before sending it again, or it could arrive twice.\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_wa_2\",\"channel\":\"whatsapp\",\"status\":\"queued\"}"));

        SendlyException e = assertThrows(SendlyException.class, () ->
            retrying.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+12025550143")
                .from("+15125550188")
                .text("Your table is ready!")
                .build()));
        assertEquals("whatsapp_send_unconfirmed", e.getApiErrorCode());
        assertEquals(409, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    // ==================== SMS fallback ====================

    private SendWhatsAppMessageRequest.Builder fallbackRequest() {
        return SendWhatsAppMessageRequest.builder()
                .to("+12025550143")
                .from("+15125550188")
                .text("Your table is ready!");
    }

    @Test
    void testSendWhatsApp_fallbackToSmsTrue_isSent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        client.messages().send(fallbackRequest().fallbackToSms(true).build());

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertTrue(body.contains("\"fallbackToSms\":true"));
        assertTrue(body.contains("\"channel\":\"whatsapp\""));
    }

    @Test
    void testSendWhatsApp_fallbackToSmsFalse_isSent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        client.messages().send(fallbackRequest().fallbackToSms(false).build());

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertTrue(body.contains("\"fallbackToSms\":false"));
    }

    @Test
    void testSendWhatsApp_fallbackToSmsUnset_isOmitted() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        client.messages().send(fallbackRequest().build());

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertFalse(body.contains("fallbackToSms"));
    }

    @Test
    void testSendWhatsApp_fellBack_parsesFallback() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(201).addHeader("Content-Type", "application/json").setBody(
            "{\"id\":\"msg_wa_9\",\"channel\":\"whatsapp\",\"message_format\":\"whatsapp\"," +
            "\"messageFormat\":\"whatsapp\",\"to\":\"+12025550143\",\"from\":\"+15125550188\"," +
            "\"text\":\"Your table is ready!\",\"status\":\"failed\",\"direction\":\"outbound\"," +
            "\"segments\":1,\"creditsUsed\":0,\"whatsapp\":{\"kind\":\"text\",\"messageId\":null}," +
            "\"createdAt\":\"2026-10-17T10:00:00.000Z\",\"metadata\":{}," +
            "\"fallback\":{\"channel\":\"sms\",\"status\":\"sent\",\"reason\":\"window_closed\"," +
            "\"messageId\":\"msg_sms_9\",\"creditsUsed\":2,\"contentDropped\":[\"quick_replies\"]}}"));

        WhatsAppMessage message = client.messages().send(fallbackRequest().fallbackToSms(true).build());

        assertEquals("failed", message.getStatus());
        assertEquals(0, message.getCreditsUsed());
        assertTrue(message.fellBack());
        WhatsAppSmsFallback fallback = message.getFallback();
        assertNotNull(fallback);
        assertEquals("sms", fallback.getChannel());
        assertEquals(WhatsAppSmsFallback.STATUS_SENT, fallback.getStatus());
        assertTrue(fallback.isSent());
        assertFalse(fallback.isPending());
        assertEquals(WhatsAppSmsFallback.REASON_WINDOW_CLOSED, fallback.getReason());
        assertEquals("msg_sms_9", fallback.getMessageId());
        assertEquals(Integer.valueOf(2), fallback.getCreditsUsed());
        assertNull(fallback.getError());
        assertEquals(List.of("quick_replies"), fallback.getContentDropped());
    }

    @Test
    void testSendWhatsApp_pendingFallback_hasNotFallenBack() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_wa_10\",\"channel\":\"whatsapp\",\"status\":\"queued\"," +
            "\"fallback\":{\"channel\":\"sms\",\"status\":\"pending\"}}"));

        WhatsAppMessage message = client.messages().send(fallbackRequest().fallbackToSms(true).build());

        assertFalse(message.fellBack());
        assertTrue(message.getFallback().isPending());
        assertNull(message.getFallback().getReason());
        assertNull(message.getFallback().getMessageId());
        assertNull(message.getFallback().getCreditsUsed());
        assertTrue(message.getFallback().getContentDropped().isEmpty());
    }

    @Test
    void testSendWhatsApp_withoutFallback_hasNoFallback() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(textMessageJson()));

        WhatsAppMessage message = client.messages().send(fallbackRequest().build());

        assertNull(message.getFallback());
        assertFalse(message.fellBack());
    }

    @Test
    void testSendWhatsApp_smsFallbackUnavailable_carriesCode() {
        mockServer.enqueue(new MockResponse().setResponseCode(403).addHeader("Content-Type", "application/json").setBody(
            "{\"error\":\"sms_fallback_unavailable\"," +
            "\"message\":\"This number can't send SMS to this recipient.\"," +
            "\"suggestion\":\"Send without fallbackToSms, or make sure this number can send SMS to this recipient first.\"}"));

        SendlyException e = assertThrows(SendlyException.class, () ->
            client.messages().send(fallbackRequest().fallbackToSms(true).build()));
        assertEquals(403, e.getStatusCode());
        assertEquals("sms_fallback_unavailable", e.getApiErrorCode());
        assertEquals("This number can't send SMS to this recipient.", e.getMessage());
        assertEquals("Send without fallbackToSms, or make sure this number can send SMS to this recipient first.",
            e.getResponseBody().get("suggestion").getAsString());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSendWhatsApp_smsFallbackNotSupported_carriesCode() {
        mockServer.enqueue(new MockResponse().setResponseCode(422).addHeader("Content-Type", "application/json").setBody(
            "{\"error\":\"sms_fallback_not_supported\",\"reason\":\"marketing_template\"," +
            "\"message\":\"Marketing templates can't fall back to SMS: SMS marketing needs its own consent. Send without fallbackToSms.\"}"));

        ValidationException e = assertThrows(ValidationException.class, () ->
            client.messages().send(SendWhatsAppMessageRequest.builder()
                .to("+12025550143")
                .from("+15125550188")
                .template(new WhatsAppTemplateSendParams("spring_sale", "en_US", Map.of()))
                .fallbackToSms(true)
                .build()));
        assertEquals(422, e.getStatusCode());
        assertEquals("sms_fallback_not_supported", e.getApiErrorCode());
        assertEquals("marketing_template", e.getResponseBody().get("reason").getAsString());
    }

    @Test
    void testGetMessage_whatsAppFallbackAndSmsFallbackOf() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_wa_9\",\"to\":\"+12025550143\",\"from\":\"+15125550188\"," +
            "\"text\":\"Your table is ready!\",\"status\":\"failed\",\"direction\":\"outbound\"," +
            "\"messageFormat\":\"whatsapp\",\"creditsUsed\":0," +
            "\"fallback\":{\"channel\":\"sms\",\"status\":\"sent\",\"reason\":\"whatsapp_refused\"," +
            "\"messageId\":\"msg_sms_9\",\"creditsUsed\":2}}"));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"msg_sms_9\",\"to\":\"+12025550143\",\"from\":\"+15125550188\"," +
            "\"text\":\"Your table is ready!\",\"status\":\"delivered\",\"direction\":\"outbound\"," +
            "\"messageFormat\":\"sms\",\"creditsUsed\":2,\"fallbackOf\":\"msg_wa_9\"}"));

        Message whatsapp = client.messages().get("msg_wa_9");
        assertNotNull(whatsapp.getFallback());
        assertTrue(whatsapp.getFallback().isSent());
        assertEquals(WhatsAppSmsFallback.REASON_WHATSAPP_REFUSED, whatsapp.getFallback().getReason());
        assertEquals("msg_sms_9", whatsapp.getFallback().getMessageId());
        assertNull(whatsapp.getFallbackOf());

        Message sms = client.messages().get("msg_sms_9");
        assertNull(sms.getFallback());
        assertEquals("msg_wa_9", sms.getFallbackOf());
    }
}
