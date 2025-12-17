package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.BatchList;
import com.sendly.models.BatchMessageItem;
import com.sendly.models.BatchMessageResponse;
import com.sendly.models.CancelScheduledMessageResponse;
import com.sendly.models.ListBatchesRequest;
import com.sendly.models.ListMessagesRequest;
import com.sendly.models.ListScheduledMessagesRequest;
import com.sendly.models.Message;
import com.sendly.models.MessageList;
import com.sendly.models.ScheduledMessage;
import com.sendly.models.ScheduledMessageList;
import com.sendly.models.ScheduleMessageRequest;
import com.sendly.models.SendBatchRequest;
import com.sendly.models.SendMessageRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Messages resource for sending and managing SMS.
 */
public class Messages {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    private static final int MAX_TEXT_LENGTH = 1600;

    private final Sendly client;

    public Messages(Sendly client) {
        this.client = client;
    }

    /**
     * Send an SMS message.
     *
     * @param to   Recipient phone number in E.164 format
     * @param text Message content
     * @return The sent message
     * @throws SendlyException if the request fails
     */
    public Message send(String to, String text) throws SendlyException {
        return send(new SendMessageRequest(to, text));
    }

    /**
     * Send an SMS message.
     *
     * @param request Send message request
     * @return The sent message
     * @throws SendlyException if the request fails
     */
    public Message send(SendMessageRequest request) throws SendlyException {
        validatePhone(request.getTo());
        validateText(request.getText());

        JsonObject response = client.post("/messages", request);
        JsonObject data = response.has("message") ?
                response.getAsJsonObject("message") :
                response.has("data") ? response.getAsJsonObject("data") : response;

        return new Message(data);
    }

    /**
     * List messages.
     *
     * @return List of messages
     * @throws SendlyException if the request fails
     */
    public MessageList list() throws SendlyException {
        return list(ListMessagesRequest.builder().build());
    }

    /**
     * List messages with options.
     *
     * @param request List options
     * @return List of messages
     * @throws SendlyException if the request fails
     */
    public MessageList list(ListMessagesRequest request) throws SendlyException {
        JsonObject response = client.get("/messages", request.toParams());
        return new MessageList(response);
    }

    /**
     * Get a message by ID.
     *
     * @param id Message ID
     * @return The message
     * @throws SendlyException if the request fails
     */
    public Message get(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Message ID is required");
        }

        JsonObject response = client.get("/messages/" + id, null);
        JsonObject data = response.has("data") ?
                response.getAsJsonObject("data") :
                response.has("message") ? response.getAsJsonObject("message") : response;

