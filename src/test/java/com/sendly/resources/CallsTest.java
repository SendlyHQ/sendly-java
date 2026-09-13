package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.Call;
import com.sendly.models.CallBilling;
import com.sendly.models.CallDirection;
import com.sendly.models.CallErrorCode;
import com.sendly.models.CallHandledBy;
import com.sendly.models.CallKind;
import com.sendly.models.CallListResponse;
import com.sendly.models.CallRecording;
import com.sendly.models.CallRecordingStatus;
import com.sendly.models.CallStatus;
import com.sendly.models.CallTranscriptLine;
import com.sendly.models.CreateCallRequest;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.ListCallsOptions;
import com.sendly.models.OwnedNumber;
import com.sendly.models.OwnedNumbersResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Calls resource: create, list, get, hangup and recording.
 */
class CallsTest {
    private static final String CALL_ID = "6f1c2d3e-4a5b-4c6d-8e9f-0a1b2c3d4e5f";
    private static final String AGENT_ID = "3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b";

    private static final String RINGING_CALL_JSON =
        "{\"id\":\"" + CALL_ID + "\",\"object\":\"call\",\"kind\":\"pstn\",\"direction\":\"outbound\"," +
        "\"status\":\"ringing\",\"handledBy\":\"agent\",\"agentId\":\"" + AGENT_ID + "\"," +
        "\"from\":\"+15555550188\",\"to\":\"+15555550123\",\"callerName\":\"Front Desk\",\"calleeName\":\"+15555550123\"," +
        "\"startedAt\":\"2026-09-12T14:03:11.000Z\",\"answeredAt\":null,\"endedAt\":null," +
        "\"durationSecs\":0,\"creditsCharged\":0,\"billing\":\"metered\",\"hangupClass\":null," +
        "\"recordingStatus\":null,\"metadata\":{\"crmId\":\"lead_8812\"}}";

    private static final String COMPLETED_CALL_JSON =
        "{\"id\":\"" + CALL_ID + "\",\"object\":\"call\",\"kind\":\"pstn\",\"direction\":\"outbound\"," +
        "\"status\":\"completed\",\"handledBy\":\"agent\",\"agentId\":\"" + AGENT_ID + "\"," +
        "\"from\":\"+15555550188\",\"to\":\"+15555550123\",\"callerName\":\"Front Desk\",\"calleeName\":\"+15555550123\"," +
        "\"startedAt\":\"2026-09-12T14:03:11.000Z\",\"answeredAt\":\"2026-09-12T14:03:19.000Z\"," +
        "\"endedAt\":\"2026-09-12T14:05:02.000Z\",\"durationSecs\":103,\"creditsCharged\":20," +
        "\"billing\":\"settled\",\"hangupClass\":\"agent_agent_hangup\",\"recordingStatus\":\"ready\"," +
        "\"metadata\":{\"crmId\":\"lead_8812\"}," +
        "\"transcript\":[{\"speaker\":\"agent\",\"text\":\"Hi Jordan, calling to confirm Tuesday at 3pm.\",\"atMs\":1200}," +
        "{\"speaker\":\"caller\",\"text\":\"Yes, that works.\",\"atMs\":4800}]}";

    private static final String DASHBOARD_CALL_JSON =
        "{\"id\":\"call_dash_1\",\"object\":\"call\",\"kind\":\"pstn\",\"direction\":\"inbound\"," +
        "\"status\":\"completed\",\"handledBy\":\"dashboard\",\"agentId\":null," +
        "\"from\":\"+15555550142\",\"to\":\"+15555550188\",\"callerName\":null,\"calleeName\":\"Front Desk\"," +
        "\"startedAt\":\"2026-09-12T10:00:00.000Z\",\"answeredAt\":\"2026-09-12T10:00:05.000Z\"," +
        "\"endedAt\":\"2026-09-12T10:01:00.000Z\",\"durationSecs\":55,\"creditsCharged\":2," +
        "\"billing\":\"settled\",\"hangupClass\":\"caller_hung_up\",\"recordingStatus\":null,\"metadata\":{}}";

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

    private static MockResponse apiError(int status, String code, String message) {
        return new MockResponse()
                .setResponseCode(status)
                .setBody("{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}")
                .addHeader("Content-Type", "application/json");
    }

