package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.models.*;
import com.sendly.exceptions.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Verify API resource for OTP verification.
 */
public class VerifyResource {
    private final Sendly client;
    private final SessionsResource sessions;

    public VerifyResource(Sendly client) {
        this.client = client;
        this.sessions = new SessionsResource(client);
    }

    public SessionsResource sessions() {
        return sessions;
    }

    /**
     * Send an OTP verification code.
     */
    public SendVerificationResponse send(SendVerificationRequest request) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        body.put("to", request.getTo());
        if (request.getTemplateId() != null) body.put("template_id", request.getTemplateId());
        if (request.getProfileId() != null) body.put("profile_id", request.getProfileId());
        if (request.getAppName() != null) body.put("app_name", request.getAppName());
        if (request.getTimeoutSecs() != null) body.put("timeout_secs", request.getTimeoutSecs());
        if (request.getCodeLength() != null) body.put("code_length", request.getCodeLength());

        return client.request("POST", "/verify", body, SendVerificationResponse.class);
    }

    /**
     * Check/verify an OTP code.
     */
    public CheckVerificationResponse check(String verificationId, String code) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        return client.request("POST", "/verify/" + verificationId + "/check", body, CheckVerificationResponse.class);
    }

    /**
     * Get a verification by ID.
     */
    public Verification get(String verificationId) throws SendlyException {
        return client.request("GET", "/verify/" + verificationId, null, Verification.class);
    }

    /**
     * List recent verifications.
     */
    public VerificationListResponse list(ListVerificationsRequest request) throws SendlyException {
        StringBuilder path = new StringBuilder("/verify");
        if (request != null) {
            StringBuilder params = new StringBuilder();
            if (request.getLimit() != null) {
                params.append(params.length() == 0 ? "?" : "&").append("limit=").append(request.getLimit());
            }
            if (request.getStatus() != null) {
                params.append(params.length() == 0 ? "?" : "&").append("status=").append(request.getStatus());
            }
            path.append(params);
        }
        return client.request("GET", path.toString(), null, VerificationListResponse.class);
    }

    /**
     * Resend an OTP verification code.
     */
    public SendVerificationResponse resend(String verificationId) throws SendlyException {
        return client.request("POST", "/verify/" + verificationId + "/resend", null, SendVerificationResponse.class);
    }
}
