package com.sendly.resources;

import com.sendly.Sendly;
import com.sendly.models.*;
import com.sendly.exceptions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rules API resource for managing auto-labeling rules.
 */
public class RulesResource {
    private final Sendly client;

    public RulesResource(Sendly client) {
        this.client = client;
    }

    /**
     * List all rules.
     *
     * @return List of rules
     * @throws SendlyException if the request fails
     */
    public RuleListResponse list() throws SendlyException {
        return client.request("GET", "/rules", null, RuleListResponse.class);
    }

    /**
     * Create a new rule.
     *
     * @param name       Rule name
     * @param conditions Rule conditions
     * @param actions    Rule actions
     * @return The created rule
     * @throws SendlyException if the request fails
     */
    public Rule create(String name, List<Map<String, Object>> conditions, List<Map<String, Object>> actions) throws SendlyException {
        return create(name, conditions, actions, 0);
    }

    /**
     * Create a new rule.
     *
     * @param name       Rule name
     * @param conditions Rule conditions
     * @param actions    Rule actions
     * @param priority   Rule priority (0 for default)
     * @return The created rule
     * @throws SendlyException if the request fails
     */
    public Rule create(String name, List<Map<String, Object>> conditions, List<Map<String, Object>> actions, int priority) throws SendlyException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Rule name is required");
        }
        if (conditions == null || conditions.isEmpty()) {
            throw new ValidationException("Rule conditions are required");
        }
        if (actions == null || actions.isEmpty()) {
            throw new ValidationException("Rule actions are required");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("conditions", conditions);
        body.put("actions", actions);
        if (priority > 0) {
            body.put("priority", priority);
        }

        return client.request("POST", "/rules", body, Rule.class);
    }

    /**
     * Update a rule.
     *
     * @param id   Rule ID
     * @param data Partial update data
     * @return The updated rule
     * @throws SendlyException if the request fails
     */
    public Rule update(String id, Map<String, Object> data) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Rule ID is required");
        }

        return client.request("PATCH", "/rules/" + id, data, Rule.class);
    }

    /**
     * Delete a rule.
     *
     * @param id Rule ID
     * @throws SendlyException if the request fails
     */
    public void delete(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Rule ID is required");
        }

        client.request("DELETE", "/rules/" + id, null, Void.class);
    }
}
