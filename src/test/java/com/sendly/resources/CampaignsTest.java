package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.Campaign;
import com.sendly.models.CampaignPreview;
import com.sendly.models.ScheduleCampaignRequest;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the campaigns resource against the shapes the API sends and accepts.
 */
class CampaignsTest {
    static final String PUBLIC_CAMPAIGN =
        "{\"id\":\"camp_1\",\"userId\":\"user_1\",\"organizationId\":null,\"name\":\"Fall\",\"status\":\"scheduled\","
            + "\"messageText\":\"Hi\",\"fromSender\":null,\"targetType\":\"contact_list\",\"targetListId\":\"lst_1\","
            + "\"manualRecipients\":null,\"excludeOptedOut\":true,\"sendNow\":false,"
            + "\"scheduledAt\":\"2026-10-01T10:00:00.000Z\",\"timezone\":\"UTC\",\"batchId\":null,"
            + "\"totalRecipients\":3,\"estimatedCredits\":6,\"sentCount\":0,\"deliveredCount\":0,\"failedCount\":0,"
            + "\"creditsUsed\":0,\"creditsRefunded\":0,\"createdAt\":\"2026-09-25T00:00:00.000Z\","
            + "\"updatedAt\":\"2026-09-25T00:00:01.000Z\",\"sentAt\":null,\"completedAt\":null,"
            + "\"text\":\"Hi\",\"contact_list_ids\":[\"lst_1\"],\"created_at\":\"2026-09-25T00:00:00.000Z\","
            + "\"updated_at\":\"2026-09-25T00:00:01.000Z\"}";

    private static String draftPublicCampaign(String id, String name, String timezone) {
        return "{\"id\":\"" + id + "\",\"userId\":\"user_1\",\"organizationId\":null,\"name\":\"" + name + "\","
            + "\"status\":\"draft\",\"messageText\":\"Hi\",\"fromSender\":null,\"targetType\":\"contact_list\","
            + "\"targetListId\":\"lst_1\",\"manualRecipients\":null,\"excludeOptedOut\":true,\"sendNow\":false,"
            + "\"scheduledAt\":null,\"timezone\":\"" + timezone + "\",\"batchId\":null,\"totalRecipients\":0,"
            + "\"estimatedCredits\":0,\"sentCount\":0,\"deliveredCount\":0,\"failedCount\":0,\"creditsUsed\":0,"
            + "\"creditsRefunded\":0,\"createdAt\":\"2026-09-25T00:10:00.000Z\",\"updatedAt\":\"2026-09-25T00:10:00.000Z\","
            + "\"sentAt\":null,\"completedAt\":null,\"text\":\"Hi\",\"contact_list_ids\":[\"lst_1\"],"
            + "\"created_at\":\"2026-09-25T00:10:00.000Z\",\"updated_at\":\"2026-09-25T00:10:00.000Z\"}";
    }

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

    static final String RAW_ROW =
        "{\"id\":\"camp_1\",\"name\":\"Fall\",\"status\":\"sent\",\"messageText\":\"Hi\",\"targetListId\":\"lst_1\","
            + "\"totalRecipients\":3,\"estimatedCredits\":6,\"sentCount\":3,\"deliveredCount\":2,\"failedCount\":1,"
            + "\"creditsUsed\":6,\"createdAt\":\"2026-09-25T00:00:00.000Z\",\"updatedAt\":\"2026-09-25T00:05:00.000Z\","
            + "\"scheduledAt\":null,\"sentAt\":\"2026-09-25T00:01:00.000Z\",\"completedAt\":\"2026-09-25T00:04:00.000Z\"}";

    static final String SENT_PUBLIC_CAMPAIGN = RAW_ROW.substring(0, RAW_ROW.length() - 1)
            + ",\"text\":\"Hi\",\"contact_list_ids\":[\"lst_1\"],\"created_at\":\"2026-09-25T00:00:00.000Z\","
            + "\"updated_at\":\"2026-09-25T00:05:00.000Z\"}";

