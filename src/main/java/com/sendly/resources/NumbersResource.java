package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.AvailableNumbersResponse;
import com.sendly.models.BuyNumberRequest;
import com.sendly.models.BuyNumberResponse;
import com.sendly.models.NumberCountriesResponse;
import com.sendly.models.OwnedNumbersResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Numbers resource — phone number discovery &amp; provisioning.
 * <p>
 * Browse the countries and number types Sendly can provision, search available
 * numbers (already priced for your account), list the numbers you own, and buy
 * a new one.
 * <p>
 * Buying a number is asynchronous. {@link #buy(BuyNumberRequest)} returns a
 * status:
 * <ul>
 *   <li>{@code provisioning} — the number is being set up; poll {@link #list()}
 *       until it appears as active.</li>
 *   <li>{@code documents_required} / {@code payment_required} — the purchase
 *       needs the user to finish something on a hosted Sendly page first. The
 *       response carries an {@code action} object with a {@code url} (the hosted
 *       page) and a short {@code code} the user types to prove they have
 *       terminal access. Hand the user the URL + code, wait for the action to
 *       complete, then call {@code buy()} again with the SAME body plus
 *       {@code actionCode} set to the completed action's code.</li>
 * </ul>
 *
 * <pre>{@code
 * // 1. Browse what's available
 * NumberCountriesResponse countries = client.numbers().listCountries();
 * AvailableNumbersResponse available = client.numbers().listAvailable("GB", "mobile");
 *
 * // 2. Buy one
 * AvailableNumber pick = available.getNumbers().get(0);
 * BuyNumberResponse result = client.numbers().buy(BuyNumberRequest.builder()
 *     .phoneNumber(pick.getPhoneNumber())
 *     .countryCode(pick.getCountry())
 *     .phoneNumberType(pick.getNumberType())
 *     .monthlyCost(pick.getMonthlyCost())
 *     .build());
 *
 * if ("provisioning".equals(result.getStatus())) {
 *     // the number is being set up
 * } else if (result.getAction() != null) {
 *     // documents_required / payment_required: send the user to the hosted page
 *     // result.getAction().getUrl() + result.getAction().getCode()
 *     // ...after they complete it, re-call buy() with actionCode set.
 * }
 *
 * // 3. List the numbers you own
 * OwnedNumbersResponse owned = client.numbers().list();
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/numbers">Numbers docs</a>
 */
public class NumbersResource {
    private final Sendly client;

    public NumbersResource(Sendly client) {
        this.client = client;
    }

    /**
     * List the countries Sendly can provision numbers in, with the number
     * types available in each.
     *
     * @return Supported countries and their number types
     * @throws SendlyException if the request fails
     */
    public NumberCountriesResponse listCountries() throws SendlyException {
        JsonObject response = client.get("/numbers/countries", null);
        return new NumberCountriesResponse(response);
    }

    /**
     * Search numbers available to buy in a country. Prices are already
     * customer-priced for your account.
     *
     * @param country ISO 3166-1 alpha-2 country code to search in (e.g. "GB")
     * @param type    Number type to search for (e.g. "mobile")
     * @return Available numbers with pricing
     * @throws SendlyException if the request fails
     */
    public AvailableNumbersResponse listAvailable(String country, String type) throws SendlyException {
        return listAvailable(country, type, null);
    }

    /**
     * Search numbers available to buy in a country, optionally constrained to
     * numbers containing a substring. Prices are already customer-priced for
     * your account.
     *
     * @param country  ISO 3166-1 alpha-2 country code to search in (e.g. "GB")
     * @param type     Number type to search for (e.g. "mobile")
     * @param contains Optional substring the number must contain (digits only)
     * @return Available numbers with pricing
     * @throws SendlyException if the request fails
     */
    public AvailableNumbersResponse listAvailable(String country, String type, String contains) throws SendlyException {
        if (country == null || country.isEmpty()) {
            throw new ValidationException("Country is required");
        }
        if (type == null || type.isEmpty()) {
            throw new ValidationException("Number type is required");
        }

        Map<String, String> params = new HashMap<>();
        params.put("country", country);
        params.put("type", type);
        if (contains != null && !contains.isEmpty()) {
            params.put("contains", contains);
        }

        JsonObject response = client.get("/numbers/available", params);
        return new AvailableNumbersResponse(response);
    }

    /**
     * List the phone numbers you own.
     *
     * @return Your numbers and their lifecycle status
     * @throws SendlyException if the request fails
     */
    public OwnedNumbersResponse list() throws SendlyException {
        JsonObject response = client.get("/numbers", null);
        return new OwnedNumbersResponse(response);
    }

    /**
     * Buy a phone number. Asynchronous — returns a status.
     * <p>
     * When the status is {@code documents_required} or {@code payment_required},
     * the response carries an {@code action} hand-off (hosted-page URL + short
     * code). Hand it to the user, wait for them to complete it, then call
     * {@code buy()} again with the SAME body plus {@code actionCode} set to the
     * completed action's code.
     *
     * @param request The number to buy (from a listing) and optional actionCode
     * @return The buy outcome
     * @throws SendlyException if the request fails
     */
    public BuyNumberResponse buy(BuyNumberRequest request) throws SendlyException {
        if (request == null) {
            throw new ValidationException("Request is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        if (request.getCountryCode() == null || request.getCountryCode().isEmpty()) {
            throw new ValidationException("Country code is required");
        }
        if (request.getPhoneNumberType() == null || request.getPhoneNumberType().isEmpty()) {
            throw new ValidationException("Phone number type is required");
        }
        if (request.getMonthlyCost() == null || request.getMonthlyCost().isEmpty()) {
            throw new ValidationException("Monthly cost is required");
        }

        JsonObject response = client.post("/numbers/buy", request.toJson());
        return new BuyNumberResponse(response);
    }
}
