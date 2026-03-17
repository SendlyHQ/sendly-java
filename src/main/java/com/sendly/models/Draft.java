package com.sendly.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Draft {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_FAILED = "failed";

    private String id;
    @SerializedName("conversationId")
    private String conversationId;
    private String text;
    @SerializedName("mediaUrls")
    private List<String> mediaUrls;
    private Map<String, Object> metadata;
    private String status;
    private String source;
    @SerializedName("createdBy")
    private String createdBy;
    @SerializedName("reviewedBy")
    private String reviewedBy;
    @SerializedName("reviewedAt")
    private String reviewedAt;
    @SerializedName("rejectionReason")
    private String rejectionReason;
    @SerializedName("messageId")
    private String messageId;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;

    public String getId() { return id; }
    public String getConversationId() { return conversationId; }
    public String getText() { return text; }
    public List<String> getMediaUrls() { return mediaUrls; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getStatus() { return status; }
    public String getSource() { return source; }
    public String getCreatedBy() { return createdBy; }
    public String getReviewedBy() { return reviewedBy; }
    public String getReviewedAt() { return reviewedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public String getMessageId() { return messageId; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public boolean isPending() { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }
}
