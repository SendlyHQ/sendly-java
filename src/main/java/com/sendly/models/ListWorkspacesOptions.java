package com.sendly.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filters for {@code enterprise().workspaces().list(...)}. Only the options
 * you set are sent.
 */
public class ListWorkspacesOptions {
    private final Integer page;
    private final Integer limit;
    private final String search;
    private final String status;
    private final String verification;
    private final String sort;
    private final List<String> tags;
    private final boolean compact;

    private ListWorkspacesOptions(Builder builder) {
        this.page = builder.page;
        this.limit = builder.limit;
        this.search = builder.search;
        this.status = builder.status;
        this.verification = builder.verification;
        this.sort = builder.sort;
        this.tags = builder.tags;
        this.compact = builder.compact;
    }

    public Map<String, String> toParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", String.valueOf(page));
        if (limit != null && limit > 0) params.put("limit", String.valueOf(Math.min(limit, 100)));
        if (search != null && !search.isEmpty()) params.put("search", search);
        if (status != null && !status.isEmpty()) params.put("status", status);
        if (verification != null && !verification.isEmpty()) params.put("verification", verification);
        if (sort != null && !sort.isEmpty()) params.put("sort", sort);
        if (tags != null && !tags.isEmpty()) params.put("tags", String.join(",", tags));
        if (compact) params.put("compact", "true");
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer page;
        private Integer limit;
        private String search;
        private String status;
        private String verification;
        private String sort;
        private List<String> tags;
        private boolean compact;

        /** Page number, starting at 1. */
        public Builder page(int page) {
            this.page = page;
            return this;
        }

        /** Workspaces per page (default 50, max 100). */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /** Only workspaces whose name contains this text. */
        public Builder search(String search) {
            this.search = search;
            return this;
        }

        /** {@code all} (the default), {@code active} or {@code suspended}. */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Only workspaces in this verification state: {@code unverified}
         * (those without one), or a status such as {@code verified} or
         * {@code rejected}. {@code all} is the default.
         */
        public Builder verification(String verification) {
            this.verification = verification;
            return this;
        }

        /**
         * {@code created_desc} (the default), {@code created_asc},
         * {@code name_asc}, {@code name_desc}, {@code credits_desc} or
         * {@code credits_asc}.
         */
        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        /** Only workspaces with any of these tag ids. */
        public Builder tags(List<String> tags) {
            this.tags = tags == null ? null : new ArrayList<>(tags);
            return this;
        }

        /**
         * Every workspace with only its id, name and balance, unpaginated;
         * the other options do not apply.
         */
        public Builder compact(boolean compact) {
            this.compact = compact;
            return this;
        }

        public ListWorkspacesOptions build() {
            return new ListWorkspacesOptions(this);
        }
    }
}
