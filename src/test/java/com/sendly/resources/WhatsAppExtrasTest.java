package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.CreateWhatsAppSignupRequest;
import com.sendly.models.UpdateWhatsAppConversationalComponentsRequest;
import com.sendly.models.WhatsAppCallingSettings;
import com.sendly.models.WhatsAppCommand;
import com.sendly.models.WhatsAppConversationalComponents;
import com.sendly.models.WhatsAppSender;
import com.sendly.models.WhatsAppSenderProfile;
import com.sendly.models.WhatsAppSendersResponse;
import com.sendly.models.WhatsAppSignup;
import com.sendly.models.WhatsAppSignupSession;
import com.sendly.models.WhatsAppVerificationMethod;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the WhatsApp extras: profile photo, conversational components,
 * calling, adding a number by code.
 */
class WhatsAppExtrasTest {
    private static final String SENDER = "+15125550188";
    private static final String SENDER_PATH = "/api/v1/whatsapp/senders/%2B15125550188";
    private static final byte[] PNG_BYTES = new byte[] {
        (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x01, 0x02, 0x03
    };

    private static final String PROFILE_JSON =
        "{\"phoneNumber\":\"+15125550188\",\"displayName\":\"Acme Inc\"," +
        "\"profilePhotoUrl\":\"https://cdn.example.com/acme.png\",\"category\":\"RETAIL\"," +
        "\"about\":\"Fast delivery\",\"description\":null,\"email\":null,\"website\":null,\"address\":null}";

    private static final String PROFILE_NO_PHOTO_JSON =
        "{\"phoneNumber\":\"+15125550188\",\"displayName\":\"Acme Inc\",\"profilePhotoUrl\":null," +
        "\"category\":\"RETAIL\",\"about\":\"Fast delivery\",\"description\":null,\"email\":null," +
        "\"website\":null,\"address\":null}";

    private static final String COMPONENTS_JSON =
        "{\"phoneNumber\":\"+15125550188\",\"iceBreakers\":[\"What are your hours?\",\"Track my order\"]," +
        "\"commands\":[{\"command\":\"menu\",\"description\":\"See today's menu\"}]}";

    private static final String VERIFYING_SIGNUP_JSON =
        "{\"id\":\"was_456\",\"status\":\"verifying\",\"phoneNumber\":\"+15125550199\"," +
        "\"businessAccountId\":\"102938475610\",\"failureReasons\":null," +
        "\"verificationMethod\":\"voice\",\"verificationAttemptsRemaining\":5," +
        "\"updatedAt\":\"2026-10-01T10:00:00.000Z\"}";

    private static final String ACTIVE_SIGNUP_JSON =
        "{\"id\":\"was_456\",\"status\":\"active\",\"phoneNumber\":\"+15125550199\"," +
        "\"businessAccountId\":\"102938475610\",\"failureReasons\":null," +
        "\"updatedAt\":\"2026-10-01T10:02:00.000Z\"}";

    private MockWebServer mockServer;
    private Sendly client;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        client = clientWithRetries(0);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private Sendly clientWithRetries(int retries) {
        return new Sendly("sk_live_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(retries));
    }

    private static MockResponse apiError(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setBody(body)
                .addHeader("Content-Type", "application/json");
    }

    private static JsonObject bodyOf(RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    // ==================== senders().list() new fields ====================

    @Test
    void testSendersList_parsesAccountAndCallingFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"senders\":[" +
            "{\"phoneNumber\":\"+15125550188\",\"displayName\":\"Acme Inc\",\"status\":\"active\"," +
            "\"qualityRating\":\"GREEN\",\"businessAccountId\":\"102938475610\",\"businessName\":\"Acme Inc\"," +
            "\"callingEnabled\":true,\"outboundCallingAllowed\":false,\"createdAt\":\"2026-09-30T10:00:00.000Z\"}," +
            "{\"phoneNumber\":\"+447700900188\",\"displayName\":null,\"status\":\"pending\"," +
            "\"qualityRating\":null,\"businessAccountId\":null,\"businessName\":null," +
            "\"callingEnabled\":false,\"outboundCallingAllowed\":true,\"createdAt\":\"2026-09-30T11:00:00.000Z\"}]}"
        ));

        WhatsAppSendersResponse response = client.whatsapp().senders().list();

        WhatsAppSender active = response.getSenders().get(0);
        assertEquals("102938475610", active.getBusinessAccountId());
        assertEquals("Acme Inc", active.getBusinessName());
        assertTrue(active.isCallingEnabled());
        assertFalse(active.isOutboundCallingAllowed());

        WhatsAppSender pending = response.getSenders().get(1);
        assertNull(pending.getBusinessAccountId());
        assertNull(pending.getBusinessName());
        assertFalse(pending.isCallingEnabled());
        assertTrue(pending.isOutboundCallingAllowed());
    }

