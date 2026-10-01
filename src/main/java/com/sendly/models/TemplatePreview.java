package com.sendly.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TemplatePreview {
    @SerializedName(value = "template_id", alternate = {"id"})
    private String id;
    private String name;
    @SerializedName("original_text")
    private String originalText;
    @SerializedName(value = "rendered_text", alternate = {"preview_text"})
    private String previewText;
    private List<Template.TemplateVariable> variables;
    @SerializedName("character_count")
    private int characterCount;
    @SerializedName("segment_count")
    private int segmentCount;

    /** The ID of the template that was rendered. */
    public String getId() { return id; }
    /** Not part of the preview response; null. Use {@code templates().get(id)} for the name. */
    public String getName() { return name; }
    public String getOriginalText() { return originalText; }
    /** The template text with the variables filled in. */
    public String getPreviewText() { return previewText; }
    /** Not part of the preview response; null. Use {@code templates().get(id)} for the variables. */
    public List<Template.TemplateVariable> getVariables() { return variables; }
    /** Length of the rendered text in characters. */
    public int getCharacterCount() { return characterCount; }
    /** Number of SMS segments the rendered text takes. */
    public int getSegmentCount() { return segmentCount; }
}
