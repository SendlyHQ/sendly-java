package com.sendly.models;

/**
 * What kind of call it is, as reported by {@code Call.getKind()}.
 */
public final class CallKind {
    /** A phone call to or from a phone number. */
    public static final String PSTN = "pstn";
    /** A browser-to-browser call between teammates. Free, and never charged. */
    public static final String INTERNAL = "internal";

    private CallKind() {}
}
