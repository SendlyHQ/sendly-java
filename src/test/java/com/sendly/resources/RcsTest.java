package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.RcsAgent;
import com.sendly.models.RcsAgentsResponse;
import com.sendly.models.RcsCapability;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the RCS resource — agents and recipient capability.
 */
class RcsTest {
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

    // ==================== agents().list() Tests ====================

    @Test
    void testAgentsList_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"agents\":[{\"id\":\"rag_1\",\"name\":\"Acme Inc\",\"status\":\"approved\"," +
            "\"useCase\":\"OTP\",\"sendable\":true,\"createdAt\":\"2026-07-30T10:00:00.000Z\"}]}"
        ));

        RcsAgentsResponse response = client.rcs().agents().list();

        assertNotNull(response);
        assertEquals(1, response.getAgents().size());
        RcsAgent agent = response.getAgents().get(0);
        assertEquals("rag_1", agent.getId());
        assertEquals("Acme Inc", agent.getName());
        assertEquals("approved", agent.getStatus());
        assertEquals("OTP", agent.getUseCase());
        assertTrue(agent.isSendable());
        assertEquals("2026-07-30T10:00:00.000Z", agent.getCreatedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/rcs/agents"));
    }

    @Test
    void testAgentsList_pendingAgent_nullFields() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"agents\":[{\"id\":\"rag_2\",\"name\":\"Acme Inc\",\"status\":\"pending\"," +
            "\"useCase\":null,\"sendable\":false,\"createdAt\":\"2026-07-30T10:00:00.000Z\"}]}"
        ));

        RcsAgentsResponse response = client.rcs().agents().list();

        RcsAgent agent = response.getAgents().get(0);
        assertEquals("pending", agent.getStatus());
        assertNull(agent.getUseCase());
        assertFalse(agent.isSendable());
    }

    @Test
    void testAgentsList_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"agents\":[]}"));

        RcsAgentsResponse response = client.rcs().agents().list();

        assertNotNull(response);
        assertTrue(response.getAgents().isEmpty());
    }

    // ==================== capability() Tests ====================

    @Test
    void testCapability_capable() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"to\":\"+15551234567\",\"agentId\":\"rag_1\",\"capable\":true," +
            "\"features\":[\"RICHCARD_STANDALONE\",\"ACTION_CREATE_CALENDAR_EVENT\"]}"
        ));

        RcsCapability capability = client.rcs().capability("+15551234567");

        assertEquals("+15551234567", capability.getTo());
        assertEquals("rag_1", capability.getAgentId());
        assertTrue(capability.isCapable());
        assertEquals(2, capability.getFeatures().size());
        assertEquals("RICHCARD_STANDALONE", capability.getFeatures().get(0));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        String path = request.getPath();
        assertTrue(path.contains("/rcs/capability"));
        assertTrue(path.contains("to=%2B15551234567"));
        assertFalse(path.contains("agentId"));
    }

    @Test
    void testCapability_notCapable_emptyFeatures() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"to\":\"+15551234567\",\"agentId\":\"rag_1\",\"capable\":false,\"features\":[]}"
        ));

        RcsCapability capability = client.rcs().capability("+15551234567");

        assertFalse(capability.isCapable());
        assertTrue(capability.getFeatures().isEmpty());
    }

    @Test
    void testCapability_withAgentId_passesQueryParam() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"to\":\"+15551234567\",\"agentId\":\"rag_2\",\"capable\":true,\"features\":[]}"
        ));

        client.rcs().capability("+15551234567", "rag_2");

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertTrue(path.contains("to=%2B15551234567"));
        assertTrue(path.contains("agentId=rag_2"));
    }

    @Test
    void testCapability_invalidPhone_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.rcs().capability("invalid");
        });
        assertThrows(ValidationException.class, () -> {
            client.rcs().capability(null);
        });
    }

    @Test
    void testCapability_403TestKey_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(403)
                .setBody("{\"error\":\"rcs_requires_live_key\"," +
                        "\"message\":\"RCS capability checks require a live API key.\"}"));

        SendlyException e = assertThrows(SendlyException.class, () -> {
            client.rcs().capability("+15551234567");
        });
        assertEquals("RCS capability checks require a live API key.", e.getMessage());
    }

    @Test
    void testCapability_404NotEnabled_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockSuccess("{}").setResponseCode(404)
                .setBody("{\"error\":\"rcs_not_enabled\"," +
                        "\"message\":\"RCS isn't set up on this workspace yet.\"}"));

        assertThrows(NotFoundException.class, () -> {
            client.rcs().capability("+15551234567");
        });
    }
}
