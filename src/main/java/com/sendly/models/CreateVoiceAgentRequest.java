package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Request body for {@code voice().agents().create()}: an AI agent that
 * answers real callers on any number pointed at it and talks on the calls you
 * place with it. Only {@code name} is required.
 *
 * <pre>{@code
 * CreateVoiceAgentRequest request = CreateVoiceAgentRequest.builder()
 *     .name("Front desk")
 *     .voice("ashley")
 *     .greeting("Thanks for calling Acme, how can I help?")
 *     .instructions("Answer questions about opening hours and take a message for anything else.")
 *     .tools(VoiceAgentTools.builder().sendSms(true).build())
 *     .build();
 *
 * // Your own agent, reached at your chat completions endpoint
 * CreateVoiceAgentRequest own = CreateVoiceAgentRequest.builder()
 *     .name("Own model")
 *     .kind(VoiceAgentKind.CUSTOM_LLM)
 *     .connection(VoiceAgentConnectionInput.builder()
 *         .baseUrl("https://llm.example.com/v1")
 *         .model("acme-receptionist-1")
 *         .apiKey("sk-example")
 *         .build())
 *     .build();
 * }</pre>
 */
public class CreateVoiceAgentRequest {
    private final String name;
    private final Boolean enabled;
    private final String kind;
    private final VoiceAgentConnectionInput connection;
    private final String voice;
    private final String language;
    private final String greeting;
    private final String instructions;
    private final VoiceAgentTools tools;

    private CreateVoiceAgentRequest(Builder builder) {
        this.name = builder.name;
        this.enabled = builder.enabled;
        this.kind = builder.kind;
        this.connection = builder.connection;
        this.voice = builder.voice;
        this.language = builder.language;
        this.greeting = builder.greeting;
        this.instructions = builder.instructions;
        this.tools = builder.tools;
    }

    public String getName() { return name; }
    public Boolean getEnabled() { return enabled; }
    public String getKind() { return kind; }
    public VoiceAgentConnectionInput getConnection() { return connection; }
    public String getVoice() { return voice; }
    public String getLanguage() { return language; }
    public String getGreeting() { return greeting; }
    public String getInstructions() { return instructions; }
    public VoiceAgentTools getTools() { return tools; }

    /** Serialize to the JSON body the API expects (camelCase keys). Unset fields are omitted. */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (name != null) o.addProperty("name", name);
        if (enabled != null) o.addProperty("enabled", enabled);
        if (kind != null) o.addProperty("kind", kind);
        if (connection != null) o.add("connection", connection.toJson());
        if (voice != null) o.addProperty("voice", voice);
        if (language != null) o.addProperty("language", language);
        if (greeting != null) o.addProperty("greeting", greeting);
        if (instructions != null) o.addProperty("instructions", instructions);
        if (tools != null) o.add("tools", tools.toJson());
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Boolean enabled;
        private String kind;
        private VoiceAgentConnectionInput connection;
        private String voice;
        private String language;
        private String greeting;
        private String instructions;
        private VoiceAgentTools tools;

        /** The agent's name, 1 to 80 characters. Required. */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Whether the agent is switched on. Defaults to true. */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Who answers: {@code "sendly"} (the default), or your own agent,
         * {@code "custom_llm"}, {@code "sip"} or {@code "websocket"}; see
         * {@link VoiceAgentKind}. Your own agents need {@link #connection}
         * and the workspace's bring-your-own-agent feature (403
         * {@code byo_agents_not_enabled} otherwise; support switches it on).
         */
        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        /**
         * How Sendly reaches your own agent, matching {@link #kind}. Required
         * for your own agents.
         */
        public Builder connection(VoiceAgentConnectionInput connection) {
            this.connection = connection;
            return this;
        }

        /** A voice id from {@code voice().voices().list()}. An unknown id falls back to the default voice. */
        public Builder voice(String voice) {
            this.voice = voice;
            return this;
        }

        /** Language tag, up to 16 characters. Defaults to {@code "en-US"}. */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /** What the agent says when it picks up, up to 500 characters. */
        public Builder greeting(String greeting) {
            this.greeting = greeting;
            return this;
        }

        /** Business instructions the agent follows, up to 4000 characters. */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /** What the agent may do on a call. {@code sendSms} defaults to true, {@code transferTo} to none. */
        public Builder tools(VoiceAgentTools tools) {
            this.tools = tools;
            return this;
        }

        public CreateVoiceAgentRequest build() {
            return new CreateVoiceAgentRequest(this);
        }
    }
}
