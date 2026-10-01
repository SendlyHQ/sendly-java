package com.sendly.models;

import com.google.gson.annotations.SerializedName;

public class Label {
    private String id;
    private String name;
    private String color;
    private String description;
    @SerializedName(value = "created_at", alternate = {"createdAt"})
    private String createdAt;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
}
