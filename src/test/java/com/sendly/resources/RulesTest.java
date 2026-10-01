package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.models.Rule;
import com.sendly.models.RuleListResponse;
import okhttp3.mockwebserver.Dispatcher;
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
 * Tests for the rules resource against the shapes the API stores and evaluates.
 */
class RulesTest {
    private static final String OBJECT_RULE =
        "{\"id\":\"rule_1\",\"userId\":\"user_1\",\"organizationId\":null,\"name\":\"billing\","
            + "\"conditions\":{\"intent\":\"billing\"},\"actions\":{\"addLabels\":[\"lbl_1\"]},"
            + "\"enabled\":true,\"priority\":0,\"createdAt\":\"2026-09-25T00:00:00.000Z\","
            + "\"updatedAt\":\"2026-09-25T00:00:01.000Z\"}";

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

    private void echoCreatedRule() {
        mockServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                JsonObject body = JsonParser.parseString(request.getBody().clone().readUtf8()).getAsJsonObject();
                JsonObject row = JsonParser.parseString(OBJECT_RULE).getAsJsonObject();
                row.add("name", body.get("name"));
                row.add("conditions", body.get("conditions"));
                row.add("actions", body.get("actions"));
                return new MockResponse().setResponseCode(201).setBody(row.toString());
            }
        });
    }

    private JsonObject sentBody() throws InterruptedException {
        return JsonParser.parseString(mockServer.takeRequest().getBody().readUtf8()).getAsJsonObject();
    }

    @Test
    void testList_readsObjectShapedRules() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + OBJECT_RULE + "]}"));

        RuleListResponse rules = client.rules().list();

        assertEquals(1, rules.getData().size());
        Rule rule = rules.getData().get(0);
        assertEquals("rule_1", rule.getId());
        assertEquals("2026-09-25T00:00:00.000Z", rule.getCreatedAt());
        assertEquals("2026-09-25T00:00:01.000Z", rule.getUpdatedAt());
    }

    @Test
    void testCreate_sendsConditionsAndActionsAsObjects() throws Exception {
        echoCreatedRule();

        Rule rule = client.rules().create("billing",
                Map.of("intent", "billing"),
                Map.of("addLabels", List.of("lbl_1")));

        JsonObject body = sentBody();
        assertEquals("{\"intent\":\"billing\"}", body.get("conditions").toString());
        assertEquals("{\"addLabels\":[\"lbl_1\"]}", body.get("actions").toString());
        assertFalse(body.has("priority"));
        assertEquals("billing", rule.getConditionsMap().get("intent"));
        assertEquals(List.of("lbl_1"), rule.getActionsMap().get("addLabels"));
        assertTrue(rule.isEnabled());
    }

    @Test
    void testCreate_withPrioritySendsIt() throws Exception {
        echoCreatedRule();

        client.rules().create("billing", Map.of("intent", List.of("billing", "refund")),
                Map.of("addLabels", List.of("lbl_1"), "closeConversation", true), 2);

        JsonObject body = sentBody();
        assertEquals(2, body.get("priority").getAsInt());
        assertEquals("[\"billing\",\"refund\"]", body.getAsJsonObject("conditions").get("intent").toString());
        assertTrue(body.getAsJsonObject("actions").get("closeConversation").getAsBoolean());
    }

    @Test
    void testList_exposesConditionsAndActionsAsMaps() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + OBJECT_RULE + "]}"));

        Rule rule = client.rules().list().getData().get(0);

        assertEquals("billing", rule.getConditionsMap().get("intent"));
        assertEquals(List.of("lbl_1"), rule.getActionsMap().get("addLabels"));
        assertTrue(rule.isEnabled());
        assertEquals(0, rule.getPriority());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedListGetters_wrapTheObject() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[" + OBJECT_RULE + "]}"));

        Rule rule = client.rules().list().getData().get(0);

        assertEquals(1, rule.getConditions().size());
        assertEquals("billing", rule.getConditions().get(0).get("intent"));
        assertEquals(List.of("lbl_1"), rule.getActions().get(0).get("addLabels"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedListGetters_keepAnArraysEntries() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[{\"id\":\"rule_old\",\"name\":\"old\",\"conditions\":[{\"intent\":\"billing\"}],"
                + "\"actions\":[{\"addLabels\":[\"lbl_1\"]}],\"enabled\":false,\"priority\":1}]}"
        ));

        Rule rule = client.rules().list().getData().get(0);

        assertEquals("billing", rule.getConditions().get(0).get("intent"));
        assertEquals(1, rule.getActions().size());
        assertTrue(rule.getConditionsMap().isEmpty());
        assertFalse(rule.isEnabled());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testCreate_listOverloadRefusesMoreThanOneEntry() {
        assertThrows(com.sendly.exceptions.ValidationException.class, () -> client.rules().create("billing",
                List.of(Map.of("intent", "billing"), Map.of("sentiment", "negative")),
                List.of(Map.of("addLabels", List.of("lbl_1")))));
        assertEquals(0, mockServer.getRequestCount());
    }

    @Test
    void testUpdate_readsTheObjectShapedRule() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(OBJECT_RULE.replace("\"enabled\":true", "\"enabled\":false")));

        Rule rule = client.rules().update("rule_1", Map.of("enabled", false));

        assertFalse(rule.isEnabled());
        assertEquals("billing", rule.getConditionsMap().get("intent"));
        assertEquals("{\"enabled\":false}", mockServer.takeRequest().getBody().readUtf8());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testCreate_listOverloadSendsItsSingleEntryAsTheObject() throws Exception {
        echoCreatedRule();

        client.rules().create("billing",
                List.of(Map.of("intent", "billing")),
                List.of(Map.of("addLabels", List.of("lbl_1"))));

        JsonObject body = sentBody();
        assertTrue(body.get("conditions").isJsonObject(), body.toString());
        assertEquals("billing", body.getAsJsonObject("conditions").get("intent").getAsString());
        assertTrue(body.get("actions").isJsonObject(), body.toString());
        assertEquals("lbl_1", body.getAsJsonObject("actions").getAsJsonArray("addLabels").get(0).getAsString());
    }
}
