package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.models.*;
import com.sendly.exceptions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Labels API resource for managing conversation labels.
 */
public class LabelsResource {
    private final Sendly client;

    public LabelsResource(Sendly client) {
        this.client = client;
    }

    /**
     * Create a new label.
     *
     * @param name Label name
     * @return The created label
     * @throws SendlyException if the request fails
     */
    public Label create(String name) throws SendlyException {
        return create(name, null, null);
    }

    /**
     * Create a new label.
     *
     * @param name        Label name
     * @param color       Optional color
     * @param description Optional description
     * @return The created label
     * @throws SendlyException if the request fails
     */
    public Label create(String name, String color, String description) throws SendlyException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Label name is required");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        if (color != null) body.put("color", color);
        if (description != null) body.put("description", description);

        return client.request("POST", "/labels", body, Label.class);
    }

    /**
     * List all labels.
     *
     * @return List of labels
     * @throws SendlyException if the request fails
     */
    public LabelListResponse list() throws SendlyException {
        return client.request("GET", "/labels", null, LabelListResponse.class);
    }

    /**
     * Delete a label.
     *
     * @param id Label ID
     * @throws SendlyException if the request fails
     */
    public void delete(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Label ID is required");
        }

        client.request("DELETE", "/labels/" + id, null, Void.class);
    }
}
