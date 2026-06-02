package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.TestHelpers;
import com.sendly.exceptions.*;
import com.sendly.models.AvailableNumber;
import com.sendly.models.AvailableNumbersResponse;
import com.sendly.models.BuyNumberRequest;
import com.sendly.models.BuyNumberResponse;
import com.sendly.models.NumberCountriesResponse;
import com.sendly.models.NumberCountry;
import com.sendly.models.OwnedNumber;
import com.sendly.models.OwnedNumbersResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Numbers resource — discover, buy, and list phone numbers.
 */
class NumbersTest {
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

    // ==================== listCountries() Tests ====================

    @Test
    void testListCountries_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"countries\":[{\"code\":\"GB\",\"name\":\"United Kingdom\",\"numberTypes\":[\"mobile\",\"local\"]}," +
            "{\"code\":\"US\",\"name\":\"United States\",\"numberTypes\":[\"local\",\"toll_free\"]}]}"
        ));

        NumberCountriesResponse response = client.numbers().listCountries();

        assertNotNull(response);
        assertEquals(2, response.getCountries().size());
        NumberCountry gb = response.getCountries().get(0);
        assertEquals("GB", gb.getCode());
        assertEquals("United Kingdom", gb.getName());
        assertEquals(2, gb.getNumberTypes().size());
        assertEquals("mobile", gb.getNumberTypes().get(0));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/numbers/countries"));
    }

    @Test
    void testListCountries_401Unauthorized_throwsAuthenticationException() {
        mockServer.enqueue(TestHelpers.mockAuthError());

        assertThrows(AuthenticationException.class, () -> {
            client.numbers().listCountries();
        });
    }

    // ==================== listAvailable() Tests ====================

    @Test
    void testListAvailable_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"numbers\":[{\"phoneNumber\":\"+447700900123\",\"country\":\"GB\"," +
            "\"numberType\":\"mobile\",\"monthlyCost\":\"1.50\",\"currency\":\"USD\"}]}"
        ));

        AvailableNumbersResponse response = client.numbers().listAvailable("GB", "mobile");

        assertNotNull(response);
        assertEquals(1, response.getNumbers().size());
        AvailableNumber n = response.getNumbers().get(0);
        assertEquals("+447700900123", n.getPhoneNumber());
        assertEquals("GB", n.getCountry());
        assertEquals("mobile", n.getNumberType());
        assertEquals("1.50", n.getMonthlyCost());
        assertEquals("USD", n.getCurrency());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        String path = request.getPath();
        assertTrue(path.contains("/numbers/available"));
        assertTrue(path.contains("country=GB"));
        assertTrue(path.contains("type=mobile"));
    }

    @Test
    void testListAvailable_withContains_passesParam() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"numbers\":[]}"));

        client.numbers().listAvailable("GB", "mobile", "7700");

        RecordedRequest request = mockServer.takeRequest();
        assertTrue(request.getPath().contains("contains=7700"));
    }

    @Test
    void testListAvailable_missingCountry_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.numbers().listAvailable(null, "mobile");
        });
        assertThrows(ValidationException.class, () -> {
            client.numbers().listAvailable("", "mobile");
        });
    }

    @Test
    void testListAvailable_missingType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            client.numbers().listAvailable("GB", null);
        });
        assertThrows(ValidationException.class, () -> {
            client.numbers().listAvailable("GB", "");
        });
    }

    // ==================== list() Tests ====================

    @Test
    void testList_happyPath() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"numbers\":[{\"id\":\"num_1\",\"phoneNumber\":\"+447700900123\",\"status\":\"active\"," +
            "\"source\":\"purchased\",\"countryCode\":\"GB\",\"phoneNumberType\":\"mobile\",\"monthlyCostCents\":150}]}"
        ));

        OwnedNumbersResponse response = client.numbers().list();

        assertNotNull(response);
        assertEquals(1, response.getNumbers().size());
        OwnedNumber n = response.getNumbers().get(0);
        assertEquals("num_1", n.getId());
        assertEquals("+447700900123", n.getPhoneNumber());
        assertEquals("active", n.getStatus());
        assertEquals("purchased", n.getSource());
        assertEquals("GB", n.getCountryCode());
        assertEquals("mobile", n.getPhoneNumberType());
        assertEquals(150, n.getMonthlyCostCents());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/numbers"));
    }

    @Test
    void testList_emptyResults() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"numbers\":[]}"));

        OwnedNumbersResponse response = client.numbers().list();

        assertNotNull(response);
        assertTrue(response.getNumbers().isEmpty());
    }

    // ==================== buy() Tests ====================

    @Test
    void testBuy_provisioning() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"status\":\"provisioning\",\"number\":{\"id\":\"num_1\",\"phoneNumber\":\"+447700900123\"," +
            "\"status\":\"provisioning\",\"source\":\"purchased\",\"countryCode\":\"GB\"," +
            "\"phoneNumberType\":\"mobile\",\"monthlyCostCents\":150}}"
        ));

        BuyNumberResponse response = client.numbers().buy(BuyNumberRequest.builder()
                .phoneNumber("+447700900123")
                .countryCode("GB")
                .phoneNumberType("mobile")
                .monthlyCost("1.50")
                .build());

        assertNotNull(response);
        assertEquals("provisioning", response.getStatus());
        assertNotNull(response.getNumber());
        assertEquals("num_1", response.getNumber().getId());
        assertNull(response.getAction());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getPath().contains("/numbers/buy"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"phoneNumber\":\"+447700900123\""));
        assertTrue(body.contains("\"countryCode\":\"GB\""));
        assertTrue(body.contains("\"phoneNumberType\":\"mobile\""));
        assertTrue(body.contains("\"monthlyCost\":\"1.50\""));
        assertFalse(body.contains("actionCode"));
    }

    @Test
    void testBuy_documentsRequired_returnsAction() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess(
            "{\"status\":\"documents_required\",\"requirements\":[{\"docType\":\"address_proof\"}]," +
            "\"action\":{\"url\":\"https://sendly.live/action/abc\",\"code\":\"ABC123\"," +
            "\"actionCode\":\"0123456789abcdef0123456789abcdef\"," +
            "\"expiresAt\":1767225600000}}"
        ));

        BuyNumberResponse response = client.numbers().buy(BuyNumberRequest.builder()
                .phoneNumber("+447700900123")
                .countryCode("GB")
                .phoneNumberType("mobile")
                .monthlyCost("1.50")
                .build());

        assertEquals("documents_required", response.getStatus());
        assertNotNull(response.getRequirements());
        assertEquals("address_proof",
                response.getRequirements().get(0).getAsJsonObject().get("docType").getAsString());
        assertNotNull(response.getAction());
        assertEquals("https://sendly.live/action/abc", response.getAction().getUrl());
        assertEquals("ABC123", response.getAction().getCode());
        assertEquals("0123456789abcdef0123456789abcdef", response.getAction().getActionCode());
        assertEquals(1767225600000L, response.getAction().getExpiresAt());
    }

    @Test
    void testBuy_withActionCode_sendsActionCode() throws Exception {
        mockServer.enqueue(TestHelpers.mockSuccess("{\"status\":\"provisioning\"}"));

        client.numbers().buy(BuyNumberRequest.builder()
                .phoneNumber("+447700900123")
                .countryCode("GB")
                .phoneNumberType("mobile")
                .monthlyCost("1.50")
                .actionCode("ABC123")
                .build());

        RecordedRequest request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"actionCode\":\"ABC123\""));
    }

    @Test
    void testBuy_missingFields_throwsValidationException() {
        assertThrows(ValidationException.class, () -> client.numbers().buy(null));

        assertThrows(ValidationException.class, () -> client.numbers().buy(BuyNumberRequest.builder()
                .countryCode("GB").phoneNumberType("mobile").monthlyCost("1.50").build()));

        assertThrows(ValidationException.class, () -> client.numbers().buy(BuyNumberRequest.builder()
                .phoneNumber("+447700900123").phoneNumberType("mobile").monthlyCost("1.50").build()));

        assertThrows(ValidationException.class, () -> client.numbers().buy(BuyNumberRequest.builder()
                .phoneNumber("+447700900123").countryCode("GB").monthlyCost("1.50").build()));

        assertThrows(ValidationException.class, () -> client.numbers().buy(BuyNumberRequest.builder()
                .phoneNumber("+447700900123").countryCode("GB").phoneNumberType("mobile").build()));
    }

    @Test
    void testBuy_402InsufficientCredits_throwsInsufficientCreditsException() {
        mockServer.enqueue(TestHelpers.mockInsufficientCredits());

        assertThrows(InsufficientCreditsException.class, () -> {
            client.numbers().buy(BuyNumberRequest.builder()
                    .phoneNumber("+447700900123")
                    .countryCode("GB")
                    .phoneNumberType("mobile")
                    .monthlyCost("1.50")
                    .build());
        });
    }

    @Test
    void testBuy_500ServerError_throwsSendlyException() {
        mockServer.enqueue(TestHelpers.mockServerError());

        assertThrows(SendlyException.class, () -> {
            client.numbers().buy(BuyNumberRequest.builder()
                    .phoneNumber("+447700900123")
                    .countryCode("GB")
                    .phoneNumberType("mobile")
                    .monthlyCost("1.50")
                    .build());
        });
    }
}
