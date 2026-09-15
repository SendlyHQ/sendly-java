package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.CreateVoiceAgentRequest;
import com.sendly.models.DeletedVoiceAgent;
import com.sendly.models.EmergencyAddress;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.UpdateVoiceAgentRequest;
import com.sendly.models.UpdateVoiceNumberRequest;
import com.sendly.models.VoiceAgent;
import com.sendly.models.VoiceAgentListResponse;
import com.sendly.models.VoiceListResponse;
import com.sendly.models.VoiceNumber;
import com.sendly.models.VoiceNumberListResponse;

/**
 * Voice resource: configure numbers, AI agents and voices for phone calls.
 * <p>
 * Everything a call depends on, configured from code: switch voice on for a
 * number and choose how it answers, register the number's emergency address,
 * and create the AI agents that talk on calls. Place and follow calls with
 * {@code calls()}.
 * </p>
 * <p>
 * Reads need the {@code calls:read} scope and writes {@code calls:write}.
 * Writes need a live API key and accept an {@link IdempotentRequestOptions}
 * overload; POST requests get an idempotency key automatically. In a team
 * workspace, changing a number or its emergency address also needs a role
 * that can change settings, and managing agents a role that can manage API
 * keys (each agent holds its own scoped sending key); otherwise the API
 * answers 403 {@code forbidden}. Until voice is enabled for your workspace
 * every method answers 404 {@code voice_not_enabled}, a
 * {@code NotFoundException}.
 * </p>
 *
 * <pre>{@code
 * // Create an agent with one of the available voices
 * VoiceListResponse voices = client.voice().voices().list();
 * VoiceAgent agent = client.voice().agents().create(CreateVoiceAgentRequest.builder()
 *     .name("Front desk")
 *     .voice(voices.getData().get(0).getId())
 *     .greeting("Thanks for calling Acme, how can I help?")
 *     .build());
 *
 * // Register the emergency address, then have the agent answer the number
 * client.voice().numbers().registerEmergencyAddress("+15555550188", EmergencyAddress.builder()
 *     .street("500 Example Ave")
 *     .city("Austin")
 *     .state("TX")
 *     .zip("78701")
 *     .build());
 * client.voice().numbers().update("+15555550188", UpdateVoiceNumberRequest.builder()
 *     .voiceEnabled(true)
 *     .voiceMode(VoiceMode.AGENT)
 *     .agentId(agent.getId())
 *     .build());
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/voice">Voice docs</a>
 */
public class VoiceResource {
    private final Numbers numbers;
    private final Agents agents;
    private final Voices voices;

    public VoiceResource(Sendly client) {
        this.numbers = new Numbers(client);
        this.agents = new Agents(client);
        this.voices = new Voices(client);
    }

    /**
     * Voice settings and emergency addresses for the workspace's numbers.
     */
    public Numbers numbers() {
        return numbers;
    }

    /**
     * The AI agents that answer and place calls.
     */
    public Agents agents() {
        return agents;
    }

    /**
     * The voices an agent can speak with.
     */
    public Voices voices() {
        return voices;
    }

    /**
     * Numbers sub-resource: how each number answers phone calls, and its
     * emergency address. Every method takes the number's id or its E.164
     * phone number.
     */
    public static class Numbers {
        private final Sendly client;

        Numbers(Sendly client) {
            this.client = client;
        }

        /**
         * List the workspace's active numbers with their voice settings, in
         * the same order as the dashboard.
         *
         * @return The numbers
         * @throws SendlyException if the request fails
         */
        public VoiceNumberListResponse list() throws SendlyException {
            JsonObject response = client.get("/voice/numbers", null);
            return new VoiceNumberListResponse(response);
        }

        /**
         * Retrieve a number's voice settings.
         *
         * @param number The number's id or its E.164 phone number
         * @return The number
         * @throws SendlyException if the request fails; 404
         *         {@code number_not_found} when the number isn't active in
         *         this workspace
         */
        public VoiceNumber get(String number) throws SendlyException {
            JsonObject response = client.get(numberPath(number), null);
            return new VoiceNumber(response);
        }