    private static void assertSentCampaign(Campaign campaign) {
        assertEquals("camp_1", campaign.getId());
        assertEquals("Hi", campaign.getText());
        assertEquals(java.util.List.of("lst_1"), campaign.getContactListIds());
        assertEquals(3, campaign.getRecipientCount());
        assertEquals(3, campaign.getSentCount());
        assertEquals(2, campaign.getDeliveredCount());
        assertEquals(1, campaign.getFailedCount());
        assertEquals(6.0, campaign.getEstimatedCredits());
        assertEquals(6.0, campaign.getCreditsUsed());
        assertNull(campaign.getScheduledAt());
        assertEquals("2026-09-25T00:01:00.000Z", campaign.getStartedAt());
        assertEquals("2026-09-25T00:04:00.000Z", campaign.getCompletedAt());
        assertEquals("2026-09-25T00:00:00.000Z", campaign.getCreatedAt());
        assertEquals("2026-09-25T00:05:00.000Z", campaign.getUpdatedAt());
    }

    @Test
    void testGet_readsTheRawCampaignRow() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(RAW_ROW));

        assertSentCampaign(client.campaigns().get("camp_1"));
    }

    @Test
    void testGet_readsTheBatchIdOfASentCampaign() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(RAW_ROW.replace("\"creditsUsed\":6,", "\"creditsUsed\":6,\"batchId\":\"batch_1\",")));

        assertEquals("batch_1", client.campaigns().get("camp_1").getBatchId());
    }

    @Test
    void testGet_readsThePublicCampaign() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(SENT_PUBLIC_CAMPAIGN));

        assertSentCampaign(client.campaigns().get("camp_1"));
    }

    @Test
    void testGet_readsAScheduledPublicCampaign() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(PUBLIC_CAMPAIGN));

        Campaign campaign = client.campaigns().get("camp_1");

        assertEquals("scheduled", campaign.getStatus());
        assertEquals("2026-10-01T10:00:00.000Z", campaign.getScheduledAt());
        assertEquals(3, campaign.getRecipientCount());
        assertEquals(6.0, campaign.getEstimatedCredits());
        assertEquals(0, campaign.getSentCount());
        assertEquals("UTC", campaign.getTimezone());
        assertNull(campaign.getStartedAt());
    }

    @Test
    void testList_readsThePublicCampaigns() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"campaigns\":[" + SENT_PUBLIC_CAMPAIGN + "],\"total\":1,\"limit\":50,\"offset\":0}"
        ));

        com.sendly.models.CampaignList list = client.campaigns().list();

        assertEquals(1, list.getTotal());
        assertSentCampaign(list.getCampaigns().get(0));
    }

    @Test
    void testClone_readsTheDraftCopyTheApiCreates() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(201)
                .setBody(draftPublicCampaign("camp_2", "Fall (Copy)", "UTC")));

        Campaign campaign = client.campaigns().clone("camp_1");

        assertEquals("camp_2", campaign.getId());
        assertEquals("Fall (Copy)", campaign.getName());
        assertEquals("draft", campaign.getStatus());
        assertEquals("Hi", campaign.getText());
        assertEquals(java.util.List.of("lst_1"), campaign.getContactListIds());
        assertNull(campaign.getScheduledAt());
        assertEquals(0, campaign.getRecipientCount());
        assertEquals(0, campaign.getSentCount());
        assertEquals(0.0, campaign.getEstimatedCredits());
        assertEquals("UTC", campaign.getTimezone());
        assertNull(campaign.getStartedAt());
        assertEquals("2026-09-25T00:10:00.000Z", campaign.getCreatedAt());
        assertEquals("/api/v1/campaigns/camp_1/clone", mockServer.takeRequest().getPath());
    }

    @Test
    void testCreate_sendsTheKeysTheApiReads() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(201)
                .setBody(draftPublicCampaign("camp_1", "Fall", "America/New_York")));

        Campaign campaign = client.campaigns().create(com.sendly.models.CreateCampaignRequest.builder()
                .name("Fall")
                .text("Hi")
                .contactListIds(java.util.List.of("lst_1"))
                .build());

        JsonObject sent = JsonParser.parseString(mockServer.takeRequest().getBody().readUtf8()).getAsJsonObject();
        assertEquals("Fall", sent.get("name").getAsString());
        assertEquals("Hi", sent.get("text").getAsString());
        assertEquals("lst_1", sent.getAsJsonArray("contact_list_ids").get(0).getAsString());
        assertEquals("camp_1", campaign.getId());
        assertEquals("Hi", campaign.getText());
        assertEquals("draft", campaign.getStatus());
        assertNull(campaign.getScheduledAt());
        assertEquals(0, campaign.getRecipientCount());
    }

    @Test
    void testSend_readsTheBatchResultTheApiReturns() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"batchId\":\"batch_1\",\"status\":\"completed\",\"total\":3,\"sent\":3,\"failed\":0,"
                + "\"creditsUsed\":6,\"creditsRefunded\":0,\"messages\":[]}"
        ));

        Campaign campaign = client.campaigns().send("camp_1");

        assertEquals("camp_1", campaign.getId());
        assertEquals(3, campaign.getRecipientCount());
        assertEquals(3, campaign.getSentCount());
        assertEquals(0, campaign.getFailedCount());
        assertEquals(6.0, campaign.getCreditsUsed());
        assertEquals("completed", campaign.getStatus());
        assertEquals("batch_1", campaign.getBatchId());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/campaigns/camp_1/send", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
    }

    @Test
    void testPreview_readsTheCountsTheApiSends() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"totalRecipients\":3,\"estimatedCredits\":6,\"optedOutCount\":1,\"invalidCount\":0,"
                + "\"sampleRecipients\":[{\"phone\":\"+15551234567\",\"name\":\"Ann\"}],\"blockedCount\":0,"
                + "\"sendableCount\":3,\"byCountry\":{\"US\":{\"count\":3,\"credits\":6,\"allowed\":true}},"
                + "\"warnings\":[\"1 contact has opted out\"],\"recipientCount\":3,\"currentBalance\":100,"
                + "\"hasEnoughCredits\":true}"
        ));

        CampaignPreview preview = client.campaigns().preview("camp_1");

        assertEquals(3, preview.getRecipientCount());
        assertEquals(6.0, preview.getEstimatedCredits());
        assertEquals(0, preview.getBlockedCount());
        assertEquals(3, preview.getSendableCount());
        assertEquals(java.util.List.of("1 contact has opted out"), preview.getWarnings());
        assertEquals(100, preview.getCurrentBalance());
        assertTrue(preview.hasEnoughCredits());
        assertEquals(1, preview.getOptedOutCount());
        assertEquals(0, preview.getInvalidCount());
        assertEquals(0.0, preview.getEstimatedCost());
        assertEquals("/api/v1/campaigns/camp_1/preview", mockServer.takeRequest().getPath());
    }

    @Test
    void testSchedule_sendsTheScheduledAtKeyTheRouteReads() throws Exception {
        mockServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                JsonObject body = JsonParser.parseString(request.getBody().clone().readUtf8()).getAsJsonObject();
                if (!body.has("scheduledAt")) {
                    return new MockResponse().setResponseCode(400)
                            .setBody("{\"error\":\"invalid_request\",\"message\":\"scheduledAt is required\"}");
                }
                return TestHelpers.mockSuccess(PUBLIC_CAMPAIGN);
            }
        });

        Campaign campaign = client.campaigns().schedule("camp_1", ScheduleCampaignRequest.builder()
                .scheduledAt("2026-10-01T10:00:00Z")
                .timezone("UTC")
                .build());

        assertEquals("camp_1", campaign.getId());
        assertEquals("scheduled", campaign.getStatus());
        assertEquals("2026-10-01T10:00:00.000Z", campaign.getScheduledAt());
        assertEquals(3, campaign.getRecipientCount());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("/api/v1/campaigns/camp_1/schedule", request.getPath());
        JsonObject sent = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals("2026-10-01T10:00:00Z", sent.get("scheduledAt").getAsString());
        assertEquals("UTC", sent.get("timezone").getAsString());
        assertFalse(sent.has("scheduled_at"));
    }
}