    private static JsonObject bodyOf(RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static CreateCallRequest.Builder minimalCreate() {
        return CreateCallRequest.builder().to("+15555550123").agentId(AGENT_ID);
    }

    // ==================== create() Tests ====================

    @Test
    void testCreate_happyPath() throws Exception {
        mockServer.enqueue(created(RINGING_CALL_JSON));

        Call call = client.calls().create(CreateCallRequest.builder()
                .to("+15555550123")
                .agentId(AGENT_ID)
                .from("+15555550188")
                .context("You are calling Jordan to confirm the 3pm appointment on Tuesday.")
                .metadata("crmId", "lead_8812")
                .build());

        assertEquals(CALL_ID, call.getId());
        assertEquals("call", call.getObject());
        assertEquals(CallKind.PSTN, call.getKind());
        assertEquals(CallDirection.OUTBOUND, call.getDirection());
        assertEquals(CallStatus.RINGING, call.getStatus());
        assertEquals(CallHandledBy.AGENT, call.getHandledBy());
        assertEquals(AGENT_ID, call.getAgentId());
        assertEquals("+15555550188", call.getFrom());
        assertEquals("+15555550123", call.getTo());
        assertEquals("Front Desk", call.getCallerName());
        assertEquals("+15555550123", call.getCalleeName());
        assertEquals("2026-09-12T14:03:11.000Z", call.getStartedAt());
        assertNull(call.getAnsweredAt());
        assertNull(call.getEndedAt());
        assertEquals(0, call.getDurationSecs());
        assertEquals(0, call.getCreditsCharged());
        assertEquals(CallBilling.METERED, call.getBilling());
        assertNull(call.getHangupClass());
        assertNull(call.getRecordingStatus());
        assertEquals(Map.of("crmId", "lead_8812"), call.getMetadata());
        assertNull(call.getTranscript());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().endsWith("/calls"));
        assertEquals("Bearer sk_live_123", request.getHeader("Authorization"));
        assertNotNull(request.getHeader("Idempotency-Key"));
        assertTrue(request.getHeader("Idempotency-Key").startsWith("sendly-java-retry-"));
        JsonObject body = bodyOf(request);
        assertEquals("+15555550123", body.get("to").getAsString());
        assertEquals(AGENT_ID, body.get("agentId").getAsString());
        assertEquals("+15555550188", body.get("from").getAsString());
        assertEquals("You are calling Jordan to confirm the 3pm appointment on Tuesday.", body.get("context").getAsString());
        assertEquals("lead_8812", body.getAsJsonObject("metadata").get("crmId").getAsString());
        assertEquals(5, body.size());
    }

    @Test
    void testCreate_minimalBody_omitsOptionalKeys() throws Exception {
        mockServer.enqueue(created(RINGING_CALL_JSON));

        client.calls().create(minimalCreate().build());

        JsonObject body = bodyOf(mockServer.takeRequest());
        assertEquals("+15555550123", body.get("to").getAsString());
        assertEquals(AGENT_ID, body.get("agentId").getAsString());
        assertFalse(body.has("from"));
        assertFalse(body.has("context"));
        assertFalse(body.has("metadata"));
    }

    @Test
    void testCreate_metadataMap_replacesPairs() throws Exception {
        mockServer.enqueue(created(RINGING_CALL_JSON));

        client.calls().create(minimalCreate()
                .metadata("dropped", "yes")
                .metadata(Map.of("crmId", "lead_8812", "source", "nightly"))
                .build());

        JsonObject metadata = bodyOf(mockServer.takeRequest()).getAsJsonObject("metadata");
        assertEquals(2, metadata.size());
        assertEquals("lead_8812", metadata.get("crmId").getAsString());
        assertEquals("nightly", metadata.get("source").getAsString());
        assertFalse(metadata.has("dropped"));
    }

    @Test
    void testCreate_callerIdempotencyKey_isSent() throws Exception {
        mockServer.enqueue(created(RINGING_CALL_JSON));

        client.calls().create(minimalCreate().build(), new IdempotentRequestOptions("call-create-1"));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("call-create-1", request.getHeader("Idempotency-Key"));
    }