        /**
         * Change how a number answers phone calls.
         * <p>
         * This changes what happens when real people call the number. Turning
         * voice on connects the number for calls before the change is saved.
         * A mode alone is enough: {@code voiceMode("ring_dashboard")} or
         * {@code voiceMode("agent")} switches voice on and
         * {@code voiceMode("none")} switches it off. {@code voiceEnabled(false)}
         * wins over any mode, and {@code none} with {@code voiceEnabled(true)}
         * becomes {@code ring_dashboard}.
         * </p>
         *
         * @param number  The number's id or its E.164 phone number
         * @param request {@code voiceEnabled}, {@code voiceMode} and
         *                {@code agentId}; set only what changes
         * @return The number after the change
         * @throws SendlyException if the request fails; 400
         *         {@code invalid_voice_mode} or {@code agent_required} when
         *         {@code agent} mode has no agent, 404
         *         {@code number_not_found} or {@code agent_not_found}, 409
         *         {@code agent_disabled} when the agent is switched off, 502
         *         {@code voice_attach_failed} when voice couldn't be switched
         *         on (try again), 503 {@code voice_unavailable}; the 502 and
         *         503 can follow a mode alone on a number whose voice is off
         */
        public VoiceNumber update(String number, UpdateVoiceNumberRequest request) throws SendlyException {
            return update(number, request, null);
        }

        /**
         * Change how a number answers phone calls, with per-call options.
         *
         * @param number  The number's id or its E.164 phone number
         * @param request The settings to change
         * @param options Per-call options (optional idempotency key)
         * @return The number after the change
         * @throws SendlyException if the request fails
         */
        public VoiceNumber update(String number, UpdateVoiceNumberRequest request, IdempotentRequestOptions options) throws SendlyException {
            String path = numberPath(number);
            if (request == null) {
                throw new ValidationException("Request is required");
            }

            JsonObject response = client.patch(path, request.toJson(), idempotencyKeyOf(options));
            return new VoiceNumber(response);
        }

        /**
         * Register the street address emergency services are sent to when
         * someone calls them from this number.
         * <p>
         * A US or Canadian number needs one before it can place calls. The
         * first registration adds 1.50 USD a month to the number; registering
         * again replaces the address without adding the charge a second time.
         * An idempotency key is generated automatically and reused across the
         * SDK's own retries.
         * </p>
         *
         * @param number  The number's id or its E.164 phone number
         * @param address The address; {@code country} defaults to {@code "US"}
         * @return The number with its emergency address
         * @throws SendlyException if the request fails; a
         *         {@code ValidationException} with {@code invalid_address}
         *         for a malformed field (400) or an address that couldn't be
         *         validated (422), 400 {@code e911_not_applicable} for a
         *         number outside the US and Canada, 404
         *         {@code number_not_found}, 502 {@code carrier_refused} when
         *         the registration was refused, thrown after the client has
         *         already retried the 5xx on its own. When the message says
         *         the number couldn't be found for emergency registration,
         *         retrying won't help: contact support. When it says the
         *         address couldn't be registered or emergency calling
         *         couldn't be switched on, try again later.
         */
        public VoiceNumber registerEmergencyAddress(String number, EmergencyAddress address) throws SendlyException {
            return registerEmergencyAddress(number, address, null);
        }

        /**
         * Register a number's emergency address with per-call options.
         *
         * @param number  The number's id or its E.164 phone number
         * @param address The address
         * @param options Per-call options (optional idempotency key)
         * @return The number with its emergency address
         * @throws SendlyException if the request fails
         */
        public VoiceNumber registerEmergencyAddress(String number, EmergencyAddress address, IdempotentRequestOptions options) throws SendlyException {
            String path = numberPath(number) + "/emergency-address";
            if (address == null) {
                throw new ValidationException("An emergency address is required");
            }
            requireText(address.getStreet(), "An emergency address 'street' is required");
            requireText(address.getCity(), "An emergency address 'city' is required");
            requireText(address.getState(), "An emergency address 'state' is required");
            requireText(address.getZip(), "An emergency address 'zip' is required");

            JsonObject response = client.post(path, address.toJson(), idempotencyKeyOf(options));
            return new VoiceNumber(response);
        }

        private static String numberPath(String number) throws ValidationException {
            requireText(number, "A number is required: the number's id or its E.164 phone number");
            return "/voice/numbers/" + PathParams.encode(number);
        }
    }

    /**
     * Agents sub-resource: the AI agents that answer real callers and talk on
     * the calls you place.
     */
    public static class Agents {
        private final Sendly client;

        Agents(Sendly client) {
            this.client = client;
        }

        /**
         * List the workspace's AI agents with their call stats.
         *
         * @return The agents
         * @throws SendlyException if the request fails
         */
        public VoiceAgentListResponse list() throws SendlyException {
            JsonObject response = client.get("/voice/agents", null);
            return new VoiceAgentListResponse(response);
        }

