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
     * <pre>{@code
     * Rule rule = client.rules().create("Billing questions",
     *     Map.of("intent", "billing"),
     *     Map.of("addLabels", List.of("lbl_123")));
     * }</pre>
     *
     * @param name       Rule name
     * @param conditions What a message must match: {@code intent} and
     *                   {@code sentiment} (a string or a list of strings),
     *                   {@code intentConfidenceMin} and
     *                   {@code sentimentConfidenceMin}. An empty map matches
     *                   every message.
     * @param actions    What the rule does: {@code addLabels} (label IDs)
     *                   and {@code closeConversation}
     * @return The created rule
     * @throws SendlyException if the request fails
     */
    public Rule create(String name, Map<String, ?> conditions, Map<String, ?> actions) throws SendlyException {
        return create(name, conditions, actions, 0);
    }

    /**
     * Create a new rule.
     *
     * @param name       Rule name
     * @param conditions What a message must match (see
     *                   {@link #create(String, Map, Map)})
     * @param actions    What the rule does (see
     *                   {@link #create(String, Map, Map)})
     * @param priority   Rule priority (0 for default); lower runs first
     * @return The created rule
     * @throws SendlyException if the request fails
     */
    public Rule create(String name, Map<String, ?> conditions, Map<String, ?> actions, int priority) throws SendlyException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Rule name is required");
        }
        if (conditions == null) {
            throw new ValidationException("Rule conditions are required");
        }
        if (actions == null) {
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
     * Create a new rule.
     *
     * @param name       Rule name
     * @param conditions Rule conditions, as a list holding one conditions object
     * @param actions    Rule actions, as a list holding one actions object
     * @return The created rule
     * @throws SendlyException if the request fails
     * @deprecated conditions and actions are objects; use
     *             {@link #create(String, Map, Map)}. The only entry of each
     *             list is sent as the object, and a list with more than one
     *             entry is refused with a {@link ValidationException}.
     */
    @Deprecated
    public Rule create(String name, List<Map<String, Object>> conditions, List<Map<String, Object>> actions) throws SendlyException {
        return create(name, conditions, actions, 0);
    }

    /**
     * Create a new rule.
     *
     * @param name       Rule name
     * @param conditions Rule conditions, as a list holding one conditions object
     * @param actions    Rule actions, as a list holding one actions object
     * @param priority   Rule priority (0 for default)
     * @return The created rule
     * @throws SendlyException if the request fails
     * @deprecated conditions and actions are objects; use
     *             {@link #create(String, Map, Map, int)}. The only entry of
     *             each list is sent as the object, and a list with more than
     *             one entry is refused with a {@link ValidationException}.
     */
    @Deprecated
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
        if (conditions.size() > 1 || actions.size() > 1) {
            throw new ValidationException(
                "A rule has one conditions object and one actions object; pass each as a Map");
        }

        return create(name, conditions.get(0), actions.get(0), priority);
    }

    /**
     * Update a rule.
     *
     * @param id   Rule ID
     * @param data Partial update data: {@code name}, {@code conditions} and
     *             {@code actions} (objects, as in
     *             {@link #create(String, Map, Map)}), {@code priority} or
     *             {@code enabled}
     * @return The updated rule
     * @throws SendlyException if the request fails
     */
    public Rule update(String id, Map<String, Object> data) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Rule ID is required");
        }

        return client.request("PATCH", "/rules/" + PathParams.encode(id), data, Rule.class);
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

        client.request("DELETE", "/rules/" + PathParams.encode(id), null, Void.class);
    }
}
