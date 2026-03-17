package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.models.*;
import com.sendly.exceptions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Drafts API resource for managing message drafts.
 */
public class DraftsResource {
    private final Sendly client;

    public DraftsResource(Sendly client) {
        this.client = client;
    }

    /**
     * Create a new draft.
     *
     * @param conversationId Conversation ID
     * @param text           Draft text
     * @return The created draft
     * @throws SendlyException if the request fails
     */
    public Draft create(String conversationId, String text) throws SendlyException {
        return create(conversationId, text, null, null, null);
    }

    /**
     * Create a new draft.
     *
     * @param conversationId Conversation ID
     * @param text           Draft text
     * @param mediaUrls      Optional media URLs
     * @param metadata       Optional metadata
     * @param source         Optional source
     * @return The created draft
     * @throws SendlyException if the request fails
     */
    public Draft create(String conversationId, String text, List<String> mediaUrls,
                        Map<String, Object> metadata, String source) throws SendlyException {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }
        if (text == null || text.isEmpty()) {
            throw new ValidationException("Draft text is required");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", conversationId);
        body.put("text", text);
        if (mediaUrls != null && !mediaUrls.isEmpty()) body.put("mediaUrls", mediaUrls);
        if (metadata != null && !metadata.isEmpty()) body.put("metadata", metadata);
        if (source != null) body.put("source", source);

        return client.request("POST", "/drafts", body, Draft.class);
    }

    /**
     * List drafts.
     *
     * @return List of drafts
     * @throws SendlyException if the request fails
     */
    public DraftListResponse list() throws SendlyException {
        return list(null, null, null, null);
    }

    /**
     * List drafts with filters.
     *
     * @param conversationId Filter by conversation ID
     * @param status         Filter by status
     * @param limit          Maximum results
     * @param offset         Number to skip
     * @return List of drafts
     * @throws SendlyException if the request fails
     */
    public DraftListResponse list(String conversationId, String status, Integer limit, Integer offset) throws SendlyException {
        Map<String, String> params = new HashMap<>();
        if (conversationId != null) params.put("conversation_id", conversationId);
        if (status != null) params.put("status", status);
        if (limit != null) params.put("limit", String.valueOf(limit));
        if (offset != null) params.put("offset", String.valueOf(offset));

        return client.request("GET", "/drafts" + buildQueryString(params), null, DraftListResponse.class);
    }

    /**
     * Get a draft by ID.
     *
     * @param id Draft ID
     * @return The draft
     * @throws SendlyException if the request fails
     */
    public Draft get(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Draft ID is required");
        }

        return client.request("GET", "/drafts/" + id, null, Draft.class);
    }

    /**
     * Update a draft.
     *
     * @param id   Draft ID
     * @param text Optional new text
     * @return The updated draft
     * @throws SendlyException if the request fails
     */
    public Draft update(String id, String text) throws SendlyException {
        return update(id, text, null, null);
    }

    /**
     * Update a draft.
     *
     * @param id        Draft ID
     * @param text      Optional new text
     * @param mediaUrls Optional new media URLs
     * @param metadata  Optional new metadata
     * @return The updated draft
     * @throws SendlyException if the request fails
     */
    public Draft update(String id, String text, List<String> mediaUrls, Map<String, Object> metadata) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Draft ID is required");
        }

        Map<String, Object> body = new HashMap<>();
        if (text != null) body.put("text", text);
        if (mediaUrls != null) body.put("mediaUrls", mediaUrls);
        if (metadata != null) body.put("metadata", metadata);

        return client.request("PATCH", "/drafts/" + id, body, Draft.class);
    }

    /**
     * Approve a draft.
     *
     * @param id Draft ID
     * @return The approved draft
     * @throws SendlyException if the request fails
     */
    public Draft approve(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Draft ID is required");
        }

        return client.request("POST", "/drafts/" + id + "/approve", null, Draft.class);
    }

    /**
     * Reject a draft.
     *
     * @param id Draft ID
     * @return The rejected draft
     * @throws SendlyException if the request fails
     */
    public Draft reject(String id) throws SendlyException {
        return reject(id, null);
    }

    /**
     * Reject a draft with a reason.
     *
     * @param id     Draft ID
     * @param reason Optional rejection reason
     * @return The rejected draft
     * @throws SendlyException if the request fails
     */
    public Draft reject(String id, String reason) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Draft ID is required");
        }

        Map<String, Object> body = new HashMap<>();
        if (reason != null) body.put("reason", reason);

        return client.request("POST", "/drafts/" + id + "/reject", body, Draft.class);
    }

    private String buildQueryString(Map<String, String> params) {
        if (params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }
}
