package com.sendly.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Request options for listing messages.
 */
public class ListMessagesRequest {
    private final Integer limit;
    private final Integer offset;
    private final String status;
    private final String to;

    private ListMessagesRequest(Builder builder) {
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.status = builder.status;
        this.to = builder.to;
    }

    /**
     * Convert to query parameters map.
     */
    public Map<String, String> toParams() {
        Map<String, String> params = new HashMap<>();
        if (limit != null) {
            params.put("limit", String.valueOf(Math.min(limit, 100)));
        }
        if (offset != null) {
            params.put("offset", String.valueOf(offset));
        }
        if (status != null) {
            params.put("status", status);
        }
        if (to != null) {
            params.put("to", to);
        }
        return params;
    }

    /**
     * Create a builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ListMessagesRequest.
     */
    public static class Builder {
        private Integer limit;
        private Integer offset;
        private String status;
        private String to;

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

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public ListMessagesRequest build() {
            return new ListMessagesRequest(this);
        }
    }
}
