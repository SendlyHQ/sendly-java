package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.CreateTenDlcBrandRequest;
import com.sendly.models.CreateTenDlcCampaignRequest;
import com.sendly.models.TenDlcAssignment;
import com.sendly.models.TenDlcAssignmentListResponse;
import com.sendly.models.TenDlcAssignmentResponse;
import com.sendly.models.TenDlcBrand;
import com.sendly.models.TenDlcBrandListResponse;
import com.sendly.models.TenDlcBrandResponse;
import com.sendly.models.TenDlcCampaign;
import com.sendly.models.TenDlcCampaignListResponse;
import com.sendly.models.TenDlcCampaignResponse;
import com.sendly.models.TenDlcQualifyResponse;
import com.sendly.models.TenDlcQualifyResult;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the 10DLC resource — brands, campaigns, and number assignments.
 */
class TenDlcTest {
    private MockWebServer mockServer;
    private Sendly client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        Sendly.Builder builder = new Sendly.Builder()
                .baseUrl(mockServer.url("/").toString())
                .maxRetries(0);

        client = new Sendly("sk_test_123", builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // ==================== listBrands() Tests ====================

    @Test
    void testListBrands_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[{\"id\":\"brd_1\",\"legalName\":\"Acme Holdings LLC\",\"dba\":\"Acme\"," +
            "\"entityType\":\"PRIVATE_PROFIT\",\"ein\":\"12-3456789\",\"vertical\":\"RETAIL\"," +
            "\"website\":\"https://acme.example\",\"status\":\"verified\",\"identityStatus\":\"VERIFIED\"," +
            "\"failureReasons\":null,\"createdAt\":\"2026-06-01T10:00:00.000Z\",\"updatedAt\":\"2026-06-02T10:00:00.000Z\"}]}"
        ));

        TenDlcBrandListResponse response = client.tenDlc().listBrands();

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        TenDlcBrand brand = response.getData().get(0);
        assertEquals("brd_1", brand.getId());
        assertEquals("Acme Holdings LLC", brand.getLegalName());
        assertEquals("Acme", brand.getDba());
        assertEquals("PRIVATE_PROFIT", brand.getEntityType());
        assertEquals("12-3456789", brand.getEin());
        assertEquals("RETAIL", brand.getVertical());
        assertEquals("https://acme.example", brand.getWebsite());
        assertEquals("verified", brand.getStatus());
        assertEquals("VERIFIED", brand.getIdentityStatus());
        assertNull(brand.getFailureReasons());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/tendlc/brands"));
    }

