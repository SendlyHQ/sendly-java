package com.sendly.models;

import com.google.gson.annotations.SerializedName;

public class MediaFile {
    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;

    @SerializedName("contentType")
    private String contentType;

    @SerializedName("sizeBytes")
    private long sizeBytes;

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}
