package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.CreateRcsAgentRequest;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.RcsAgentResponse;
import com.sendly.models.RcsAgentsResponse;
import com.sendly.models.RcsBrandInput;
import com.sendly.models.RcsBrandResponse;
import com.sendly.models.RcsCapability;
import com.sendly.models.RcsDossier;
import com.sendly.models.RcsLaunchRequest;
import com.sendly.models.RcsRegistration;
import com.sendly.models.RcsTestDeviceInput;
import com.sendly.models.RcsTestDevicesResponse;
import com.sendly.models.UpdateRcsAgentRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * RCS resource — register your brand and agent, discover your agents, and
 * pre-flight recipient capability.
 * <p>
 * RCS is a first-class Sendly channel: branded rich messaging (cards,
 * suggestion chips) delivered over RCS when the recipient's device supports
 * it, with automatic SMS fallback (billed as SMS) for plain-text sends when it
 * doesn't. Send via {@code messages().send(SendRcsMessageRequest)}.
 * <p>
 * Sending as a brand requires an RCS agent on your workspace. Registration is
 * self-serve, from the dashboard or this API: draft a brand and an agent,
 * submit them for review by Sendly, and Sendly passes them to the carrier
 * network. Once the agent is in {@code testing} it reaches invited test
 * devices; request launch when testing is done, and after launch review it
 * reaches everyone. Registration calls need an API key with the
 * {@code rcs:read} / {@code rcs:write} scopes; sends and capability checks
 * require a live API key.
 * <p>
 * Assets can't be uploaded over the API: logo, hero and call-to-action media
 * must already be public https URLs. Upload files from the dashboard instead.
 *
 * <pre>{@code
 * // 1. Register: prefill from what's already on file, then draft a brand
 * RcsDossier dossier = client.rcs().dossier().get();
 * RcsBrand brand = client.rcs().brands().create(dossier.getBrand().toBuilder()
 *     .displayName("Acme")
 *     .build()).getBrand();
 *
 * // 2. Draft an agent under it and submit for review
 * RcsAgentDetails agent = client.rcs().agents().create(CreateRcsAgentRequest.builder()
 *     .brandId(brand.getId())
 *     .displayName("Acme")
 *     .useCase(RcsAgentUseCase.TRANSACTIONAL)
 *     .basics(RcsAgentBasics.builder()
 *         .description("Order updates from Acme")
 *         .logoUrl("https://acme.example/logo.png")
 *         .heroUrl("https://acme.example/hero.png")
 *         .brandColor("#0055FF")
 *         .privacyPolicyUrl("https://acme.example/privacy")
 *         .termsAndConditionsUrl("https://acme.example/terms")
 *         .build())
 *     .build()).getAgent();
 * client.rcs().agents().setTestDevices(agent.getId(),
 *     List.of(new RcsTestDeviceInput("+13125550100", "Sam's phone")));
 * client.rcs().agents().submit(agent.getId());
 *
 * // 3. Poll the stage; once "testing", finish testing and request launch
 * RcsRegistration registration = client.rcs().registration().get();
 * if (RcsCustomerStage.TESTING.equals(registration.getStage())) {
 *     client.rcs().agents().requestLaunch(agent.getId());
 * }
 *
 * // 4. Send — text falls back to SMS for non-RCS recipients
 * RcsCapability capability = client.rcs().capability("+15551234567");
 * RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
 *     .to("+15551234567")
 *     .text("Your order has shipped!")
 *     .build());
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/rcs">RCS docs</a>
 */
public class RcsResource {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    private static final int MAX_TEST_DEVICES = 20;

    private final Sendly client;
    private final Agents agents;
    private final Registration registration;
    private final Dossier dossier;
    private final Brands brands;

    public RcsResource(Sendly client) {
        this.client = client;
        this.agents = new Agents(client);
        this.registration = new Registration(client);
        this.dossier = new Dossier(client);
        this.brands = new Brands(client);
    }

    /**
     * List the RCS agents on your workspace.
     */
    public Agents agents() {
        return agents;
    }

    /**
     * Where your RCS registration stands: the newest agent, its brand and
     * test devices, and the current stage.
     */
    public Registration registration() {
        return registration;
    }

    /**
     * Brand fields prefilled from what your workspace has already registered.
     */
    public Dossier dossier() {
        return dossier;
    }

    /**
     * RCS brands — the business identity behind your agents.
     */
    public Brands brands() {
        return brands;
    }

    /**
     * Check whether a recipient can receive RCS from your workspace's agent.
     * <p>
     * When the workspace has more than one agent, use
     * {@link #capability(String, String)} to pick one.
     * </p>
     *
     * @param to The recipient's number, in E.164 format
     * @return Whether the recipient is RCS-capable and which features their
     *         device reports
     * @throws SendlyException if the request fails (requires a live API key)
     */
    public RcsCapability capability(String to) throws SendlyException {
        return capability(to, null);
    }

