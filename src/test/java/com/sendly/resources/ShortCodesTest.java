package com.sendly.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.SendlyException;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.ShortCode;
import com.sendly.models.ShortCodeApplicationInput;
import com.sendly.models.ShortCodeApplicationView;
import com.sendly.models.ShortCodeLease;
import com.sendly.models.ShortCodePreflight;
import com.sendly.models.ShortCodeSetupFee;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the short codes resource against the bodies the API sends.
 */
class ShortCodesTest {
    private static final String LEASE =
        "{\"monthlyCents\":100000,\"minimumTermMonths\":3,\"state\":\"not_started\",\"billingEnabled\":true,"
            + "\"startedAt\":null,\"nextChargeAt\":null,\"paidThrough\":null,\"termEndsAt\":null,\"endsAt\":null,"
            + "\"cancelRequestedAt\":null,\"pastDue\":null,\"dueMonths\":0,\"dueCents\":0";

    private static final String APPLICATION =
        "{\"id\":\"sc_1\",\"shortCode\":null,\"countryCode\":\"US\",\"status\":\"requested\","
            + "\"reviewStatus\":\"awaiting_review\",\"reviewNote\":null,\"orderType\":\"new\",\"codeType\":\"random\","
            + "\"contentProviderSameAsBrand\":true,\"sampleMessages\":[\"Acme: your order shipped. Reply STOP to opt out.\"],"
            + "\"useCase\":\"Order and delivery alerts\",\"optInFlow\":\"Customers text JOIN after checkout.\","
            + "\"expectedMonthlyVolume\":\"50000\",\"expectedDailyVolume\":\"2000\",\"helpResponse\":null,"
            + "\"stopConfirmation\":null,\"optInConfirmation\":null,\"privacyPolicyUrl\":\"https://acme.example/privacy\","
            + "\"termsUrl\":\"https://acme.example/terms\",\"messageFrequency\":\"Up to 4 messages per order\","
            + "\"campaignKeyword\":\"JOIN\",\"requestedDigits\":null,\"losingProvider\":null,"
            + "\"brandContactName\":\"Ada Lovelace\",\"brandContactEmail\":\"ada@acme.example\","
            + "\"brandContactPhone\":\"+15125550100\",\"contentProviderLegalName\":null,\"contentProviderEin\":null,"
            + "\"contentProviderContactName\":null,\"contentProviderContactEmail\":null,"
            + "\"contentProviderContactPhone\":null,\"brandRegistrationStatus\":\"registered\","
            + "\"contentProviderRegistrationStatus\":\"not_started\",\"submittedAt\":\"2026-09-01T10:00:00.000Z\","
            + "\"reviewedAt\":null,\"filedAt\":null,\"activatedAt\":null,\"registryRevettingDueAt\":null,"
            + "\"createdAt\":\"2026-08-30T10:00:00.000Z\",\"updatedAt\":\"2026-09-01T10:00:00.000Z\"}";

    private static final String VIEW_FIELDS =
        "\"enabled\":true,\"application\":" + APPLICATION + ",\"prefill\":{\"source\":\"none\"},"
            + "\"editable\":false,\"lockMessage\":\"Sendly is reviewing this application.\","
            + "\"canStartNewApplication\":false,\"generatedDefaults\":null,\"requiredDocuments\":[\"loa\"],"
            + "\"missingDocuments\":[],\"documents\":[{\"kind\":\"loa\",\"templateReady\":true,"
            + "\"signedAt\":\"2026-09-02T10:00:00.000Z\",\"signedByName\":\"Ada Lovelace\",\"awaitingSignature\":false}],"
            + "\"quote\":{\"codeType\":\"random\",\"monthlyUsd\":1000,\"setupUsd\":999,\"currency\":\"USD\","
            + "\"autoBilled\":false,\"setupCents\":99900,\"monthlyCents\":100000,\"minimumTermMonths\":3,"
            + "\"setupChargedAt\":\"submit\",\"leaseStartsAt\":\"go_live\"},"
            + "\"billing\":{\"setupFee\":{\"amountCents\":99900,\"status\":\"paid\",\"chargedAt\":\"submit\","
            + "\"paidAt\":\"2026-09-01T10:00:01.000Z\",\"refundedCents\":0,\"refundedAt\":null},"
            + "\"lease\":" + LEASE + ",\"startsAt\":\"go_live\"},"
            + "\"terms\":{\"version\":\"2026-09\",\"acceptedVersion\":\"2026-09\",\"acceptedAt\":\"2026-09-01T10:00:00.000Z\"},"
            + "\"refundPolicy\":\"full_refund_before_filing\",\"exempt\":null,"
            + "\"charges\":[{\"id\":\"scc_1\",\"kind\":\"setup\",\"amountCents\":99900,\"status\":\"paid\","
            + "\"paidAt\":\"2026-09-01T10:00:01.000Z\",\"refundedCents\":0,\"refundedAt\":null,\"periodStart\":null,"
            + "\"periodEnd\":null,\"createdAt\":\"2026-09-01T10:00:00.000Z\"}]},"
            + "\"verification\":{\"verified\":true,\"state\":\"verified\",\"href\":\"/verify\"},"
            + "\"carriers\":{\"approved\":0,\"total\":4,\"overall\":\"not_submitted\",\"byCarrier\":[]}";