        return new Message(data);
    }

    /**
     * Iterate over all messages with automatic pagination.
     *
     * @return Iterable over all messages
     */
    public Iterable<Message> each() {
        return each(ListMessagesRequest.builder().build());
    }

    /**
     * Iterate over all messages with automatic pagination.
     *
     * @param request List options (status, to filters)
     * @return Iterable over all messages
     */
    public Iterable<Message> each(ListMessagesRequest request) {
        return () -> new MessageIterator(this, request);
    }

    // ==================== Scheduling Methods ====================

    /**
     * Schedule a message for future delivery.
     *
     * @param to          Recipient phone number in E.164 format
     * @param text        Message content
     * @param scheduledAt ISO 8601 datetime (must be at least 1 minute in the future)
     * @return The scheduled message
     * @throws SendlyException if the request fails
     */
    public ScheduledMessage schedule(String to, String text, String scheduledAt) throws SendlyException {
        return schedule(new ScheduleMessageRequest(to, text, scheduledAt));
    }

    /**
     * Schedule a message for future delivery.
     *
     * @param request Schedule message request
     * @return The scheduled message
     * @throws SendlyException if the request fails
     */
    public ScheduledMessage schedule(ScheduleMessageRequest request) throws SendlyException {
        validatePhone(request.getTo());
        validateText(request.getText());
        validateScheduledAt(request.getScheduledAt());

        JsonObject response = client.post("/messages/schedule", request);
        JsonObject data = response.has("data") ?
                response.getAsJsonObject("data") : response;

        return new ScheduledMessage(data);
    }

    /**
     * List scheduled messages.
     *
     * @return List of scheduled messages
     * @throws SendlyException if the request fails
     */
    public ScheduledMessageList listScheduled() throws SendlyException {
        return listScheduled(ListScheduledMessagesRequest.builder().build());
    }

    /**
     * List scheduled messages with options.
     *
     * @param request List options
     * @return List of scheduled messages
     * @throws SendlyException if the request fails
     */
    public ScheduledMessageList listScheduled(ListScheduledMessagesRequest request) throws SendlyException {
        JsonObject response = client.get("/messages/scheduled", request.toParams());
        return new ScheduledMessageList(response);
    }

    /**
     * Get a scheduled message by ID.
     *
     * @param id Scheduled message ID
     * @return The scheduled message
     * @throws SendlyException if the request fails
     */
    public ScheduledMessage getScheduled(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Scheduled message ID is required");
        }

        String encodedId = encodePathParam(id);
        JsonObject response = client.get("/messages/scheduled/" + encodedId, null);
        JsonObject data = response.has("data") ?
                response.getAsJsonObject("data") : response;

        return new ScheduledMessage(data);
    }

    /**
     * Cancel a scheduled message.
     *
     * @param id Scheduled message ID
     * @return The cancellation response with refunded credits
     * @throws SendlyException if the request fails
     */
    public CancelScheduledMessageResponse cancelScheduled(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Scheduled message ID is required");
        }

        String encodedId = encodePathParam(id);
        JsonObject response = client.delete("/messages/scheduled/" + encodedId);
        return new CancelScheduledMessageResponse(response);
    }

    // ==================== Batch Methods ====================

    /**
     * Send a batch of messages.
     *
     * @param request Batch send request
     * @return The batch response with results
     * @throws SendlyException if the request fails
     */
    public BatchMessageResponse sendBatch(SendBatchRequest request) throws SendlyException {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new ValidationException("At least one message is required");
        }

        // Validate all messages
        for (BatchMessageItem item : request.getMessages()) {
            validatePhone(item.getTo());
            validateText(item.getText());
        }

        JsonObject response = client.post("/messages/batch", request);
        return new BatchMessageResponse(response);
    }

    /**
     * Get a batch by ID.
     *
     * @param batchId Batch ID
     * @return The batch response
     * @throws SendlyException if the request fails
     */
    public BatchMessageResponse getBatch(String batchId) throws SendlyException {
        if (batchId == null || batchId.isEmpty()) {
            throw new ValidationException("Batch ID is required");
        }

        String encodedId = encodePathParam(batchId);
        JsonObject response = client.get("/messages/batch/" + encodedId, null);
        return new BatchMessageResponse(response);
    }

    /**
     * List all batches.
     *
     * @return List of batches
     * @throws SendlyException if the request fails
     */
    public BatchList listBatches() throws SendlyException {
        return listBatches(ListBatchesRequest.builder().build());
    }

    /**
     * List batches with options.
     *
     * @param request List options
     * @return List of batches
     * @throws SendlyException if the request fails
     */
    public BatchList listBatches(ListBatchesRequest request) throws SendlyException {
        JsonObject response = client.get("/messages/batches", request.toParams());
        return new BatchList(response);
    }

    // ==================== Validation Helpers ====================

    private void validatePhone(String phone) throws ValidationException {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(
                "Invalid phone number format. Use E.164 format (e.g., +15551234567)"
            );
        }
    }

    private void validateText(String text) throws ValidationException {
        if (text == null || text.isEmpty()) {
            throw new ValidationException("Message text is required");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ValidationException(
                "Message text exceeds maximum length (" + MAX_TEXT_LENGTH + " characters)"
            );
        }
    }

    private void validateScheduledAt(String scheduledAt) throws ValidationException {
        if (scheduledAt == null || scheduledAt.isEmpty()) {
            throw new ValidationException("Scheduled time is required");
        }
        // Basic ISO 8601 format validation
        if (!scheduledAt.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*$")) {
            throw new ValidationException(
                "Invalid scheduled time format. Use ISO 8601 format (e.g., 2025-01-20T10:00:00Z)"
            );
        }
    }

    private String encodePathParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            return param;
        }
    }

    /**
     * Iterator for paginating through all messages.
     */
    private static class MessageIterator implements Iterator<Message> {
        private final Messages messages;
        private final String status;
        private final String to;
        private final int batchSize;

        private MessageList currentPage;
        private Iterator<Message> pageIterator;
        private int offset;

        MessageIterator(Messages messages, ListMessagesRequest request) {
            this.messages = messages;
            this.status = null; // Extract from request if needed
            this.to = null;
            this.batchSize = 100;
            this.offset = 0;
            fetchNextPage();
        }

        private void fetchNextPage() {
            try {
                currentPage = messages.list(
                    ListMessagesRequest.builder()
                        .limit(batchSize)
                        .offset(offset)
                        .status(status)
                        .to(to)
                        .build()
                );
                pageIterator = currentPage.iterator();
                offset += batchSize;
            } catch (SendlyException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            if (pageIterator.hasNext()) {
                return true;
            }
            if (currentPage.hasMore()) {
                fetchNextPage();
                return pageIterator.hasNext();
            }
            return false;
        }

        @Override
        public Message next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return pageIterator.next();
        }
    }
}
