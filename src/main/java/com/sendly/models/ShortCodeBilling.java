package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Where the money stands on a short code: the setup fee, the monthly lease,
 * the terms and every charge. A listed code carries only the setup fee's
 * status and amount and the lease; the rest is empty or null there.
 */
public class ShortCodeBilling {
    private final ShortCodeSetupFee setupFee;
    private final ShortCodeLease lease;
    private final String termsVersion;
    private final String termsAcceptedVersion;
    private final String termsAcceptedAt;
    private final String refundPolicy;
    private final String exempt;
    private final List<ShortCodeCharge> charges;

    public ShortCodeBilling(JsonObject json) {
        JsonObject setupFeeJson = ShortCodeJson.object(json, "setupFee");
        this.setupFee = setupFeeJson != null ? new ShortCodeSetupFee(setupFeeJson) : null;
        JsonObject leaseJson = ShortCodeJson.object(json, "lease");
        this.lease = leaseJson != null ? new ShortCodeLease(leaseJson) : null;
        JsonObject terms = ShortCodeJson.object(json, "terms");
        this.termsVersion = ShortCodeJson.string(terms, "version");
        this.termsAcceptedVersion = ShortCodeJson.string(terms, "acceptedVersion");
        this.termsAcceptedAt = ShortCodeJson.string(terms, "acceptedAt");
        this.refundPolicy = ShortCodeJson.string(json, "refundPolicy");
        this.exempt = ShortCodeJson.string(json, "exempt");
        List<ShortCodeCharge> list = new ArrayList<>();
        for (JsonObject charge : ShortCodeJson.objects(json, "charges")) {
            list.add(new ShortCodeCharge(charge));
        }
        this.charges = Collections.unmodifiableList(list);
    }

    /** The one-time setup fee. */
    public ShortCodeSetupFee getSetupFee() { return setupFee; }

    /** The monthly lease. */
    public ShortCodeLease getLease() { return lease; }

    /** The current terms version, or null on a listed code. */
    public String getTermsVersion() { return termsVersion; }

    /** The terms version accepted at submit, or null. */
    public String getTermsAcceptedVersion() { return termsAcceptedVersion; }

    /** When the terms were accepted, or null. */
    public String getTermsAcceptedAt() { return termsAcceptedAt; }

    /** {@code full_refund_before_filing}, or null on a listed code. */
    public String getRefundPolicy() { return refundPolicy; }

    /** {@code mock} or {@code enterprise} when the workspace isn't charged by card; otherwise null. */
    public String getExempt() { return exempt; }

    /** Every charge of the application; empty on a listed code. */
    public List<ShortCodeCharge> getCharges() { return charges; }
}