        /**
         * Create an AI agent.
         * <p>
         * The agent answers real callers on any number pointed at it and talks
         * on the calls you place with it. Each agent gets its own scoped
         * sending key so it can text callers; {@code canSendSms()} says
         * whether it has one. A workspace can have up to 20 agents. An
         * idempotency key is generated automatically and reused across the
         * SDK's own retries.
         * </p>
         *
         * @param request The agent's name, voice, greeting, instructions and tools
         * @return The new agent
         * @throws SendlyException if the request fails; 400
         *         {@code invalid_request} naming the field the API rejected,
         *         409 {@code agent_limit} when the workspace already has 20
         *         agents
         */
        public VoiceAgent create(CreateVoiceAgentRequest request) throws SendlyException {
            return create(request, null);
        }

        /**
         * Create an AI agent with per-call options.
         *
         * @param request The agent's details ({@code name} is required)
         * @param options Per-call options (optional idempotency key)
         * @return The new agent
         * @throws SendlyException if the request fails
         */
        public VoiceAgent create(CreateVoiceAgentRequest request, IdempotentRequestOptions options) throws SendlyException {
            if (request == null) {
                throw new ValidationException("An agent 'name' is required");
            }
            requireText(request.getName(), "An agent 'name' is required");

            JsonObject response = client.post("/voice/agents", request.toJson(), idempotencyKeyOf(options));
            return new VoiceAgent(response);
        }

        /**
         * Retrieve an agent.
         *
         * @param id The agent's id
         * @return The agent
         * @throws SendlyException if the request fails; 404
         *         {@code agent_not_found} when the agent isn't in this
         *         workspace
         */
        public VoiceAgent get(String id) throws SendlyException {
            JsonObject response = client.get(agentPath(id), null);
            return new VoiceAgent(response);
        }

        /**
         * Update an agent. Only the fields you set are sent; tool settings
         * you leave out keep their current values. Changes apply to the next
         * call the agent takes.
         *
         * @param id      The agent's id
         * @param request Any subset of the create fields
         * @return The agent after the change
         * @throws SendlyException if the request fails; 400
         *         {@code invalid_request} naming the field the API rejected,
         *         404 {@code agent_not_found}
         */
        public VoiceAgent update(String id, UpdateVoiceAgentRequest request) throws SendlyException {
            return update(id, request, null);
        }

        /**
         * Update an agent with per-call options.
         *
         * @param id      The agent's id
         * @param request The fields to change
         * @param options Per-call options (optional idempotency key)
         * @return The agent after the change
         * @throws SendlyException if the request fails
         */
        public VoiceAgent update(String id, UpdateVoiceAgentRequest request, IdempotentRequestOptions options) throws SendlyException {
            String path = agentPath(id);
            if (request == null) {
                throw new ValidationException("Request is required");
            }

            JsonObject response = client.patch(path, request.toJson(), idempotencyKeyOf(options));
            return new VoiceAgent(response);
        }

        /**
         * Delete an agent and revoke its sending key.
         * <p>
         * An agent that still answers a number can't be deleted: point those
         * numbers at another agent or back to the team first with
         * {@code numbers().update()}.
         * </p>
         *
         * @param id The agent's id
         * @return Deletion confirmation
         * @throws SendlyException if the request fails; 404
         *         {@code agent_not_found}, 409 {@code agent_in_use} while a
         *         number answers with the agent (the message says how many)
         */
        public DeletedVoiceAgent delete(String id) throws SendlyException {
            return delete(id, null);
        }

        /**
         * Delete an agent with per-call options.
         *
         * @param id      The agent's id
         * @param options Per-call options (optional idempotency key)
         * @return Deletion confirmation
         * @throws SendlyException if the request fails
         */
        public DeletedVoiceAgent delete(String id, IdempotentRequestOptions options) throws SendlyException {
            JsonObject response = client.delete(agentPath(id), idempotencyKeyOf(options));
            return new DeletedVoiceAgent(response);
        }

        private static String agentPath(String id) throws ValidationException {
            requireText(id, "An agent 'id' is required");
            return "/voice/agents/" + PathParams.encode(id);
        }
    }

    /**
     * Voices sub-resource: the voices an agent can speak with.
     */
    public static class Voices {
        private final Sendly client;

        Voices(Sendly client) {
            this.client = client;
        }

        /**
         * List the voices an agent can speak with. Pass a voice's id to
         * {@code CreateVoiceAgentRequest.Builder.voice(String)}.
         *
         * @return The voices
         * @throws SendlyException if the request fails
         */
        public VoiceListResponse list() throws SendlyException {
            JsonObject response = client.get("/voice/voices", null);
            return new VoiceListResponse(response);
        }
    }

    private static void requireText(String value, String message) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(message);
        }
    }

    private static String idempotencyKeyOf(IdempotentRequestOptions options) {
        return options != null ? options.getIdempotencyKey() : null;
    }
}
