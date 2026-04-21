package com.sendly.models;

/**
 * Source of a list-health event. Frozen enum — new values will be
 * added in minor SDK versions, never removed.
 */
public enum ListHealthEventSource {
    SEND_FAILURE("send_failure"),
    CARRIER_LOOKUP("carrier_lookup"),
    USER_ACTION("user_action"),
    BULK_MARK_VALID("bulk_mark_valid");

    private final String value;

    ListHealthEventSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
