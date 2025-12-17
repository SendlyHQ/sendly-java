package com.sendly.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Request object for listing scheduled messages.
 */
public class ListScheduledMessagesRequest {
    private final Integer limit;
    private final Integer offset;
    private final String status;

    private ListScheduledMessagesRequest(Builder builder) {
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.status = builder.status;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Convert to query parameters map.
     */
    public Map<String, String> toParams() {
        Map<String, String> params = new HashMap<>();
        if (limit != null) {
            params.put("limit", limit.toString());
        }
        if (offset != null) {
            params.put("offset", offset.toString());
        }
        if (status != null) {
            params.put("status", status);
        }
        return params;
    }

    /**
     * Create a builder for ListScheduledMessagesRequest.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ListScheduledMessagesRequest.
     */
    public static class Builder {
        private Integer limit;
        private Integer offset;
        private String status;

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public ListScheduledMessagesRequest build() {
            return new ListScheduledMessagesRequest(this);
        }
    }
}
