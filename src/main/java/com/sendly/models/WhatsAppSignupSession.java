package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Response from {@code whatsapp().signup().create()}.
 * <p>
 * For a Facebook connection, hand {@link #getConnectUrl() connectUrl} to a
 * human: they open it in a browser and log in with Facebook to link their
 * WhatsApp Business Account. Poll {@code whatsapp().signup().get(id)} until
 * the status is {@code active}.
 * </p>
 * <p>
 * For a number added to an already-connected account (a request with
 * {@code businessAccountId}), there is no connect URL: the status is
 * {@code verifying} and WhatsApp sends the number a code to submit with
 * {@code whatsapp().signup().verify(id, code)}.
 * </p>
 */
public class WhatsAppSignupSession {
    private String id;
    private String connectUrl;
    private String status;
    private String phoneNumber;
    private String businessAccountId;
    private String verificationMethod;
    private Integer verificationAttemptsRemaining;

    public WhatsAppSignupSession() {}

    public WhatsAppSignupSession(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
        }
        if (json.has("connectUrl") && !json.get("connectUrl").isJsonNull()) {
            this.connectUrl = json.get("connectUrl").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("phoneNumber") && !json.get("phoneNumber").isJsonNull()) {
            this.phoneNumber = json.get("phoneNumber").getAsString();
        }
        if (json.has("businessAccountId") && !json.get("businessAccountId").isJsonNull()) {
            this.businessAccountId = json.get("businessAccountId").getAsString();
        }
        if (json.has("verificationMethod") && !json.get("verificationMethod").isJsonNull()) {
            this.verificationMethod = json.get("verificationMethod").getAsString();
        }
        if (json.has("verificationAttemptsRemaining") && !json.get("verificationAttemptsRemaining").isJsonNull()) {
            this.verificationAttemptsRemaining = json.get("verificationAttemptsRemaining").getAsInt();
        }
    }

    /** Unique signup identifier — use with {@code whatsapp().signup().get(id)}. */
    public String getId() { return id; }

    /**
     * Hosted connect page URL. A person must open this in a browser. Null
     * for a number added to an already-connected account.
     */
    public String getConnectUrl() { return connectUrl; }

    /**
     * Current signup status: {@code initiated}, {@code registering},
     * {@code verifying} (a number added to a connected account, waiting
     * for its code), {@code active}, or {@code failed}. The API does not
     * send {@code expired}.
     */
    public String getStatus() { return status; }

    /**
     * The number being added, in E.164 format, for a number added to a
     * connected account; null for a Facebook connection, whose create
     * response does not send it.
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * The WhatsApp Business Account the number is being added to; null for
     * a Facebook connection.
     */
    public String getBusinessAccountId() { return businessAccountId; }

    /**
     * How the code is delivered ({@code sms} or {@code voice}) while
     * {@code verifying}; null otherwise.
     */
    public String getVerificationMethod() { return verificationMethod; }

    /**
     * How many wrong codes may still be submitted while {@code verifying};
     * null otherwise.
     */
    public Integer getVerificationAttemptsRemaining() { return verificationAttemptsRemaining; }
}
