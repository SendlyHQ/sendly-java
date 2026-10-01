package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.exceptions.ValidationException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PathParamsTest {
    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        client = new Sendly("sk_test_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(0));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ".", ".."})
    void testEncode_refusesDotSegmentsAndEmpty(String id) {
        assertThrows(ValidationException.class, () -> PathParams.encode(id));
    }

    @Test
    void testEncode_refusesNull() {
        assertThrows(ValidationException.class, () -> PathParams.encode(null));
    }

    @Test
    void testEncode_keepsOrdinaryIdsAndIdsContainingDots() {
        assertEquals("key_1", PathParams.encode("key_1"));
        assertEquals("...", PathParams.encode("..."));
        assertEquals("a.b", PathParams.encode("a.b"));
        assertEquals("..%2F..", PathParams.encode("../.."));
    }

    @ParameterizedTest
    @ValueSource(strings = {".", ".."})
    void testRevokeKey_dotSegmentKeyIdSendsNothing(String keyId) {
        mockServer.enqueue(new MockResponse().setResponseCode(204));

        assertThrows(ValidationException.class,
                () -> client.enterprise().workspaces().revokeKey("ws_1", keyId));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testRemoveContact_dotSegmentContactIdSendsNothing() {
        mockServer.enqueue(new MockResponse().setResponseCode(204));

        assertThrows(ValidationException.class,
                () -> client.contacts().lists().removeContact("lst_1", ".."));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testResourceEncoders_refuseDotSegments() {
        assertThrows(ValidationException.class, () -> client.numbers().get(".."));
        assertThrows(ValidationException.class, () -> client.tenDlc().getBrand("."));
        assertThrows(ValidationException.class, () -> client.messages().getBatch(".."));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testRevokeKey_ordinaryKeyIdStillSent() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(204));

        client.enterprise().workspaces().revokeKey("ws_1", "key_1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/api/v1/enterprise/workspaces/ws_1/keys/key_1", request.getPath());
    }
}
