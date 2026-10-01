package com.sendly.models;

/**
 * Request body for {@code whatsapp().signup().create(CreateWhatsAppSignupRequest)}.
 * <p>
 * With only a phone number it starts a Facebook connection, like
 * {@code create(String)}. With {@code businessAccountId} it adds the number
 * to a WhatsApp Business Account already connected in this workspace: there
 * is no Facebook step, WhatsApp sends the number a code instead, and the
 * signup is {@code verifying} until the code is submitted with
 * {@code signup().verify(id, code)}.
 * </p>
 */
public class CreateWhatsAppSignupRequest {
    private final String phoneNumber;
    private final String businessAccountId;
    private final String verificationMethod;
    private final String displayName;

    private CreateWhatsAppSignupRequest(Builder builder) {
        this.phoneNumber = builder.phoneNumber;
        this.businessAccountId = builder.businessAccountId;
        this.verificationMethod = builder.verificationMethod;
        this.displayName = builder.displayName;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getBusinessAccountId() { return businessAccountId; }
    public String getVerificationMethod() { return verificationMethod; }
    public String getDisplayName() { return displayName; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String phoneNumber;
        private String businessAccountId;
        private String verificationMethod;
        private String displayName;

        /**
         * The number to connect, in E.164 format. Must be an active number
         * in your workspace (provisioned, purchased, or fully ported into
         * Sendly). Required.
         */
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * The id of a WhatsApp Business Account connected in this workspace,
         * as {@code WhatsAppSender.getBusinessAccountId()} or
         * {@code WhatsAppSignup.getBusinessAccountId()} reports it. The
         * account needs at least one active number.
         */
        public Builder businessAccountId(String businessAccountId) {
            this.businessAccountId = businessAccountId;
            return this;
        }

        /**
         * How WhatsApp delivers the code: {@code sms} (the default) or
         * {@code voice}; see {@link WhatsAppVerificationMethod}. Only used
         * with {@code businessAccountId}.
         */
        public Builder verificationMethod(String verificationMethod) {
            this.verificationMethod = verificationMethod;
            return this;
        }

        /**
         * The name WhatsApp shows for the number (at most 512 characters).
         * Defaults to the account's existing sender display name, else its
         * business name. Only used with {@code businessAccountId}.
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public CreateWhatsAppSignupRequest build() {
            return new CreateWhatsAppSignupRequest(this);
        }
    }
}
