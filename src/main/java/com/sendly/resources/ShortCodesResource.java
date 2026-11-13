package com.sendly.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.models.IdempotentRequestOptions;
import com.sendly.models.ShortCode;
import com.sendly.models.ShortCodeApplicationInput;
import com.sendly.models.ShortCodeApplicationView;
import com.sendly.models.ShortCodePreflight;

import java.util.ArrayList;
import java.util.List;

/**
 * Short codes resource: apply for a US short code and track the
 * application.
 * <p>
 * A short code is a 5 or 6 digit US sender. Getting one is an application,
 * not a purchase: you fill it in, Sendly reviews it, the carrier forms are
 * signed, Sendly files it, and each carrier certifies the code separately.
 * Reads need the {@code short_codes:read} scope and writes
 * {@code short_codes:write}. While short codes aren't enabled for the
 * account every method gets 404 {@code short_codes_not_enabled}.
 * </p>
 *
 * <pre>{@code
 * ShortCodeApplicationView view = client.shortCodes().application().update(
 *     ShortCodeApplicationInput.builder()
 *         .useCase("Delivery alerts for Acme orders")
 *         .messageFrequency("4 messages per month")
 *         .build());
 * ShortCodePreflight check = client.shortCodes().application().check();
 * if (check.isOk()) {
 *     client.shortCodes().application().submit(
 *         ShortCodeApplicationInput.builder().acceptTerms(true).build());
 * }
 * }</pre>
 */
public class ShortCodesResource {
    private final Sendly client;
    private final Application application;

    public ShortCodesResource(Sendly client) {
        this.client = client;
        this.application = new Application(client);
    }

    /**
     * The workspace's short code application.
     *
     * @return Application sub-resource
     */
    public Application application() {
        return application;
    }

    /**
     * List every short code application of the workspace, newest first, with
     * its code once Sendly assigns one. Drafts (with a null short code),
     * rejected and cancelled applications are listed too, so filter on
     * {@link ShortCode#isActive()} for the codes you can send from.
     *
     * @return Every application and code, each with its billing
     * @throws SendlyException if the request fails
     */
    public List<ShortCode> list() throws SendlyException {
        JsonObject response = client.get("/short_codes", null);
        List<ShortCode> codes = new ArrayList<>();
        if (response.has("shortCodes") && response.get("shortCodes").isJsonArray()) {
            for (JsonElement e : response.getAsJsonArray("shortCodes")) {
                if (e.isJsonObject()) {
                    codes.add(new ShortCode(e.getAsJsonObject()));
                }
            }
        }
        return codes;
    }

    /**
     * The workspace's short code application.
     */
    public static class Application {
        private final Sendly client;

        Application(Sendly client) {
            this.client = client;
        }

        /**
         * Fetch the application, with the documents it needs, the quote and
         * where the setup fee and the monthly lease stand.
         *
         * @return The application, whether it is still editable, and why not when it isn't
         * @throws SendlyException if the request fails
         */
        public ShortCodeApplicationView get() throws SendlyException {
            JsonObject response = client.get("/short_codes/application", null);
            return new ShortCodeApplicationView(response);
        }

        /**
         * Save answers on the application, creating it on the first call.
         * Fields Sendly or the carriers own are ignored and named back in
         * {@link ShortCodeApplicationView#getIgnoredFields()}.
         *
         * @param input The answers to save
         * @return The saved application
         * @throws SendlyException if the request fails; 409
         *         {@code short_code_locked} once the application is with
         *         Sendly or the carriers
         */
        public ShortCodeApplicationView update(ShortCodeApplicationInput input) throws SendlyException {
            return update(input, null);
        }

        /**
         * Save answers on the application, with per-call options. A repeat
         * of the same save with the same idempotency key within 24 hours
         * returns the first answer.
         *
         * @param input   The answers to save
         * @param options Per-call options (optional idempotency key)
         * @return The saved application
         * @throws SendlyException if the request fails
         */
        public ShortCodeApplicationView update(ShortCodeApplicationInput input, IdempotentRequestOptions options) throws SendlyException {
            JsonObject response = client.put("/short_codes/application", bodyOf(input), idempotencyKeyOf(options));
            return new ShortCodeApplicationView(response);
        }

        /**
         * Check the saved application against the carrier rules without
         * changing anything.
         *
         * @return {@code ok}, every issue found, and the documents still outstanding
         * @throws SendlyException if the request fails
         */
        public ShortCodePreflight check() throws SendlyException {
            return check(null);
        }

        /**
         * Check the application against the carrier rules without changing
         * anything, with unsaved answers checked on top of what is saved.
         *
         * @param input Unsaved answers to check as well (may be null)
         * @return {@code ok}, every issue found, and the documents still outstanding
         * @throws SendlyException if the request fails
         */
        public ShortCodePreflight check(ShortCodeApplicationInput input) throws SendlyException {
            JsonObject response = client.post("/short_codes/application/preflight", bodyOf(input));
            return new ShortCodePreflight(response);
        }

        /**
         * Submit the application to Sendly for review.
         * <p>
         * Submitting charges the one-time setup fee to the workspace's card
         * on file, so set {@code acceptTerms(true)} to accept it, the monthly
         * lease from go-live and the minimum term. The fee is refunded in full
         * if Sendly rejects the application before filing it. Submitting
         * twice is safe: the second call reports
         * {@link ShortCodeApplicationView#getAlreadySubmitted()} and charges
         * nothing.
         *
         * @param input {@code acceptTerms}, plus optional final answers saved before the check
         * @return The submitted application, with whether this call charged the fee
         * @throws SendlyException if the request fails: 403
         *         {@code verification_required} until the business is
         *         verified, 422 {@code short_code_invalid_application} or
         *         {@code short_code_ai_draft_unedited}, or 402
         *         {@code payment_method_required}, {@code payment_failed} or
         *         {@code payment_requires_authentication}
         */
        public ShortCodeApplicationView submit(ShortCodeApplicationInput input) throws SendlyException {
            return submit(input, null);
        }

        /**
         * Submit the application to Sendly for review, with per-call options.
         * A repeat of the same submit with the same idempotency key within 24
         * hours returns the first answer.
         *
         * @param input   {@code acceptTerms}, plus optional final answers
         * @param options Per-call options (optional idempotency key)
         * @return The submitted application
         * @throws SendlyException if the request fails
         */
        public ShortCodeApplicationView submit(ShortCodeApplicationInput input, IdempotentRequestOptions options) throws SendlyException {
            JsonObject response = client.post("/short_codes/application/submit", bodyOf(input), idempotencyKeyOf(options));
            return new ShortCodeApplicationView(response);
        }
    }

    private static JsonObject bodyOf(ShortCodeApplicationInput input) {
        return input != null ? input.toJson() : new JsonObject();
    }

    private static String idempotencyKeyOf(IdempotentRequestOptions options) {
        return options != null ? options.getIdempotencyKey() : null;
    }
}
