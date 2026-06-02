package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Outcome of a {@code numbers().buy()} attempt. Buying is asynchronous; the
 * call returns 202 with a {@link #getStatus() status}:
 * <ul>
 *   <li>{@code provisioning} — purchase accepted, the number is being set up;
 *       poll {@code numbers().list()} until it appears as active.</li>
 *   <li>{@code documents_required} — the user must upload documents on a hosted
 *       page (see {@link #getAction() action}) before the purchase can
 *       complete.</li>
 *   <li>{@code payment_required} — the user must complete payment on a hosted
 *       page (see {@link #getAction() action}) before the purchase can
 *       complete.</li>
 * </ul>
 * When {@code status} is {@code documents_required} or {@code payment_required},
 * {@link #getAction() action} carries the hosted-page hand-off (URL + code).
 * Hand it to the user, wait for completion, then call {@code buy()} again with
 * the same body plus {@code actionCode} set to the completed action's code.
 */
public class BuyNumberResponse {
    private String status;
    private OwnedNumber number;
    private JsonArray requirements;
    private NumberBuyAction action;

    public BuyNumberResponse() {}

    public BuyNumberResponse(JsonObject json) {
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("number") && json.get("number").isJsonObject()) {
            this.number = new OwnedNumber(json.getAsJsonObject("number"));
        }
        if (json.has("requirements") && json.get("requirements").isJsonArray()) {
            this.requirements = json.getAsJsonArray("requirements");
        }
        if (json.has("action") && json.get("action").isJsonObject()) {
            this.action = new NumberBuyAction(json.getAsJsonObject("action"));
        }
    }

    /**
     * Buy status: {@code provisioning}, {@code documents_required}, or
     * {@code payment_required}.
     */
    public String getStatus() { return status; }

    /** The provisioned number, when available. */
    public OwnedNumber getNumber() { return number; }

    /**
     * Outstanding requirements, when {@code status} is
     * {@code documents_required}. Opaque, carrier-dependent shape.
     */
    public JsonArray getRequirements() { return requirements; }

    /** Hosted-page hand-off, when {@code status} requires user action. */
    public NumberBuyAction getAction() { return action; }
}
