package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.CallErrorCode;
import com.sendly.models.CreateVoiceAgentRequest;
import com.sendly.models.DeletedVoiceAgent;
import com.sendly.models.EmergencyAddress;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.TestVoiceAgentRequest;
import com.sendly.models.UpdateVoiceAgentRequest;
import com.sendly.models.UpdateVoiceNumberRequest;
import com.sendly.models.Voice;
import com.sendly.models.VoiceAgent;
import com.sendly.models.VoiceAgentConnection;
import com.sendly.models.VoiceAgentConnectionInput;
import com.sendly.models.VoiceAgentKind;
import com.sendly.models.VoiceAgentListResponse;
import com.sendly.models.VoiceAgentTest;
import com.sendly.models.VoiceAgentTestOutcome;
import com.sendly.models.VoiceAgentTestStep;
import com.sendly.models.VoiceAgentTestStepName;
import com.sendly.models.VoiceAgentTools;
import com.sendly.models.VoiceListResponse;
import com.sendly.models.VoiceMode;
import com.sendly.models.VoiceNumber;
import com.sendly.models.VoiceNumberEmergencyAddress;
import com.sendly.models.VoiceNumberListResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Voice resource: numbers, emergency addresses, agents and voices.
 */
class VoiceTest {
    private static final String NUMBER_ID = "5f0c1c2e-2a44-4d4b-9d51-0a9b0f6f4a11";
    private static final String PHONE = "+15555550188";
    private static final String AGENT_ID = "3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b";

    private static final String AGENT_NUMBER_JSON =
        "{\"id\":\"" + NUMBER_ID + "\",\"object\":\"voice_number\",\"phoneNumber\":\"" + PHONE + "\"," +
        "\"phoneNumberType\":\"local\",\"countryCode\":\"US\",\"isDefault\":true," +
        "\"voiceEnabled\":true,\"voiceMode\":\"agent\",\"agentId\":\"" + AGENT_ID + "\"," +
        "\"emergencyAddress\":{\"status\":\"active\",\"address\":{\"street\":\"500 Example Ave\",\"unit\":\"Suite 2\"," +
        "\"city\":\"Austin\",\"state\":\"TX\",\"zip\":\"78701\",\"country\":\"US\"}}," +
        "\"ratePerMinute\":{\"inbound\":2,\"outbound\":2,\"agent\":10}}";

    private static final String VOICE_OFF_NUMBER_JSON =
        "{\"id\":\"num_2\",\"object\":\"voice_number\",\"phoneNumber\":\"+15555550199\"," +
        "\"phoneNumberType\":\"local\",\"countryCode\":\"US\",\"isDefault\":false," +
        "\"voiceEnabled\":false,\"voiceMode\":\"none\",\"agentId\":null,\"emergencyAddress\":null," +
        "\"ratePerMinute\":{\"inbound\":2,\"outbound\":2,\"agent\":10}}";

    private static final String AGENT_JSON =
        "{\"id\":\"" + AGENT_ID + "\",\"object\":\"voice_agent\",\"name\":\"Front desk\",\"enabled\":true," +
        "\"kind\":\"sendly\",\"connection\":null,\"hasSigningSecret\":false," +
        "\"voice\":\"ashley\",\"voiceLabel\":\"Ashley (US, warm)\",\"language\":\"en-US\"," +
        "\"greeting\":\"Thanks for calling Acme, how can I help?\"," +
        "\"instructions\":\"Answer questions about opening hours.\"," +
        "\"tools\":{\"sendSms\":true,\"transferTo\":null},\"canSendSms\":true," +
        "\"callsHandled\":12,\"avgDurationSecs\":74," +
        "\"createdAt\":\"2026-09-14T17:00:00.000Z\",\"updatedAt\":\"2026-09-14T17:05:00.000Z\"}";

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

    private static MockResponse created(String body) {
        return TestHelpers.mockSuccess(body).setResponseCode(201);
    }

    private static MockResponse apiError(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setBody(body)
                .addHeader("Content-Type", "application/json");
    }

