package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.CreateWhatsAppTemplateRequest;
import com.sendly.models.UpdateWhatsAppTemplateRequest;
import com.sendly.models.WhatsAppSender;
import com.sendly.models.WhatsAppSendersResponse;
import com.sendly.models.WhatsAppSignup;
import com.sendly.models.WhatsAppSignupSession;
import com.sendly.models.WhatsAppTemplate;
import com.sendly.models.WhatsAppTemplateButton;
import com.sendly.models.WhatsAppTemplateDeletedResponse;
import com.sendly.models.WhatsAppTemplateListResponse;
import com.sendly.models.WhatsAppWindow;
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
 * Tests for the WhatsApp resource — signup, senders, templates, and windows.
 */
class WhatsAppTest {
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

    // ==================== signup().create() Tests ====================

    @Test
    void testSignupCreate_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_123\",\"connectUrl\":\"https://sendly.live/whatsapp/connect/tok_abc\"," +
            "\"status\":\"initiated\"}"
        ));

        WhatsAppSignupSession signup = client.whatsapp().signup().create("+15559876543");

        assertNotNull(signup);
        assertEquals("was_123", signup.getId());
        assertEquals("https://sendly.live/whatsapp/connect/tok_abc", signup.getConnectUrl());
        assertEquals("initiated", signup.getStatus());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/signup"));
        assertTrue(request.getBody().readUtf8().contains("\"phoneNumber\":\"+15559876543\""));
    }

    @Test
    void testSignupCreate_invalidPhone_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().signup().create("15559876543");
        });
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().signup().create(null);
        });
    }

    @Test
    void testSignupCreate_403TestKey_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(403)
                .setBody("{\"error\":\"whatsapp_requires_live_key\"," +
                        "\"message\":\"WhatsApp requires a live API key.\"}"));

        SendlyException e = assertThrows(SendlyException.class, () -> {
            client.whatsapp().signup().create("+15559876543");
        });
        assertEquals("WhatsApp requires a live API key.", e.getMessage());
    }

    // ==================== signup().get() Tests ====================

    @Test
    void testSignupGet_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_123\",\"status\":\"active\",\"phoneNumber\":\"+15559876543\"," +
            "\"businessAccountId\":\"1234567890\",\"failureReasons\":null," +
            "\"updatedAt\":\"2026-07-30T10:00:00.000Z\"}"
        ));

        WhatsAppSignup signup = client.whatsapp().signup().get("was_123");

        assertNotNull(signup);
        assertEquals("was_123", signup.getId());
        assertEquals("active", signup.getStatus());
        assertEquals("+15559876543", signup.getPhoneNumber());
        assertEquals("1234567890", signup.getBusinessAccountId());
        assertNull(signup.getFailureReasons());
        assertEquals("2026-07-30T10:00:00.000Z", signup.getUpdatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/signup/was_123"));
    }

    @Test
    void testSignupGet_failed_parsesFailureReasons() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_123\",\"status\":\"failed\",\"phoneNumber\":\"+15559876543\"," +
            "\"businessAccountId\":null,\"failureReasons\":[\"display_name_rejected\"]," +
            "\"updatedAt\":\"2026-07-30T10:00:00.000Z\"}"
        ));

        WhatsAppSignup signup = client.whatsapp().signup().get("was_123");

        assertEquals("failed", signup.getStatus());
        assertNull(signup.getBusinessAccountId());
        assertNotNull(signup.getFailureReasons());
        assertEquals(1, signup.getFailureReasons().size());
        assertEquals("display_name_rejected", signup.getFailureReasons().get(0));
    }

    @Test
    void testSignupGet_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().signup().get(null);
        });
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().signup().get("");
        });
    }

    @Test
    void testSignupGet_404_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.whatsapp().signup().get("was_missing");
        });
    }

    // ==================== senders().list() Tests ====================

    @Test
    void testSendersList_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"senders\":[{\"phoneNumber\":\"+15559876543\",\"displayName\":\"Acme Inc\"," +
            "\"status\":\"active\",\"qualityRating\":\"GREEN\"," +
            "\"createdAt\":\"2026-07-30T10:00:00.000Z\"}]}"
        ));

        WhatsAppSendersResponse response = client.whatsapp().senders().list();

        assertNotNull(response);
        assertEquals(1, response.getSenders().size());
        WhatsAppSender sender = response.getSenders().get(0);
        assertEquals("+15559876543", sender.getPhoneNumber());
        assertEquals("Acme Inc", sender.getDisplayName());
        assertEquals("active", sender.getStatus());
        assertEquals("GREEN", sender.getQualityRating());
        assertEquals("2026-07-30T10:00:00.000Z", sender.getCreatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/senders"));
    }

    @Test
    void testSendersList_pendingSender_nullFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"senders\":[{\"phoneNumber\":\"+15559876543\",\"displayName\":null," +
            "\"status\":\"pending\",\"qualityRating\":null," +
            "\"createdAt\":\"2026-07-30T10:00:00.000Z\"}]}"
        ));

        WhatsAppSendersResponse response = client.whatsapp().senders().list();

        WhatsAppSender sender = response.getSenders().get(0);
        assertEquals("pending", sender.getStatus());
        assertNull(sender.getDisplayName());
        assertNull(sender.getQualityRating());
    }

    @Test
    void testSendersList_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"senders\":[]}"));

        WhatsAppSendersResponse response = client.whatsapp().senders().list();

        assertNotNull(response);
        assertTrue(response.getSenders().isEmpty());
    }

    // ==================== templates().list() Tests ====================

    @Test
    void testTemplatesList_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"templates\":[{\"id\":\"wat_1\",\"name\":\"order_shipped\",\"language\":\"en_US\"," +
            "\"category\":\"UTILITY\",\"status\":\"APPROVED\",\"qualityRating\":\"GREEN\"," +
            "\"rejectionReason\":null,\"createdAt\":\"2026-07-28T10:00:00.000Z\"," +
            "\"updatedAt\":\"2026-07-30T10:00:00.000Z\"}]}"
        ));

        WhatsAppTemplateListResponse response = client.whatsapp().templates().list();

        assertNotNull(response);
        assertEquals(1, response.getTemplates().size());
        WhatsAppTemplate template = response.getTemplates().get(0);
        assertEquals("wat_1", template.getId());
        assertEquals("order_shipped", template.getName());
        assertEquals("en_US", template.getLanguage());
        assertEquals("UTILITY", template.getCategory());
        assertEquals("APPROVED", template.getStatus());
        assertEquals("GREEN", template.getQualityRating());
        assertNull(template.getRejectionReason());
        assertNull(template.getWarnings());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/templates"));
    }

    // ==================== templates().create() Tests ====================

    @Test
    void testTemplatesCreate_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"wat_1\",\"name\":\"order_shipped\",\"language\":\"en_US\"," +
            "\"category\":\"UTILITY\",\"status\":\"PENDING\",\"qualityRating\":null," +
            "\"rejectionReason\":null,\"createdAt\":\"2026-07-30T10:00:00.000Z\"," +
            "\"updatedAt\":\"2026-07-30T10:00:00.000Z\",\"warnings\":[\"display_name_pending\"]}"
        ));

        WhatsAppTemplate template = client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .sender("+15559876543")
                .name("order_shipped")
                .language("en_US")
                .category("UTILITY")
                .body("Hi {{1}}, your order {{2}} has shipped!")
                .examples(Map.of("1", "Sam", "2", "#4821"))
                .build());

        assertNotNull(template);
        assertEquals("wat_1", template.getId());
        assertEquals("PENDING", template.getStatus());
        assertNotNull(template.getWarnings());
        assertEquals("display_name_pending", template.getWarnings().get(0));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/templates"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"sender\":\"+15559876543\""));
        assertTrue(body.contains("\"name\":\"order_shipped\""));
        assertTrue(body.contains("\"language\":\"en_US\""));
        assertTrue(body.contains("\"category\":\"UTILITY\""));
        assertTrue(body.contains("\"body\":\"Hi {{1}}, your order {{2}} has shipped!\""));
        assertTrue(body.contains("\"1\":\"Sam\""));
        assertFalse(body.contains("footer"));
        assertFalse(body.contains("header"));
        assertFalse(body.contains("buttons"));
    }

    @Test
    void testTemplatesCreate_withButtons_serializesButtons() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"wat_2\",\"name\":\"spring_sale\",\"language\":\"en_US\"," +
            "\"category\":\"MARKETING\",\"status\":\"PENDING\",\"qualityRating\":null," +
            "\"rejectionReason\":null,\"createdAt\":\"2026-07-30T10:00:00.000Z\"," +
            "\"updatedAt\":\"2026-07-30T10:00:00.000Z\"}"
        ));

        client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .sender("+15559876543")
                .name("spring_sale")
                .language("en_US")
                .category("MARKETING")
                .body("Our spring sale is on!")
                .footer("Reply STOP to opt out")
                .buttons(List.of(
                    WhatsAppTemplateButton.builder()
                        .type("url")
                        .text("Shop now")
                        .url("https://example.com/sale/{{1}}")
                        .example(List.of("spring"))
                        .build(),
                    new WhatsAppTemplateButton("quick_reply", "Stop promotions")))
                .build());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"footer\":\"Reply STOP to opt out\""));
        assertTrue(body.contains("\"type\":\"url\""));
        assertTrue(body.contains("\"text\":\"Shop now\""));
        assertTrue(body.contains("\"url\":\"https://example.com/sale/{{1}}\""));
        assertTrue(body.contains("\"example\":[\"spring\"]"));
        assertTrue(body.contains("\"type\":\"quick_reply\""));
    }

    @Test
    void testTemplatesCreate_missingFields_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.whatsapp().templates().create(null));

        assertThrows(ValidationException.class, () -> client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .name("order_shipped").language("en_US").category("UTILITY").body("Hi!").build()));

        assertThrows(ValidationException.class, () -> client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .sender("+15559876543").language("en_US").category("UTILITY").body("Hi!").build()));

        assertThrows(ValidationException.class, () -> client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .sender("+15559876543").name("order_shipped").category("UTILITY").body("Hi!").build()));

        assertThrows(ValidationException.class, () -> client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .sender("+15559876543").name("order_shipped").language("en_US").body("Hi!").build()));

        assertThrows(ValidationException.class, () -> client.whatsapp().templates().create(
            CreateWhatsAppTemplateRequest.builder()
                .sender("+15559876543").name("order_shipped").language("en_US").category("UTILITY").build()));
    }

    // ==================== templates().update() Tests ====================

    @Test
    void testTemplatesUpdate_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"wat_1\",\"name\":\"order_shipped\",\"language\":\"en_US\"," +
            "\"category\":\"UTILITY\",\"status\":\"PENDING\",\"qualityRating\":null," +
            "\"rejectionReason\":null,\"createdAt\":\"2026-07-28T10:00:00.000Z\"," +
            "\"updatedAt\":\"2026-07-31T10:00:00.000Z\"}"
        ));

        WhatsAppTemplate template = client.whatsapp().templates().update("wat_1",
            UpdateWhatsAppTemplateRequest.builder()
                .body("Hi {{1}}, your order {{2}} is on its way!")
                .examples(Map.of("1", "Sam", "2", "#4821"))
                .build());

        assertEquals("wat_1", template.getId());
        assertEquals("PENDING", template.getStatus());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/templates/wat_1"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"body\":\"Hi {{1}}, your order {{2}} is on its way!\""));
        assertFalse(body.contains("footer"));
        assertFalse(body.contains("header"));
        assertFalse(body.contains("buttons"));
    }

    @Test
    void testTemplatesUpdate_missingArgs_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().templates().update(null,
                UpdateWhatsAppTemplateRequest.builder().body("Hi!").build());
        });
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().templates().update("wat_1", null);
        });
    }

    // ==================== templates().delete() Tests ====================

    @Test
    void testTemplatesDelete_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"wat_1\",\"deleted\":true}"));

        WhatsAppTemplateDeletedResponse response = client.whatsapp().templates().delete("wat_1");

        assertEquals("wat_1", response.getId());
        assertTrue(response.isDeleted());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertTrue(request.getPath().contains("/whatsapp/templates/wat_1"));
    }

    @Test
    void testTemplatesDelete_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().templates().delete(null);
        });
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().templates().delete("");
        });
    }

    @Test
    void testTemplatesDelete_404_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.whatsapp().templates().delete("wat_missing");
        });
    }

    // ==================== window() Tests ====================

    @Test
    void testWindow_open() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"open\":true,\"expiresAt\":\"2026-07-31T20:00:00.000Z\"}"
        ));

        WhatsAppWindow window = client.whatsapp().window("+15559876543", "+15551234567");

        assertTrue(window.isOpen());
        assertEquals("2026-07-31T20:00:00.000Z", window.getExpiresAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        String path = request.getPath();
        assertTrue(path.contains("/whatsapp/window"));
        assertTrue(path.contains("from=%2B15559876543"));
        assertTrue(path.contains("to=%2B15551234567"));
    }

    @Test
    void testWindow_closed() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"open\":false,\"expiresAt\":null}"));

        WhatsAppWindow window = client.whatsapp().window("+15559876543", "+15551234567");

        assertFalse(window.isOpen());
        assertNull(window.getExpiresAt());
    }

    @Test
    void testWindow_invalidPhones_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().window("invalid", "+15551234567");
        });
        assertThrows(ValidationException.class, () -> {
            client.whatsapp().window("+15559876543", "invalid");
        });
    }
}
