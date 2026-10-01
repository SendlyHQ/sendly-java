package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.Account;
import com.sendly.models.ApiKey;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the account resource against the shapes the API sends.
 */
class AccountTest {
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
    void testGet_readsTheUserTheApiNests() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"user\":{\"id\":\"user_1\",\"email\":\"dev@example.com\",\"createdAt\":\"2026-01-01T00:00:00.000Z\"},"
                + "\"organization\":{\"id\":\"org_1\",\"name\":\"Acme\",\"isPersonal\":false},"
                + "\"credits\":{\"balance\":\"100\",\"reservedBalance\":\"0\"},\"verification\":null,"
                + "\"apiKey\":{\"id\":\"key_1\",\"name\":\"CI\",\"type\":\"test\",\"scopes\":[\"sms:send\"]},"
                + "\"limits\":{\"messagesPerMinute\":60,\"messagesPerDay\":100}}"
        ));

        Account account = client.account().get();

        assertEquals("user_1", account.getId());
        assertEquals("dev@example.com", account.getEmail());
        assertEquals("2026-01-01T00:00:00.000Z", account.getCreatedAt());
        assertEquals("/api/v1/account", mockServer.takeRequest().getPath());
    }

    @Test
    void testRevokeApiKey_reportsTheKeyRevoked() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"key_1\",\"name\":\"n\",\"revoked\":true,\"revokedAt\":\"2026-09-25T00:00:00.000Z\"}"
        ));

        ApiKey key = client.account().revokeApiKey("key_1");

        assertTrue(key.isRevoked());
        assertEquals("2026-09-25T00:00:00.000Z", key.getRevokedAt());
        assertEquals("/api/v1/account/keys/key_1/revoke", mockServer.takeRequest().getPath());
    }

    @Test
    void testGetApiKey_readsScopesAndIsActive() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"key_1\",\"name\":\"CI\",\"type\":\"live\",\"prefix\":\"sk_live_v1_ab...\","
                + "\"scopes\":[\"sms:send\"],\"isActive\":false,\"createdAt\":\"2026-09-01T00:00:00.000Z\","
                + "\"lastUsedAt\":null,\"expiresAt\":null,\"revokedAt\":\"2026-09-25T00:00:00.000Z\"}"
        ));
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"id\":\"key_2\",\"name\":\"CI\",\"type\":\"live\",\"prefix\":\"sk_live_v1_cd...\","
                + "\"scopes\":[\"sms:send\",\"sms:read\"],\"isActive\":true,\"revokedAt\":null}"
        ));

        ApiKey revoked = client.account().getApiKey("key_1");
        ApiKey active = client.account().getApiKey("key_2");

        assertEquals(List.of("sms:send"), revoked.getPermissions());
        assertTrue(revoked.isRevoked());
        assertEquals("2026-09-25T00:00:00.000Z", revoked.getRevokedAt());
        assertEquals(List.of("sms:send", "sms:read"), active.getPermissions());
        assertFalse(active.isRevoked());
        assertNull(active.getRevokedAt());
    }

    @Test
    void testListApiKeys_keepsPermissionsAndIsRevoked() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"keys\":[{\"id\":\"key_1\",\"name\":\"CI\",\"type\":\"test\",\"prefix\":\"sk_test_v1_ab...\","
                + "\"scopes\":[\"sms:send\"],\"permissions\":[\"sms:send\"],\"isActive\":false,\"isRevoked\":true,"
                + "\"createdAt\":\"2026-09-01T00:00:00.000Z\",\"lastUsedAt\":null,\"expiresAt\":null}]}"
        ));

        List<ApiKey> keys = client.account().listApiKeys();

        assertEquals(List.of("sms:send"), keys.get(0).getPermissions());
        assertTrue(keys.get(0).isRevoked());
        assertNull(keys.get(0).getRevokedAt());
    }
}
