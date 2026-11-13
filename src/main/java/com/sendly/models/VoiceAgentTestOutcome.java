package com.sendly.models;

/**
 * What a connection test found, as reported by
 * {@code VoiceAgentTest.getOutcome()}. An outcome added later is returned as
 * sent.
 */
public final class VoiceAgentTestOutcome {
    /** The agent answered the way a call needs. */
    public static final String OK = "ok";
    /** The host isn't a public address. */
    public static final String BLOCKED_DESTINATION = "blocked_destination";
    /** The host couldn't be reached. */
    public static final String UNREACHABLE = "unreachable";
    /** The agent didn't answer in time. */
    public static final String TIMEOUT = "timeout";
    /** A secure connection couldn't be set up. */
    public static final String TLS_FAILED = "tls_failed";
    /** The SIP server wants credentials the agent doesn't have. */
    public static final String AUTH_REQUIRED = "auth_required";
    /** The key, token or credentials were refused. */
    public static final String AUTH_FAILED = "auth_failed";
    /** Wrong path or SIP user. */
    public static final String NOT_FOUND = "not_found";
    /** Any other refusal. */
    public static final String REJECTED = "rejected";
    /** An answer outside the protocol. */
    public static final String BAD_REPLY = "bad_reply";

    private VoiceAgentTestOutcome() {}
}
