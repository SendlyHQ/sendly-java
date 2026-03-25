package com.sendly.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Rule {
    private String id;
    private String name;
    private List<Map<String, Object>> conditions;
    private List<Map<String, Object>> actions;
    private int priority;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Map<String, Object>> getConditions() { return conditions; }
    public List<Map<String, Object>> getActions() { return actions; }
    public int getPriority() { return priority; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