    private static MockResponse apiError(int status, String code, String message) {
        return apiError(status, "{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }

    private static JsonObject bodyOf(RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static EmergencyAddress.Builder austin() {
        return EmergencyAddress.builder()
                .street("500 Example Ave")
                .unit("Suite 2")
                .city("Austin")
                .state("TX")
                .zip("78701");
    }

    @Test
    void testVoiceAccessor_exposesSubResources() {
        assertSame(client.voice(), client.voice());
        assertNotNull(client.voice().numbers());
        assertNotNull(client.voice().agents());
        assertNotNull(client.voice().voices());
    }

    // ==================== numbers().list() / get() ====================

    @Test
    void testNumbersList_unwrapsData() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + AGENT_NUMBER_JSON + "," + VOICE_OFF_NUMBER_JSON + "]}"));

        VoiceNumberListResponse list = client.voice().numbers().list();

        assertEquals(2, list.getData().size());
        VoiceNumber number = list.getData().get(0);
        assertEquals(NUMBER_ID, number.getId());
        assertEquals("voice_number", number.getObject());
        assertEquals(PHONE, number.getPhoneNumber());
        assertEquals("local", number.getPhoneNumberType());
        assertEquals("US", number.getCountryCode());
        assertTrue(number.isDefault());
        assertTrue(number.isVoiceEnabled());
        assertEquals(VoiceMode.AGENT, number.getVoiceMode());
        assertEquals(AGENT_ID, number.getAgentId());
        VoiceNumberEmergencyAddress emergency = number.getEmergencyAddress();
        assertNotNull(emergency);
        assertEquals(VoiceNumberEmergencyAddress.STATUS_ACTIVE, emergency.getStatus());
        assertTrue(emergency.isOnFile());
        assertEquals("500 Example Ave", emergency.getAddress().getStreet());
        assertEquals("Suite 2", emergency.getAddress().getUnit());
        assertEquals("Austin", emergency.getAddress().getCity());
        assertEquals("TX", emergency.getAddress().getState());
        assertEquals("78701", emergency.getAddress().getZip());
        assertEquals("US", emergency.getAddress().getCountry());
        assertEquals(2, number.getRatePerMinute().getInbound());
        assertEquals(2, number.getRatePerMinute().getOutbound());
        assertEquals(10, number.getRatePerMinute().getAgent());

        VoiceNumber off = list.getData().get(1);
        assertFalse(off.isVoiceEnabled());
        assertEquals(VoiceMode.NONE, off.getVoiceMode());
        assertNull(off.getAgentId());
        assertNull(off.getEmergencyAddress());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/numbers"));
        assertNull(request.getHeader("Idempotency-Key"));
    }

