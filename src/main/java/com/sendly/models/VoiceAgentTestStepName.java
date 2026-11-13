package com.sendly.models;

/**
 * The step of a connection test, as reported by
 * {@code VoiceAgentTestStep.getStep()}. A step added later is returned as
 * sent.
 */
public final class VoiceAgentTestStepName {
    /** The host resolved to a public address. */
    public static final String ADDRESS = "address";
    /** A connection opened. */
    public static final String CONNECT = "connect";
    /** The SIP server accepted the credentials. */
    public static final String LOGIN = "login";
    /** The agent answered as it should. */
    public static final String REPLY = "reply";

    private VoiceAgentTestStepName() {}
}
