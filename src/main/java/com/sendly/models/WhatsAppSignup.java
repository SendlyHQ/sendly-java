package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from {@code whatsapp().signup().get(id)}.
 */
public class WhatsAppSignup {
    private String id;
    private String status;
    private String phoneNumber;
    private String businessAccountId;
    private List<String> failureReasons;
    private String verificationMethod;
    private Integer verificationAttemptsRemaining;
    private String verificationCode;
    private String updatedAt;

    public WhatsAppSignup() {}

    public WhatsAppSignup(JsonObject json) {
        if (json.has("id") && !json.get("id").isJsonNull()) {
            this.id = json.get("id").getAsString();
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
        if (json.has("failureReasons") && json.get("failureReasons").isJsonArray()) {
            this.failureReasons = new ArrayList<>();
            JsonArray reasons = json.getAsJsonArray("failureReasons");
            for (int i = 0; i < reasons.size(); i++) {
                if (!reasons.get(i).isJsonNull()) {
                    this.failureReasons.add(reasons.get(i).getAsString());
                }
            }
        }
        if (json.has("verificationMethod") && !json.get("verificationMethod").isJsonNull()) {
            this.verificationMethod = json.get("verificationMethod").getAsString();
        }
        if (json.has("verificationAttemptsRemaining") && !json.get("verificationAttemptsRemaining").isJsonNull()) {
            this.verificationAttemptsRemaining = json.get("verificationAttemptsRemaining").getAsInt();
        }
        if (json.has("verificationCode") && !json.get("verificationCode").isJsonNull()) {
            this.verificationCode = json.get("verificationCode").getAsString();
        }
        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            this.updatedAt = json.get("updatedAt").getAsString();
        }
    }

    /** Unique signup identifier. */
    public String getId() { return id; }

    /**
     * Current signup status: {@code initiated}, {@code registering}
     * (WhatsApp is activating the number; activation usually takes a few
     * minutes but can take hours, and a session that hasn't finished about 6
     * hours after it began fails with {@code registration_timeout} and the
     * fee is refunded), {@code verifying} (a number being added to a
     * connected WhatsApp Business Account is waiting for the code WhatsApp
     * sent it; submit it with {@code signup().verify(id, code)}),
     * {@code active}, or {@code failed}. The API does not send
     * {@code expired}. Compare as a string: a status added later is returned
     * as sent.
     */
    public String getStatus() { return status; }

    /** The number being connected, in E.164 format. */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * The customer's WhatsApp Business Account id while the signup is
     * {@code verifying} or {@code active}; null otherwise, including before
     * the human completes the connect step.
     */
    public String getBusinessAccountId() { return businessAccountId; }

    /**
     * Why the signup failed, when status is {@code failed}; null otherwise.
     * Holds one code: {@code setup_fee_payment_failed},
     * {@code signup_abandoned}, {@code meta_exchange_failed},
     * {@code registration_failed}, {@code waba_already_connected},
     * {@code waba_mismatch} (the WhatsApp Business Account chosen in the
     * Facebook step doesn't hold the verified number),
     * {@code registration_timeout} (activation hadn't finished about 6 hours
     * after the session began), {@code phone_number_mismatch},
     * {@code verification_start_failed} (WhatsApp could not be asked to
     * send the code), {@code verification_failed} (too many wrong codes) or
     * {@code verification_expired} (no code was accepted in time). If the
     * connection fails, the $19 fee is refunded automatically.
     */
    public List<String> getFailureReasons() { return failureReasons; }

    /**
     * How the code is delivered while {@code verifying}: {@code sms} or
     * {@code voice} (see {@link WhatsAppVerificationMethod}); null in any
     * other status.
     */
    public String getVerificationMethod() { return verificationMethod; }

    /**
     * How many more wrong codes {@code signup().verify(id, code)} accepts
     * before the signup fails with {@code verification_failed}; null unless
     * {@code verifying}.
     */
    public Integer getVerificationAttemptsRemaining() { return verificationAttemptsRemaining; }

    /**
     * The code WhatsApp sent, when its text has already arrived on the
     * number (read from the workspace's inbound messages); null until then
     * and in any status but {@code verifying}. Only {@code signup().get(id)}
     * sets it. Until a code has been submitted it is the newest code that
     * has arrived since the signup started, so after a resend it still
     * shows the earlier code until the new one arrives. Once WhatsApp has
     * checked a code, only a code that arrived after the last submission or
     * resend is returned. A submission answered with 502
     * {@code whatsapp_verification_unavailable} is not counted, so the same
     * unchecked code can come back, and submitting it again is safe.
     */
    public String getVerificationCode() { return verificationCode; }

    /** ISO 8601 timestamp of the last status change. */
    public String getUpdatedAt() { return updatedAt; }
}
