package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business Upgrade resource — Entity-Upgrade ("fork-with-new-number").
 * <p>
 * Manages the toll-free business entity upgrade flow: when a customer
 * forms a new legal entity (e.g. an LLC), this resource lets them
 * reserve a new toll-free number under the new entity, submit it for
 * carrier review, and atomically swap to it on approval — without
 * disrupting outbound SMS during the 1-2 week review window.
 *
 * <pre>{@code
 * // Preview validation before submitting
 * JsonObject preview = client.businessUpgrade().preflight(
 *     BusinessUpgradeResource.PreflightCandidate.builder()
 *         .businessName("Acme Holdings LLC")
 *         .brn("12-3456789")
 *         .brnType("EIN")
 *         .brnCountry("US")
 *         .entityType("PRIVATE_PROFIT")
 *         .build());
 *
 * // Submit the upgrade with the IRS letter
 * JsonObject result = client.businessUpgrade().start(
 *     "ws_abc",
 *     BusinessUpgradeResource.StartUpgradeParams.builder()
 *         .businessName("Acme Holdings LLC")
 *         .brn("12-3456789")
 *         .brnType("EIN")
 *         .brnCountry("US")
 *         .entityType("PRIVATE_PROFIT")
 *         .build(),
 *     BusinessUpgradeResource.EinDocument.fromFile(new File("./CP-575.pdf")));
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/business-upgrade">Business Upgrade docs</a>
 */
public class BusinessUpgradeResource {
    private final Sendly client;

    public BusinessUpgradeResource(Sendly client) {
        this.client = client;
    }

    /**
     * Validate a candidate entity upgrade payload before submission.
     * Returns issues and proposed auto-fixes. No writes — purely advisory.
     */
    public JsonObject preflight(PreflightCandidate candidate) throws SendlyException {
        if (candidate == null) {
            throw new ValidationException("Candidate is required");
        }
        return client.post("/verification/preflight", candidate.toJson());
    }

    /**
     * Get a "best-of" prefill across all the caller's verified workspaces.
     * Returns most-recent non-empty values per messaging field. Use this
     * to pre-populate the upgrade form for users whose current workspace
     * has incomplete data.
     */
    public JsonObject bestPrefill() throws SendlyException {
        return client.get("/verification/best-prefill", null);
    }

    /**
     * Start an entity upgrade for the given workspace. Auto-provisions
     * a new toll-free number + messaging profile and submits to the
     * carrier for review. Returns the pending verification details.
     * <p>
     * The current toll-free number continues sending throughout the
     * 1-2 week carrier review; on approval, an atomic swap promotes
     * the new number.
     */
    public JsonObject start(String workspaceId, StartUpgradeParams params) throws SendlyException {
        return start(workspaceId, params, null);
    }

    /**
     * Start an entity upgrade with an EIN document attached.
     */
    public JsonObject start(String workspaceId, StartUpgradeParams params, EinDocument einDoc) throws SendlyException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ValidationException("Workspace ID is required");
        }
        if (params == null) {
            throw new ValidationException("Params are required");
        }
        RequestBody body = buildMultipart(params.toFormFields(), einDoc);
        return client.postMultipart("/workspaces/" + encode(workspaceId) + "/upgrade", body);
    }

    /**
     * Check whether the given workspace has a pending entity upgrade.
     * Returns {@code { "pending": null }} if no upgrade is in flight.
     */
    public JsonObject status(String workspaceId) throws SendlyException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ValidationException("Workspace ID is required");
        }
        return client.get("/workspaces/" + encode(workspaceId) + "/upgrade/status", null);
    }

    /**
     * Cancel a pending entity upgrade for the given workspace. Releases
     * the reserved toll-free number, deletes the new messaging profile,
     * and removes the stored EIN document. Idempotent.
     */
    public JsonObject cancel(String workspaceId) throws SendlyException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ValidationException("Workspace ID is required");
        }
        return client.post("/workspaces/" + encode(workspaceId) + "/upgrade/cancel", new JsonObject());
    }

    /**
     * Resubmit a rejected (or waiting-for-customer) entity upgrade with
     * updated fields and optionally a new EIN document.
     */
    public JsonObject resubmit(String workspaceId, StartUpgradeParams params) throws SendlyException {
        return resubmit(workspaceId, params, null);
    }

    /**
     * Resubmit with an EIN document attached.
     */
    public JsonObject resubmit(String workspaceId, StartUpgradeParams params, EinDocument einDoc) throws SendlyException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ValidationException("Workspace ID is required");
        }
        Map<String, String> fields = params != null ? params.toFormFields() : new LinkedHashMap<>();
        RequestBody body = buildMultipart(fields, einDoc);
        return client.postMultipart("/workspaces/" + encode(workspaceId) + "/upgrade/resubmit", body);
    }

    /**
     * After a successful entity-upgrade approval, choose what happens to
     * the old toll-free number:
     * <ul>
     *   <li>{@code "moved"}: keep it active under another workspace owned by the
     *       same user (requires {@code targetWorkspaceId})</li>
     *   <li>{@code "released"}: return it to the carrier pool</li>
     * </ul>
     */
    public JsonObject setDisposition(String workspaceId, String disposition, String targetWorkspaceId) throws SendlyException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ValidationException("Workspace ID is required");
        }
        if (disposition == null || disposition.isEmpty()) {
            throw new ValidationException("Disposition is required");
        }
        JsonObject body = new JsonObject();
        body.addProperty("disposition", disposition);
        if (targetWorkspaceId != null && !targetWorkspaceId.isEmpty()) {
            body.addProperty("targetOrgId", targetWorkspaceId);
        }
        return client.post("/workspaces/" + encode(workspaceId) + "/upgrade/disposition", body);
    }

    /**
     * Convenience overload for {@code released} disposition.
     */
    public JsonObject setDisposition(String workspaceId, String disposition) throws SendlyException {
        return setDisposition(workspaceId, disposition, null);
    }

    private static RequestBody buildMultipart(Map<String, String> fields, EinDocument einDoc) {
        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fields != null) {
            for (Map.Entry<String, String> e : fields.entrySet()) {
                if (e.getValue() == null) continue;
                mb.addFormDataPart(e.getKey(), e.getValue());
            }
        }
        if (einDoc != null) {
            RequestBody fileBody;
            String filename = einDoc.filename != null ? einDoc.filename : "ein-doc.pdf";
            String contentType = einDoc.contentType != null ? einDoc.contentType : "application/pdf";
            okhttp3.MediaType mediaType = okhttp3.MediaType.parse(contentType);
            if (einDoc.file != null) {
                fileBody = RequestBody.create(einDoc.file, mediaType);
            } else {
                fileBody = RequestBody.create(einDoc.bytes, mediaType);
            }
            mb.addFormDataPart("einDoc", filename, fileBody);
        }
        return mb.build();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * EIN document attachment for {@link #start} and {@link #resubmit}.
     * Construct with {@link #fromFile} or {@link #fromBytes}.
     */
    public static final class EinDocument {
        final File file;
        final byte[] bytes;
        final String filename;
        final String contentType;

        private EinDocument(File file, byte[] bytes, String filename, String contentType) {
            this.file = file;
            this.bytes = bytes;
            this.filename = filename;
            this.contentType = contentType;
        }

        public static EinDocument fromFile(File file) {
            return new EinDocument(file, null, file.getName(), "application/pdf");
        }

        public static EinDocument fromFile(File file, String contentType) {
            return new EinDocument(file, null, file.getName(), contentType);
        }

        public static EinDocument fromBytes(byte[] bytes, String filename) {
            return new EinDocument(null, bytes, filename, "application/pdf");
        }

        public static EinDocument fromBytes(byte[] bytes, String filename, String contentType) {
            return new EinDocument(null, bytes, filename, contentType);
        }
    }

    /**
     * Preflight candidate — fields validated before any writes happen.
     */
    public static final class PreflightCandidate {
        private String businessName;
        private String doingBusinessAs;
        private String brn;
        private String brnType;
        private String brnCountry;
        private String entityType;
        private String website;
        private String address1;
        private String address2;
        private String city;
        private String state;
        private String zip;
        private String addressCountry;
        private String contactFirstName;
        private String contactLastName;
        private String contactEmail;
        private String contactPhone;
        private String monthlyVolume;
        private String useCase;
        private String useCaseSummary;
        private String sampleMessages;
        private String optInWorkflow;
        private String privacyUrl;
        private String termsUrl;
        private String additionalInformation;
        private Boolean ageGatedContent;

        public static Builder builder() {
            return new Builder();
        }

        JsonObject toJson() {
            JsonObject o = new JsonObject();
            if (businessName != null) o.addProperty("businessName", businessName);
            if (doingBusinessAs != null) o.addProperty("doingBusinessAs", doingBusinessAs);
            if (brn != null) o.addProperty("brn", brn);
            if (brnType != null) o.addProperty("brnType", brnType);
            if (brnCountry != null) o.addProperty("brnCountry", brnCountry);
            if (entityType != null) o.addProperty("entityType", entityType);
            if (website != null) o.addProperty("website", website);
            if (address1 != null) o.addProperty("address1", address1);
            if (address2 != null) o.addProperty("address2", address2);
            if (city != null) o.addProperty("city", city);
            if (state != null) o.addProperty("state", state);
            if (zip != null) o.addProperty("zip", zip);
            if (addressCountry != null) o.addProperty("addressCountry", addressCountry);
            if (contactFirstName != null) o.addProperty("contactFirstName", contactFirstName);
            if (contactLastName != null) o.addProperty("contactLastName", contactLastName);
            if (contactEmail != null) o.addProperty("contactEmail", contactEmail);
            if (contactPhone != null) o.addProperty("contactPhone", contactPhone);
            if (monthlyVolume != null) o.addProperty("monthlyVolume", monthlyVolume);
            if (useCase != null) o.addProperty("useCase", useCase);
            if (useCaseSummary != null) o.addProperty("useCaseSummary", useCaseSummary);
            if (sampleMessages != null) o.addProperty("sampleMessages", sampleMessages);
            if (optInWorkflow != null) o.addProperty("optInWorkflow", optInWorkflow);
            if (privacyUrl != null) o.addProperty("privacyUrl", privacyUrl);
            if (termsUrl != null) o.addProperty("termsUrl", termsUrl);
            if (additionalInformation != null) o.addProperty("additionalInformation", additionalInformation);
            if (ageGatedContent != null) o.addProperty("ageGatedContent", ageGatedContent);
            return o;
        }

        public static final class Builder {
            private final PreflightCandidate c = new PreflightCandidate();
            public Builder businessName(String v) { c.businessName = v; return this; }
            public Builder doingBusinessAs(String v) { c.doingBusinessAs = v; return this; }
            public Builder brn(String v) { c.brn = v; return this; }
            public Builder brnType(String v) { c.brnType = v; return this; }
            public Builder brnCountry(String v) { c.brnCountry = v; return this; }
            public Builder entityType(String v) { c.entityType = v; return this; }
            public Builder website(String v) { c.website = v; return this; }
            public Builder address1(String v) { c.address1 = v; return this; }
            public Builder address2(String v) { c.address2 = v; return this; }
            public Builder city(String v) { c.city = v; return this; }
            public Builder state(String v) { c.state = v; return this; }
            public Builder zip(String v) { c.zip = v; return this; }
            public Builder addressCountry(String v) { c.addressCountry = v; return this; }
            public Builder contactFirstName(String v) { c.contactFirstName = v; return this; }
            public Builder contactLastName(String v) { c.contactLastName = v; return this; }
            public Builder contactEmail(String v) { c.contactEmail = v; return this; }
            public Builder contactPhone(String v) { c.contactPhone = v; return this; }
            public Builder monthlyVolume(String v) { c.monthlyVolume = v; return this; }
            public Builder useCase(String v) { c.useCase = v; return this; }
            public Builder useCaseSummary(String v) { c.useCaseSummary = v; return this; }
            public Builder sampleMessages(String v) { c.sampleMessages = v; return this; }
            public Builder optInWorkflow(String v) { c.optInWorkflow = v; return this; }
            public Builder privacyUrl(String v) { c.privacyUrl = v; return this; }
            public Builder termsUrl(String v) { c.termsUrl = v; return this; }
            public Builder additionalInformation(String v) { c.additionalInformation = v; return this; }
            public Builder ageGatedContent(Boolean v) { c.ageGatedContent = v; return this; }
            public PreflightCandidate build() { return c; }
        }
    }

    /**
     * Params for {@link #start} and {@link #resubmit}. Sent as multipart
     * form fields alongside the optional EIN document.
     */
    public static final class StartUpgradeParams {
        private String businessName;
        private String brn;
        private String brnType;
        private String brnCountry;
        private String entityType;
        private String doingBusinessAs;
        private String website;
        private String address1;
        private String address2;
        private String city;
        private String state;
        private String zip;
        private String addressCountry;
        private String contactFirstName;
        private String contactLastName;
        private String contactEmail;
        private String contactPhone;
        private String monthlyVolume;
        private String useCase;
        private String useCaseSummary;
        private String sampleMessages;
        private String optInWorkflow;
        private String privacyUrl;
        private String termsUrl;
        private String additionalInformation;
        private Boolean ageGatedContent;

        public static Builder builder() {
            return new Builder();
        }

        Map<String, String> toFormFields() {
            Map<String, String> m = new LinkedHashMap<>();
            if (businessName != null) m.put("businessName", businessName);
            if (brn != null) m.put("brn", brn);
            if (brnType != null) m.put("brnType", brnType);
            if (brnCountry != null) m.put("brnCountry", brnCountry);
            if (entityType != null) m.put("entityType", entityType);
            if (doingBusinessAs != null) m.put("doingBusinessAs", doingBusinessAs);
            if (website != null) m.put("website", website);
            if (address1 != null) m.put("address1", address1);
            if (address2 != null) m.put("address2", address2);
            if (city != null) m.put("city", city);
            if (state != null) m.put("state", state);
            if (zip != null) m.put("zip", zip);
            if (addressCountry != null) m.put("addressCountry", addressCountry);
            if (contactFirstName != null) m.put("contactFirstName", contactFirstName);
            if (contactLastName != null) m.put("contactLastName", contactLastName);
            if (contactEmail != null) m.put("contactEmail", contactEmail);
            if (contactPhone != null) m.put("contactPhone", contactPhone);
            if (monthlyVolume != null) m.put("monthlyVolume", monthlyVolume);
            if (useCase != null) m.put("useCase", useCase);
            if (useCaseSummary != null) m.put("useCaseSummary", useCaseSummary);
            if (sampleMessages != null) m.put("sampleMessages", sampleMessages);
            if (optInWorkflow != null) m.put("optInWorkflow", optInWorkflow);
            if (privacyUrl != null) m.put("privacyUrl", privacyUrl);
            if (termsUrl != null) m.put("termsUrl", termsUrl);
            if (additionalInformation != null) m.put("additionalInformation", additionalInformation);
            if (ageGatedContent != null) m.put("ageGatedContent", String.valueOf(ageGatedContent));
            return m;
        }

        public static final class Builder {
            private final StartUpgradeParams p = new StartUpgradeParams();
            public Builder businessName(String v) { p.businessName = v; return this; }
            public Builder brn(String v) { p.brn = v; return this; }
            public Builder brnType(String v) { p.brnType = v; return this; }
            public Builder brnCountry(String v) { p.brnCountry = v; return this; }
            public Builder entityType(String v) { p.entityType = v; return this; }
            public Builder doingBusinessAs(String v) { p.doingBusinessAs = v; return this; }
            public Builder website(String v) { p.website = v; return this; }
            public Builder address1(String v) { p.address1 = v; return this; }
            public Builder address2(String v) { p.address2 = v; return this; }
            public Builder city(String v) { p.city = v; return this; }
            public Builder state(String v) { p.state = v; return this; }
            public Builder zip(String v) { p.zip = v; return this; }
            public Builder addressCountry(String v) { p.addressCountry = v; return this; }
            public Builder contactFirstName(String v) { p.contactFirstName = v; return this; }
            public Builder contactLastName(String v) { p.contactLastName = v; return this; }
            public Builder contactEmail(String v) { p.contactEmail = v; return this; }
            public Builder contactPhone(String v) { p.contactPhone = v; return this; }
            public Builder monthlyVolume(String v) { p.monthlyVolume = v; return this; }
            public Builder useCase(String v) { p.useCase = v; return this; }
            public Builder useCaseSummary(String v) { p.useCaseSummary = v; return this; }
            public Builder sampleMessages(String v) { p.sampleMessages = v; return this; }
            public Builder optInWorkflow(String v) { p.optInWorkflow = v; return this; }
            public Builder privacyUrl(String v) { p.privacyUrl = v; return this; }
            public Builder termsUrl(String v) { p.termsUrl = v; return this; }
            public Builder additionalInformation(String v) { p.additionalInformation = v; return this; }
            public Builder ageGatedContent(Boolean v) { p.ageGatedContent = v; return this; }
            public StartUpgradeParams build() { return p; }
        }
    }
}
