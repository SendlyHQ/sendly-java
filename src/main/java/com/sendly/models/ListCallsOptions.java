package com.sendly.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Filters and paging for {@code calls().list()}. Every field is optional; the
 * server orders by {@code startedAt} descending and defaults to 50 per page.
 *
 * <pre>{@code
 * ListCallsOptions options = ListCallsOptions.builder()
 *     .status(CallStatus.COMPLETED)
 *     .direction(CallDirection.OUTBOUND)
 *     .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
 *     .limit(20)
 *     .build();
 * }</pre>
 */
public class ListCallsOptions {
    private final Integer limit;
    private final Integer offset;
    private final String status;
    private final String direction;
    private final String kind;
    private final String agentId;
    private final String to;
    private final String from;

    private ListCallsOptions(Builder builder) {
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.status = builder.status;
        this.direction = builder.direction;
        this.kind = builder.kind;
        this.agentId = builder.agentId;
        this.to = builder.to;
        this.from = builder.from;
    }

    /**
     * Convert to query parameters map.
     */
    public Map<String, String> toParams() {
        Map<String, String> params = new HashMap<>();
        if (limit != null) params.put("limit", String.valueOf(limit));
        if (offset != null) params.put("offset", String.valueOf(offset));
        if (status != null) params.put("status", status);
        if (direction != null) params.put("direction", direction);
        if (kind != null) params.put("kind", kind);
        if (agentId != null) params.put("agentId", agentId);
        if (to != null) params.put("to", to);
        if (from != null) params.put("from", from);
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer limit;
        private Integer offset;
        private String status;
        private String direction;
        private String kind;
        private String agentId;
        private String to;
        private String from;

        /** Page size, 1 to 100 (default 50). */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /** Calls to skip (default 0). */
        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        /** One status value; see {@link CallStatus}. */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /** {@code "inbound"} or {@code "outbound"}; see {@link CallDirection}. */
        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        /** {@code "pstn"} or {@code "internal"}; see {@link CallKind}. */
        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        /** Only calls handled by this agent. */
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        /** Exact E.164 match on the called number. */
        public Builder to(String to) {
            this.to = to;
            return this;
        }

        /** Exact E.164 match on the calling number. */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public ListCallsOptions build() {
            return new ListCallsOptions(this);
        }
    }
}
