package com.sendly.models;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * How Sendly reaches your own agent, set with
 * {@code CreateVoiceAgentRequest.Builder.connection()} or
 * {@code UpdateVoiceAgentRequest.Builder.connection()}. Set the fields that
 * match the agent's kind:
 * <ul>
 *   <li>{@code custom_llm}: {@link Builder#baseUrl(String)},
 *       {@link Builder#model(String)}, {@link Builder#apiKey(String)},
 *       {@link Builder#headers(Map)}</li>
 *   <li>{@code sip}: {@link Builder#uri(String)},
 *       {@link Builder#transport(String)},
 *       {@link Builder#mediaEncryption(String)},
 *       {@link Builder#username(String)}, {@link Builder#password(String)},
 *       {@link Builder#headers(Map)}</li>
 *   <li>{@code websocket}: {@link Builder#url(String)},
 *       {@link Builder#audioFormat(String)},
 *       {@link Builder#authToken(String)},
 *       {@link Builder#customParameters(Map)}</li>
 * </ul>
 * Only the fields you set are sent. On an update that keeps the kind, the
 * connection is merged into the saved one: fields you leave out keep their
 * values, and a secret, username, header set or custom parameter set passed
 * as {@code null} is sent as {@code null}, which removes it. Hosts must
 * resolve to public internet addresses.
 *
 * <pre>{@code
 * VoiceAgentConnectionInput connection = VoiceAgentConnectionInput.builder()
 *     .baseUrl("https://llm.example.com/v1")
 *     .model("acme-receptionist-1")
 *     .apiKey("sk-example")
 *     .build();
 * }</pre>
 */
public class VoiceAgentConnectionInput {
    private final JsonObject json;

    private VoiceAgentConnectionInput(Builder builder) {
        this.json = builder.json.deepCopy();
    }

    /**
     * Serialize to the JSON the API expects. Unset fields are omitted, and
     * fields set to {@code null} to remove them are JSON {@code null}.
     */
    public JsonObject toJson() {
        return json.deepCopy();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final JsonObject json = new JsonObject();

        private Builder string(String key, String value) {
            if (value == null) {
                json.remove(key);
            } else {
                json.addProperty(key, value);
            }
            return this;
        }

        private Builder nullable(String key, String value) {
            if (value == null) {
                json.add(key, JsonNull.INSTANCE);
            } else {
                json.addProperty(key, value);
            }
            return this;
        }

        private Builder map(String key, Map<String, String> values) {
            if (values == null) {
                json.add(key, JsonNull.INSTANCE);
                return this;
            }
            JsonObject o = new JsonObject();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                o.addProperty(entry.getKey(), entry.getValue());
            }
            json.add(key, o);
            return this;
        }

        /**
         * {@code custom_llm}: an {@code https://} URL, up to 2048 characters,
         * with no credentials or {@code #}. Sendly sends
         * {@code POST {baseUrl}/chat/completions} with streaming on; a
         * trailing {@code /} is dropped.
         */
        public Builder baseUrl(String baseUrl) {
            return string("baseUrl", baseUrl);
        }

        /** {@code custom_llm}: the model name, 1 to 200 letters, digits and {@code _ . : / @ + -}. */
        public Builder model(String model) {
            return string("model", model);
        }

        /**
         * {@code custom_llm}: sent as {@code Authorization: Bearer}, 1 to
         * 2048 printable ASCII characters. Left unset, the saved key is kept;
         * {@code null} removes it.
         */
        public Builder apiKey(String apiKey) {
            return nullable("apiKey", apiKey);
        }

        /**
         * {@code custom_llm}: up to 10 extra headers with secret values
         * ({@code Authorization}, {@code Content-Type}, {@code Host},
         * {@code User-Agent} and {@code X-Sendly-*} can't be set).
         * {@code sip}: up to 10 headers named {@code X-...}, not
         * {@code X-Sendly-...}. Replaces the saved set; {@code null} removes
         * them.
         */
        public Builder headers(Map<String, String> headers) {
            return map("headers", headers);
        }

        /**
         * {@code sip}: {@code sip:} or {@code sips:} with a user part and a
         * hostname (not an IP address), such as
         * {@code sip:+15125550100@sip.example.com}. Only {@code ;transport=}
         * may follow.
         */
        public Builder uri(String uri) {
            return string("uri", uri);
        }

        /** {@code sip}: {@code "auto"}, {@code "udp"}, {@code "tcp"} (the default) or {@code "tls"}. A {@code sips:} address always uses {@code tls}. */
        public Builder transport(String transport) {
            return string("transport", transport);
        }

        /** {@code sip}: {@code "disable"}, {@code "allow"} (the default) or {@code "require"}, which needs the {@code tls} transport. */
        public Builder mediaEncryption(String mediaEncryption) {
            return string("mediaEncryption", mediaEncryption);
        }

        /** {@code sip}: the digest auth username. {@code null} removes it. */
        public Builder username(String username) {
            return nullable("username", username);
        }

        /**
         * {@code sip}: the digest auth password. Left unset, the saved one is
         * kept; {@code null} removes it.
         */
        public Builder password(String password) {
            return nullable("password", password);
        }

        /** {@code websocket}: a {@code wss://} URL, up to 2048 characters, with no credentials or {@code #}. */
        public Builder url(String url) {
            return string("url", url);
        }

        /**
         * {@code websocket}: {@code "pcm16_16000"} (the default),
         * {@code "pcm16_24000"}, {@code "pcm16_8000"} or {@code "mulaw_8000"},
         * both ways.
         */
        public Builder audioFormat(String audioFormat) {
            return string("audioFormat", audioFormat);
        }

        /**
         * {@code websocket}: sent as {@code Authorization: Bearer}. Left
         * unset, the saved one is kept; {@code null} removes it.
         */
        public Builder authToken(String authToken) {
            return nullable("authToken", authToken);
        }

        /**
         * {@code websocket}: up to 20 values (keys 1 to 40 letters, digits and
         * {@code _ . : -}, values up to 500 characters) sent to your server in
         * the {@code start} message. Replaces the saved set; {@code null}
         * removes them.
         */
        public Builder customParameters(Map<String, String> customParameters) {
            return map("customParameters", customParameters);
        }

        public VoiceAgentConnectionInput build() {
            return new VoiceAgentConnectionInput(this);
        }
    }

    @Override
    public String toString() {
        return "VoiceAgentConnectionInput{keys=" + json.keySet() + '}';
    }
}