    @Test
    void testListBrands_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[]}"));

        TenDlcBrandListResponse response = client.tenDlc().listBrands();

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testListBrands_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.tenDlc().listBrands();
        });
    }

    // ==================== createBrand() Tests ====================

    @Test
    void testCreateBrand_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"brd_1\",\"legalName\":\"Acme Holdings LLC\",\"entityType\":\"PRIVATE_PROFIT\"," +
            "\"status\":\"pending\",\"createdAt\":\"2026-06-01T10:00:00.000Z\",\"updatedAt\":\"2026-06-01T10:00:00.000Z\"}}"
        ));

        TenDlcBrandResponse response = client.tenDlc().createBrand(CreateTenDlcBrandRequest.builder()
                .legalName("Acme Holdings LLC")
                .ein("12-3456789")
                .website("https://acme.example")
                .email("ops@acme.example")
                .build());

        assertNotNull(response);
        assertEquals("brd_1", response.getData().getId());
        assertEquals("pending", response.getData().getStatus());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/tendlc/brands"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"legalName\":\"Acme Holdings LLC\""));
        assertTrue(body.contains("\"ein\":\"12-3456789\""));
        assertTrue(body.contains("\"website\":\"https://acme.example\""));
        assertTrue(body.contains("\"email\":\"ops@acme.example\""));
        assertFalse(body.contains("dba"));
    }

    @Test
    void testCreateBrand_missingLegalName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.tenDlc().createBrand(null));

        assertThrows(ValidationException.class, () -> client.tenDlc().createBrand(
                CreateTenDlcBrandRequest.builder().ein("12-3456789").build()));

        assertThrows(ValidationException.class, () -> client.tenDlc().createBrand(
                CreateTenDlcBrandRequest.builder().legalName("").build()));
    }

    // ==================== getBrand() Tests ====================

    @Test
    void testGetBrand_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"brd_1\",\"legalName\":\"Acme Holdings LLC\",\"entityType\":\"PRIVATE_PROFIT\"," +
            "\"status\":\"failed\",\"failureReasons\":[\"EIN mismatch\"]," +
            "\"createdAt\":\"2026-06-01T10:00:00.000Z\",\"updatedAt\":\"2026-06-02T10:00:00.000Z\"}}"
        ));

        TenDlcBrandResponse response = client.tenDlc().getBrand("brd_1");

        assertNotNull(response);
        assertEquals("failed", response.getData().getStatus());
        assertEquals(1, response.getData().getFailureReasons().size());
        assertEquals("EIN mismatch", response.getData().getFailureReasons().get(0));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/tendlc/brands/brd_1"));
    }

    @Test
    void testGetBrand_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.tenDlc().getBrand(null));
        assertThrows(ValidationException.class, () -> client.tenDlc().getBrand(""));
    }

    @Test
    void testGetBrand_404NotFound_throwsNotFoundException() {
        mockServer.enqueue(TestHelpers.mockNotFound());

        assertThrows(NotFoundException.class, () -> {
            client.tenDlc().getBrand("brd_missing");
        });
    }

    // ==================== qualify() Tests ====================

    @Test
    void testQualify_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"useCase\":\"MIXED\",\"qualified\":true,\"reason\":null," +
            "\"throughput\":{\"tier\":\"Standard\",\"carriersReady\":3}}}"
        ));

        TenDlcQualifyResponse response = client.tenDlc().qualify("brd_1", "MIXED");

        assertNotNull(response);
        TenDlcQualifyResult result = response.getData();
        assertEquals("MIXED", result.getUseCase());
        assertTrue(result.isQualified());
        assertNull(result.getReason());
        assertNotNull(result.getThroughput());
        assertEquals("Standard", result.getThroughput().getTier());
        assertEquals(3, result.getThroughput().getCarriersReady());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/tendlc/brands/brd_1/qualify/MIXED"));
    }

    @Test
    void testQualify_notQualified_returnsReason() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"useCase\":\"MARKETING\",\"qualified\":false," +
            "\"reason\":\"Brand not verified\",\"throughput\":null}}"
        ));

        TenDlcQualifyResponse response = client.tenDlc().qualify("brd_1", "MARKETING");

        assertFalse(response.getData().isQualified());
        assertEquals("Brand not verified", response.getData().getReason());
        assertNull(response.getData().getThroughput());
    }

    @Test
    void testQualify_missingArgs_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.tenDlc().qualify(null, "MIXED"));
        assertThrows(ValidationException.class, () -> client.tenDlc().qualify("", "MIXED"));
        assertThrows(ValidationException.class, () -> client.tenDlc().qualify("brd_1", null));
        assertThrows(ValidationException.class, () -> client.tenDlc().qualify("brd_1", ""));
    }

    // ==================== listCampaigns() Tests ====================

    @Test
    void testListCampaigns_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[{\"id\":\"cmp_1\",\"brandId\":\"brd_1\",\"useCase\":\"MIXED\"," +
            "\"subUseCases\":[\"CUSTOMER_CARE\"],\"description\":\"Order updates\",\"status\":\"active\"," +
            "\"sampleMessages\":[\"Your order #123 has shipped!\"]," +
            "\"throughput\":{\"tier\":\"Standard\",\"carriersReady\":3},\"failureReasons\":null," +
            "\"createdAt\":\"2026-06-01T10:00:00.000Z\",\"updatedAt\":\"2026-06-02T10:00:00.000Z\"}]}"
        ));

        TenDlcCampaignListResponse response = client.tenDlc().listCampaigns();

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        TenDlcCampaign campaign = response.getData().get(0);
        assertEquals("cmp_1", campaign.getId());
        assertEquals("brd_1", campaign.getBrandId());
        assertEquals("MIXED", campaign.getUseCase());
        assertEquals(1, campaign.getSubUseCases().size());
        assertEquals("CUSTOMER_CARE", campaign.getSubUseCases().get(0));
        assertEquals("active", campaign.getStatus());
        assertEquals(1, campaign.getSampleMessages().size());
        assertNotNull(campaign.getThroughput());
        assertEquals("Standard", campaign.getThroughput().getTier());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/tendlc/campaigns"));
    }

    // ==================== createCampaign() Tests ====================

    @Test
    void testCreateCampaign_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"cmp_1\",\"brandId\":\"brd_1\",\"useCase\":\"MIXED\"," +
            "\"subUseCases\":[],\"status\":\"pending\",\"sampleMessages\":[\"Your order #123 has shipped!\"]," +
            "\"throughput\":null,\"createdAt\":\"2026-06-01T10:00:00.000Z\",\"updatedAt\":\"2026-06-01T10:00:00.000Z\"}}"
        ));

        TenDlcCampaignResponse response = client.tenDlc().createCampaign(CreateTenDlcCampaignRequest.builder()
                .brandId("brd_1")
                .useCase("MIXED")
                .description("Order updates and support replies")
                .messageFlow("Customers opt in at checkout")
                .sampleMessages(List.of("Your order #123 has shipped!"))
                .optOutKeywords("STOP")
                .build());

        assertNotNull(response);
        assertEquals("cmp_1", response.getData().getId());
        assertEquals("pending", response.getData().getStatus());
        assertNull(response.getData().getThroughput());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/tendlc/campaigns"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"brandId\":\"brd_1\""));
        assertTrue(body.contains("\"useCase\":\"MIXED\""));
        assertTrue(body.contains("\"description\":\"Order updates and support replies\""));
        assertTrue(body.contains("\"messageFlow\":\"Customers opt in at checkout\""));
        assertTrue(body.contains("\"sampleMessages\":[\"Your order #123 has shipped!\"]"));
        assertTrue(body.contains("\"optOutKeywords\":\"STOP\""));
        assertFalse(body.contains("embeddedLink"));
    }

    @Test
    void testCreateCampaign_missingFields_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(null));

        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(
                CreateTenDlcCampaignRequest.builder()
                        .useCase("MIXED").description("d").messageFlow("m")
                        .sampleMessages(List.of("s")).build()));

        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(
                CreateTenDlcCampaignRequest.builder()
                        .brandId("brd_1").description("d").messageFlow("m")
                        .sampleMessages(List.of("s")).build()));

        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(
                CreateTenDlcCampaignRequest.builder()
                        .brandId("brd_1").useCase("MIXED").messageFlow("m")
                        .sampleMessages(List.of("s")).build()));

        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(
                CreateTenDlcCampaignRequest.builder()
                        .brandId("brd_1").useCase("MIXED").description("d")
                        .sampleMessages(List.of("s")).build()));

        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(
                CreateTenDlcCampaignRequest.builder()
                        .brandId("brd_1").useCase("MIXED").description("d")
                        .messageFlow("m").build()));

        assertThrows(ValidationException.class, () -> client.tenDlc().createCampaign(
                CreateTenDlcCampaignRequest.builder()
                        .brandId("brd_1").useCase("MIXED").description("d")
                        .messageFlow("m").sampleMessages(List.of()).build()));
    }

    // ==================== getCampaign() Tests ====================

    @Test
    void testGetCampaign_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"cmp_1\",\"brandId\":\"brd_1\",\"useCase\":\"MIXED\"," +
            "\"subUseCases\":[],\"status\":\"active\",\"sampleMessages\":[]," +
            "\"throughput\":{\"tier\":\"Standard\",\"carriersReady\":3}," +
            "\"createdAt\":\"2026-06-01T10:00:00.000Z\",\"updatedAt\":\"2026-06-02T10:00:00.000Z\"}}"
        ));

        TenDlcCampaignResponse response = client.tenDlc().getCampaign("cmp_1");

        assertNotNull(response);
        assertEquals("active", response.getData().getStatus());
        assertEquals("Standard", response.getData().getThroughput().getTier());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/tendlc/campaigns/cmp_1"));
    }

    @Test
    void testGetCampaign_missingId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.tenDlc().getCampaign(null));
        assertThrows(ValidationException.class, () -> client.tenDlc().getCampaign(""));
    }

    // ==================== assignNumber() Tests ====================

    @Test
    void testAssignNumber_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":{\"id\":\"asn_1\",\"campaignId\":\"cmp_1\",\"phoneNumber\":\"+15551234567\"," +
            "\"status\":\"Under review\",\"assignedAt\":null}}"
        ));

        TenDlcAssignmentResponse response = client.tenDlc().assignNumber("cmp_1", "+15551234567");

        assertNotNull(response);
        TenDlcAssignment assignment = response.getData();
        assertEquals("asn_1", assignment.getId());
        assertEquals("cmp_1", assignment.getCampaignId());
        assertEquals("+15551234567", assignment.getPhoneNumber());
        assertEquals("Under review", assignment.getStatus());
        assertNull(assignment.getAssignedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/tendlc/campaigns/cmp_1/assign"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"phoneNumber\":\"+15551234567\""));
    }

    @Test
    void testAssignNumber_missingArgs_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.tenDlc().assignNumber(null, "+15551234567"));
        assertThrows(ValidationException.class, () -> client.tenDlc().assignNumber("", "+15551234567"));
        assertThrows(ValidationException.class, () -> client.tenDlc().assignNumber("cmp_1", null));
        assertThrows(ValidationException.class, () -> client.tenDlc().assignNumber("cmp_1", ""));
    }

    @Test
    void testAssignNumber_400_throwsValidationException() {
        mockServer.enqueue(TestHelpers.mockValidationError("Campaign is not active"));

        assertThrows(ValidationException.class, () -> {
            client.tenDlc().assignNumber("cmp_1", "+15551234567");
        });
    }

    // ==================== listAssignments() Tests ====================

    @Test
    void testListAssignments_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"data\":[{\"id\":\"asn_1\",\"campaignId\":\"cmp_1\",\"phoneNumber\":\"+15551234567\"," +
            "\"status\":\"Active\",\"assignedAt\":\"2026-06-03T10:00:00.000Z\"}]}"
        ));

        TenDlcAssignmentListResponse response = client.tenDlc().listAssignments();

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        TenDlcAssignment assignment = response.getData().get(0);
        assertEquals("Active", assignment.getStatus());
        assertEquals("2026-06-03T10:00:00.000Z", assignment.getAssignedAt());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/tendlc/assignments"));
    }

    @Test
    void testListAssignments_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"data\":[]}"));

        TenDlcAssignmentListResponse response = client.tenDlc().listAssignments();

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testListAssignments_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.tenDlc().listAssignments();
        });
    }
}
