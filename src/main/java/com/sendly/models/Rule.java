package com.sendly.models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Rule {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private String id;
    private String name;
    private JsonElement conditions;
    private JsonElement actions;
    private int priority;
    private Boolean enabled;
    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;
    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    public String getId() { return id; }
    public String getName() { return name; }

    /**
     * What a message must match for the rule to apply: {@code intent} and
     * {@code sentiment} (a string or a list of strings),
     * {@code intentConfidenceMin} and {@code sentimentConfidenceMin}. A key
     * that is absent matches every message. Empty when the rule has no
     * conditions, or when they were stored as a list, which matches every
     * message.
     */
    public Map<String, Object> getConditionsMap() { return mapOf(conditions); }

    /**
     * What the rule does: {@code addLabels} (label IDs) and
     * {@code closeConversation}. Empty when the rule has no actions, or when
     * they were stored as a list, which does nothing.
     */
    public Map<String, Object> getActionsMap() { return mapOf(actions); }

    /**
     * The conditions as a list: the conditions object as its only entry, or
     * each entry of a rule whose conditions were stored as a list.
     *
     * @deprecated conditions are an object; use {@link #getConditionsMap()}.
     */
    @Deprecated
    public List<Map<String, Object>> getConditions() { return listOf(conditions); }

    /**
     * The actions as a list: the actions object as its only entry, or each
     * entry of a rule whose actions were stored as a list.
     *
     * @deprecated actions are an object; use {@link #getActionsMap()}.
     */
    @Deprecated
    public List<Map<String, Object>> getActions() { return listOf(actions); }

    public int getPriority() { return priority; }

    /**
     * Whether the rule runs. Only enabled rules are applied to incoming
     * messages.
     */
    public boolean isEnabled() { return Boolean.TRUE.equals(enabled); }

    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    private static Map<String, Object> mapOf(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return new LinkedHashMap<>();
        }
        return GSON.fromJson(element, MAP_TYPE);
    }

    private static List<Map<String, Object>> listOf(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        if (element.isJsonObject()) {
            out.add(mapOf(element));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> {
                if (e.isJsonObject()) out.add(mapOf(e));
            });
        }
        return out;
    }
}
