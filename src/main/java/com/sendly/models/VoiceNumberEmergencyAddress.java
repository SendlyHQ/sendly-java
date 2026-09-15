package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A number's emergency address registration, from
 * {@code VoiceNumber.getEmergencyAddress()}.
 */
public class VoiceNumberEmergencyAddress {
    /** The registration is being switched on. */
    public static final String STATUS_PROVISIONING = "provisioning";
    /** The registration is in place. */
    public static final String STATUS_ACTIVE = "active";

    private final String status;
    private final EmergencyAddress address;

    /**
     * Create a VoiceNumberEmergencyAddress from a JSON object.
     */
    public VoiceNumberEmergencyAddress(JsonObject json) {
        this.status = json.has("status") && !json.get("status").isJsonNull() ? json.get("status").getAsString() : null;
        this.address = json.has("address") && json.get("address").isJsonObject()
                ? new EmergencyAddress(json.getAsJsonObject("address")) : null;
    }

    /**
     * {@code "provisioning"} while the registration is being switched on,
     * {@code "active"} once it is in place, otherwise the failure status as
     * recorded.
     */
    public String getStatus() {
        return status;
    }

    /** The registered address, or {@code null} when none is on file. */
    public EmergencyAddress getAddress() {
        return address;
    }

    /** True when the registration is active or being switched on, which is what placing calls needs. */
    public boolean isOnFile() {
        return STATUS_ACTIVE.equals(status) || STATUS_PROVISIONING.equals(status);
    }

    @Override
    public String toString() {
        return "VoiceNumberEmergencyAddress{" +
                "status='" + status + '\'' +
                ", address=" + address +
                '}';
    }
}
