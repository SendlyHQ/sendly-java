package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The SMS fallback of a WhatsApp message sent with
 * {@code fallbackToSms(true)}.
 * <p>
 * {@code status} is {@code pending} until WhatsApp's outcome is known,
 * {@code sent} when the SMS went, {@code failed} when the SMS was refused or
 * credits ran out, {@code unavailable} when the SMS could no longer be sent
 * from this number, and {@code skipped} when the WhatsApp failure isn't one an
 * SMS fixes. {@code reason} is what made the message fall back. Values the API
 * adds later come through unchanged.
 * </p>
 */
public class WhatsAppSmsFallback {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_UNAVAILABLE = "unavailable";
    public static final String STATUS_SKIPPED = "skipped";

    public static final String REASON_WINDOW_CLOSED = "window_closed";
    public static final String REASON_WHATSAPP_REFUSED = "whatsapp_refused";
    public static final String REASON_WHATSAPP_FAILED = "whatsapp_failed";

    private String channel = "sms";
    private String status;
    private String reason;
    private String messageId;
    private Integer creditsUsed;
    private String error;
    private List<String> contentDropped = Collections.emptyList();

    public WhatsAppSmsFallback() {}

    public WhatsAppSmsFallback(JsonObject json) {
        if (json.has("channel") && !json.get("channel").isJsonNull()) {
            this.channel = json.get("channel").getAsString();
        }
        if (json.has("status") && !json.get("status").isJsonNull()) {
            this.status = json.get("status").getAsString();
        }
        if (json.has("reason") && !json.get("reason").isJsonNull()) {
            this.reason = json.get("reason").getAsString();
        }
        if (json.has("messageId") && !json.get("messageId").isJsonNull()) {
            this.messageId = json.get("messageId").getAsString();
        }
        if (json.has("creditsUsed") && !json.get("creditsUsed").isJsonNull()) {
            this.creditsUsed = json.get("creditsUsed").getAsInt();
        }
        if (json.has("error") && !json.get("error").isJsonNull()) {
            this.error = json.get("error").getAsString();
        }
        if (json.has("contentDropped") && json.get("contentDropped").isJsonArray()) {
            List<String> dropped = new ArrayList<>();
            JsonArray array = json.getAsJsonArray("contentDropped");
            for (JsonElement e : array) {
                if (!e.isJsonNull()) {
                    dropped.add(e.getAsString());
                }
            }
            this.contentDropped = Collections.unmodifiableList(dropped);
        }
    }

    /** Always "sms". */
    public String getChannel() { return channel; }

    /**
     * {@code pending}, {@code sent}, {@code failed}, {@code unavailable} or
     * {@code skipped}; see the {@code STATUS_} constants.
     */
    public String getStatus() { return status; }

    /**
     * What made the message fall back: {@code window_closed},
     * {@code whatsapp_refused} or {@code whatsapp_failed}; null while it
     * hasn't.
     */
    public String getReason() { return reason; }

    /** The SMS message's id once it exists; null before. */
    public String getMessageId() { return messageId; }

    /** Credits charged for the SMS; null when none were. */
    public Integer getCreditsUsed() { return creditsUsed; }

    /** Why the SMS didn't go, in plain English; null otherwise. */
    public String getError() { return error; }

    /**
     * Template parts with no SMS form, such as {@code quick_replies} or
     * {@code header_media}. Empty when nothing was dropped.
     */
    public List<String> getContentDropped() { return contentDropped; }

    /** Whether WhatsApp's outcome is still unknown. */
    public boolean isPending() { return STATUS_PENDING.equals(status); }

    /** Whether the SMS went. */
    public boolean isSent() { return STATUS_SENT.equals(status); }

    @Override
    public String toString() {
        return "WhatsAppSmsFallback{" +
                "status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", messageId='" + messageId + '\'' +
                ", creditsUsed=" + creditsUsed +
                '}';
    }
}
