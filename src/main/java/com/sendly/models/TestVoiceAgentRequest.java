package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Settings to test with {@code voice().agents().test()}. Leave it out to test
 * the saved connection. Only the fields you set are sent, and nothing is
 * saved.
 *
 * <pre>{@code
 * // Try a new address before saving it
 * TestVoiceAgentRequest request = TestVoiceAgentRequest.builder()
 *     .connection(VoiceAgentConnectionInput.builder()
 *         .uri("sip:+15125550100@sip.example.com")
 *         .transport("tls")
 *         .build())
 *     .build();
 * }</pre>
 */
public class TestVoiceAgentRequest {
    private final String kind;
    private final VoiceAgentConnectionInput connection;
    private final String instructions;

    private TestVoiceAgentRequest(Builder builder) {
        this.kind = builder.kind;
        this.connection = builder.connection;
        this.instructions = builder.instructions;
    }

    public String getKind() { return kind; }
    public VoiceAgentConnectionInput getConnection() { return connection; }
    public String getInstructions() { return instructions; }

    /** Serialize to the JSON body the API expects (camelCase keys). Unset fields are omitted. */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (kind != null) o.addProperty("kind", kind);
        if (connection != null) o.add("connection", connection.toJson());
        if (instructions != null) o.addProperty("instructions", instructions);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kind;
        private VoiceAgentConnectionInput connection;
        private String instructions;

        /**
         * The kind of the settings in {@link #connection}: {@code custom_llm},
         * {@code sip} or {@code websocket}; see {@link VoiceAgentKind}.
         * Defaults to the agent's own kind. Needed to test settings of
         * another kind, such as on a {@code sendly} agent before switching it.
         */
        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        /**
         * Settings to test instead of the saved ones. Nothing is saved. For
         * the agent's own kind, a secret left out is taken from the saved
         * connection, while the host stays the same.
         */
        public Builder connection(VoiceAgentConnectionInput connection) {
            this.connection = connection;
            return this;
        }

        /** For {@code custom_llm}: the system prompt to send, up to 4000 characters. Defaults to the agent's instructions. */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public TestVoiceAgentRequest build() {
            return new TestVoiceAgentRequest(this);
        }
    }
}
