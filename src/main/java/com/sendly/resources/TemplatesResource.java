package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.models.*;
import com.sendly.exceptions.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Templates API resource for managing SMS templates.
 */
public class TemplatesResource {
    private final Sendly client;

    public TemplatesResource(Sendly client) {
        this.client = client;
    }

    /**
     * List all templates (presets + custom).
     */
    public TemplateListResponse list() throws SendlyException {
        return client.request("GET", "/templates", null, TemplateListResponse.class);
    }

    /**
     * List preset templates only.
     */
    public TemplateListResponse presets() throws SendlyException {
        return client.request("GET", "/templates/presets", null, TemplateListResponse.class);
    }

    /**
     * Get a template by ID.
     */
    public Template get(String templateId) throws SendlyException {
        return client.request("GET", "/templates/" + templateId, null, Template.class);
    }

    /**
     * Create a new template.
     */
    public Template create(String name, String text) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("text", text);
        return client.request("POST", "/templates", body, Template.class);
    }

    /**
     * Update a template.
     */
    public Template update(String templateId, String name, String text) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        if (name != null) body.put("name", name);
        if (text != null) body.put("text", text);
        return client.request("PATCH", "/templates/" + templateId, body, Template.class);
    }

    /**
     * Publish a draft template.
     */
    public Template publish(String templateId) throws SendlyException {
        return client.request("POST", "/templates/" + templateId + "/publish", null, Template.class);
    }

    /**
     * Delete a template.
     */
    public void delete(String templateId) throws SendlyException {
        client.request("DELETE", "/templates/" + templateId, null, Void.class);
    }

    /**
     * Preview a template with sample values.
     */
    public TemplatePreview preview(String templateId, Map<String, String> variables) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        if (variables != null) {
            body.put("variables", variables);
        }
        return client.request("POST", "/templates/" + templateId + "/preview", body, TemplatePreview.class);
    }

    /**
     * Clone a template.
     */
    public Template clone(String templateId) throws SendlyException {
        return client.request("POST", "/templates/" + templateId + "/clone", null, Template.class);
    }

    /**
     * Clone a template with a new name.
     */
    public Template clone(String templateId, String name) throws SendlyException {
        Map<String, Object> body = new HashMap<>();
        if (name != null) {
            body.put("name", name);
        }
        return client.request("POST", "/templates/" + templateId + "/clone", body, Template.class);
    }

    /**
     * Generate a template using AI.
     *
     * @param description Description of the template to generate
     * @return The generated template
     * @throws SendlyException if the request fails
     */
    public GeneratedTemplate generate(String description) throws SendlyException {
        return generate(description, null);
    }

    /**
     * Generate a template using AI.
     *
     * @param description Description of the template to generate
     * @param category    Optional category for the template
     * @return The generated template
     * @throws SendlyException if the request fails
     */
    public GeneratedTemplate generate(String description, String category) throws SendlyException {
        if (description == null || description.isEmpty()) {
            throw new ValidationException("Description is required");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        if (category != null) {
            body.put("category", category);
        }
        return client.request("POST", "/templates/generate", body, GeneratedTemplate.class);
    }
}
