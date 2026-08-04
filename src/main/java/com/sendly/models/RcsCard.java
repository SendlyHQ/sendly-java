package com.sendly.models;

import java.util.List;

/**
 * A standalone RCS rich card — title, description, optional image, and
 * optional suggestion chips.
 * <p>
 * Rich cards have no SMS form: sending a card to a recipient whose device or
 * network doesn't support RCS fails with 422
 * {@code rcs_not_supported_for_recipient} rather than falling back.
 * </p>
 */
public class RcsCard {
    private final String title;
    private final String description;
    private final String mediaUrl;
    private final String orientation;
    private final List<RcsSuggestion> suggestions;

    /**
     * Create a card with just a title and description.
     *
     * @param title       Card title (required)
     * @param description Card description (required)
     */
    public RcsCard(String title, String description) {
        this.title = title;
        this.description = description;
        this.mediaUrl = null;
        this.orientation = null;
        this.suggestions = null;
    }

    private RcsCard(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.mediaUrl = builder.mediaUrl;
        this.orientation = builder.orientation;
        this.suggestions = builder.suggestions;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getMediaUrl() { return mediaUrl; }
    public String getOrientation() { return orientation; }
    public List<RcsSuggestion> getSuggestions() { return suggestions; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String description;
        private String mediaUrl;
        private String orientation;
        private List<RcsSuggestion> suggestions;

        /** Card title. Required. */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /** Card description. Required. */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Public JPEG, PNG, or GIF image URL shown on the card. Optional.
         */
        public Builder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }

        /**
         * Card layout: {@code vertical} (default) or {@code horizontal}.
         */
        public Builder orientation(String orientation) {
            this.orientation = orientation;
            return this;
        }

        /** Suggestion chips shown under the card. */
        public Builder suggestions(List<RcsSuggestion> suggestions) {
            this.suggestions = suggestions;
            return this;
        }

        public RcsCard build() {
            return new RcsCard(this);
        }
    }
}
