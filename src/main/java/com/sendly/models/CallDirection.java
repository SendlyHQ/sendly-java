package com.sendly.models;

/**
 * Which way a call was placed, as reported by {@code Call.getDirection()}.
 */
public final class CallDirection {
    /** Someone called one of your numbers. */
    public static final String INBOUND = "inbound";
    /** Your workspace placed the call. */
    public static final String OUTBOUND = "outbound";

    private CallDirection() {}
}
