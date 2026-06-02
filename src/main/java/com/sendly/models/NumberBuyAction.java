package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * Hosted-page hand-off returned when a number purchase needs the user to
 * finish a step (documents or payment).
 * <p>
 * Hand the user the {@link #getUrl() url} and the short {@link #getCode() code}
 * (the code proves they have terminal access — display only). Once they
 * complete the hosted page, re-call {@code buy()} with the same body plus
 * {@code actionCode} set to this action's {@link #getActionCode() actionCode}
 * (NOT the display {@code code}).
 */
public class NumberBuyAction {
    private String url;
    private String code;
    private String actionCode;
    private long expiresAt;

    public NumberBuyAction() {}

    public NumberBuyAction(JsonObject json) {
        if (json.has("url") && !json.get("url").isJsonNull()) {
            this.url = json.get("url").getAsString();
        }
        if (json.has("code") && !json.get("code").isJsonNull()) {
            this.code = json.get("code").getAsString();
        }
        if (json.has("actionCode") && !json.get("actionCode").isJsonNull()) {
            this.actionCode = json.get("actionCode").getAsString();
        }
        if (json.has("expiresAt") && !json.get("expiresAt").isJsonNull()) {
            this.expiresAt = json.get("expiresAt").getAsLong();
        }
    }

    /** Hosted Sendly page URL to send the user to. */
    public String getUrl() { return url; }

    /**
     * Short user-facing code the user types on the hosted page to prove
     * terminal access. Display only — do not use it to poll or re-buy.
     */
    public String getCode() { return code; }

    /**
     * The action identifier. Use this to poll
     * {@code GET /api/cli/action/<actionCode>} and to re-call {@code buy()}
     * with {@code actionCode} set, once the hosted page is complete.
     */
    public String getActionCode() { return actionCode; }

    /** When this action expires (epoch milliseconds). */
    public long getExpiresAt() { return expiresAt; }
}