    /**
     * Check whether a recipient can receive RCS from a specific agent.
     * <p>
     * A not-capable recipient still receives plain-text sends via the SMS
     * fallback; card sends to them fail with 422
     * {@code rcs_not_supported_for_recipient}.
     * </p>
     *
     * @param to      The recipient's number, in E.164 format
     * @param agentId The agent to check against; null to use the workspace's
     *                only agent
     * @return Whether the recipient is RCS-capable and which features their
     *         device reports
     * @throws SendlyException if the request fails (requires a live API key)
     */
    public RcsCapability capability(String to, String agentId) throws SendlyException {
        validatePhone(to);

        Map<String, String> params = new HashMap<>();
        params.put("to", to);
        if (agentId != null && !agentId.isEmpty()) {
            params.put("agentId", agentId);
        }

        JsonObject response = client.get("/rcs/capability", params);
        return new RcsCapability(response);
    }

    /**
     * Registration status sub-resource.
     */
    public static class Registration {
        private final Sendly client;

        Registration(Sendly client) {
            this.client = client;
        }

        /**
         * Fetch where your RCS registration stands.
         * <p>
         * Returns the newest agent, its brand and test devices, and the
         * stage they are at (see {@code RcsCustomerStage}). Poll this after
         * {@code agents().submit(...)} to watch the review progress; the
         * stage is {@code draft} when nothing has been created yet.
         * </p>
         *
         * @return The registration snapshot
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_enabled} when RCS registration isn't enabled
         *         for this account yet
         */
        public RcsRegistration get() throws SendlyException {
            JsonObject response = client.get("/rcs/registration", null);
            return new RcsRegistration(response);
        }
    }

    /**
     * Prefill sub-resource.
     */
    public static class Dossier {
        private final Sendly client;

        Dossier(Sendly client) {
            this.client = client;
        }

        /**
         * Fetch brand fields prefilled from what your workspace has already
         * registered (your newest 10DLC brand, else your active toll-free
         * verification), so you can complete them and pass the result to
         * {@code brands().create(...)}.
         *
         * @return The prefilled brand fields and where they came from
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_enabled} when RCS registration isn't enabled
         *         for this account yet
         */
        public RcsDossier get() throws SendlyException {
            JsonObject response = client.get("/rcs/dossier", null);
            return new RcsDossier(response);
        }
    }

    /**
     * RCS brands sub-resource — the business identity behind your agents.
     */
    public static class Brands {
        private final Sendly client;

        Brands(Sendly client) {
            this.client = client;
        }

        /**
         * Draft a brand. Every field is optional here; required-field checks
         * run when the agent is submitted for review.
         * <p>
         * An idempotency key is generated automatically and reused across
         * the SDK's own retries; see {@link #create(RcsBrandInput, IdempotentRequestOptions)}
         * to supply your own.
         * </p>
         *
         * @param input Business identity fields; the address must name
         *              {@code countryCode} "US"
         * @return The created brand
         * @throws SendlyException if the request fails; 422
         *         {@code rcs_us_only} for a non-US address
         */
        public RcsBrandResponse create(RcsBrandInput input) throws SendlyException {
            return create(input, null);
        }

        /**
         * Draft a brand with per-call options.
         *
         * @param input   Business identity fields
         * @param options Per-call options (optional idempotency key)
         * @return The created brand
         * @throws SendlyException if the request fails
         */
        public RcsBrandResponse create(RcsBrandInput input, IdempotentRequestOptions options) throws SendlyException {
            if (input == null) {
                throw new ValidationException("Brand input is required");
            }

            JsonObject response = client.post("/rcs/brands", input.toJson(), idempotencyKeyOf(options));
            return new RcsBrandResponse(response);
        }

        /**
         * Edit a draft brand. Only the fields you set are changed.
         *
         * @param id    Brand identifier
         * @param input Fields to change
         * @return The brand as stored
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_found} when the brand isn't in this
         *         workspace, 409 {@code rcs_field_locked} while it is under
         *         review
         */
        public RcsBrandResponse update(String id, RcsBrandInput input) throws SendlyException {
            return update(id, input, null);
        }

        /**
         * Edit a draft brand with per-call options.
         *
         * @param id      Brand identifier
         * @param input   Fields to change
         * @param options Per-call options (optional idempotency key)
         * @return The brand as stored
         * @throws SendlyException if the request fails
         */
        public RcsBrandResponse update(String id, RcsBrandInput input, IdempotentRequestOptions options) throws SendlyException {
            requireId(id, "Brand ID is required");
            if (input == null) {
                throw new ValidationException("Brand input is required");
            }

            JsonObject response = client.patch("/rcs/brands/" + encode(id), input.toJson(), idempotencyKeyOf(options));
            return new RcsBrandResponse(response);
        }
    }

