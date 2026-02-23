package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.Account;
import com.sendly.models.ApiKey;
import com.sendly.models.CreditTransaction;
import com.sendly.models.Credits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Account resource for accessing account information, credits, and API keys.
 */
public class AccountResource {
    private final Sendly client;

    public AccountResource(Sendly client) {
        this.client = client;
    }

    /**
     * Get account information.
     *
     * @return The account information
     * @throws SendlyException if the request fails
     */
    public Account get() throws SendlyException {
        JsonObject response = client.get("/account", null);
        JsonObject data = response.has("account") ?
            response.getAsJsonObject("account") :
            response.has("data") ? response.getAsJsonObject("data") : response;
        return new Account(data);
    }

    /**
     * Get credit balance.
     *
     * @return The credit balance
     * @throws SendlyException if the request fails
     */
    public Credits getCredits() throws SendlyException {
        JsonObject response = client.get("/credits", null);
        JsonObject data = response.has("credits") ?
            response.getAsJsonObject("credits") :
            response.has("data") ? response.getAsJsonObject("data") : response;
        return new Credits(data);
    }

    /**
     * Get credit transaction history.
     *
     * @return List of credit transactions
     * @throws SendlyException if the request fails
     */
    public List<CreditTransaction> getCreditTransactions() throws SendlyException {
        return getCreditTransactions(null, null);
    }

    /**
     * Get credit transaction history with pagination.
     *
     * @param limit Maximum number of transactions to return
     * @param offset Number of transactions to skip
     * @return List of credit transactions
     * @throws SendlyException if the request fails
     */
    public List<CreditTransaction> getCreditTransactions(Integer limit, Integer offset) throws SendlyException {
        Map<String, String> params = new HashMap<>();
        if (limit != null) params.put("limit", String.valueOf(limit));
        if (offset != null) params.put("offset", String.valueOf(offset));

        JsonObject response = client.get("/credits/transactions", params.isEmpty() ? null : params);

        List<CreditTransaction> transactions = new ArrayList<>();
        JsonArray array = null;

        if (response.has("data") && response.get("data").isJsonArray()) {
            array = response.getAsJsonArray("data");
        } else if (response.has("transactions") && response.get("transactions").isJsonArray()) {
            array = response.getAsJsonArray("transactions");
        }

        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                transactions.add(new CreditTransaction(array.get(i).getAsJsonObject()));
            }
        }

        return transactions;
    }

    public JsonObject transferCredits(String targetOrganizationId, int amount) throws SendlyException {
        if (targetOrganizationId == null || targetOrganizationId.isEmpty()) {
            throw new ValidationException("Target organization ID is required");
        }
        if (amount <= 0) {
            throw new ValidationException("Amount must be a positive integer");
        }

        JsonObject body = new JsonObject();
        body.addProperty("targetOrganizationId", targetOrganizationId);
        body.addProperty("amount", amount);

        return client.post("/credits/transfer", body);
    }

    /**
     * List all API keys for the account.
     *
     * @return List of API keys
     * @throws SendlyException if the request fails
     */
    public List<ApiKey> listApiKeys() throws SendlyException {
        JsonObject response = client.get("/account/keys", null);

        List<ApiKey> keys = new ArrayList<>();
        JsonArray array = null;

        if (response.has("data") && response.get("data").isJsonArray()) {
            array = response.getAsJsonArray("data");
        } else if (response.has("keys") && response.get("keys").isJsonArray()) {
            array = response.getAsJsonArray("keys");
        }

        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                keys.add(new ApiKey(array.get(i).getAsJsonObject()));
            }
        }

        return keys;
    }

    /**
     * Get a specific API key by ID.
     *
     * @param keyId API key ID
     * @return The API key
     * @throws SendlyException if the request fails
     */
    public ApiKey getApiKey(String keyId) throws SendlyException {
        if (keyId == null || keyId.isEmpty()) {
            throw new ValidationException("API key ID is required");
        }

        JsonObject response = client.get("/account/keys/" + keyId, null);
        JsonObject data = response.has("key") ?
            response.getAsJsonObject("key") :
            response.has("data") ? response.getAsJsonObject("data") : response;
        return new ApiKey(data);
    }

    /**
     * Get usage statistics for an API key.
     *
     * @param keyId API key ID
     * @return Usage statistics as JsonObject
     * @throws SendlyException if the request fails
     */
    public JsonObject getApiKeyUsage(String keyId) throws SendlyException {
        if (keyId == null || keyId.isEmpty()) {
            throw new ValidationException("API key ID is required");
        }
        return client.get("/account/keys/" + keyId + "/usage", null);
    }

    /**
     * Create a new API key.
     *
     * @param name Name for the API key
     * @param type Key type: "test" or "live"
     * @return The created API key with the raw key value (shown only once)
     * @throws SendlyException if the request fails
     */
    public JsonObject createApiKey(String name, String type) throws SendlyException {
        return createApiKey(name, type, null);
    }

    /**
     * Create a new API key with scopes.
     *
     * @param name Name for the API key
     * @param type Key type: "test" or "live"
     * @param scopes Permission scopes (optional)
     * @return The created API key with the raw key value (shown only once)
     * @throws SendlyException if the request fails
     */
    public JsonObject createApiKey(String name, String type, List<String> scopes) throws SendlyException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("API key name is required");
        }
        if (type == null || (!type.equals("test") && !type.equals("live"))) {
            throw new ValidationException("API key type must be 'test' or 'live'");
        }

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("type", type);
        if (scopes != null && !scopes.isEmpty()) {
            com.google.gson.JsonArray scopesArray = new com.google.gson.JsonArray();
            scopes.forEach(scopesArray::add);
            body.add("scopes", scopesArray);
        }

        return client.post("/account/keys", body);
    }

    /**
     * Revoke an API key.
     *
     * @param keyId API key ID
     * @return The revoked API key
     * @throws SendlyException if the request fails
     */
    public ApiKey revokeApiKey(String keyId) throws SendlyException {
        return revokeApiKey(keyId, null);
    }

    /**
     * Revoke an API key with a reason.
     *
     * @param keyId API key ID
     * @param reason Reason for revocation (optional)
     * @return The revoked API key
     * @throws SendlyException if the request fails
     */
    public ApiKey revokeApiKey(String keyId, String reason) throws SendlyException {
        if (keyId == null || keyId.isEmpty()) {
            throw new ValidationException("API key ID is required");
        }

        JsonObject body = new JsonObject();
        if (reason != null && !reason.isEmpty()) {
            body.addProperty("reason", reason);
        }

        JsonObject response = client.patch("/account/keys/" + keyId + "/revoke", body);
        JsonObject data = response.has("key") ?
            response.getAsJsonObject("key") :
            response.has("data") ? response.getAsJsonObject("data") : response;
        return new ApiKey(data);
    }
}