    private static final String VIEW = "{" + VIEW_FIELDS + "}";

    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        client = new Sendly("sk_live_123", new Sendly.Builder()
                .baseUrl(mockServer.url("/api/v1").toString())
                .maxRetries(0));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private static JsonObject bodyOf(RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    @Test
    void testList_readsEachCodeWithItsBilling() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"shortCodes\":[{\"id\":\"sc_1\",\"shortCode\":\"55123\",\"countryCode\":\"US\",\"status\":\"active\","
                + "\"reviewStatus\":\"filed\",\"useCase\":\"Order and delivery alerts\","
                + "\"createdAt\":\"2026-08-30T10:00:00.000Z\",\"billing\":{\"setupFee\":{\"status\":\"paid\","
                + "\"amountCents\":99900},\"lease\":" + LEASE + "}}},"
                + "{\"id\":\"sc_2\",\"shortCode\":null,\"countryCode\":\"US\",\"status\":\"requested\","
                + "\"reviewStatus\":\"draft\",\"useCase\":\"\",\"createdAt\":\"2026-09-30T10:00:00.000Z\","
                + "\"billing\":{\"setupFee\":{\"status\":\"unpaid\",\"amountCents\":99900},\"lease\":" + LEASE + "}}}]}"
        ));

        List<ShortCode> codes = client.shortCodes().list();

        assertEquals(2, codes.size());
        ShortCode first = codes.get(0);
        assertEquals("sc_1", first.getId());
        assertEquals("55123", first.getShortCode());
        assertTrue(first.isActive());
        assertEquals(ShortCode.REVIEW_FILED, first.getReviewStatus());
        assertEquals(ShortCodeSetupFee.STATUS_PAID, first.getBilling().getSetupFee().getStatus());
        assertEquals(99900, first.getBilling().getSetupFee().getAmountCents());
        assertEquals(ShortCodeLease.STATE_NOT_STARTED, first.getBilling().getLease().getState());
        assertEquals(100000, first.getBilling().getLease().getMonthlyCents());
        assertNull(first.getBilling().getLease().getPastDue());
        assertTrue(first.getBilling().getCharges().isEmpty());
        assertNull(codes.get(1).getShortCode());
        assertFalse(codes.get(1).isActive());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v1/short_codes", request.getPath());
    }

    @Test
    void testApplicationGet_readsTheApplicationDocumentsAndBilling() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VIEW));

        ShortCodeApplicationView view = client.shortCodes().application().get();

        assertTrue(view.isEnabled());
        assertFalse(view.isEditable());
        assertEquals("Sendly is reviewing this application.", view.getLockMessage());
        assertEquals("sc_1", view.getApplication().getId());
        assertEquals("awaiting_review", view.getApplication().getReviewStatus());
        assertEquals("Order and delivery alerts", view.getApplication().getUseCase());
        assertEquals(1, view.getApplication().getSampleMessages().size());
        assertTrue(view.getApplication().isContentProviderSameAsBrand());
        assertNull(view.getApplication().getHelpResponse());
        assertEquals(Arrays.asList("loa"), view.getRequiredDocuments());
        assertTrue(view.getMissingDocuments().isEmpty());
        assertEquals("loa", view.getDocuments().get(0).getKind());
        assertTrue(view.getDocuments().get(0).isTemplateReady());
        assertEquals("Ada Lovelace", view.getDocuments().get(0).getSignedByName());
        assertEquals(99900, view.getQuote().get("setupCents").getAsInt());
        assertEquals("paid", view.getBilling().getSetupFee().getStatus());
        assertEquals("submit", view.getBilling().getSetupFee().getChargedAt());
        assertEquals("go_live", view.getBilling().getLease().getStartsAt());
        assertEquals("2026-09", view.getBilling().getTermsAcceptedVersion());
        assertEquals("full_refund_before_filing", view.getBilling().getRefundPolicy());
        assertNull(view.getBilling().getExempt());
        assertEquals("setup", view.getBilling().getCharges().get(0).getKind());
        assertTrue(view.isVerified());
        assertEquals("verified", view.getVerificationState());
        assertEquals(4, view.getCarriers().get("total").getAsInt());
        assertNull(view.getGeneratedDefaults());
        assertNull(view.getSubmitted());
        assertTrue(view.getIgnoredFields().isEmpty());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v1/short_codes/application", request.getPath());
    }

    @Test
    void testApplicationUpdate_putsOnlyTheSetAnswersWithTheKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{" + VIEW_FIELDS + ",\"ignoredFields\":[\"status\"]}"));

        ShortCodeApplicationView view = client.shortCodes().application().update(
            ShortCodeApplicationInput.builder()
                .useCase("Delivery alerts for Acme orders")
                .messageFrequency("4 messages per month")
                .contentProviderSameAsBrand(true)
                .sampleMessages(Arrays.asList("Acme: your order shipped."))
                .build(),
            new IdempotentRequestOptions("sc-save-1"));

        assertEquals(Arrays.asList("status"), view.getIgnoredFields());
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/api/v1/short_codes/application", request.getPath());
        assertEquals("sc-save-1", request.getHeader("Idempotency-Key"));
        JsonObject body = bodyOf(request);
        assertEquals(4, body.size());
        assertEquals("Delivery alerts for Acme orders", body.get("useCase").getAsString());
        assertEquals("4 messages per month", body.get("messageFrequency").getAsString());
        assertTrue(body.get("contentProviderSameAsBrand").getAsBoolean());
        assertEquals("Acme: your order shipped.", body.getAsJsonArray("sampleMessages").get(0).getAsString());
    }

    @Test
    void testApplicationUpdate_withoutOptionsSendsNoKey() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(VIEW));

        client.shortCodes().application().update(ShortCodeApplicationInput.builder().useCase("Alerts").build());

        assertNull(mockServer.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void testApplicationCheck_postsToPreflightAndReadsTheIssues() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"ok\":false,\"issues\":[{\"path\":\"helpResponse\",\"message\":\"Add a HELP reply.\"}],"
                + "\"requiredDocuments\":[\"loa\"],\"missingDocuments\":[\"loa\"],\"documents\":[]}"
        ));

        ShortCodePreflight check = client.shortCodes().application().check();

        assertFalse(check.isOk());
        assertEquals(1, check.getIssues().size());
        assertEquals("helpResponse", check.getIssues().get(0).getPath());
        assertEquals("Add a HELP reply.", check.getIssues().get(0).getMessage());
        assertEquals(Arrays.asList("loa"), check.getMissingDocuments());
        assertTrue(check.getDocuments().isEmpty());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/short_codes/application/preflight", request.getPath());
        assertEquals("{}", request.getBody().readUtf8());
    }

    @Test
    void testApplicationCheck_sendsUnsavedAnswers() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"ok\":true,\"issues\":[],\"requiredDocuments\":[],\"missingDocuments\":[],\"documents\":[]}"));

        ShortCodePreflight check = client.shortCodes().application().check(
            ShortCodeApplicationInput.builder().helpResponse("Acme: reply STOP to opt out.").build());

        assertTrue(check.isOk());
        assertEquals("Acme: reply STOP to opt out.",
            bodyOf(mockServer.takeRequest()).get("helpResponse").getAsString());
    }

    @Test
    void testApplicationSubmit_sendsAcceptTermsAndTheKeyAndReadsThePayment() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{" + VIEW_FIELDS + ",\"submitted\":true,\"alreadySubmitted\":false,\"notified\":true,"
                + "\"payment\":{\"status\":\"paid\",\"charged\":true}}"));

        ShortCodeApplicationView view = client.shortCodes().application().submit(
            ShortCodeApplicationInput.builder().acceptTerms(true).build(),
            new IdempotentRequestOptions("sc-submit-1"));

        assertEquals(Boolean.TRUE, view.getSubmitted());
        assertEquals(Boolean.FALSE, view.getAlreadySubmitted());
        assertEquals(Boolean.TRUE, view.getNotified());
        assertEquals("paid", view.getPaymentStatus());
        assertEquals(Boolean.TRUE, view.getPaymentCharged());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v1/short_codes/application/submit", request.getPath());
        assertEquals("sc-submit-1", request.getHeader("Idempotency-Key"));
        assertEquals("{\"acceptTerms\":true}", request.getBody().readUtf8());
    }

    @Test
    void testApplicationSubmit_paymentRequiredThrowsWithTheCode() {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(402)
            .setBody("{\"error\":\"payment_method_required\",\"message\":\"Add a card to submit.\"}")
            .addHeader("Content-Type", "application/json"));

        SendlyException e = assertThrows(SendlyException.class, () ->
            client.shortCodes().application().submit(ShortCodeApplicationInput.builder().acceptTerms(true).build()));
        assertEquals(402, e.getStatusCode());
    }
}
