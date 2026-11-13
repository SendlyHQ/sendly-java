package com.sendly.models;

/**
 * Who answers a call for an agent, as reported by
 * {@code VoiceAgent.getKind()} and {@code Call.getAgentKind()}, and set with
 * {@code CreateVoiceAgentRequest.Builder.kind(String)} or
 * {@code UpdateVoiceAgentRequest.Builder.kind(String)}.
 * <p>
 * Kinds are plain strings so a kind introduced later still decodes; compare
 * against these constants and treat one you don't know as your own agent.
 * </p>
 */
public final class VoiceAgentKind {
    /** Sendly writes the replies from the agent's instructions. The default. */
    public static final String SENDLY = "sendly";
    /** Sendly listens and speaks, and your chat completions endpoint writes each reply. */
    public static final String CUSTOM_LLM = "custom_llm";
    /** Sendly connects the caller to your agent's SIP address. Answers incoming calls only. */
    public static final String SIP = "sip";
    /** Sendly streams the call audio to your {@code wss://} server and plays back what it sends. Answers incoming calls only. */
    public static final String WEBSOCKET = "websocket";

    private VoiceAgentKind() {}
}
