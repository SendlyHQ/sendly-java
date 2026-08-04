package com.sendly.models;

/**
 * A suggestion chip on an RCS message — exactly one of {@code reply} or
 * {@code action}.
 * <ul>
 *   <li><b>Reply</b> — a tap-to-reply chip; the tap comes back as an inbound
 *       message carrying your {@code postbackData}</li>
 *   <li><b>Action</b> — an open-URL chip; tapping opens {@code url} and the
 *       tap is reported with your {@code postbackData}</li>
 * </ul>
 * <p>
 * Suggestions ride on text messages ({@code SendRcsMessageRequest.suggestions})
 * or on a rich card ({@code RcsCard.suggestions}). They have no SMS form: when
 * a text send falls back to SMS, suggestions are dropped (disclosed via
 * {@code suggestionsDropped} on the response).
 * </p>
 */
public class RcsSuggestion {
    private final Reply reply;
    private final Action action;

    private RcsSuggestion(Reply reply, Action action) {
        this.reply = reply;
        this.action = action;
    }

    /**
     * Create a tap-to-reply chip.
     *
     * @param text         The chip label the recipient sees
     * @param postbackData Opaque payload returned when the chip is tapped
     * @return The suggestion
     */
    public static RcsSuggestion reply(String text, String postbackData) {
        return new RcsSuggestion(new Reply(text, postbackData), null);
    }

    /**
     * Create an open-URL chip.
     *
     * @param text         The chip label the recipient sees
     * @param postbackData Opaque payload reported when the chip is tapped
     * @param url          The URL to open
     * @return The suggestion
     */
    public static RcsSuggestion action(String text, String postbackData, String url) {
        return new RcsSuggestion(null, new Action(text, postbackData, url));
    }

    /** The reply chip, or null when this is an action. */
    public Reply getReply() { return reply; }

    /** The action chip, or null when this is a reply. */
    public Action getAction() { return action; }

    /**
     * A tap-to-reply chip.
     */
    public static class Reply {
        private final String text;
        private final String postbackData;

        public Reply(String text, String postbackData) {
            this.text = text;
            this.postbackData = postbackData;
        }

        /** The chip label. */
        public String getText() { return text; }

        /** Opaque payload returned when the chip is tapped. */
        public String getPostbackData() { return postbackData; }
    }

    /**
     * An open-URL chip.
     */
    public static class Action {
        private final String text;
        private final String postbackData;
        private final String url;

        public Action(String text, String postbackData, String url) {
            this.text = text;
            this.postbackData = postbackData;
            this.url = url;
        }

        /** The chip label. */
        public String getText() { return text; }

        /** Opaque payload reported when the chip is tapped. */
        public String getPostbackData() { return postbackData; }

        /** The URL the chip opens. */
        public String getUrl() { return url; }
    }
}
