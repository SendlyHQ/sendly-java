package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.CreateTenDlcBrandRequest;
import com.sendly.models.CreateTenDlcCampaignRequest;
import com.sendly.models.TenDlcAssignmentListResponse;
import com.sendly.models.TenDlcAssignmentResponse;
import com.sendly.models.TenDlcBrandListResponse;
import com.sendly.models.TenDlcBrandResponse;
import com.sendly.models.TenDlcCampaignListResponse;
import com.sendly.models.TenDlcCampaignResponse;
import com.sendly.models.TenDlcQualifyResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 10DLC resource — local-number texting registration.
 * <p>
 * Register your business for carrier review so you can text from local
 * (10-digit) US numbers. The flow has three steps:
 * <ol>
 *   <li><b>Brand</b> — register your business identity. Starts {@code pending};
 *       poll {@link #getBrand(String)} until it becomes {@code verified} (or
 *       {@code failed}, with {@code failureReasons} explaining why).</li>
 *   <li><b>Campaign</b> — describe your messaging use case under a verified
 *       brand and submit it for carrier review. Starts {@code pending}; poll
 *       {@link #getCampaign(String)} until it becomes {@code active}.
 *       {@link #qualify(String, String)} pre-checks a use case before you
 *       create the campaign.</li>
 *   <li><b>Assign</b> — attach a number you own to the active campaign with
 *       {@link #assignNumber(String, String)}. Once the assignment is
 *       {@code Active}, the number can send.</li>
 * </ol>
 * Brand, campaign, and number-assignment writes require a live API key
 * ({@code sk_live_v1_xxx}).
 *
 * <pre>{@code
 * // 1. Register a brand and poll until it's verified
 * TenDlcBrand brand = client.tenDlc().createBrand(CreateTenDlcBrandRequest.builder()
 *     .legalName("Acme Holdings LLC")
 *     .ein("12-3456789")
 *     .website("https://acme.example")
 *     .email("ops@acme.example")
 *     .build()).getData();
 * // ...poll client.tenDlc().getBrand(brand.getId()) until status is "verified"
 *
 * // 2. Pre-check the use case, then create a campaign
 * TenDlcQualifyResult check = client.tenDlc().qualify(brand.getId(), "MIXED").getData();
 * if (check.isQualified()) {
 *     TenDlcCampaign campaign = client.tenDlc().createCampaign(CreateTenDlcCampaignRequest.builder()
 *         .brandId(brand.getId())
 *         .useCase("MIXED")
 *         .description("Order updates and support replies for Acme customers")
 *         .messageFlow("Customers opt in at checkout on acme.example")
 *         .sampleMessages(List.of("Your order #123 has shipped!"))
 *         .build()).getData();
 *     // ...poll client.tenDlc().getCampaign(campaign.getId()) until status is "active"
 *
 *     // 3. Assign a number you own
 *     client.tenDlc().assignNumber(campaign.getId(), "+15551234567");
 * }
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/10dlc">10DLC docs</a>
 */
public class TenDlcResource {
    private final Sendly client;

    public TenDlcResource(Sendly client) {
        this.client = client;
    }

    /**
     * List the brands registered for carrier review.
     *
     * @return Your brands and their review status
     * @throws SendlyException if the request fails
     */
    public TenDlcBrandListResponse listBrands() throws SendlyException {
        JsonObject response = client.get("/tendlc/brands", null);
        return new TenDlcBrandListResponse(response);
    }

    /**
     * Register a brand for carrier review — step 1 of enabling local-number
     * texting. Requires a live API key.
     * <p>
     * The brand starts {@code pending}. Poll {@link #getBrand(String)} until it
     * becomes {@code verified} before creating a campaign.
     *
     * @param request Business identity details (legalName is required)
     * @return The created brand
     * @throws SendlyException if the request fails
     */
    public TenDlcBrandResponse createBrand(CreateTenDlcBrandRequest request) throws SendlyException {
        if (request == null) {
            throw new ValidationException("Request is required");
        }
        if (request.getLegalName() == null || request.getLegalName().isEmpty()) {
            throw new ValidationException("Legal name is required");
        }

        JsonObject response = client.post("/tendlc/brands", request.toJson());
        return new TenDlcBrandResponse(response);
    }

    /**
     * Fetch one brand. Also refreshes its carrier-review status, so polling
     * this method shows progress ({@code pending} to {@code verified}/{@code failed}).
     *
     * @param id Brand identifier
     * @return The brand with its current review status
     * @throws SendlyException if the request fails
     */
    public TenDlcBrandResponse getBrand(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Brand ID is required");
        }

        JsonObject response = client.get("/tendlc/brands/" + encode(id), null);
        return new TenDlcBrandResponse(response);
    }

    /**
     * Pre-check whether a use case qualifies for a brand on the carrier
     * network before creating a campaign.
     *
     * @param brandId Brand identifier
     * @param useCase Use-case code (e.g. "MIXED", "MARKETING", "ACCOUNT_NOTIFICATION", "2FA")
     * @return Whether the use case qualifies, and the expected throughput
     * @throws SendlyException if the request fails
     */
    public TenDlcQualifyResponse qualify(String brandId, String useCase) throws SendlyException {
        if (brandId == null || brandId.isEmpty()) {
            throw new ValidationException("Brand ID is required");
        }
        if (useCase == null || useCase.isEmpty()) {
            throw new ValidationException("Use case is required");
        }

        JsonObject response = client.get("/tendlc/brands/" + encode(brandId) + "/qualify/" + encode(useCase), null);
        return new TenDlcQualifyResponse(response);
    }

    /**
     * List your messaging campaigns.
     *
     * @return Your campaigns and their review status
     * @throws SendlyException if the request fails
     */
    public TenDlcCampaignListResponse listCampaigns() throws SendlyException {
        JsonObject response = client.get("/tendlc/campaigns", null);
        return new TenDlcCampaignListResponse(response);
    }

    /**
     * Create a messaging campaign under a verified brand and submit it for
     * carrier review. Requires a live API key.
     * <p>
     * The campaign starts {@code pending}. Poll {@link #getCampaign(String)}
     * until it becomes {@code active} before assigning numbers.
     *
     * @param request Campaign details (brandId, useCase, description,
     *                messageFlow, and sampleMessages are required)
     * @return The created campaign
     * @throws SendlyException if the request fails
     */
    public TenDlcCampaignResponse createCampaign(CreateTenDlcCampaignRequest request) throws SendlyException {
        if (request == null) {
            throw new ValidationException("Request is required");
        }
        if (request.getBrandId() == null || request.getBrandId().isEmpty()) {
            throw new ValidationException("Brand ID is required");
        }
        if (request.getUseCase() == null || request.getUseCase().isEmpty()) {
            throw new ValidationException("Use case is required");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new ValidationException("Description is required");
        }
        if (request.getMessageFlow() == null || request.getMessageFlow().isEmpty()) {
            throw new ValidationException("Message flow is required");
        }
        if (request.getSampleMessages() == null || request.getSampleMessages().isEmpty()) {
            throw new ValidationException("Sample messages are required");
        }

        JsonObject response = client.post("/tendlc/campaigns", request.toJson());
        return new TenDlcCampaignResponse(response);
    }

    /**
     * Fetch one campaign. Also refreshes its carrier-review status, so polling
     * this method shows progress ({@code pending} to {@code active}) including
     * throughput once carriers approve.
     *
     * @param id Campaign identifier
     * @return The campaign with its current review status
     * @throws SendlyException if the request fails
     */
    public TenDlcCampaignResponse getCampaign(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }

        JsonObject response = client.get("/tendlc/campaigns/" + encode(id), null);
        return new TenDlcCampaignResponse(response);
    }

    /**
     * Assign a phone number you own to an active (carrier-approved) campaign,
     * making the number sendable. Requires a live API key.
     * <p>
     * Idempotent — re-assigning the same number to the same campaign returns
     * the existing assignment.
     *
     * @param campaignId  Campaign identifier
     * @param phoneNumber E.164 number the workspace already owns
     * @return The assignment; the number can send once its status is "Active"
     * @throws SendlyException if the request fails
     */
    public TenDlcAssignmentResponse assignNumber(String campaignId, String phoneNumber) throws SendlyException {
        if (campaignId == null || campaignId.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new ValidationException("Phone number is required");
        }

        JsonObject body = new JsonObject();
        body.addProperty("phoneNumber", phoneNumber);
        JsonObject response = client.post("/tendlc/campaigns/" + encode(campaignId) + "/assign", body);
        return new TenDlcAssignmentResponse(response);
    }

    /**
     * List your number-to-campaign assignments.
     *
     * @return Your assignments and their status
     * @throws SendlyException if the request fails
     */
    public TenDlcAssignmentListResponse listAssignments() throws SendlyException {
        JsonObject response = client.get("/tendlc/assignments", null);
        return new TenDlcAssignmentListResponse(response);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
