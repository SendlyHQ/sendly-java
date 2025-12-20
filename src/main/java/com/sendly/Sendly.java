package com.sendly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sendly.exceptions.*;
import com.sendly.resources.Messages;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Sendly API Client
 * <p>
 * Official Java SDK for the Sendly SMS API.
 * </p>
 *
 * <pre>{@code
 * Sendly client = new Sendly("sk_live_v1_xxx");
 * Message message = client.messages().send("+15551234567", "Hello!");
 * }</pre>
 */
public class Sendly {
    public static final String VERSION = "1.0.5";
    public static final String DEFAULT_BASE_URL = "https://sendly.live/api/v1";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final int maxRetries;
    private final Messages messages;

    /**
     * Create a new Sendly client with default settings.
     *
     * @param apiKey Your Sendly API key
     */
    public Sendly(String apiKey) {
        this(apiKey, new Builder());
    }

    /**
     * Create a new Sendly client with custom configuration.
     *
     * @param apiKey  Your Sendly API key
     * @param builder Configuration builder
     */
    public Sendly(String apiKey, Builder builder) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new AuthenticationException("API key is required");
        }

        this.apiKey = apiKey;
        this.baseUrl = builder.baseUrl;
        this.maxRetries = builder.maxRetries;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(builder.readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(builder.writeTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();

        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        this.messages = new Messages(this);
    }

    /**
     * Get the Messages resource.
     *
     * @return Messages resource
     */
    public Messages messages() {
        return messages;
    }

    /**
     * Make a GET request.
     *
     * @param path   API endpoint path
     * @param params Query parameters
     * @return Response as JsonObject
     * @throws SendlyException if the request fails
     */
    public JsonObject get(String path, Map<String, String> params) throws SendlyException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    urlBuilder.addQueryParameter(key, value);
                }
            });
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "sendly-java/" + VERSION)
                .build();

        return executeWithRetry(request);
    }

    /**
     * Make a POST request.
     *
     * @param path API endpoint path
     * @param body Request body
     * @return Response as JsonObject
     * @throws SendlyException if the request fails
     */
    public JsonObject post(String path, Object body) throws SendlyException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "sendly-java/" + VERSION)
                .build();

        return executeWithRetry(request);
    }

    /**
     * Make a DELETE request.
     *
     * @param path API endpoint path
     * @return Response as JsonObject
     * @throws SendlyException if the request fails
     */
    public JsonObject delete(String path) throws SendlyException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "sendly-java/" + VERSION)
                .build();

        return executeWithRetry(request);
    }

    /**
     * Execute request with retries.
     */
    private JsonObject executeWithRetry(Request request) throws SendlyException {
        SendlyException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                try {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000;
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new NetworkException("Request interrupted");
                }
            }

            try {
                return execute(request);
            } catch (AuthenticationException | ValidationException |
                     NotFoundException | InsufficientCreditsException e) {
                throw e; // Don't retry these
            } catch (RateLimitException e) {
                if (e.getRetryAfter() > 0) {
                    try {
                        Thread.sleep(e.getRetryAfter() * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                lastException = e;
            } catch (SendlyException e) {
                lastException = e;
            }
        }

        throw lastException != null ? lastException : new SendlyException("Request failed after retries");
    }

    /**
     * Execute a single request.
     */
    private JsonObject execute(Request request) throws SendlyException {
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                return body.isEmpty() ? new JsonObject() : gson.fromJson(body, JsonObject.class);
            }

            JsonObject error = body.isEmpty() ? new JsonObject() : gson.fromJson(body, JsonObject.class);
            String message = error.has("message") ? error.get("message").getAsString() : "Unknown error";

            throw switch (response.code()) {
                case 401 -> new AuthenticationException(message);
                case 402 -> new InsufficientCreditsException(message);
                case 404 -> new NotFoundException(message);
                case 429 -> {
                    String retryAfter = response.header("Retry-After");
                    int seconds = retryAfter != null ? Integer.parseInt(retryAfter) : 0;
                    yield new RateLimitException(message, seconds);
                }
                case 400, 422 -> new ValidationException(message);
                default -> new SendlyException(message, response.code());
            };
        } catch (IOException e) {
            throw new NetworkException("Request failed: " + e.getMessage());
        }
    }

    /**
     * Get the Gson instance.
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Builder for Sendly client configuration.
     */
    public static class Builder {
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = DEFAULT_TIMEOUT;
        private Duration writeTimeout = DEFAULT_TIMEOUT;
        private int maxRetries = 3;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        public Builder readTimeout(Duration timeout) {
            this.readTimeout = timeout;
            return this;
        }

        public Builder writeTimeout(Duration timeout) {
            this.writeTimeout = timeout;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.readTimeout = timeout;
            this.writeTimeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
    }
}
