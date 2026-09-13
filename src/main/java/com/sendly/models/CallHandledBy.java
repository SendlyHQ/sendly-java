package com.sendly.models;

/**
 * Who is on your side of the call, as reported by {@code Call.getHandledBy()}.
 */
public final class CallHandledBy {
    /** An AI agent talks on the call. Every API-placed call is agent-handled. */
    public static final String AGENT = "agent";
    /** The team answers in the dashboard. */
    public static final String DASHBOARD = "dashboard";

    private CallHandledBy() {}
}
