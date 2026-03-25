package com.sendly.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeneratedTemplate {
    private String name;
    private String text;
    private List<String> variables;
    private String category;

    public String getName() { return name; }
    public String getText() { return text; }
    public List<String> getVariables() { return variables; }
    public String getCategory() { return category; }
}
