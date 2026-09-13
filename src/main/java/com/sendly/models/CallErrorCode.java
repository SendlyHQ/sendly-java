package com.sendly.models;

/**
 * {@code error} codes the calls endpoints answer with, surfaced on
 * {@code SendlyException.getApiErrorCode()}.
 */
public final class CallErrorCode {
    /** 404: voice is not enabled for this workspace yet. */
    public static final String VOICE_NOT_ENABLED = "voice_not_enabled";
    /** 404: calls to phone numbers are not enabled for this workspace yet. */
    public static final String OUTBOUND_CALLS_NOT_ENABLED = "outbound_calls_not_enabled";
    /** 503: outbound calling is not available right now. */
    public static final String VOICE_UNAVAILABLE = "voice_unavailable";
    /** 503: AI agents are not switched on for this deployment yet. */
    public static final String AGENTS_UNAVAILABLE = "agents_unavailable";
    /** 400: calls placed over the API are answered by an AI agent; pass {@code agentId}. */
    public static final String AGENT_REQUIRED = "agent_required";
    /** 404: that agent does not exist in this workspace. */
    public static final String AGENT_NOT_FOUND = "agent_not_found";
    /** 409: switch the agent on before calling it. */
    public static final String AGENT_DISABLED = "agent_disabled";
    /** 400: the number is not a valid phone number, or you tried to call your own number. */
    public static final String INVALID_NUMBER = "invalid_number";
    /** 400: calls can only be placed to US and Canadian numbers. */
    public static final String DESTINATION_NOT_SUPPORTED = "destination_not_supported";
    /** 400: {@code metadata} breaks the key or value limits. */
    public static final String INVALID_METADATA = "invalid_metadata";
    /** 400: another field is invalid; the message names it. */
    public static final String INVALID_REQUEST = "invalid_request";
    /** 400: the workspace has more than one voice-enabled number; choose {@code from}. */
    public static final String FROM_NUMBER_REQUIRED = "from_number_required";
    /** 409: enable voice on one of your numbers first. */
    public static final String NO_VOICE_NUMBER = "no_voice_number";
    /** 404: the {@code from} number is not in your workspace. */
    public static final String NUMBER_NOT_FOUND = "number_not_found";
    /** 428: register an emergency address for the number before placing calls. */
    public static final String E911_REQUIRED = "e911_required";
    /** 402: the balance does not cover one minute at the agent rate. */
    public static final String INSUFFICIENT_CREDITS = "insufficient_credits";
    /** 409: every line in the workspace is in use; try again in a moment. */
    public static final String LINES_BUSY = "lines_busy";
    /** 429: today's calling limit has been reached. */
    public static final String DAILY_CALL_LIMIT = "daily_call_limit";
    /** 429: too many calls placed in the last minute. */
    public static final String RATE_LIMIT_EXCEEDED = "rate_limit_exceeded";
    /** 404: no call with that id is in this workspace. */
    public static final String CALL_NOT_FOUND = "call_not_found";
    /** 403: phone calls need a live API key. */
    public static final String LIVE_KEY_REQUIRED = "live_key_required";
    /** 403: the key's workspace role may not place or end calls. */
    public static final String FORBIDDEN = "forbidden";
    /** 403: the API key lacks the {@code calls:read} or {@code calls:write} scope. */
    public static final String INSUFFICIENT_PERMISSIONS = "insufficient_permissions";
    /** 500: something went wrong on Sendly's side. */
    public static final String VOICE_INTERNAL_ERROR = "voice_internal_error";

    private CallErrorCode() {}
}
