package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.Conversation;
import com.sendly.models.ConversationListResponse;
import com.sendly.models.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conversations resource for managing two-way messaging threads.
 */
public class ConversationsResource {
    private final Sendly client;

    public ConversationsResource(Sendly client) {
        this.client = client;
    }

    /**
     * List conversations.
     *
     * @return List of conversations
     * @throws SendlyException if the request fails
     */
    public ConversationListResponse list() throws SendlyException {
        return list(null, 20, 0);
    }

    /**
     * List conversations with options.
     *
     * @param status Filter by status ("active" or "closed")
     * @param limit  Maximum conversations to return
     * @param offset Number of conversations to skip
     * @return List of conversations
     * @throws SendlyException if the request fails
     */
    public ConversationListResponse list(String status, int limit, int offset) throws SendlyException {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(Math.min(limit, 100)));
        params.put("offset", String.valueOf(offset));
        if (status != null) {
            params.put("status", status);
        }

        JsonObject response = client.get("/conversations", params);
        return new ConversationListResponse(response);
    }

    /**
     * Get a conversation by ID.
     *
     * @param id Conversation ID
     * @return The conversation
     * @throws SendlyException if the request fails
     */
    public Conversation get(String id) throws SendlyException {
        return get(id, false, 0, 0);
    }

    /**
     * Get a conversation by ID with messages.
     *
     * @param id              Conversation ID
     * @param includeMessages Whether to include messages
     * @param messageLimit    Maximum messages to return
     * @param messageOffset   Number of messages to skip
     * @return The conversation with optional messages
     * @throws SendlyException if the request fails
     */
    public Conversation get(String id, boolean includeMessages, int messageLimit, int messageOffset) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }

        Map<String, String> params = new HashMap<>();
        if (includeMessages) {
            params.put("include_messages", "true");
            if (messageLimit > 0) {
                params.put("message_limit", String.valueOf(messageLimit));
            }
            if (messageOffset > 0) {
                params.put("message_offset", String.valueOf(messageOffset));
            }
        }

        JsonObject response = client.get("/conversations/" + id, params.isEmpty() ? null : params);
        return new Conversation(response);
    }

    /**
     * Reply to a conversation.
     *
     * @param id   Conversation ID
     * @param text Message text
     * @return The sent message
     * @throws SendlyException if the request fails
     */
    public Message reply(String id, String text) throws SendlyException {
        return reply(id, text, null, null);
    }

    /**
     * Reply to a conversation.
     *
     * @param id        Conversation ID
     * @param text      Message text
     * @param mediaUrls Optional media URLs
     * @param metadata  Optional metadata
     * @return The sent message
     * @throws SendlyException if the request fails
     */
    public Message reply(String id, String text, List<String> mediaUrls, Map<String, Object> metadata) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }
        if (text == null || text.isEmpty()) {
            throw new ValidationException("Message text is required");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            body.put("mediaUrls", mediaUrls);
        }
        if (metadata != null && !metadata.isEmpty()) {
            body.put("metadata", metadata);
        }

        JsonObject response = client.post("/conversations/" + id + "/messages", body);
        return new Message(response);
    }

    /**
     * Update a conversation.
     *
     * @param id       Conversation ID
     * @param metadata Optional metadata to set
     * @param tags     Optional tags to set
     * @return The updated conversation
     * @throws SendlyException if the request fails
     */
    public Conversation update(String id, Map<String, Object> metadata, List<String> tags) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }

        Map<String, Object> body = new HashMap<>();
        if (metadata != null) {
            body.put("metadata", metadata);
        }
        if (tags != null) {
            body.put("tags", tags);
        }

        JsonObject response = client.patch("/conversations/" + id, body);
        return new Conversation(response);
    }

    /**
     * Close a conversation.
     *
     * @param id Conversation ID
     * @return The closed conversation
     * @throws SendlyException if the request fails
     */
    public Conversation close(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }

        JsonObject response = client.post("/conversations/" + id + "/close", new HashMap<>());
        return new Conversation(response);
    }

    /**
     * Reopen a conversation.
     *
     * @param id Conversation ID
     * @return The reopened conversation
     * @throws SendlyException if the request fails
     */
    public Conversation reopen(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }

        JsonObject response = client.post("/conversations/" + id + "/reopen", new HashMap<>());
        return new Conversation(response);
    }

    /**
     * Mark a conversation as read.
     *
     * @param id Conversation ID
     * @return The updated conversation
     * @throws SendlyException if the request fails
     */
    public Conversation markRead(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Conversation ID is required");
        }

        JsonObject response = client.post("/conversations/" + id + "/mark-read", new HashMap<>());
        return new Conversation(response);
    }
}
