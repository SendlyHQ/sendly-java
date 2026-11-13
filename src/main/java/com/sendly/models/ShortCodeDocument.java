package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * A carrier form a short code application needs, and where it stands.
 */
public class ShortCodeDocument {
    private final String kind;
    private final boolean templateReady;
    private final String signedAt;
    private final String signedByName;
    private final boolean awaitingSignature;

    public ShortCodeDocument(JsonObject json) {
        this.kind = ShortCodeJson.string(json, "kind");
        this.templateReady = ShortCodeJson.bool(json, "templateReady");
        this.signedAt = ShortCodeJson.string(json, "signedAt");
        this.signedByName = ShortCodeJson.string(json, "signedByName");
        this.awaitingSignature = ShortCodeJson.bool(json, "awaitingSignature");
    }

    /** The carrier form. */
    public String getKind() { return kind; }

    /** Whether Sendly has prepared the form for signing. */
    public boolean isTemplateReady() { return templateReady; }

    /** When the form was signed, or null. */
    public String getSignedAt() { return signedAt; }

    /** Who signed it, or null. */
    public String getSignedByName() { return signedByName; }

    /** Whether a signing link is out and has not expired. */
    public boolean isAwaitingSignature() { return awaitingSignature; }
}
