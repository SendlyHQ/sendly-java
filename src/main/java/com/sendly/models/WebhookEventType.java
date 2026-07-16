package com.sendly.models;

/**
 * Webhook event types emitted by Sendly. Use the {@code value} string when
 * subscribing so you catch typos at compile time.
 */
public enum WebhookEventType {
    MESSAGE_QUEUED("message.queued"),
    MESSAGE_SENT("message.sent"),
    MESSAGE_DELIVERED("message.delivered"),
    MESSAGE_FAILED("message.failed"),
    MESSAGE_BOUNCED("message.bounced"),
    MESSAGE_RETRYING("message.retrying"),
    MESSAGE_RECEIVED("message.received"),
    MESSAGE_OPT_OUT("message.opt_out"),
    MESSAGE_OPT_IN("message.opt_in"),
    VERIFICATION_CREATED("verification.created"),
    VERIFICATION_DELIVERED("verification.delivered"),
    VERIFICATION_VERIFIED("verification.verified"),
    VERIFICATION_EXPIRED("verification.expired"),
    VERIFICATION_FAILED("verification.failed"),
    VERIFICATION_RESENT("verification.resent"),
    VERIFICATION_DELIVERY_FAILED("verification.delivery_failed"),
    CONTACT_AUTO_FLAGGED("contact.auto_flagged"),
    CONTACT_MARKED_VALID("contact.marked_valid"),
    CONTACTS_LOOKUP_COMPLETED("contacts.lookup_completed"),
    CONTACTS_BULK_MARKED_VALID("contacts.bulk_marked_valid"),
    BRAND_VERIFIED("brand.verified"),
    BRAND_FAILED("brand.failed"),
    CAMPAIGN_APPROVED("campaign.approved"),
    CAMPAIGN_REJECTED("campaign.rejected"),
    CAMPAIGN_SUSPENDED("campaign.suspended"),
    ASSIGNMENT_CONFIRMED("assignment.confirmed"),
    ASSIGNMENT_FAILED("assignment.failed"),
    PORT_COMPLETED("port.completed"),
    PORT_OUT_REQUESTED("port_out.requested"),
    PORT_OUT_COMPLETED("port_out.completed"),
    PORT_OUT_REJECTED("port_out.rejected"),
    PORT_OUT_CANCELLED("port_out.cancelled"),
    NUMBER_ACTIVATED("number.activated"),
    NUMBER_FAILED("number.failed"),
    NUMBER_REQUIREMENTS_REQUIRED("number.requirements_required"),
    NUMBER_RELEASED("number.released");

    private final String value;

    WebhookEventType(String value) {
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
