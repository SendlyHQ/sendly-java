package com.sendly.models;

/**
 * How a call reaches the other party, as reported by
 * {@code Call.getChannel()} and by the {@code channel} key of
 * {@code call.started}, {@code call.completed} and
 * {@code call.recording.ready} webhook objects.
 * <p>
 * Channels are plain strings so a channel introduced later still decodes;
 * compare against these constants.
 * </p>
 */
public final class CallChannel {
    /** A phone call over the phone network. */
    public static final String PHONE = "phone";
    /**
     * A WhatsApp call to or from a number with WhatsApp calling switched on.
     * Inbound WhatsApp calls read {@code phone} until the carrier labels
     * them as WhatsApp.
     */
    public static final String WHATSAPP = "whatsapp";
    /** A browser-to-browser call between teammates. */
    public static final String BROWSER = "browser";

    private CallChannel() {}
}
