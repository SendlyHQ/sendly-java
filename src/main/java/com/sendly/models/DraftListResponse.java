package com.sendly.models;

import java.util.List;

public class DraftListResponse {
    private List<Draft> data;
    private Pagination pagination;

    public List<Draft> getData() { return data; }
    public Pagination getPagination() { return pagination; }

    public static class Pagination {
        private int total;
        private int limit;
        private int offset;
        private boolean hasMore;

        public int getTotal() { return total; }
        public int getLimit() { return limit; }
        public int getOffset() { return offset; }
        public boolean isHasMore() { return hasMore; }
    }
}
