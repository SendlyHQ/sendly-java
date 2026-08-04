package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.RcsAgentsResponse;
import com.sendly.models.RcsCapability;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * RCS resource — discover your agents and pre-flight recipient capability.
 * <p>
 * RCS is a first-class Sendly channel: branded rich messaging (cards,
 * suggestion chips) delivered over RCS when the recipient's device supports
 * it, with automatic SMS fallback (billed as SMS) for plain-text sends when it
 * doesn't. Send via {@code messages().send(SendRcsMessageRequest)}.
 * <p>
 * Sending as a brand requires an RCS agent registered on your workspace —
 * contact support to register one. An agent with status {@code testing}
 * reaches invited test numbers; {@code approved} reaches everyone. Sends and
 * capability checks require a live API key.
 *
 * <pre>{@code
 * // 1. Find your agent
 * RcsAgentsResponse agents = client.rcs().agents().list();
 *
 * // 2. Optional pre-flight: can this recipient receive RCS?
 * RcsCapability capability = client.rcs().capability("+15551234567");
 *
 * // 3. Send — text falls back to SMS for non-RCS recipients
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

    private final Sendly client;
    private final Agents agents;

    public RcsResource(Sendly client) {
        this.client = client;
        this.agents = new Agents(client);
    }

    /**
     * List the RCS agents on your workspace.
     */
    public Agents agents() {
        return agents;
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
         * Returns the agents registered on your workspace, newest first. An
         * empty list means no agent is registered yet — contact support to
         * register one for your brand.
         *
         * @return Your agents with status and sendability
         * @throws SendlyException if the request fails
         */
        public RcsAgentsResponse list() throws SendlyException {
            JsonObject response = client.get("/rcs/agents", null);
            return new RcsAgentsResponse(response);
        }
    }

    private static void validatePhone(String phone) throws ValidationException {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(
                "Invalid phone number format. Use E.164 format (e.g., +15551234567)"
            );
        }
    }
}