    /**
     * RCS agents sub-resource — the brand identities you send as.
     */
    public static class Agents {
        private final Sendly client;

        Agents(Sendly client) {
            this.client = client;
        }

        /**
         * List your RCS agents.
         * <p>
         * Returns the agents on your workspace, newest first. An empty list
         * means no agent exists yet — draft one with
         * {@link #create(CreateRcsAgentRequest)} (or from the dashboard).
         *
         * @return Your agents with status, stage and sendability
         * @throws SendlyException if the request fails
         */
        public RcsAgentsResponse list() throws SendlyException {
            JsonObject response = client.get("/rcs/agents", null);
            return new RcsAgentsResponse(response);
        }

        /**
         * Draft an agent under a brand. Only {@code brandId} is required to
         * start; fill in the basics, campaign and testing details here or
         * with {@link #update(String, UpdateRcsAgentRequest)}.
         * <p>
         * Logo, hero and call-to-action media must be public https URLs;
         * files can't be uploaded over the API. An idempotency key is
         * generated automatically and reused across the SDK's own retries.
         * </p>
         *
         * @param request Agent details ({@code brandId} is required)
         * @return The created agent
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_found} when the brand isn't in this
         *         workspace, 422 {@code rcs_invalid_content} for a non-https
         *         media URL
         */
        public RcsAgentResponse create(CreateRcsAgentRequest request) throws SendlyException {
            return create(request, null);
        }

        /**
         * Draft an agent with per-call options.
         *
         * @param request Agent details ({@code brandId} is required)
         * @param options Per-call options (optional idempotency key)
         * @return The created agent
         * @throws SendlyException if the request fails
         */
        public RcsAgentResponse create(CreateRcsAgentRequest request, IdempotentRequestOptions options) throws SendlyException {
            if (request == null) {
                throw new ValidationException("Request is required");
            }
            if (request.getBrandId() == null || request.getBrandId().isEmpty()) {
                throw new ValidationException("Brand ID is required");
            }

            JsonObject response = client.post("/rcs/agents", request.toJson(), idempotencyKeyOf(options));
            return new RcsAgentResponse(response);
        }

        /**
         * Fetch one agent in full, with its test devices and stage.
         *
         * @param id Agent identifier
         * @return The agent, its devices and stage
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_found} when the agent isn't in this workspace
         */
        public RcsAgentResponse get(String id) throws SendlyException {
            requireId(id, "Agent ID is required");

            JsonObject response = client.get("/rcs/agents/" + encode(id), null);
            return new RcsAgentResponse(response);
        }

        /**
         * Edit a draft agent. Only the groups you set are changed; see
         * {@link UpdateRcsAgentRequest} for how they merge and when they
         * lock.
         *
         * @param id      Agent identifier
         * @param request Fields to change
         * @return The agent as stored
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_found}, 409 {@code rcs_field_locked} while
         *         under review, 422 {@code rcs_invalid_content}
         */
        public RcsAgentResponse update(String id, UpdateRcsAgentRequest request) throws SendlyException {
            return update(id, request, null);
        }

        /**
         * Edit a draft agent with per-call options.
         *
         * @param id      Agent identifier
         * @param request Fields to change
         * @param options Per-call options (optional idempotency key)
         * @return The agent as stored
         * @throws SendlyException if the request fails
         */
        public RcsAgentResponse update(String id, UpdateRcsAgentRequest request, IdempotentRequestOptions options) throws SendlyException {
            requireId(id, "Agent ID is required");
            if (request == null) {
                throw new ValidationException("Request is required");
            }

            JsonObject response = client.patch("/rcs/agents/" + encode(id), request.toJson(), idempotencyKeyOf(options));
            return new RcsAgentResponse(response);
        }

        /**
         * Replace the agent's test devices — the phones that receive its
         * messages while it is in testing.
         * <p>
         * The list is authoritative: numbers missing from it are removed and
         * new ones are invited. Up to 20 devices.
         * </p>
         *
         * @param id      Agent identifier
         * @param devices The full device list (E.164 or 10-digit US numbers)
         * @return The device list after the change
         * @throws SendlyException if the request fails; 404
         *         {@code rcs_not_found}, 409 {@code rcs_field_locked} while
         *         under review, 422 {@code rcs_invalid_content} for a bad
         *         number
         */
        public RcsTestDevicesResponse setTestDevices(String id, List<RcsTestDeviceInput> devices) throws SendlyException {
            return setTestDevices(id, devices, null);
        }

