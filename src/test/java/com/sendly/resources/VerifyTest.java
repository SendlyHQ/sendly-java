package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.ListVerificationsRequest;
import com.sendly.models.SendVerificationResponse;
import com.sendly.models.VerificationListResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the verify resource against the shapes the API sends and accepts.
 */
class VerifyTest {
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
    void testList_readsHasMoreTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"verifications\":[{\"id\":\"ver_1\",\"status\":\"verified\",\"phone\":\"+15551234567\","
                + "\"delivery_status\":\"delivered\",\"attempts\":1,\"max_attempts\":3,"
                + "\"expires_at\":\"2026-09-25T00:10:00.000Z\",\"verified_at\":\"2026-09-25T00:01:00.000Z\","
                + "\"created_at\":\"2026-09-25T00:00:00.000Z\",\"sandbox\":false,\"app_name\":\"Acme\","
                + "\"template_id\":null,\"profile_id\":null}],\"pagination\":{\"limit\":1,\"has_more\":true}}"
        ));

        VerificationListResponse list = client.verify().list(new ListVerificationsRequest().setLimit(1));

        assertTrue(list.getPagination().isHasMore());
        assertEquals(1, list.getPagination().getLimit());
        assertEquals("ver_1", list.getVerifications().get(0).getId());
        assertEquals("/api/v1/verify?limit=1", mockServer.takeRequest().getPath());
    }

    @Test
    void testResend_sendsAnEmptyJsonObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"ver_1\",\"status\":\"pending\",\"phone\":\"+15551234567\","
                + "\"expires_at\":\"2026-09-25T00:10:00.000Z\",\"sandbox\":true,\"sandbox_code\":\"123456\","
                + "\"message\":\"Sandbox mode: OTP resent. Use the sandbox_code to verify.\"}"
        ));

        SendVerificationResponse response = client.verify().resend("ver_1");

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/verify/ver_1/resend", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
        assertEquals("ver_1", response.getId());
        assertEquals("123456", response.getSandboxCode());
    }
}
