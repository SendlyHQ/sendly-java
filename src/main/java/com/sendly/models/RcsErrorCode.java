package com.sendly.models;

/**
 * {@code error} codes the RCS registration endpoints answer with, surfaced on
 * {@code SendlyException.getApiErrorCode()}.
 */
public final class RcsErrorCode {
    /** 404: RCS registration isn't enabled for this account yet. */
    public static final String NOT_ENABLED = "rcs_not_enabled";
    /** 404: no brand or agent with that id in this workspace. */
    public static final String NOT_FOUND = "rcs_not_found";
    /** 409: the record is under review and cannot be edited right now. */
    public static final String FIELD_LOCKED = "rcs_field_locked";
    /** 422: RCS registration is available to US businesses for now. */
    public static final String US_ONLY = "rcs_us_only";
    /** 422: a field is missing or invalid; see {@code getFieldErrors()}. */
    public static final String INVALID_CONTENT = "rcs_invalid_content";
    /** 409: the brand failed carrier verification, so the agent cannot be submitted. */
    public static final String BRAND_NOT_VERIFIED = "rcs_brand_not_verified";
    /** 409: the agent isn't ready to launch; finish testing on an invited device first. */
    public static final String LAUNCH_NOT_READY = "rcs_launch_not_ready";
    /** 500: something went wrong on Sendly's side. */
    public static final String INTERNAL_ERROR = "rcs_internal_error";
    /** 403: the API key lacks the {@code rcs:read} or {@code rcs:write} scope. */
    public static final String INSUFFICIENT_PERMISSIONS = "insufficient_permissions";
    /** 403: the key's workspace role may not read or edit verifications. */
    public static final String FORBIDDEN = "forbidden";
    /**
     * 429: this address has sent too many invalid API keys, so the key is
     * wrong. Never retried automatically; {@code getRetryAfter()} says when
     * the lockout ends.
     */
    public static final String TOO_MANY_FAILED_KEY_ATTEMPTS = "too_many_failed_key_attempts";
    /** 429: too many API key checks are already running; retried automatically after a second. */
    public static final String TOO_MANY_CONCURRENT_VERIFICATIONS = "too_many_concurrent_verifications";

    private RcsErrorCode() {}
}
