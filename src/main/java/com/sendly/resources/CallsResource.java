package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.Call;
import com.sendly.models.CallListResponse;
import com.sendly.models.CallRecording;
import com.sendly.models.CreateCallRequest;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.ListCallsOptions;

/**
 * Calls resource: phone calls handled by your AI agents.
 * <p>
 * Place an outbound call that one of your agents talks on, list and inspect
 * calls (with the transcript of an agent-handled call), end a call, and fetch
 * a recording. Which numbers can call is configured in the dashboard: switch
 * voice on for a number, pick how it answers, and register its emergency
 * address. {@code numbers().list()} reports {@code voiceEnabled} and
 * {@code voiceMode} so you can find a {@code from} number.
 * </p>
 * <p>
 * Calls are prepaid from the workspace balance per started minute: an
 * agent-handled outbound call costs 10 credits a minute (2 for the call, 8 for
 * the agent). An unanswered call costs nothing.
 * </p>
 * <p>
 * Reads need the {@code calls:read} scope, writes {@code calls:write} and a
 * live API key. Until voice is enabled for your workspace every method answers
 * 404 {@code voice_not_enabled}, a {@code NotFoundException}.
 * </p>
 *
 * <pre>{@code
 * Call call = client.calls().create(CreateCallRequest.builder()
 *     .to("+15555550123")
 *     .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
 *     .context("You are calling Jordan to confirm the 3pm appointment on Tuesday.")
 *     .metadata("crmId", "lead_8812")
 *     .build());
 *
 * Call finished = client.calls().get(call.getId());
 * System.out.println(finished.getStatus() + " " + finished.getHangupClass());
 * for (CallTranscriptLine line : finished.getTranscript()) {
 *     System.out.println(line.getSpeaker() + ": " + line.getText());
 * }
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/voice">Voice docs</a>
 */
public class CallsResource {
    private final Sendly client;

    public CallsResource(Sendly client) {
        this.client = client;
    }

    /**
     * Place a phone call that one of your AI agents handles. The call is
     * returned while it rings ({@code status} "ringing"); poll {@link #get(String)}
     * or subscribe to the {@code call.started} and {@code call.completed}
     * webhooks to follow it.
     * <p>
     * An idempotency key is generated automatically and reused across the
     * SDK's own retries; see {@link #create(CreateCallRequest, IdempotentRequestOptions)}
     * to supply your own.
     * </p>
     *
     * @param request Who to call, which agent talks, and optionally which
     *                number to call from, context for the agent and metadata
     * @return The new call, ringing
     * @throws SendlyException if the request fails; 402
     *         {@code insufficient_credits} when the balance does not cover a
     *         minute, 428 {@code e911_required} until the number has an
     *         emergency address, 409 {@code lines_busy} when every line is in
     *         use; see {@code CallErrorCode} for the full list
     */
    public Call create(CreateCallRequest request) throws SendlyException {
        return create(request, null);
    }

    /**
     * Place a phone call with per-call options.
     *
     * @param request Who to call and which agent talks
     * @param options Per-call options (optional idempotency key)
     * @return The new call, ringing
     * @throws SendlyException if the request fails
     */
    public Call create(CreateCallRequest request, IdempotentRequestOptions options) throws SendlyException {
        if (request == null) {
            throw new ValidationException("Request is required");
        }
        if (request.getTo() == null || request.getTo().trim().isEmpty()) {
            throw new ValidationException("A destination 'to' is required");
        }
        if (request.getAgentId() == null || request.getAgentId().trim().isEmpty()) {
            throw new ValidationException("An 'agentId' is required: calls placed over the API are answered by an AI agent");
        }

        JsonObject response = client.post("/calls", request.toJson(), idempotencyKeyOf(options));
        return new Call(response);
    }

    /**
     * List calls, most recently started first, 50 per page.
     *
     * @return The first page of calls
     * @throws SendlyException if the request fails
     */
    public CallListResponse list() throws SendlyException {
        return list(null);
    }

    /**
     * List calls with filters and paging. Live calls are reconciled before
     * they are returned, so a ring past its deadline shows as
     * {@code no_answer}.
     *
     * @param options Filters (status, direction, kind, agentId, to, from) and
     *                paging (limit 1 to 100, offset); may be null
     * @return One page of calls
     * @throws SendlyException if the request fails; 400
     *         {@code invalid_request} for an unknown filter value
     */
    public CallListResponse list(ListCallsOptions options) throws SendlyException {
        JsonObject response = client.get("/calls", options != null ? options.toParams() : null);
        return new CallListResponse(response);
    }

    /**
     * Retrieve one call. Agent-handled calls carry their transcript; other
     * calls return {@code null} from {@code getTranscript()}.
     *
     * @param id The call's id
     * @return The call
     * @throws SendlyException if the request fails; 404
     *         {@code call_not_found} when the call is not in this workspace
     */
    public Call get(String id) throws SendlyException {
        requireId(id);
        JsonObject response = client.get("/calls/" + PathParams.encode(id), null);
        return new Call(response);
    }

    /**
     * End a call. A ringing call becomes {@code cancelled}
     * ({@code hangupClass} "caller_cancelled"), an active call becomes
     * {@code completed} ("normal"). A call that has already ended is returned
     * unchanged.
     * <p>
     * An idempotency key is generated automatically and reused across the
     * SDK's own retries; see {@link #hangup(String, IdempotentRequestOptions)}
     * to supply your own.
     * </p>
     *
     * @param id The call's id
     * @return The call after the hangup
     * @throws SendlyException if the request fails; 404
     *         {@code call_not_found} when the call is not in this workspace
     */
    public Call hangup(String id) throws SendlyException {
        return hangup(id, null);
    }

    /**
     * End a call with per-call options.
     *
     * @param id      The call's id
     * @param options Per-call options (optional idempotency key)
     * @return The call after the hangup
     * @throws SendlyException if the request fails
     */
    public Call hangup(String id, IdempotentRequestOptions options) throws SendlyException {
        requireId(id);
        JsonObject response = client.post("/calls/" + PathParams.encode(id) + "/hangup", new JsonObject(), idempotencyKeyOf(options));
        return new Call(response);
    }

    /**
     * Fetch a call's recording. When the status is {@code ready} the response
     * carries a signed download URL that works for five minutes; fetch again
     * for a fresh one. Recordings are Ogg/Opus; agent-handled calls are
     * recorded dual-channel (caller left, agent right).
     *
     * @param id The call's id
     * @return The recording's status and, when ready, its URL and expiry
     * @throws SendlyException if the request fails; 404
     *         {@code call_not_found} when the call is not in this workspace
     */
    public CallRecording recording(String id) throws SendlyException {
        requireId(id);
        JsonObject response = client.get("/calls/" + PathParams.encode(id) + "/recording", null);
        return new CallRecording(response);
    }

    private static void requireId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Call ID is required");
        }
    }

    private static String idempotencyKeyOf(IdempotentRequestOptions options) {
        return options != null ? options.getIdempotencyKey() : null;
    }
}
