package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A number in the workspace with its voice settings, from
 * {@code voice().numbers()}.
 */
public class VoiceNumber {
    private final String id;
    private final String object;
    private final String phoneNumber;
    private final String phoneNumberType;
    private final String countryCode;
    private final boolean isDefault;
    private final boolean voiceEnabled;
    private final String voiceMode;
    private final String agentId;
    private final VoiceNumberEmergencyAddress emergencyAddress;
    private final VoiceNumberRates ratePerMinute;

    /**
     * Create a VoiceNumber from a JSON object.
     */
    public VoiceNumber(JsonObject json) {
        this.id = getStringOrNull(json, "id");
        this.object = getStringOrNull(json, "object");
        this.phoneNumber = getStringOrNull(json, "phoneNumber");
        this.phoneNumberType = getStringOrNull(json, "phoneNumberType");
        this.countryCode = getStringOrNull(json, "countryCode");
        this.isDefault = getBoolean(json, "isDefault");
        this.voiceEnabled = getBoolean(json, "voiceEnabled");
        this.voiceMode = getStringOrNull(json, "voiceMode");
        this.agentId = getStringOrNull(json, "agentId");
        this.emergencyAddress = json.has("emergencyAddress") && json.get("emergencyAddress").isJsonObject()
                ? new VoiceNumberEmergencyAddress(json.getAsJsonObject("emergencyAddress")) : null;
        this.ratePerMinute = json.has("ratePerMinute") && json.get("ratePerMinute").isJsonObject()
                ? new VoiceNumberRates(json.getAsJsonObject("ratePerMinute")) : null;
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static boolean getBoolean(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).getAsBoolean();
    }

    /** Unique number identifier. Accepted wherever a number is expected, as is the E.164 phone number. */
    public String getId() {
        return id;
    }

    /** Always {@code "voice_number"}. */
    public String getObject() {
        return object;
    }

    /** The phone number in E.164. */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** The number's type, for example {@code "local"} or {@code "toll_free"}, or {@code null}. */
    public String getPhoneNumberType() {
        return phoneNumberType;
    }

    /** ISO 3166-1 alpha-2 country code, or {@code null}. */
    public String getCountryCode() {
        return countryCode;
    }

    /** True for the workspace's default sending number. */
    public boolean isDefault() {
        return isDefault;
    }

    /** True when the number takes and places phone calls. */
    public boolean isVoiceEnabled() {
        return voiceEnabled;
    }

    /** How inbound calls are answered; always {@code "none"} when voice is off. See {@link VoiceMode}. */
    public String getVoiceMode() {
        return voiceMode;
    }

    /**
     * The agent that answers when the mode is {@code "agent"}. In other
     * modes, whichever agent was last stored, or {@code null}.
     */
    public String getAgentId() {
        return agentId;
    }

    /** The emergency address registration, or {@code null} if one was never registered. */
    public VoiceNumberEmergencyAddress getEmergencyAddress() {
        return emergencyAddress;
    }

    /** Credits charged per started minute on this number. */
    public VoiceNumberRates getRatePerMinute() {
        return ratePerMinute;
    }

    @Override
    public String toString() {
        return "VoiceNumber{" +
                "id='" + id + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", voiceEnabled=" + voiceEnabled +
                ", voiceMode='" + voiceMode + '\'' +
                ", agentId='" + agentId + '\'' +
                '}';
    }
}