    // ==================== profile photo ====================

    @Test
    void testUploadProfilePhoto_file_sendsMultipartFileField() throws Exception {
        File photo = tempDir.resolve("logo.png").toFile();
        Files.write(photo.toPath(), PNG_BYTES);
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));

        WhatsAppSenderProfile profile = client.whatsapp().senders().uploadProfilePhoto(SENDER, photo);

        assertEquals("https://cdn.example.com/acme.png", profile.getProfilePhotoUrl());
        assertEquals("Acme Inc", profile.getDisplayName());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(SENDER_PATH + "/profile/photo", request.getPath());
        assertTrue(request.getHeader("Content-Type").startsWith("multipart/form-data"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("name=\"file\"; filename=\"logo.png\""));
        assertTrue(body.contains("Content-Type: image/png"));
    }

    @Test
    void testUploadProfilePhoto_bytes_sendsMultipartFileField() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));

        WhatsAppSenderProfile profile = client.whatsapp().senders()
                .uploadProfilePhoto(SENDER, PNG_BYTES, "logo.png");

        assertEquals("https://cdn.example.com/acme.png", profile.getProfilePhotoUrl());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals(SENDER_PATH + "/profile/photo", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("name=\"file\"; filename=\"logo.png\""));
    }

    @Test
    void testUploadProfilePhoto_missingArgs_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, (File) null));
        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, tempDir.resolve("missing.png").toFile()));
        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, new byte[0], "logo.png"));
        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto("15125550188", PNG_BYTES, "logo.png"));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testUploadProfilePhoto_emptyFile_throwsValidationException() throws Exception {
        File empty = tempDir.resolve("empty.png").toFile();
        Files.write(empty.toPath(), new byte[0]);

        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, empty));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testUploadProfilePhoto_invalidImage_surfacesApiCode() {
        mockServer.enqueue(apiError(400,
            "{\"error\":\"whatsapp_profile_photo_invalid\",\"message\":\"The photo must be a JPEG or PNG image.\"}"));

        ValidationException e = assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, PNG_BYTES, "logo.gif"));
        assertEquals("whatsapp_profile_photo_invalid", e.getApiErrorCode());
    }

    @Test
    void testUploadProfilePhoto_serverError_isThrownOnFirstAttempt() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(502,
            "{\"error\":\"whatsapp_profile_update_failed\",\"message\":\"The photo couldn't be uploaded. " +
            "WhatsApp needs a square JPEG or PNG at least 192 pixels wide. Please try again shortly.\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));

        SendlyException e = assertThrows(SendlyException.class, () ->
            retrying.whatsapp().senders().uploadProfilePhoto(SENDER, PNG_BYTES, "logo.png"));
        assertEquals("whatsapp_profile_update_failed", e.getApiErrorCode());
        assertEquals(502, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testUploadProfilePhoto_408_isThrownOnFirstAttempt() {
        mockServer.enqueue(apiError(408,
            "{\"error\":\"request_timeout\",\"message\":\"Request timed out\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));

        SendlyException e = assertThrows(SendlyException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, PNG_BYTES, "logo.png"));
        assertEquals(408, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testUploadProfilePhoto_reusedConnectionDroppedAfterRequest_isThrownOnFirstAttempt() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));

        client.whatsapp().senders().getProfile(SENDER);
        assertThrows(NetworkException.class, () ->
            client.whatsapp().senders().uploadProfilePhoto(SENDER, PNG_BYTES, "logo.png"));
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testUploadProfilePhoto_rateLimited_isRetriedWithTheWholePhoto() throws Exception {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(429,
            "{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests\",\"retryAfter\":0}"));
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_JSON));

        WhatsAppSenderProfile profile = retrying.whatsapp().senders()
                .uploadProfilePhoto(SENDER, PNG_BYTES, "logo.png");

        assertEquals("https://cdn.example.com/acme.png", profile.getProfilePhotoUrl());
        assertEquals(2, mockServer.getRequestCount());
        RecordedRequest first = mockServer.takeRequest();
        RecordedRequest second = mockServer.takeRequest();
        assertTrue(second.getHeader("Content-Type").startsWith("multipart/form-data"));
        assertEquals(first.getBodySize(), second.getBodySize());
        assertTrue(second.getBody().readUtf8().contains("name=\"file\"; filename=\"logo.png\""));
    }

    @Test
    void testDeleteProfilePhoto_returnsProfileWithoutPhoto() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(PROFILE_NO_PHOTO_JSON));

        WhatsAppSenderProfile profile = client.whatsapp().senders().deleteProfilePhoto(SENDER);

        assertNull(profile.getProfilePhotoUrl());
        assertEquals("Acme Inc", profile.getDisplayName());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals(SENDER_PATH + "/profile/photo", request.getPath());
    }

    // ==================== conversational components ====================

    @Test
    void testGetConversationalComponents_parsesBothLists() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(COMPONENTS_JSON));

        WhatsAppConversationalComponents components =
            client.whatsapp().senders().getConversationalComponents(SENDER);

        assertEquals(SENDER, components.getPhoneNumber());
        assertEquals(List.of("What are your hours?", "Track my order"), components.getIceBreakers());
        assertEquals(1, components.getCommands().size());
        assertEquals("menu", components.getCommands().get(0).getCommand());
        assertEquals("See today's menu", components.getCommands().get(0).getDescription());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(SENDER_PATH + "/conversational_components", request.getPath());
    }

    @Test
    void testGetConversationalComponents_emptyLists() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"phoneNumber\":\"+15125550188\",\"iceBreakers\":[],\"commands\":[]}"));

        WhatsAppConversationalComponents components =
            client.whatsapp().senders().getConversationalComponents(SENDER);

        assertTrue(components.getIceBreakers().isEmpty());
        assertTrue(components.getCommands().isEmpty());
    }

    @Test
    void testUpdateConversationalComponents_sendsOnlyGivenLists() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(COMPONENTS_JSON));

        WhatsAppConversationalComponents components = client.whatsapp().senders()
            .updateConversationalComponents(SENDER, UpdateWhatsAppConversationalComponentsRequest.builder()
                .iceBreakers(List.of("What are your hours?", "Track my order"))
                .commands(List.of(new WhatsAppCommand("menu", "See today's menu")))
                .build());

        assertEquals(2, components.getIceBreakers().size());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertEquals(SENDER_PATH + "/conversational_components", request.getPath());
        JsonObject body = bodyOf(request);
        assertEquals(2, body.size());
        assertEquals("What are your hours?", body.getAsJsonArray("iceBreakers").get(0).getAsString());
        JsonObject command = body.getAsJsonArray("commands").get(0).getAsJsonObject();
        assertEquals("menu", command.get("command").getAsString());
        assertEquals("See today's menu", command.get("description").getAsString());
    }

    @Test
    void testUpdateConversationalComponents_emptyListClearsAndOtherIsOmitted() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"phoneNumber\":\"+15125550188\",\"iceBreakers\":[],\"commands\":[{\"command\":\"menu\",\"description\":\"See today's menu\"}]}"));

        client.whatsapp().senders().updateConversationalComponents(SENDER,
            UpdateWhatsAppConversationalComponentsRequest.builder().iceBreakers(List.of()).build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(1, body.size());
        assertEquals(0, body.getAsJsonArray("iceBreakers").size());
        assertFalse(body.has("commands"));
    }

    @Test
    void testUpdateConversationalComponents_missingArgs_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().updateConversationalComponents(SENDER, null));
        assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().updateConversationalComponents("not-a-number",
                UpdateWhatsAppConversationalComponentsRequest.builder().iceBreakers(List.of()).build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    // ==================== calling ====================

    @Test
    void testSetCalling_sendsEnabledAndParsesResult() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"phoneNumber\":\"+15125550188\",\"callingEnabled\":true,\"outboundCallingAllowed\":false}"));

        WhatsAppCallingSettings settings = client.whatsapp().senders().setCalling(SENDER, true);

        assertEquals(SENDER, settings.getPhoneNumber());
        assertTrue(settings.isCallingEnabled());
        assertFalse(settings.isOutboundCallingAllowed());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertEquals(SENDER_PATH + "/calling", request.getPath());
        JsonObject body = bodyOf(request);
        assertEquals(1, body.size());
        assertTrue(body.get("enabled").getAsBoolean());
    }

    @Test
    void testSetCalling_off_sendsFalse() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"phoneNumber\":\"+15125550188\",\"callingEnabled\":false,\"outboundCallingAllowed\":false}"));

        WhatsAppCallingSettings settings = client.whatsapp().senders().setCalling(SENDER, false);

        assertFalse(settings.isCallingEnabled());
        assertFalse(bodyOf(mockServer.takeRequest()).get("enabled").getAsBoolean());
    }

    @Test
    void testSetCalling_unavailable_surfacesApiCode() {
        mockServer.enqueue(apiError(422,
            "{\"error\":\"whatsapp_calling_unavailable\",\"message\":\"WhatsApp didn't allow calling on this number.\"}"));

        ValidationException e = assertThrows(ValidationException.class, () ->
            client.whatsapp().senders().setCalling(SENDER, true));
        assertEquals("whatsapp_calling_unavailable", e.getApiErrorCode());
        assertEquals(422, e.getStatusCode());
    }

    // ==================== signup by code ====================

    @Test
    void testSignupCreate_toConnectedAccount_sendsAdditionFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON).setResponseCode(201));

        WhatsAppSignupSession signup = client.whatsapp().signup().create(
            CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199")
                .businessAccountId("102938475610")
                .verificationMethod(WhatsAppVerificationMethod.VOICE)
                .displayName("Acme Inc")
                .build());

        assertEquals("was_456", signup.getId());
        assertEquals("verifying", signup.getStatus());
        assertNull(signup.getConnectUrl());
        assertEquals("+15125550199", signup.getPhoneNumber());
        assertEquals("102938475610", signup.getBusinessAccountId());
        assertEquals("voice", signup.getVerificationMethod());
        assertEquals(Integer.valueOf(5), signup.getVerificationAttemptsRemaining());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/whatsapp/signup", request.getPath());
        JsonObject body = bodyOf(request);
        assertEquals("+15125550199", body.get("phoneNumber").getAsString());
        assertEquals("102938475610", body.get("businessAccountId").getAsString());
        assertEquals("voice", body.get("verificationMethod").getAsString());
        assertEquals("Acme Inc", body.get("displayName").getAsString());
    }

    @Test
    void testSignupCreate_requestWithOnlyPhone_sendsOnlyPhone() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_123\",\"connectUrl\":\"https://sendly.live/whatsapp/connect?token=tok_abc\",\"status\":\"initiated\"}"
        ).setResponseCode(201));

        WhatsAppSignupSession signup = client.whatsapp().signup().create(
            CreateWhatsAppSignupRequest.builder().phoneNumber("+15125550199").build());

        assertEquals("https://sendly.live/whatsapp/connect?token=tok_abc", signup.getConnectUrl());
        assertNull(signup.getBusinessAccountId());
        assertNull(signup.getVerificationMethod());
        assertNull(signup.getVerificationAttemptsRemaining());
        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(1, body.size());
        assertEquals("+15125550199", body.get("phoneNumber").getAsString());
    }

    @Test
    void testSignupCreate_requestValidation() {
        assertThrows(ValidationException.class, () ->
            client.whatsapp().signup().create((CreateWhatsAppSignupRequest) null));
        assertThrows(ValidationException.class, () ->
            client.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("15125550199").businessAccountId("102938475610").build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testSignupCreate_blankBusinessAccountId_throwsBeforeSending() {
        assertThrows(ValidationException.class, () ->
            client.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199").businessAccountId("").build()));
        assertThrows(ValidationException.class, () ->
            client.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199").businessAccountId("   ").build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testSignupCreate_verificationStartUnreachable_isThrownOnFirstAttempt() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(502,
            "{\"error\":\"whatsapp_verification_start_failed\"," +
            "\"message\":\"WhatsApp couldn't start verifying this number. Any setup fee is refunded automatically. " +
            "Please try again shortly.\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON).setResponseCode(201));

        SendlyException e = assertThrows(SendlyException.class, () ->
            retrying.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199")
                .businessAccountId("102938475610")
                .build()));
        assertEquals("whatsapp_verification_start_failed", e.getApiErrorCode());
        assertEquals(502, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupCreate_toConnectedAccount_serverError_isThrownOnFirstAttempt() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(500,
            "{\"error\":\"internal_error\",\"message\":\"Something went wrong asking WhatsApp for the code. " +
            "Any setup fee is refunded automatically. Please try again.\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON).setResponseCode(201));

        SendlyException e = assertThrows(SendlyException.class, () ->
            retrying.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199")
                .businessAccountId("102938475610")
                .build()));
        assertEquals(500, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupCreate_toConnectedAccount_503RetryAfterZero_isThrownOnFirstAttempt() {
        mockServer.enqueue(apiError(503,
            "{\"error\":\"server_error\",\"message\":\"Something went wrong. Please try again.\"}")
            .addHeader("Retry-After", "0"));
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON).setResponseCode(201));

        SendlyException e = assertThrows(SendlyException.class, () ->
            client.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199")
                .businessAccountId("102938475610")
                .build()));
        assertEquals(503, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupCreate_toConnectedAccount_reusedConnectionDroppedAfterRequest_isThrownOnFirstAttempt()
            throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON));
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON).setResponseCode(201));

        client.whatsapp().signup().get("was_456");
        assertThrows(NetworkException.class, () ->
            client.whatsapp().signup().create(CreateWhatsAppSignupRequest.builder()
                .phoneNumber("+15125550199")
                .businessAccountId("102938475610")
                .build()));
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testSignupCreate_facebookRequest_serverErrorIsStillRetried() throws Exception {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(500,
            "{\"error\":\"server_error\",\"message\":\"Something went wrong. Please try again.\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_123\",\"connectUrl\":\"https://sendly.live/whatsapp/connect?token=tok_abc\",\"status\":\"initiated\"}"
        ).setResponseCode(201));

        WhatsAppSignupSession signup = retrying.whatsapp().signup().create(
            CreateWhatsAppSignupRequest.builder().phoneNumber("+15125550199").build());

        assertEquals("initiated", signup.getStatus());
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testSignupGet_verifying_parsesVerificationFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_456\",\"status\":\"verifying\",\"phoneNumber\":\"+15125550199\"," +
            "\"businessAccountId\":\"102938475610\",\"failureReasons\":null," +
            "\"verificationMethod\":\"sms\",\"verificationAttemptsRemaining\":4," +
            "\"updatedAt\":\"2026-10-01T10:00:00.000Z\",\"verificationCode\":\"482913\"}"));

        WhatsAppSignup signup = client.whatsapp().signup().get("was_456");

        assertEquals("verifying", signup.getStatus());
        assertEquals("sms", signup.getVerificationMethod());
        assertEquals(Integer.valueOf(4), signup.getVerificationAttemptsRemaining());
        assertEquals("482913", signup.getVerificationCode());
    }

    @Test
    void testSignupGet_verifyingWithoutCode_nullCode() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"was_456\",\"status\":\"verifying\",\"phoneNumber\":\"+15125550199\"," +
            "\"businessAccountId\":\"102938475610\",\"failureReasons\":null," +
            "\"verificationMethod\":\"sms\",\"verificationAttemptsRemaining\":5," +
            "\"updatedAt\":\"2026-10-01T10:00:00.000Z\",\"verificationCode\":null}"));

        WhatsAppSignup signup = client.whatsapp().signup().get("was_456");

        assertNull(signup.getVerificationCode());
        assertEquals(Integer.valueOf(5), signup.getVerificationAttemptsRemaining());
    }

    @Test
    void testSignupGet_active_noVerificationFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        WhatsAppSignup signup = client.whatsapp().signup().get("was_456");

        assertNull(signup.getVerificationMethod());
        assertNull(signup.getVerificationAttemptsRemaining());
        assertNull(signup.getVerificationCode());
    }

    @Test
    void testSignupVerify_sendsCodeAndReturnsActiveSignup() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        WhatsAppSignup signup = client.whatsapp().signup().verify("was_456", "482913");

        assertEquals("active", signup.getStatus());
        assertEquals("102938475610", signup.getBusinessAccountId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/whatsapp/signup/was_456/verify", request.getPath());
        JsonObject body = bodyOf(request);
        assertEquals(1, body.size());
        assertEquals("482913", body.get("code").getAsString());
    }

    @Test
    void testSignupVerify_wrongCode_exposesAttemptsRemaining() {
        mockServer.enqueue(apiError(422,
            "{\"error\":\"whatsapp_verification_code_invalid\"," +
            "\"message\":\"That code wasn't accepted. Check it, or request a new one.\",\"attemptsRemaining\":4}"));

        ValidationException e = assertThrows(ValidationException.class, () ->
            client.whatsapp().signup().verify("was_456", "000000"));
        assertEquals("whatsapp_verification_code_invalid", e.getApiErrorCode());
        assertEquals(422, e.getStatusCode());
        assertEquals(4, e.getResponseBody().get("attemptsRemaining").getAsInt());
    }

    @Test
    void testSignupVerify_tooManyWrongCodes_isThrownOnFirstAttempt() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(409,
            "{\"error\":\"whatsapp_verification_failed\"," +
            "\"message\":\"Too many wrong codes. Any setup fee is refunded automatically. Start again to retry.\"}"));
        mockServer.enqueue(apiError(409,
            "{\"error\":\"signup_not_active\"," +
            "\"message\":\"This connection isn't waiting for a verification code. Start again to retry.\"}"));

        SendlyException e = assertThrows(SendlyException.class, () ->
            retrying.whatsapp().signup().verify("was_456", "000000"));
        assertEquals("whatsapp_verification_failed", e.getApiErrorCode());
        assertEquals(409, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_serverError_isThrownOnFirstAttempt() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(502,
            "{\"error\":\"whatsapp_activation_pending\",\"message\":\"WhatsApp accepted the code, but we couldn't " +
            "finish connecting the number. Our team has been alerted; check back shortly.\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        SendlyException e = assertThrows(SendlyException.class, () ->
            retrying.whatsapp().signup().verify("was_456", "482913"));
        assertEquals("whatsapp_activation_pending", e.getApiErrorCode());
        assertEquals(502, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_droppedConnection_isThrownOnFirstAttempt() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        assertThrows(NetworkException.class, () ->
            retrying.whatsapp().signup().verify("was_456", "482913"));
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_408_isThrownOnFirstAttempt() {
        mockServer.enqueue(apiError(408,
            "{\"error\":\"request_timeout\",\"message\":\"Request timed out\"}"));
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        SendlyException e = assertThrows(SendlyException.class, () ->
            client.whatsapp().signup().verify("was_456", "482913"));
        assertEquals(408, e.getStatusCode());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_reusedConnectionDroppedAfterRequest_isThrownOnFirstAttempt() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON));
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        client.whatsapp().signup().get("was_456");
        assertThrows(NetworkException.class, () ->
            client.whatsapp().signup().verify("was_456", "482913"));
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_rateLimited_isRetried() throws Exception {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(429,
            "{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests\",\"retryAfter\":0}"));
        mockServer.enqueue(TestHelpers.mockSuccess(ACTIVE_SIGNUP_JSON));

        WhatsAppSignup signup = retrying.whatsapp().signup().verify("was_456", "482913");

        assertEquals("active", signup.getStatus());
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.whatsapp().signup().verify("", "482913"));
        assertThrows(ValidationException.class, () -> client.whatsapp().signup().verify(null, "482913"));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testSignupVerify_missingCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.whatsapp().signup().verify("was_456", null));
        assertThrows(ValidationException.class, () -> client.whatsapp().signup().verify("was_456", ""));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testSignupResend_withMethod_sendsMethod() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON));

        WhatsAppSignup signup = client.whatsapp().signup().resend("was_456", WhatsAppVerificationMethod.VOICE);

        assertEquals("verifying", signup.getStatus());
        assertEquals("voice", signup.getVerificationMethod());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/whatsapp/signup/was_456/resend", request.getPath());
        JsonObject body = bodyOf(request);
        assertEquals(1, body.size());
        assertEquals("voice", body.get("verificationMethod").getAsString());
    }

    @Test
    void testSignupResend_withoutMethod_sendsEmptyObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VERIFYING_SIGNUP_JSON));

        client.whatsapp().signup().resend("was_456");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/whatsapp/signup/was_456/resend", request.getPath());
        assertEquals(0, bodyOf(request).size());
    }

    @Test
    void testSignupResend_tooSoon_isRateLimitWithRetryAfter() {
        Sendly retrying = clientWithRetries(3);
        mockServer.enqueue(apiError(429,
            "{\"error\":\"whatsapp_verification_resend_too_soon\"," +
            "\"message\":\"Wait 30 seconds before requesting another code.\",\"retryAfter\":30}")
            .addHeader("Retry-After", "30"));

        RateLimitException e = assertThrows(RateLimitException.class, () ->
            retrying.whatsapp().signup().resend("was_456"));
        assertEquals("whatsapp_verification_resend_too_soon", e.getApiErrorCode());
        assertEquals(30, e.getRetryAfter());
        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    void testSignupResend_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.whatsapp().signup().resend(""));
        assertThrows(ValidationException.class, () -> client.whatsapp().signup().resend(null, "sms"));
        assertEquals(0, mockServer.getRequestCount());
    }
}
