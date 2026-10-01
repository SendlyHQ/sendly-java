package com.sendly.models;

/**
 * How WhatsApp delivers the code for a number added to an already-connected
 * WhatsApp Business Account, set with
 * {@code CreateWhatsAppSignupRequest.Builder.verificationMethod(String)} or
 * {@code whatsapp().signup().resend(id, verificationMethod)} and reported by
 * {@code WhatsAppSignup.getVerificationMethod()}.
 */
public final class WhatsAppVerificationMethod {
    /** A text message to the number. The default. */
    public static final String SMS = "sms";
    /** A voice call to the number that reads the code out. */
    public static final String VOICE = "voice";

    private WhatsAppVerificationMethod() {}
}