        /**
         * Replace the agent's test devices with per-call options.
         *
         * @param id      Agent identifier
         * @param devices The full device list
         * @param options Per-call options (optional idempotency key)
         * @return The device list after the change
         * @throws SendlyException if the request fails
         */
        public RcsTestDevicesResponse setTestDevices(String id, List<RcsTestDeviceInput> devices, IdempotentRequestOptions options) throws SendlyException {
            requireId(id, "Agent ID is required");
            if (devices == null) {
                throw new ValidationException("Devices list is required");
            }
            if (devices.size() > MAX_TEST_DEVICES) {
                throw new ValidationException("You can invite up to " + MAX_TEST_DEVICES + " test devices");
            }
            JsonArray list = new JsonArray();
            for (RcsTestDeviceInput device : devices) {
                if (device == null || device.getPhoneNumber() == null || device.getPhoneNumber().isEmpty()) {
                    throw new ValidationException("Each device needs a phone number");
                }
                list.add(device.toJson());
            }
            JsonObject body = new JsonObject();
            body.add("devices", list);

            JsonObject response = client.put("/rcs/agents/" + encode(id) + "/test-devices", body, idempotencyKeyOf(options));
            return new RcsTestDevicesResponse(response);
        }

        /**
         * Submit the agent and its brand for review by Sendly. Both need to
         * be complete; the response's stage becomes {@code in_review}. Poll
         * {@code registration().get()} for progress.
         * <p>
         * An idempotency key is generated automatically and reused across
         * the SDK's own retries, so a retried submit doesn't re-notify.
         * </p>
         *
         * @param id Agent identifier
         * @return The agent and its new stage
         * @throws SendlyException if the request fails; 422
         *         {@code rcs_invalid_content} listing the incomplete fields in
         *         {@code getFieldErrors()}, 409 {@code rcs_field_locked} when
         *         already submitted, 409 {@code rcs_brand_not_verified}
         */
        public RcsAgentResponse submit(String id) throws SendlyException {
            return submit(id, null);
        }

        /**
         * Submit the agent for review with per-call options.
         *
         * @param id      Agent identifier
         * @param options Per-call options (optional idempotency key)
         * @return The agent and its new stage
         * @throws SendlyException if the request fails
         */
        public RcsAgentResponse submit(String id, IdempotentRequestOptions options) throws SendlyException {
            requireId(id, "Agent ID is required");

            JsonObject response = client.post("/rcs/agents/" + encode(id) + "/submit", new JsonObject(), idempotencyKeyOf(options));
            return new RcsAgentResponse(response);
        }

        /**
         * Ask to launch an agent that has finished testing. Sendly reviews
         * the campaign details, then passes the launch to the carrier
         * network; the response's stage becomes {@code launch_review}.
         *
         * @param id Agent identifier
         * @return The agent and its new stage
         * @throws SendlyException if the request fails; 409
         *         {@code rcs_launch_not_ready} unless the agent is in testing,
         *         422 {@code rcs_invalid_content} listing what the campaign
         *         still needs in {@code getFieldErrors()}
         */
        public RcsAgentResponse requestLaunch(String id) throws SendlyException {
            return requestLaunch(id, null, null);
        }

        /**
         * Ask to launch an agent, storing testing details first.
         *
         * @param id      Agent identifier
         * @param request Testing details to store before requesting launch
         *                (may be null)
         * @return The agent and its new stage
         * @throws SendlyException if the request fails
         */
        public RcsAgentResponse requestLaunch(String id, RcsLaunchRequest request) throws SendlyException {
            return requestLaunch(id, request, null);
        }

        /**
         * Ask to launch an agent with per-call options.
         *
         * @param id      Agent identifier
         * @param request Testing details to store before requesting launch
         *                (may be null)
         * @param options Per-call options (optional idempotency key)
         * @return The agent and its new stage
         * @throws SendlyException if the request fails
         */
        public RcsAgentResponse requestLaunch(String id, RcsLaunchRequest request, IdempotentRequestOptions options) throws SendlyException {
            requireId(id, "Agent ID is required");

            JsonObject body = request != null ? request.toJson() : new JsonObject();
            JsonObject response = client.post("/rcs/agents/" + encode(id) + "/request-launch", body, idempotencyKeyOf(options));
            return new RcsAgentResponse(response);
        }
    }

    private static void validatePhone(String phone) throws ValidationException {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(
                "Invalid phone number format. Use E.164 format (e.g., +15551234567)"
            );
        }
    }

    private static void requireId(String id, String message) throws ValidationException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException(message);
        }
    }

    private static String idempotencyKeyOf(IdempotentRequestOptions options) {
        return options != null ? options.getIdempotencyKey() : null;
    }

    private static String encode(String s) {
        return PathParams.encode(s);
    }
}