    @Test
    void testCreate_missingTo_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                client.calls().create(CreateCallRequest.builder().agentId(AGENT_ID).build()));
        assertThrows(ValidationException.class, () ->
                client.calls().create(CreateCallRequest.builder().to("  ").agentId(AGENT_ID).build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testCreate_missingAgentId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                client.calls().create(CreateCallRequest.builder().to("+15555550123").build()));
        assertThrows(ValidationException.class, () ->
                client.calls().create(CreateCallRequest.builder().to("+15555550123").agentId("").build()));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testCreate_nullRequest_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.calls().create(null));
    }

    @Test
    void testCreate_402InsufficientCredits_mapsCodeAndMessage() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(402)
                .setBody("{\"error\":\"insufficient_credits\",\"message\":\"Calls cost 10 credits a minute. Current balance: 4.\"," +
                        "\"creditsNeeded\":10,\"currentBalance\":4}")
                .addHeader("Content-Type", "application/json"));

        InsufficientCreditsException e = assertThrows(InsufficientCreditsException.class, () ->
                client.calls().create(minimalCreate().build()));

        assertEquals(402, e.getStatusCode());
        assertEquals(CallErrorCode.INSUFFICIENT_CREDITS, e.getApiErrorCode());
        assertEquals("Calls cost 10 credits a minute. Current balance: 4.", e.getMessage());
    }

    @Test
    void testCreate_428E911Required_isGenericSendlyException() {
        mockServer.enqueue(apiError(428, "e911_required",
                "Register an emergency address for this number before placing calls. It's required by US law."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.calls().create(minimalCreate().build()));

        assertEquals(428, e.getStatusCode());
        assertEquals(CallErrorCode.E911_REQUIRED, e.getApiErrorCode());
    }

    @Test
    void testCreate_409LinesBusy_carriesCode() {
        mockServer.enqueue(apiError(409, "lines_busy", "Your workspace's lines are all in use. Try again in a moment."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.calls().create(minimalCreate().build()));

        assertEquals(409, e.getStatusCode());
        assertEquals(CallErrorCode.LINES_BUSY, e.getApiErrorCode());
    }

    @Test
    void testCreate_400AgentRequired_throwsValidationException() {
        mockServer.enqueue(apiError(400, "agent_required",
                "Calls placed over the API are answered by an AI agent. Pass agentId."));

        ValidationException e = assertThrows(ValidationException.class, () ->
                client.calls().create(minimalCreate().build()));

        assertEquals(CallErrorCode.AGENT_REQUIRED, e.getApiErrorCode());
    }

    @Test
    void testCreate_403LiveKeyRequired_carriesCode() {
        mockServer.enqueue(apiError(403, "live_key_required", "Phone calls need a live API key."));

        SendlyException e = assertThrows(SendlyException.class, () ->
                client.calls().create(minimalCreate().build()));

        assertEquals(403, e.getStatusCode());
        assertEquals(CallErrorCode.LIVE_KEY_REQUIRED, e.getApiErrorCode());
    }

    @Test
    void testCreate_404VoiceNotEnabled_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "voice_not_enabled", "Voice is not enabled for your account."));

        NotFoundException e = assertThrows(NotFoundException.class, () ->
                client.calls().create(minimalCreate().build()));

        assertEquals(CallErrorCode.VOICE_NOT_ENABLED, e.getApiErrorCode());
        assertEquals("Voice is not enabled for your account.", e.getMessage());
    }

    // ==================== list() Tests ====================

    @Test
    void testList_noOptions_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[" + COMPLETED_CALL_JSON + "," + DASHBOARD_CALL_JSON + "]," +
            "\"pagination\":{\"total\":132,\"limit\":50,\"offset\":0,\"hasMore\":true}}"
        ));

        CallListResponse page = client.calls().list();

        assertEquals(2, page.getData().size());
        assertEquals(CALL_ID, page.getData().get(0).getId());
        assertEquals("call_dash_1", page.getData().get(1).getId());
        assertEquals(CallHandledBy.DASHBOARD, page.getData().get(1).getHandledBy());
        assertNull(page.getData().get(1).getAgentId());
        assertTrue(page.getData().get(1).getMetadata().isEmpty());
        assertEquals(132, page.getTotal());
        assertEquals(50, page.getLimit());
        assertEquals(0, page.getOffset());
        assertTrue(page.hasMore());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/calls"));
        assertNull(request.getHeader("Idempotency-Key"));
    }

    @Test
    void testList_withOptions_encodesQuery() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[],\"pagination\":{\"total\":0,\"limit\":20,\"offset\":40,\"hasMore\":false}}"
        ));

        CallListResponse page = client.calls().list(ListCallsOptions.builder()
                .limit(20)
                .offset(40)
                .status(CallStatus.COMPLETED)
                .direction(CallDirection.OUTBOUND)
                .kind(CallKind.PSTN)
                .agentId(AGENT_ID)
                .to("+15555550123")
                .from("+15555550188")
                .build());

        assertTrue(page.getData().isEmpty());
        assertFalse(page.hasMore());
        assertEquals(40, page.getOffset());

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertTrue(path.contains("/calls?"));
        assertTrue(path.contains("limit=20"));
        assertTrue(path.contains("offset=40"));
        assertTrue(path.contains("status=completed"));
        assertTrue(path.contains("direction=outbound"));
        assertTrue(path.contains("kind=pstn"));
        assertTrue(path.contains("agentId=" + AGENT_ID));
        assertTrue(path.contains("to=%2B15555550123"));
        assertTrue(path.contains("from=%2B15555550188"));
    }

    @Test
    void testList_missingPagination_defaults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + DASHBOARD_CALL_JSON + "]}"));

        CallListResponse page = client.calls().list();

        assertEquals(1, page.getTotal());
        assertEquals(50, page.getLimit());
        assertEquals(0, page.getOffset());
        assertFalse(page.hasMore());
    }

    @Test
    void testList_400InvalidRequest_throwsValidationException() {
        mockServer.enqueue(apiError(400, "invalid_request", "status must be one of ringing, active, completed, ..."));

        ValidationException e = assertThrows(ValidationException.class, () ->
                client.calls().list(ListCallsOptions.builder().status("bogus").build()));

        assertEquals(CallErrorCode.INVALID_REQUEST, e.getApiErrorCode());
    }

    // ==================== get() Tests ====================

    @Test
    void testGet_agentCall_hasTranscript() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(COMPLETED_CALL_JSON));

        Call call = client.calls().get(CALL_ID);

        assertEquals(CallStatus.COMPLETED, call.getStatus());
        assertEquals("2026-09-12T14:03:19.000Z", call.getAnsweredAt());
        assertEquals("2026-09-12T14:05:02.000Z", call.getEndedAt());
        assertEquals(103, call.getDurationSecs());
        assertEquals(20, call.getCreditsCharged());
        assertEquals(CallBilling.SETTLED, call.getBilling());
        assertEquals("agent_agent_hangup", call.getHangupClass());
        assertEquals(CallRecordingStatus.READY, call.getRecordingStatus());
        assertNotNull(call.getTranscript());
        assertEquals(2, call.getTranscript().size());
        CallTranscriptLine first = call.getTranscript().get(0);
        assertEquals("agent", first.getSpeaker());
        assertEquals("Hi Jordan, calling to confirm Tuesday at 3pm.", first.getText());
        assertEquals(1200L, first.getAtMs());
        assertEquals("caller", call.getTranscript().get(1).getSpeaker());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/calls/" + CALL_ID));
    }

    @Test
    void testGet_dashboardCall_noTranscript() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(DASHBOARD_CALL_JSON));

        Call call = client.calls().get("call_dash_1");

        assertEquals(CallHandledBy.DASHBOARD, call.getHandledBy());
        assertNull(call.getTranscript());
        assertEquals("caller_hung_up", call.getHangupClass());
    }

    @Test
    void testGet_agentCallNothingSaid_emptyTranscript() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(RINGING_CALL_JSON.replace("\"metadata\":", "\"transcript\":[],\"metadata\":")));

        Call call = client.calls().get(CALL_ID);

        assertNotNull(call.getTranscript());
        assertTrue(call.getTranscript().isEmpty());
    }

    @Test
    void testGet_encodesPathId() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(DASHBOARD_CALL_JSON));

        client.calls().get("../../account/keys");

        RecordedRequest request = mockServer.takeRequest();
        assertTrue(request.getPath().endsWith("/calls/..%2F..%2Faccount%2Fkeys"));
    }

    @Test
    void testGet_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.calls().get(null));
        assertThrows(ValidationException.class, () -> client.calls().get(""));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testGet_404CallNotFound_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "call_not_found", "No call with that id is in this workspace."));

        NotFoundException e = assertThrows(NotFoundException.class, () -> client.calls().get("call_other_ws"));

        assertEquals(CallErrorCode.CALL_NOT_FOUND, e.getApiErrorCode());
        assertEquals("No call with that id is in this workspace.", e.getMessage());
    }

    // ==================== hangup() Tests ====================

    @Test
    void testHangup_ringing_becomesCancelled() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(RINGING_CALL_JSON
                .replace("\"status\":\"ringing\"", "\"status\":\"cancelled\"")
                .replace("\"hangupClass\":null", "\"hangupClass\":\"caller_cancelled\"")
                .replace("\"billing\":\"metered\"", "\"billing\":\"settled\"")));

        Call call = client.calls().hangup(CALL_ID);

        assertEquals(CallStatus.CANCELLED, call.getStatus());
        assertEquals("caller_cancelled", call.getHangupClass());
        assertEquals(CallBilling.SETTLED, call.getBilling());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().endsWith("/calls/" + CALL_ID + "/hangup"));
        assertNotNull(request.getHeader("Idempotency-Key"));
        assertEquals(0, bodyOf(request).size());
    }

    @Test
    void testHangup_active_becomesCompleted() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(COMPLETED_CALL_JSON.replace("agent_agent_hangup", "normal")));

        Call call = client.calls().hangup(CALL_ID);

        assertEquals(CallStatus.COMPLETED, call.getStatus());
        assertEquals("normal", call.getHangupClass());
    }

    @Test
    void testHangup_callerIdempotencyKey_isSent() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(COMPLETED_CALL_JSON));

        client.calls().hangup(CALL_ID, new IdempotentRequestOptions("hangup-1"));

        assertEquals("hangup-1", mockServer.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void testHangup_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.calls().hangup(null));
        assertThrows(ValidationException.class, () -> client.calls().hangup(" "));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testHangup_404CallNotFound_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "call_not_found", "No call with that id is in this workspace."));

        NotFoundException e = assertThrows(NotFoundException.class, () -> client.calls().hangup("call_other_ws"));

        assertEquals(CallErrorCode.CALL_NOT_FOUND, e.getApiErrorCode());
    }

    // ==================== recording() Tests ====================

    @Test
    void testRecording_ready_hasSignedUrl() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"callId\":\"" + CALL_ID + "\",\"status\":\"ready\"," +
            "\"url\":\"https://media.sendly.live/recordings/" + CALL_ID + ".ogg?sig=abc\"," +
            "\"expiresAt\":\"2026-09-12T14:10:00.000Z\",\"contentType\":\"audio/ogg\"}"
        ));

        CallRecording recording = client.calls().recording(CALL_ID);

        assertEquals(CALL_ID, recording.getCallId());
        assertEquals(CallRecordingStatus.READY, recording.getStatus());
        assertEquals("https://media.sendly.live/recordings/" + CALL_ID + ".ogg?sig=abc", recording.getUrl());
        assertEquals("2026-09-12T14:10:00.000Z", recording.getExpiresAt());
        assertEquals("audio/ogg", recording.getContentType());
        assertTrue(recording.isReady());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/calls/" + CALL_ID + "/recording"));
        assertNull(request.getHeader("Idempotency-Key"));
    }

    @Test
    void testRecording_none_urlIsNull() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"callId\":\"" + CALL_ID + "\",\"status\":\"none\",\"url\":null,\"expiresAt\":null,\"contentType\":null}"
        ));

        CallRecording recording = client.calls().recording(CALL_ID);

        assertEquals(CallRecordingStatus.NONE, recording.getStatus());
        assertNull(recording.getUrl());
        assertNull(recording.getExpiresAt());
        assertNull(recording.getContentType());
        assertFalse(recording.isReady());
    }

    @Test
    void testRecording_stillRecording_urlIsNull() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"callId\":\"" + CALL_ID + "\",\"status\":\"recording\",\"url\":null,\"expiresAt\":null,\"contentType\":null}"
        ));

        CallRecording recording = client.calls().recording(CALL_ID);

        assertEquals(CallRecordingStatus.RECORDING, recording.getStatus());
        assertNull(recording.getUrl());
        assertFalse(recording.isReady());
    }

    @Test
    void testRecording_encodesPathId() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"callId\":\"a/b\",\"status\":\"none\"}"));

        client.calls().recording("a/b");

        assertTrue(mockServer.takeRequest().getPath().endsWith("/calls/a%2Fb/recording"));
    }

    @Test
    void testRecording_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.calls().recording(null));
        assertThrows(ValidationException.class, () -> client.calls().recording(""));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testRecording_404CallNotFound_throwsNotFoundException() {
        mockServer.enqueue(apiError(404, "call_not_found", "No call with that id is in this workspace."));

        NotFoundException e = assertThrows(NotFoundException.class, () -> client.calls().recording("call_other_ws"));

        assertEquals(CallErrorCode.CALL_NOT_FOUND, e.getApiErrorCode());
    }

    // ==================== numbers().list() voice fields ====================

    @Test
    void testNumbersList_exposesVoiceFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"numbers\":[{\"id\":\"num_1\",\"phoneNumber\":\"+15555550188\",\"status\":\"active\"," +
            "\"voiceEnabled\":true,\"voiceMode\":\"agent\"}," +
            "{\"id\":\"num_2\",\"phoneNumber\":\"+15555550199\",\"status\":\"active\"," +
            "\"voiceEnabled\":false,\"voiceMode\":\"none\"}]}"
        ));

        OwnedNumbersResponse owned = client.numbers().list();

        OwnedNumber voice = owned.getNumbers().get(0);
        assertTrue(voice.isVoiceEnabled());
        assertEquals("agent", voice.getVoiceMode());
        OwnedNumber plain = owned.getNumbers().get(1);
        assertFalse(plain.isVoiceEnabled());
        assertEquals("none", plain.getVoiceMode());
    }
}
