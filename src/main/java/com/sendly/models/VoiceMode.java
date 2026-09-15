package com.sendly.models;

/**
 * How a number answers phone calls, as reported by
 * {@code VoiceNumber.getVoiceMode()} and set with
 * {@code UpdateVoiceNumberRequest.Builder.voiceMode(String)}.
 * <p>
 * Modes are plain strings so a mode introduced later still decodes; compare
 * against these constants.
 * </p>
 */
public final class VoiceMode {
    /**
     * Calls are not answered. Always reported when {@code isVoiceEnabled()}
     * is false.
     * Sent without {@code voiceEnabled} it switches voice off; with
     * {@code voiceEnabled(true)} it becomes {@code ring_dashboard}.
     */
    public static final String NONE = "none";
    /** Calls ring the team in the dashboard. */
    public static final String RING_DASHBOARD = "ring_dashboard";
    /** An AI agent answers. */
    public static final String AGENT = "agent";

    private VoiceMode() {}
}
