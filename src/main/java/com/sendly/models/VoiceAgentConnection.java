package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How Sendly reaches your own agent, from {@code VoiceAgent.getConnection()}
 * ({@code null} for a {@code sendly} agent). The fields that apply depend on
 * the agent's kind; the others return {@code null} (or {@code false}):
 * <ul>
 *   <li>{@code custom_llm}: {@link #getBaseUrl()}, {@link #getModel()},
 *       {@link #getHeaderNames()}, {@link #hasApiKey()}</li>
 *   <li>{@code sip}: {@link #getUri()}, {@link #getTransport()},
 *       {@link #getMediaEncryption()}, {@link #getUsername()},
 *       {@link #hasPassword()}, {@link #getHeaders()}</li>
 *   <li>{@code websocket}: {@link #getUrl()}, {@link #getAudioFormat()},
 *       {@link #hasAuthToken()}, {@link #getCustomParameters()}</li>
 * </ul>
 * Secrets (the API key, header values for {@code custom_llm}, the SIP
 * password and the auth token) are never returned; the {@code has} flags say
 * whether one is saved.
 */
public class VoiceAgentConnection {
    private final String baseUrl;
    private final String model;
    private final List<String> headerNames;
    private final boolean hasApiKey;
    private final String uri;
    private final String transport;
    private final String mediaEncryption;
    private final String username;
    private final boolean hasPassword;
    private final Map<String, String> headers;
    private final String url;
    private final String audioFormat;
    private final boolean hasAuthToken;
    private final Map<String, String> customParameters;

    /**
     * Create a VoiceAgentConnection from a JSON object.
     */
    public VoiceAgentConnection(JsonObject json) {
        this.baseUrl = getStringOrNull(json, "baseUrl");
        this.model = getStringOrNull(json, "model");
        if (json.has("headerNames") && json.get("headerNames").isJsonArray()) {
            List<String> names = new ArrayList<>();
            JsonArray array = json.getAsJsonArray("headerNames");
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonPrimitive()) {
                    names.add(array.get(i).getAsString());
                }
            }
            this.headerNames = Collections.unmodifiableList(names);
        } else {
            this.headerNames = null;
        }
        this.hasApiKey = getBoolean(json, "hasApiKey");
        this.uri = getStringOrNull(json, "uri");
        this.transport = getStringOrNull(json, "transport");
        this.mediaEncryption = getStringOrNull(json, "mediaEncryption");
        this.username = getStringOrNull(json, "username");
        this.hasPassword = getBoolean(json, "hasPassword");
        this.headers = getStringMapOrNull(json, "headers");
        this.url = getStringOrNull(json, "url");
        this.audioFormat = getStringOrNull(json, "audioFormat");
        this.hasAuthToken = getBoolean(json, "hasAuthToken");
        this.customParameters = getStringMapOrNull(json, "customParameters");
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static boolean getBoolean(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).getAsBoolean();
    }

    private static Map<String, String> getStringMapOrNull(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonObject()) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(key).entrySet()) {
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonPrimitive()) {
                map.put(entry.getKey(), value.getAsString());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /** {@code custom_llm}: your endpoint; Sendly sends {@code POST {baseUrl}/chat/completions} with streaming on. */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** {@code custom_llm}: the model name sent in each request. */
    public String getModel() {
        return model;
    }

    /** {@code custom_llm}: the names of the extra headers sent with each request. Their values are never returned. */
    public List<String> getHeaderNames() {
        return headerNames;
    }

    /** {@code custom_llm}: true when an API key is saved. */
    public boolean hasApiKey() {
        return hasApiKey;
    }

    /** {@code sip}: your agent's SIP address, such as {@code sip:+15125550100@sip.example.com}. */
    public String getUri() {
        return uri;
    }

    /** {@code sip}: {@code "auto"}, {@code "udp"}, {@code "tcp"} or {@code "tls"}. A {@code sips:} address always uses {@code tls}. */
    public String getTransport() {
        return transport;
    }

    /** {@code sip}: {@code "disable"}, {@code "allow"} or {@code "require"}. */
    public String getMediaEncryption() {
        return mediaEncryption;
    }

    /** {@code sip}: the digest auth username, or {@code null}. */
    public String getUsername() {
        return username;
    }

    /** {@code sip}: true when a digest auth password is saved. */
    public boolean hasPassword() {
        return hasPassword;
    }

    /** {@code sip}: the extra {@code X-} headers sent on the call. */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /** {@code websocket}: your {@code wss://} server. */
    public String getUrl() {
        return url;
    }

    /** {@code websocket}: {@code "pcm16_16000"}, {@code "pcm16_24000"}, {@code "pcm16_8000"} or {@code "mulaw_8000"}, both ways. */
    public String getAudioFormat() {
        return audioFormat;
    }

    /** {@code websocket}: true when an auth token is saved. It is sent as {@code Authorization: Bearer}. */
    public boolean hasAuthToken() {
        return hasAuthToken;
    }

    /** {@code websocket}: the values sent to your server in the {@code start} message. */
    public Map<String, String> getCustomParameters() {
        return customParameters;
    }

    @Override
    public String toString() {
        return "VoiceAgentConnection{" +
                "baseUrl='" + baseUrl + '\'' +
                ", model='" + model + '\'' +
                ", uri='" + uri + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