    @Test
    void testNumbersList_empty() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[]}"));

        assertTrue(client.voice().numbers().list().getData().isEmpty());
    }

    @Test
    void testNumbersGet_byE164_encodesPlus() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON));

        VoiceNumber number = client.voice().numbers().get(PHONE);

        assertEquals(PHONE, number.getPhoneNumber());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/numbers/%2B15555550188"));
    }

    @Test
    void testNumbersGet_byId() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON));

        client.voice().numbers().get(NUMBER_ID);

        assertTrue(mockServer.takeRequest().getPath().endsWith("/voice/numbers/" + NUMBER_ID));
    }

    @Test
    void testNumbersGet_provisioningWithoutUnit() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON
                .replace("\"status\":\"active\"", "\"status\":\"provisioning\"")
                .replace("\"unit\":\"Suite 2\",", "")));

        VoiceNumber number = client.voice().numbers().get(NUMBER_ID);

        assertEquals(VoiceNumberEmergencyAddress.STATUS_PROVISIONING, number.getEmergencyAddress().getStatus());
        assertTrue(number.getEmergencyAddress().isOnFile());
        assertNull(number.getEmergencyAddress().getAddress().getUnit());
    }

    @Test
    void testNumbersGet_failedRegistrationWithoutAddress() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VOICE_OFF_NUMBER_JSON
                .replace("\"emergencyAddress\":null", "\"emergencyAddress\":{\"status\":\"failed\",\"address\":null}")));

        VoiceNumber number = client.voice().numbers().get("num_2");

        assertEquals("failed", number.getEmergencyAddress().getStatus());
        assertFalse(number.getEmergencyAddress().isOnFile());
        assertNull(number.getEmergencyAddress().getAddress());
    }

    @Test
    void testNumbersGet_missingNumber_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.voice().numbers().get(null));
        assertThrows(ValidationException.class, () -> client.voice().numbers().get(""));
        assertThrows(ValidationException.class, () -> client.voice().numbers().get("  "));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testNumbersGet_404NumberNotFound_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "number_not_found", "This number isn't in your workspace."));

        NotFoundException e = assertThrows(NotFoundException.class, () -> client.voice().numbers().get("+15555550100"));

        assertEquals(CallErrorCode.NUMBER_NOT_FOUND, e.getApiErrorCode());
        assertEquals("This number isn't in your workspace.", e.getMessage());
    }

    // ==================== numbers().update() ====================

    @Test
    void testNumbersUpdate_sendsCamelCaseBody() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON));

        VoiceNumber number = client.voice().numbers().update(PHONE, UpdateVoiceNumberRequest.builder()
                .voiceEnabled(true)
                .voiceMode(VoiceMode.AGENT)
                .agentId(AGENT_ID)
                .build());

        assertEquals(VoiceMode.AGENT, number.getVoiceMode());
        assertEquals(AGENT_ID, number.getAgentId());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/numbers/%2B15555550188"));
        assertNull(request.getHeader("Idempotency-Key"));
        JsonObject body = bodyOf(request);
        assertTrue(body.get("voiceEnabled").getAsBoolean());
        assertEquals("agent", body.get("voiceMode").getAsString());
        assertEquals(AGENT_ID, body.get("agentId").getAsString());
        assertFalse(body.has("voiceAgentId"));
        assertEquals(3, body.size());
    }

    @Test
    void testNumbersUpdate_sendsOnlySetFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VOICE_OFF_NUMBER_JSON));

        client.voice().numbers().update("num_2", UpdateVoiceNumberRequest.builder()
                .voiceMode(VoiceMode.NONE)
                .build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(1, body.size());
        assertEquals("none", body.get("voiceMode").getAsString());
    }

    @Test
    void testNumbersUpdate_disable_sendsFalse() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VOICE_OFF_NUMBER_JSON));

        client.voice().numbers().update("num_2", UpdateVoiceNumberRequest.builder().voiceEnabled(false).build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(1, body.size());
        assertFalse(body.get("voiceEnabled").getAsBoolean());
    }

    @Test
    void testNumbersUpdate_callerIdempotencyKey_isSent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON));

        client.voice().numbers().update(PHONE, UpdateVoiceNumberRequest.builder().voiceEnabled(true).build(),
                new IdempotentRequestOptions("voice-on-0188"));

        assertEquals("voice-on-0188", mockServer.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void testNumbersUpdate_invalidInput_throwsValidationException() {
        UpdateVoiceNumberRequest request = UpdateVoiceNumberRequest.builder().voiceEnabled(true).build();
        assertThrows(ValidationException.class, () -> client.voice().numbers().update(null, request));
        assertThrows(ValidationException.class, () -> client.voice().numbers().update("", request));
        assertThrows(ValidationException.class, () -> client.voice().numbers().update(PHONE, null));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testNumbersUpdate_409AgentDisabled_carriesCode() {
        mockServer.enqueue(apiError(409, "agent_disabled",
                "That agent is switched off. Turn it on before pointing a number at it."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.voice().numbers().update(PHONE, UpdateVoiceNumberRequest.builder()
                        .voiceMode(VoiceMode.AGENT).agentId(AGENT_ID).build()));

        assertEquals(409, e.getStatusCode());
        assertEquals(CallErrorCode.AGENT_DISABLED, e.getApiErrorCode());
    }

    @Test
    void testNumbersUpdate_400AgentRequired_throwsValidationException() {
        mockServer.enqueue(apiError(400, "agent_required", "Choose an agent to answer this number."));

        ValidationException e = assertThrows(ValidationException.class, () ->
                client.voice().numbers().update(PHONE, UpdateVoiceNumberRequest.builder().voiceMode(VoiceMode.AGENT).build()));

        assertEquals(CallErrorCode.AGENT_REQUIRED, e.getApiErrorCode());
    }

    // ==================== numbers().registerEmergencyAddress() ====================

    @Test
    void testRegisterEmergencyAddress_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON.replace("\"status\":\"active\"", "\"status\":\"provisioning\"")));

        VoiceNumber number = client.voice().numbers().registerEmergencyAddress(PHONE, austin().build());

        assertEquals(VoiceNumberEmergencyAddress.STATUS_PROVISIONING, number.getEmergencyAddress().getStatus());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/numbers/%2B15555550188/emergency-address"));
        assertNotNull(request.getHeader("Idempotency-Key"));
        assertTrue(request.getHeader("Idempotency-Key").startsWith("sendly-java-retry-"));
        JsonObject body = bodyOf(request);
        assertEquals("500 Example Ave", body.get("street").getAsString());
        assertEquals("Suite 2", body.get("unit").getAsString());
        assertEquals("Austin", body.get("city").getAsString());
        assertEquals("TX", body.get("state").getAsString());
        assertEquals("78701", body.get("zip").getAsString());
        assertFalse(body.has("country"));
        assertEquals(5, body.size());
    }

    @Test
    void testRegisterEmergencyAddress_countryAndNoUnit() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_NUMBER_JSON));

        client.voice().numbers().registerEmergencyAddress(NUMBER_ID, EmergencyAddress.builder()
                .street("100 Example St")
                .city("Toronto")
                .state("ON")
                .zip("M5V 2T6")
                .country("CA")
                .build(), new IdempotentRequestOptions("e911-toronto"));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("e911-toronto", request.getHeader("Idempotency-Key"));
        assertTrue(request.getPath().endsWith("/voice/numbers/" + NUMBER_ID + "/emergency-address"));
        JsonObject body = bodyOf(request);
        assertEquals("CA", body.get("country").getAsString());
        assertFalse(body.has("unit"));
        assertEquals(5, body.size());
    }

    @Test
    void testRegisterEmergencyAddress_missingFields_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.voice().numbers().registerEmergencyAddress(PHONE, null));
        assertThrows(ValidationException.class, () -> client.voice().numbers().registerEmergencyAddress(null, austin().build()));
        assertThrows(ValidationException.class, () -> client.voice().numbers().registerEmergencyAddress(PHONE, austin().street(" ").build()));
        assertThrows(ValidationException.class, () -> client.voice().numbers().registerEmergencyAddress(PHONE, austin().city(null).build()));
        assertThrows(ValidationException.class, () -> client.voice().numbers().registerEmergencyAddress(PHONE, austin().state("").build()));
        assertThrows(ValidationException.class, () -> client.voice().numbers().registerEmergencyAddress(PHONE, austin().zip("").build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testRegisterEmergencyAddress_422InvalidAddress_exposesSuggested() {
        mockServer.enqueue(apiError(422,
                "{\"error\":\"invalid_address\",\"message\":\"We couldn't validate that address.\"," +
                "\"suggested\":{\"street\":\"500 Example Ave\",\"city\":\"Austin\",\"state\":\"TX\",\"zip\":\"78701\",\"country\":\"US\"}}"));

        ValidationException e = assertThrows(ValidationException.class, () ->
                client.voice().numbers().registerEmergencyAddress(PHONE, austin().zip("78799").build()));

        assertEquals(CallErrorCode.INVALID_ADDRESS, e.getApiErrorCode());
        assertEquals("We couldn't validate that address.", e.getMessage());
        assertNotNull(e.getResponseBody());
        assertEquals("78701", e.getResponseBody().getAsJsonObject("suggested").get("zip").getAsString());
    }

    @Test
    void testRegisterEmergencyAddress_400NotApplicable() {
        mockServer.enqueue(apiError(400, "e911_not_applicable", "Emergency registration applies to US and Canadian numbers."));

        ValidationException e = assertThrows(ValidationException.class, () ->
                client.voice().numbers().registerEmergencyAddress("num_uk", austin().build()));

        assertEquals(CallErrorCode.E911_NOT_APPLICABLE, e.getApiErrorCode());
    }

    // ==================== agents() ====================

    @Test
    void testAgentsList_unwrapsData() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + AGENT_JSON + "]}"));

        VoiceAgentListResponse list = client.voice().agents().list();

        assertEquals(1, list.getData().size());
        VoiceAgent agent = list.getData().get(0);
        assertEquals(AGENT_ID, agent.getId());
        assertEquals("voice_agent", agent.getObject());
        assertEquals("Front desk", agent.getName());
        assertTrue(agent.isEnabled());
        assertEquals("ashley", agent.getVoice());
        assertEquals("Ashley (US, warm)", agent.getVoiceLabel());
        assertEquals("en-US", agent.getLanguage());
        assertEquals("Thanks for calling Acme, how can I help?", agent.getGreeting());
        assertEquals("Answer questions about opening hours.", agent.getInstructions());
        assertTrue(agent.getTools().isSendSms());
        assertNull(agent.getTools().getTransferTo());
        assertTrue(agent.canSendSms());
        assertEquals(12, agent.getCallsHandled());
        assertEquals(74, agent.getAvgDurationSecs());
        assertEquals("2026-09-14T17:00:00.000Z", agent.getCreatedAt());
        assertEquals("2026-09-14T17:05:00.000Z", agent.getUpdatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/agents"));
    }

    @Test
    void testAgentsCreate_happyPath() throws Exception {
        mockServer.enqueue(created(AGENT_JSON));

        VoiceAgent agent = client.voice().agents().create(CreateVoiceAgentRequest.builder()
                .name("Front desk")
                .enabled(true)
                .voice("ashley")
                .language("en-US")
                .greeting("Thanks for calling Acme, how can I help?")
                .instructions("Answer questions about opening hours.")
                .tools(VoiceAgentTools.builder().sendSms(true).transferTo("+15125550123").build())
                .build());

        assertEquals(AGENT_ID, agent.getId());
        assertTrue(agent.canSendSms());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/agents"));
        assertNotNull(request.getHeader("Idempotency-Key"));
        assertTrue(request.getHeader("Idempotency-Key").startsWith("sendly-java-retry-"));
        JsonObject body = bodyOf(request);
        assertEquals("Front desk", body.get("name").getAsString());
        assertTrue(body.get("enabled").getAsBoolean());
        assertEquals("ashley", body.get("voice").getAsString());
        assertEquals("en-US", body.get("language").getAsString());
        assertEquals("Thanks for calling Acme, how can I help?", body.get("greeting").getAsString());
        assertEquals("Answer questions about opening hours.", body.get("instructions").getAsString());
        JsonObject tools = body.getAsJsonObject("tools");
        assertTrue(tools.get("sendSms").getAsBoolean());
        assertEquals("+15125550123", tools.get("transferTo").getAsString());
        assertEquals(2, tools.size());
        assertEquals(7, body.size());
    }

    @Test
    void testAgentsCreate_minimalBody_omitsOptionalKeys() throws Exception {
        mockServer.enqueue(created(AGENT_JSON));

        client.voice().agents().create(CreateVoiceAgentRequest.builder().name("Front desk").build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(1, body.size());
        assertEquals("Front desk", body.get("name").getAsString());
    }

    @Test
    void testAgentsCreate_callerIdempotencyKey_isSent() throws Exception {
        mockServer.enqueue(created(AGENT_JSON));

        client.voice().agents().create(CreateVoiceAgentRequest.builder().name("Front desk").build(),
                new IdempotentRequestOptions("agent-front-desk"));

        assertEquals("agent-front-desk", mockServer.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void testAgentsCreate_missingName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.voice().agents().create(null));
        assertThrows(ValidationException.class, () -> client.voice().agents().create(CreateVoiceAgentRequest.builder().build()));
        assertThrows(ValidationException.class, () -> client.voice().agents().create(CreateVoiceAgentRequest.builder().name("  ").build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testAgentsCreate_409AgentLimit_carriesCode() {
        mockServer.enqueue(apiError(409, "agent_limit", "You've reached the agent limit for this workspace."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.voice().agents().create(CreateVoiceAgentRequest.builder().name("One more").build()));

        assertEquals(409, e.getStatusCode());
        assertEquals(CallErrorCode.AGENT_LIMIT, e.getApiErrorCode());
    }

    @Test
    void testAgentsGet_encodesPathId() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON));

        client.voice().agents().get("../../account/keys");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/agents/..%2F..%2Faccount%2Fkeys"));
    }

    @Test
    void testAgentsGet_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.voice().agents().get(null));
        assertThrows(ValidationException.class, () -> client.voice().agents().get(""));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testAgentsGet_404AgentNotFound_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "agent_not_found", "That agent isn't in your workspace."));

        NotFoundException e = assertThrows(NotFoundException.class, () -> client.voice().agents().get("agent_other_ws"));

        assertEquals(CallErrorCode.AGENT_NOT_FOUND, e.getApiErrorCode());
    }

    @Test
    void testAgentsUpdate_sendsOnlySetFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON.replace("\"sendSms\":true", "\"sendSms\":false")));

        VoiceAgent agent = client.voice().agents().update(AGENT_ID, UpdateVoiceAgentRequest.builder()
                .greeting("Thanks for calling Acme. How can I help today?")
                .tools(VoiceAgentTools.builder().sendSms(false).build())
                .build());

        assertFalse(agent.getTools().isSendSms());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/agents/" + AGENT_ID));
        assertNull(request.getHeader("Idempotency-Key"));
        JsonObject body = bodyOf(request);
        assertEquals(2, body.size());
        assertEquals("Thanks for calling Acme. How can I help today?", body.get("greeting").getAsString());
        JsonObject tools = body.getAsJsonObject("tools");
        assertEquals(1, tools.size());
        assertFalse(tools.get("sendSms").getAsBoolean());
    }

    @Test
    void testAgentsUpdate_emptyStringsClear() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON));

        client.voice().agents().update(AGENT_ID, UpdateVoiceAgentRequest.builder()
                .enabled(false)
                .instructions("")
                .tools(VoiceAgentTools.builder().transferTo("").build())
                .build(), new IdempotentRequestOptions("agent-clear-1"));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("agent-clear-1", request.getHeader("Idempotency-Key"));
        JsonObject body = bodyOf(request);
        assertEquals(3, body.size());
        assertFalse(body.get("enabled").getAsBoolean());
        assertEquals("", body.get("instructions").getAsString());
        assertEquals("", body.getAsJsonObject("tools").get("transferTo").getAsString());
        assertFalse(body.getAsJsonObject("tools").has("sendSms"));
    }

    @Test
    void testAgentsGet_sendlyAgent_hasNoConnectionOrSecret() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON));

        VoiceAgent agent = client.voice().agents().get(AGENT_ID);

        assertEquals(VoiceAgentKind.SENDLY, agent.getKind());
        assertNull(agent.getConnection());
        assertFalse(agent.hasSigningSecret());
        assertNull(agent.getSigningSecret());
    }

    @Test
    void testAgentsCreate_ownAgent_sendsKindAndConnection() throws Exception {
        mockServer.enqueue(created(AGENT_JSON
                .replace("\"kind\":\"sendly\",\"connection\":null,\"hasSigningSecret\":false",
                        "\"kind\":\"custom_llm\",\"connection\":{\"baseUrl\":\"https://llm.example.com/v1\"," +
                        "\"model\":\"acme-receptionist-1\",\"headerNames\":[\"X-Team\"],\"hasApiKey\":true}," +
                        "\"hasSigningSecret\":true,\"signingSecret\":\"vsk_agent_once\"")));

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Team", "front-desk");
        VoiceAgent agent = client.voice().agents().create(CreateVoiceAgentRequest.builder()
                .name("Own model")
                .kind(VoiceAgentKind.CUSTOM_LLM)
                .connection(VoiceAgentConnectionInput.builder()
                        .baseUrl("https://llm.example.com/v1")
                        .model("acme-receptionist-1")
                        .apiKey("sk-example-123")
                        .headers(headers)
                        .build())
                .build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(3, body.size());
        assertEquals("Own model", body.get("name").getAsString());
        assertEquals("custom_llm", body.get("kind").getAsString());
        JsonObject connection = body.getAsJsonObject("connection");
        assertEquals(4, connection.size());
        assertEquals("https://llm.example.com/v1", connection.get("baseUrl").getAsString());
        assertEquals("acme-receptionist-1", connection.get("model").getAsString());
        assertEquals("sk-example-123", connection.get("apiKey").getAsString());
        assertEquals("front-desk", connection.getAsJsonObject("headers").get("X-Team").getAsString());
        assertFalse(body.has("rotateSigningSecret"));

        assertEquals(VoiceAgentKind.CUSTOM_LLM, agent.getKind());
        assertTrue(agent.hasSigningSecret());
        assertEquals("vsk_agent_once", agent.getSigningSecret());
        VoiceAgentConnection saved = agent.getConnection();
        assertNotNull(saved);
        assertEquals("https://llm.example.com/v1", saved.getBaseUrl());
        assertEquals("acme-receptionist-1", saved.getModel());
        assertEquals(java.util.List.of("X-Team"), saved.getHeaderNames());
        assertTrue(saved.hasApiKey());
        assertNull(saved.getUri());
        assertNull(saved.getHeaders());
    }

    @Test
    void testAgentsUpdate_connection_omittedKeepsAndNullRemoves() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON
                .replace("\"kind\":\"sendly\",\"connection\":null,\"hasSigningSecret\":false",
                        "\"kind\":\"sip\",\"connection\":{\"uri\":\"sip:+15555550100@sip.example.com\"," +
                        "\"transport\":\"tls\",\"mediaEncryption\":\"require\",\"username\":\"frontdesk\"," +
                        "\"hasPassword\":false,\"headers\":{}},\"hasSigningSecret\":true")));

        VoiceAgent agent = client.voice().agents().update(AGENT_ID, UpdateVoiceAgentRequest.builder()
                .connection(VoiceAgentConnectionInput.builder()
                        .transport("tls")
                        .mediaEncryption("require")
                        .password(null)
                        .headers(null)
                        .build())
                .build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(1, body.size());
        JsonObject connection = body.getAsJsonObject("connection");
        assertEquals(4, connection.size());
        assertEquals("tls", connection.get("transport").getAsString());
        assertEquals("require", connection.get("mediaEncryption").getAsString());
        assertTrue(connection.has("password"));
        assertTrue(connection.get("password").isJsonNull());
        assertTrue(connection.get("headers").isJsonNull());
        assertFalse(connection.has("username"));
        assertFalse(connection.has("uri"));

        assertEquals(VoiceAgentKind.SIP, agent.getKind());
        VoiceAgentConnection saved = agent.getConnection();
        assertEquals("sip:+15555550100@sip.example.com", saved.getUri());
        assertEquals("tls", saved.getTransport());
        assertEquals("require", saved.getMediaEncryption());
        assertEquals("frontdesk", saved.getUsername());
        assertFalse(saved.hasPassword());
        assertTrue(saved.getHeaders().isEmpty());
        assertNull(agent.getSigningSecret());
    }

    @Test
    void testAgentsUpdate_switchToWebsocket_returnsSecretOnce() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON
                .replace("\"kind\":\"sendly\",\"connection\":null,\"hasSigningSecret\":false",
                        "\"kind\":\"websocket\",\"connection\":{\"url\":\"wss://agent.example.com/calls\"," +
                        "\"audioFormat\":\"pcm16_24000\",\"hasAuthToken\":true,\"customParameters\":{\"tenant\":\"acme\"}}," +
                        "\"hasSigningSecret\":true,\"signingSecret\":\"vsk_switched\"")));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("tenant", "acme");
        VoiceAgent agent = client.voice().agents().update(AGENT_ID, UpdateVoiceAgentRequest.builder()
                .kind(VoiceAgentKind.WEBSOCKET)
                .connection(VoiceAgentConnectionInput.builder()
                        .url("wss://agent.example.com/calls")
                        .audioFormat("pcm16_24000")
                        .authToken("token-123")
                        .customParameters(params)
                        .build())
                .build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals(2, body.size());
        assertEquals("websocket", body.get("kind").getAsString());
        JsonObject connection = body.getAsJsonObject("connection");
        assertEquals("wss://agent.example.com/calls", connection.get("url").getAsString());
        assertEquals("pcm16_24000", connection.get("audioFormat").getAsString());
        assertEquals("token-123", connection.get("authToken").getAsString());
        assertEquals("acme", connection.getAsJsonObject("customParameters").get("tenant").getAsString());

        assertEquals("vsk_switched", agent.getSigningSecret());
        VoiceAgentConnection saved = agent.getConnection();
        assertEquals("wss://agent.example.com/calls", saved.getUrl());
        assertEquals("pcm16_24000", saved.getAudioFormat());
        assertTrue(saved.hasAuthToken());
        assertEquals("acme", saved.getCustomParameters().get("tenant"));
    }

    @Test
    void testAgentsUpdate_rotateSigningSecret_onlySentAsTrue() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON
                .replace("\"kind\":\"sendly\",\"connection\":null,\"hasSigningSecret\":false",
                        "\"kind\":\"sip\",\"connection\":null,\"hasSigningSecret\":true,\"signingSecret\":\"vsk_rotated\"")));
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_JSON));

        VoiceAgent agent = client.voice().agents().update(AGENT_ID, UpdateVoiceAgentRequest.builder()
                .rotateSigningSecret(true)
                .build());
        client.voice().agents().update(AGENT_ID, UpdateVoiceAgentRequest.builder()
                .rotateSigningSecret(false)
                .build());

        assertEquals("{\"rotateSigningSecret\":true}", mockServer.takeRequest().getBody().readUtf8());
        assertEquals("{}", mockServer.takeRequest().getBody().readUtf8());
        assertEquals("vsk_rotated", agent.getSigningSecret());
    }

    @Test
    void testAgentsCreate_403ByoAgentsNotEnabled_carriesCode() {
        mockServer.enqueue(apiError(403, "byo_agents_not_enabled", "Bring your own agent is not switched on for this workspace."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.voice().agents().create(CreateVoiceAgentRequest.builder()
                        .name("Own SIP")
                        .kind(VoiceAgentKind.SIP)
                        .connection(VoiceAgentConnectionInput.builder().uri("sip:+15555550100@sip.example.com").build())
                        .build()));

        assertEquals(403, e.getStatusCode());
        assertEquals(CallErrorCode.BYO_AGENTS_NOT_ENABLED, e.getApiErrorCode());
    }

    @Test
    void testAgentKindConstants_matchTheWire() {
        assertEquals("sendly", VoiceAgentKind.SENDLY);
        assertEquals("custom_llm", VoiceAgentKind.CUSTOM_LLM);
        assertEquals("sip", VoiceAgentKind.SIP);
        assertEquals("websocket", VoiceAgentKind.WEBSOCKET);
    }

    private static final String AGENT_TEST_JSON =
        "{\"ok\":true,\"outcome\":\"ok\",\"message\":\"Your agent's server answered in 84 ms.\"," +
        "\"status\":200,\"latencyMs\":84,\"signed\":false,\"steps\":[" +
        "{\"step\":\"address\",\"ok\":true,\"detail\":\"sip.example.com points to a public server.\"}," +
        "{\"step\":\"connect\",\"ok\":true,\"detail\":\"Connected over TLS to sip.example.com:5061 in 41 ms.\"}," +
        "{\"step\":\"reply\",\"ok\":true,\"detail\":\"SIP 200 OK\"}]}";

    @Test
    void testAgentsTest_noArgs_postsEmptyObjectWithoutIdempotencyKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_TEST_JSON));

        VoiceAgentTest result = client.voice().agents().test(AGENT_ID);

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/agents/" + AGENT_ID + "/test"));
        assertEquals("{}", request.getBody().readUtf8());
        assertNull(request.getHeader("Idempotency-Key"));

        assertTrue(result.isOk());
        assertEquals(VoiceAgentTestOutcome.OK, result.getOutcome());
        assertEquals("Your agent's server answered in 84 ms.", result.getMessage());
        assertEquals(Integer.valueOf(200), result.getStatus());
        assertEquals(Integer.valueOf(84), result.getLatencyMs());
        assertFalse(result.isSigned());
        assertEquals(3, result.getSteps().size());
        VoiceAgentTestStep first = result.getSteps().get(0);
        assertEquals(VoiceAgentTestStepName.ADDRESS, first.getStep());
        assertTrue(first.isOk());
        assertEquals("sip.example.com points to a public server.", first.getDetail());
        assertEquals(VoiceAgentTestStepName.REPLY, result.getSteps().get(2).getStep());
    }

    @Test
    void testAgentsTest_withSettings_sendsKindConnectionAndInstructions() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_TEST_JSON));

        client.voice().agents().test(AGENT_ID, TestVoiceAgentRequest.builder()
                .kind(VoiceAgentKind.SIP)
                .connection(VoiceAgentConnectionInput.builder()
                        .uri("sip:+15125550100@sip.example.com")
                        .transport("tls")
                        .build())
                .instructions("Answer questions about opening hours.")
                .build());

        RecordedRequest request = mockServer.takeRequest();
        assertNull(request.getHeader("Idempotency-Key"));
        JsonObject body = bodyOf(request);
        assertEquals(3, body.size());
        assertEquals("sip", body.get("kind").getAsString());
        JsonObject connection = body.getAsJsonObject("connection");
        assertEquals(2, connection.size());
        assertEquals("sip:+15125550100@sip.example.com", connection.get("uri").getAsString());
        assertEquals("tls", connection.get("transport").getAsString());
        assertEquals("Answer questions about opening hours.", body.get("instructions").getAsString());
    }

    @Test
    void testAgentsTest_emptyRequest_postsEmptyObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(AGENT_TEST_JSON));

        client.voice().agents().test(AGENT_ID, TestVoiceAgentRequest.builder().build());

        assertEquals("{}", mockServer.takeRequest().getBody().readUtf8());
    }

    @Test
    void testAgentsTest_failedTest_keepsUnknownOutcomeAndStep() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
                "{\"ok\":false,\"outcome\":\"proxy_refused\",\"message\":\"Something new happened.\"," +
                "\"status\":null,\"latencyMs\":null,\"signed\":true,\"steps\":[" +
                "{\"step\":\"address\",\"ok\":true,\"detail\":\"agent.example.com points to a public server.\"}," +
                "{\"step\":\"handshake\",\"ok\":false,\"detail\":\"A new kind of step failed.\"}]}"));

        VoiceAgentTest result = client.voice().agents().test(AGENT_ID);

        assertFalse(result.isOk());
        assertEquals("proxy_refused", result.getOutcome());
        assertNull(result.getStatus());
        assertNull(result.getLatencyMs());
        assertTrue(result.isSigned());
        assertEquals(2, result.getSteps().size());
        assertEquals("handshake", result.getSteps().get(1).getStep());
        assertFalse(result.getSteps().get(1).isOk());
    }

    @Test
    void testAgentsTest_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.voice().agents().test(" "));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testAgentsTest_404AgentNotFound_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "agent_not_found", "Agent not found"));

        assertThrows(NotFoundException.class, () -> client.voice().agents().test(AGENT_ID));
    }

    @Test
    void testAgentTestConstants_matchTheWire() {
        assertEquals("ok", VoiceAgentTestOutcome.OK);
        assertEquals("blocked_destination", VoiceAgentTestOutcome.BLOCKED_DESTINATION);
        assertEquals("unreachable", VoiceAgentTestOutcome.UNREACHABLE);
        assertEquals("timeout", VoiceAgentTestOutcome.TIMEOUT);
        assertEquals("tls_failed", VoiceAgentTestOutcome.TLS_FAILED);
        assertEquals("auth_required", VoiceAgentTestOutcome.AUTH_REQUIRED);
        assertEquals("auth_failed", VoiceAgentTestOutcome.AUTH_FAILED);
        assertEquals("not_found", VoiceAgentTestOutcome.NOT_FOUND);
        assertEquals("rejected", VoiceAgentTestOutcome.REJECTED);
        assertEquals("bad_reply", VoiceAgentTestOutcome.BAD_REPLY);
        assertEquals("address", VoiceAgentTestStepName.ADDRESS);
        assertEquals("connect", VoiceAgentTestStepName.CONNECT);
        assertEquals("login", VoiceAgentTestStepName.LOGIN);
        assertEquals("reply", VoiceAgentTestStepName.REPLY);
    }

    @Test
    void testAgentsUpdate_invalidInput_throwsValidationException() {
        UpdateVoiceAgentRequest request = UpdateVoiceAgentRequest.builder().name("Front desk").build();
        assertThrows(ValidationException.class, () -> client.voice().agents().update(" ", request));
        assertThrows(ValidationException.class, () -> client.voice().agents().update(AGENT_ID, null));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testAgentsDelete_returnsConfirmation() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"" + AGENT_ID + "\",\"object\":\"voice_agent\",\"deleted\":true}"));

        DeletedVoiceAgent deleted = client.voice().agents().delete(AGENT_ID);

        assertEquals(AGENT_ID, deleted.getId());
        assertEquals("voice_agent", deleted.getObject());
        assertTrue(deleted.isDeleted());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/agents/" + AGENT_ID));
        assertNull(request.getHeader("Idempotency-Key"));
    }

    @Test
    void testAgentsDelete_callerIdempotencyKey_isSent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"id\":\"" + AGENT_ID + "\",\"object\":\"voice_agent\",\"deleted\":true}"));

        client.voice().agents().delete(AGENT_ID, new IdempotentRequestOptions("agent-delete-1"));

        assertEquals("agent-delete-1", mockServer.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void testAgentsDelete_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.voice().agents().delete(null));
        assertThrows(ValidationException.class, () -> client.voice().agents().delete(""));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testAgentsDelete_409AgentInUse_exposesNumbers() {
        mockServer.enqueue(apiError(409,
                "{\"error\":\"agent_in_use\",\"message\":\"This agent answers 1 number. Point it elsewhere first.\"," +
                "\"numbers\":[\"" + PHONE + "\"]}"));

        SendlyException e = assertThrows(SendlyException.class, () -> client.voice().agents().delete(AGENT_ID));

        assertFalse(e instanceof ValidationException);
        assertEquals(409, e.getStatusCode());
        assertEquals(CallErrorCode.AGENT_IN_USE, e.getApiErrorCode());
        assertEquals("This agent answers 1 number. Point it elsewhere first.", e.getMessage());
        assertEquals(PHONE, e.getResponseBody().getAsJsonArray("numbers").get(0).getAsString());
    }

    // ==================== voices() ====================

    @Test
    void testVoicesList_unwrapsData() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[{\"id\":\"ashley\",\"label\":\"Ashley (US, warm)\",\"language\":\"en\"}," +
            "{\"id\":\"diego\",\"label\":\"Diego (Spanish, MX)\",\"language\":\"es\"}]}"
        ));

        VoiceListResponse voices = client.voice().voices().list();

        assertEquals(2, voices.getData().size());
        Voice first = voices.getData().get(0);
        assertEquals("ashley", first.getId());
        assertEquals("Ashley (US, warm)", first.getLabel());
        assertEquals("en", first.getLanguage());
        assertEquals("es", voices.getData().get(1).getLanguage());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/voice/voices"));
    }

    @Test
    void testVoicesList_404VoiceNotEnabled_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "voice_not_enabled", "Voice is not enabled for your account."));

        NotFoundException e = assertThrows(NotFoundException.class, () -> client.voice().voices().list());

        assertEquals(CallErrorCode.VOICE_NOT_ENABLED, e.getApiErrorCode());
    }

    @Test
    void testWrite_403LiveKeyRequired_carriesCode() {
        mockServer.enqueue(apiError(403, "live_key_required", "Phone calls need a live API key."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.voice().agents().create(CreateVoiceAgentRequest.builder().name("Front desk").build()));

        assertEquals(403, e.getStatusCode());
        assertEquals(CallErrorCode.LIVE_KEY_REQUIRED, e.getApiErrorCode());
    }
}
