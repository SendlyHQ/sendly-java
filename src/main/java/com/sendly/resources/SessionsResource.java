package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.models.*;
import com.sendly.exceptions.*;
import java.util.HashMap;
import java.util.Map;

public class SessionsResource {
    private final Sendly client;

    public SessionsResource(Sendly client) {
        this.client = client;
    }

    public VerifySession create(CreateSessionRequest request) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        body.put("success_url", request.getSuccessUrl());
        if (request.getCancelUrl() != null) body.put("cancel_url", request.getCancelUrl());
        if (request.getBrandName() != null) body.put("brand_name", request.getBrandName());
        if (request.getBrandColor() != null) body.put("brand_color", request.getBrandColor());
        if (request.getMetadata() != null) body.put("metadata", request.getMetadata());

        return client.request("POST", "/verify/sessions", body, VerifySession.class);
    }

    public ValidateSessionResponse validate(String token) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        return client.request("POST", "/verify/sessions/validate", body, ValidateSessionResponse.class);
    }
}
