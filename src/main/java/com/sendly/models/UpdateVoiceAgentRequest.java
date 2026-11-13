package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Request body for {@code voice().agents().update()}. Only the fields you set
 * are sent; {@link VoiceAgentTools} fields you leave out keep their current
 * values, and so do {@link VoiceAgentConnectionInput} fields when the kind
 * stays the same. Changes apply to the next call the agent takes.
 *
 * <pre>{@code
 * UpdateVoiceAgentRequest request = UpdateVoiceAgentRequest.builder()
 *     .greeting("Thanks for calling Acme. How can I help today?")
 *     .tools(VoiceAgentTools.builder().sendSms(false).build())
 *     .build();
 *
 * // Your own agent: remove the saved API key and make a new signing secret
 * UpdateVoiceAgentRequest rotate = UpdateVoiceAgentRequest.builder()
 *     .connection(VoiceAgentConnectionInput.builder().apiKey(null).build())
 *     .rotateSigningSecret(true)
 *     .build();
 * }</pre>
 */
public class UpdateVoiceAgentRequest {
    private final String name;
    private final Boolean enabled;
    private final String kind;
    private final VoiceAgentConnectionInput connection;
    private final String voice;
    private final String language;
    private final String greeting;
    private final String instructions;
    private final VoiceAgentTools tools;
    private final boolean rotateSigningSecret;

    private UpdateVoiceAgentRequest(Builder builder) {
        this.name = builder.name;
        this.enabled = builder.enabled;
        this.kind = builder.kind;
        this.connection = builder.connection;
        this.voice = builder.voice;
        this.language = builder.language;
        this.greeting = builder.greeting;
        this.instructions = builder.instructions;
        this.tools = builder.tools;
        this.rotateSigningSecret = builder.rotateSigningSecret;
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
    public boolean getRotateSigningSecret() { return rotateSigningSecret; }

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
        if (rotateSigningSecret) o.addProperty("rotateSigningSecret", true);
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
        private boolean rotateSigningSecret;

        /** The agent's name, 1 to 80 characters. */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Switch the agent on or off. */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Switch who answers; see {@link VoiceAgentKind}. Switching to one of
         * your own kinds needs {@link #connection} and returns a new signing
         * secret; switching to {@code "sendly"} removes the connection and
         * the signing secret.
         */
        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        /**
         * Connection fields to change. With the same kind they are merged
         * into the saved connection: fields you leave unset keep their
         * values, and a secret set to {@code null} is removed.
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

        /** Language tag, up to 16 characters; an empty string resets it to {@code "en-US"}. */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /** Up to 500 characters; an empty string clears it. */
        public Builder greeting(String greeting) {
            this.greeting = greeting;
            return this;
        }

        /** Up to 4000 characters; an empty string clears it. */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /** Tool settings to change; fields you leave unset keep their current values. */
        public Builder tools(VoiceAgentTools tools) {
            this.tools = tools;
            return this;
        }

        /**
         * {@code true} makes a new signing secret for your own agent,
         * returned once by {@code VoiceAgent.getSigningSecret()}; the old one
         * stops working at once. Refused (400 {@code invalid_request}) on a
         * {@code sendly} agent. {@code false} sends nothing.
         */
        public Builder rotateSigningSecret(boolean rotateSigningSecret) {
            this.rotateSigningSecret = rotateSigningSecret;
            return this;
        }

        public UpdateVoiceAgentRequest build() {
            return new UpdateVoiceAgentRequest(this);
        }
    }
}
