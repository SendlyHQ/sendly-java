package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.CreateWhatsAppSignupRequest;
import com.sendly.models.CreateWhatsAppTemplateRequest;
import com.sendly.models.UpdateWhatsAppConversationalComponentsRequest;
import com.sendly.models.UpdateWhatsAppSenderProfileRequest;
import com.sendly.models.UpdateWhatsAppTemplateRequest;
import com.sendly.models.WhatsAppCallingSettings;
import com.sendly.models.WhatsAppConversationalComponents;
import com.sendly.models.WhatsAppSenderProfile;
import com.sendly.models.WhatsAppSendersResponse;
import com.sendly.models.WhatsAppSignup;
import com.sendly.models.WhatsAppSignupSession;
import com.sendly.models.WhatsAppTemplate;
import com.sendly.models.WhatsAppTemplateDeletedResponse;
import com.sendly.models.WhatsAppTemplateListResponse;
import com.sendly.models.WhatsAppWindow;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * WhatsApp resource — connect senders, manage templates, and check 24-hour
 * windows.
 * <p>
 * WhatsApp is a first-class Sendly channel: connect a number you own, create
 * Meta-reviewed message templates, and send via
 * {@code messages().send(SendWhatsAppMessageRequest)}.
 * <p>
 * Connecting a number is a one-time $19 setup (no monthly fee). The first
 * number ends with a human step: {@code signup().create()} returns a
 * {@code connectUrl} that a person must open in a browser and log in with
 * Facebook to link their WhatsApp Business Account. Hand the URL to your
 * user; that step cannot be completed programmatically. Once an account is
 * connected, more numbers can be added to it without Facebook: create the
 * signup with the account's {@code businessAccountId}, and submit the code
 * WhatsApp sends the number with {@code signup().verify(id, code)}.
 * <p>
 * Two ways to reach a recipient:
 * <ul>
 *   <li><b>Inside a 24-hour window</b> (the recipient messaged you in the last
 *       24h): free-form text and media are allowed. Check with
 *       {@link #window(String, String)}.</li>
 *   <li><b>Anytime</b>: an approved template. Templates are reviewed by Meta
 *       (typically 24-48h) and categorized as authentication, utility, or
 *       marketing — pricing follows the category and destination country.
 *       Note: Meta has paused marketing template delivery to US (+1)
 *       numbers.</li>
 * </ul>
 * <p>
 * Pricing: free-form text or media inside the 24-hour window costs 1 credit
 * each for the first 1,000 per sending number per calendar month (UTC), then
 * the destination's utility template price; countries without a listed price
 * use the default utility price of 12 credits. Templates are priced by
 * category and destination country; countries without a listed price use 33
 * (marketing), 12 (utility) and 12 (authentication) credits. A failed send
 * gives its slot back.
 * <p>
 * Scopes and keys: sends go through
 * {@code messages().send(SendWhatsAppMessageRequest)} and need
 * {@code sms:send}, not {@code whatsapp:write}, and a live key. Reads (signup
 * status, templates, the window, senders, sender profiles and
 * conversational components) need {@code whatsapp:read} and accept test
 * keys. Signup (including verify and resend), template create/edit/delete,
 * profile edits (including the photo), conversational components and the
 * calling switch need {@code whatsapp:write} and a live key (otherwise 403
 * {@code whatsapp_requires_live_key}). In a team workspace, connecting and
 * sender edits need an owner or admin ({@code settings:write}), and
 * template writes need an owner, admin or member ({@code templates:write});
 * a missing role returns 403 {@code insufficient_permissions}. A sender
 * method for a number that isn't connected gets 404
 * {@code whatsapp_sender_not_connected}.
 * <p>
 * WhatsApp is enabled per person: the user who owns the API key, not the
 * workspace. While it is off, sends return 403 {@code whatsapp_not_enabled}
 * and every method on this resource gets 404 {@code not_found}.
 *
 * <pre>{@code
 * // 1. Connect a number ($19 one-time, no monthly fee). The connect URL
 * //    must be opened by a human — they log in with Facebook in a browser.
 * WhatsAppSignupSession signup = client.whatsapp().signup().create("+15559876543");
 * System.out.println("Have your user open: " + signup.getConnectUrl());
 *
 * // 2. Poll until active
 * WhatsAppSignup status = client.whatsapp().signup().get(signup.getId());
 *
 * // 3. Create a template (Meta reviews it, usually 24-48h)
 * client.whatsapp().templates().create(CreateWhatsAppTemplateRequest.builder()
 *     .sender("+15559876543")
 *     .name("order_shipped")
 *     .language("en_US")
 *     .category("UTILITY")
 *     .body("Hi {{1}}, your order {{2}} has shipped!")
 *     .examples(Map.of("1", "Sam", "2", "#4821"))
 *     .build());
 *
 * // 4. Send — free-form inside an open 24h window, template anytime
 * WhatsAppWindow window = client.whatsapp().window("+15559876543", "+15551234567");
 * }</pre>
 *
 * @see <a href="https://sendly.live/docs/whatsapp">WhatsApp docs</a>
 */
public class WhatsAppResource {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private final Sendly client;
    private final Signup signup;
    private final Senders senders;
    private final Templates templates;

    public WhatsAppResource(Sendly client) {
        this.client = client;
        this.signup = new Signup(client);
        this.senders = new Senders(client);
        this.templates = new Templates(client);
    }

    /**
     * Connect numbers to WhatsApp. A Facebook connection returns a
     * {@code connectUrl} a human must complete in a browser; a number added
     * to an already-connected account is verified with a code instead.
     */
    public Signup signup() {
        return signup;
    }

    /**
     * List the numbers connected (or connecting) to WhatsApp and manage
     * their business profiles.
     */
    public Senders senders() {
        return senders;
    }

    /**
     * Manage Meta-reviewed message templates.
     */
    public Templates templates() {
        return templates;
    }

    /**
     * Check whether a 24-hour customer-service window is open between one of
     * your WhatsApp senders and a recipient.
     * <p>
     * Free-form text and media only deliver while a window is open (it opens
     * when the recipient messages you and lasts 24h from their last inbound
     * message). Outside a window, send an approved template. Needs the
     * {@code whatsapp:read} scope; test keys work.
     * </p>
     * <p>
     * The response is exactly {@code open} and {@code expiresAt}: with no
     * window on record {@code open} is false and {@code expiresAt} is null;
     * after a window has expired {@code open} is false and {@code expiresAt}
     * is the past expiry.
     * </p>
     *
     * @param from Your WhatsApp-connected sending number, in E.164 format
     * @param to   The recipient's number, in E.164 format
     * @return Whether the window is open and when it closes
     * @throws SendlyException if the request fails
     */
    public WhatsAppWindow window(String from, String to) throws SendlyException {
        validatePhone(from);
        validatePhone(to);

        Map<String, String> params = new HashMap<>();
        params.put("from", from);
        params.put("to", to);

        JsonObject response = client.get("/whatsapp/window", params);
        return new WhatsAppWindow(response);
    }

    /**
     * WhatsApp signup sub-resource — connect numbers to WhatsApp.
     */
    public static class Signup {
        private final Sendly client;

        Signup(Sendly client) {
            this.client = client;
        }

        /**
         * Start connecting a number to WhatsApp.
         * <p>
         * Charges a one-time $19 setup fee (no monthly fee) and returns a
         * {@code connectUrl}. Completing the connection requires a human: hand
         * the URL to your user — they open it in a browser and log in with
         * Facebook to link their WhatsApp Business Account. Then poll
         * {@link #get(String)} until the status is {@code active}.
         * <p>
         * Calling again for a number with an in-flight signup returns the
         * existing signup (same {@code connectUrl}) without charging again.
         * Requires a live API key with the {@code whatsapp:write} scope (a
         * test key gets 403 {@code whatsapp_requires_live_key}) and, in a team
         * workspace, an owner or admin ({@code settings:write}). After the
         * Facebook step the signup stays {@code registering} while WhatsApp
         * activates the number. Activation usually takes a few minutes but can
         * take hours. If it hasn't finished about 6 hours after the session
         * began, the session fails with {@code registration_timeout} and the
         * fee is refunded. If the connection fails, the $19 fee is refunded
         * automatically; once the number has connected there is no refund,
         * and a later disconnect gets nothing back.
         *
         * @param phoneNumber The number to connect, in E.164 format. Must be an
         *                    active number in your workspace (provisioned,
         *                    purchased, or fully ported into Sendly).
         * @return The signup with its {@code connectUrl}
         * @throws SendlyException if the request fails; {@code
         *                         whatsapp_unavailable} (503) while WhatsApp
         *                         connections are unavailable, with nothing
         *                         charged, {@code retryAfter} 3600 in the
         *                         response body and a
         *                         {@code Retry-After: 3600} header, retried
         *                         like any 5xx before it is thrown. Only
         *                         signup returns it; no send does.
         * @throws com.sendly.exceptions.RateLimitException {@code
         *                         whatsapp_signup_limit_reached} (429) after 5
         *                         failed, charged signups in 24 hours; not
         *                         retried, try again the next day
         */
        public WhatsAppSignupSession create(String phoneNumber) throws SendlyException {
            validatePhone(phoneNumber);

            Map<String, Object> body = new HashMap<>();
            body.put("phoneNumber", phoneNumber);

            JsonObject response = client.post("/whatsapp/signup", body);
            return new WhatsAppSignupSession(response);
        }

        /**
         * Start connecting a number, or add one to a WhatsApp Business
         * Account already connected in this workspace.
         * <p>
         * With only {@code phoneNumber} this is {@link #create(String)}.
         * With {@code businessAccountId} there is no Facebook step: the same
         * number checks run, the same one-time $19 fee is charged (before
         * the code is requested, and refunded automatically if the signup
         * fails), and WhatsApp sends the number a 6-digit code by SMS or, with
         * {@code verificationMethod("voice")}, a voice call. The signup comes
         * back {@code verifying} with no {@code connectUrl}; submit the code
         * with {@link #verify(String, String)}. A code sent by SMS arrives on
         * the number itself, so once it has arrived
         * {@code get(id).getVerificationCode()} returns it. Calling again for
         * a number that is already verifying returns that signup without
         * charging again or sending a second code, with two exceptions. A
         * verifying signup more than 3 hours old is failed with
         * {@code verification_expired} (its fee is refunded) and a new
         * signup is started, charged and sent a new code. If the first call
         * stopped before the code was requested, the repeat call requests it
         * (201). Same scope, key and role rules as {@link #create(String)}.
         *
         * @param request The number, and for an addition the account id and
         *                optional verification method and display name
         * @return The signup: a {@code connectUrl} for a Facebook
         *         connection, or {@code verifying} for an addition
         * @throws SendlyException if the request fails. For an addition:
         *         404 {@code whatsapp_business_account_not_found} (no
         *         connected account with that id and an active number in this
         *         workspace), 400 {@code display_name_required} (no
         *         {@code displayName} given and the account has none to reuse),
         *         409 {@code whatsapp_signup_in_progress} (a Facebook
         *         connection for the number is in flight), 409
         *         {@code whatsapp_already_enabled}, 402
         *         {@code payment_method_required} or {@code payment_failed},
         *         429 {@code whatsapp_signup_limit_reached}, and
         *         {@code whatsapp_verification_start_failed}: a 422 when
         *         WhatsApp refused to send the code (final) or a 502 when it
         *         couldn't be reached; either way the signup failed and the fee
         *         is refunded. With {@code businessAccountId} the request is
         *         not retried automatically (only a 429 rate limit is): a
         *         5xx, a 4xx or a network error is thrown on the first
         *         attempt, because each attempt after a failed one starts a new
         *         signup that is charged again; a Facebook connection keeps
         *         the usual retries. A Facebook connection for a number that
         *         is verifying gets 409
         *         {@code whatsapp_verification_in_progress} with the signup's
         *         {@code id}.
         */
        public WhatsAppSignupSession create(CreateWhatsAppSignupRequest request) throws SendlyException {
            if (request == null) {
                throw new ValidationException("Request is required");
            }
            validatePhone(request.getPhoneNumber());

            String businessAccountId = request.getBusinessAccountId();
            if (businessAccountId != null && businessAccountId.trim().isEmpty()) {
                throw new ValidationException("businessAccountId must be a non-empty string");
            }
            boolean addition = businessAccountId != null && !businessAccountId.isEmpty();
            JsonObject response = client.post("/whatsapp/signup", request, null, true, !addition);
            return new WhatsAppSignupSession(response);
        }

        /**
         * Get the status of a WhatsApp signup. Needs the
         * {@code whatsapp:read} scope; test keys work.
         *
         * @param id The signup's id
         * @return The signup status
         * @throws SendlyException if the request fails (404 when no such signup)
         */
        public WhatsAppSignup get(String id) throws SendlyException {
            if (id == null || id.isEmpty()) {
                throw new ValidationException("Signup ID is required");
            }

            JsonObject response = client.get("/whatsapp/signup/" + encodePathParam(id), null);
            return new WhatsAppSignup(response);
        }

        /**
         * Submit the code WhatsApp sent a number being added to a connected
         * account.
         * <p>
         * The code is 6 digits; spaces and dashes are ignored. A correct code
         * makes the signup {@code active} and fires
         * {@code whatsapp_account.connected}. A signup that is already
         * {@code active} is returned as it is. The request is not retried
         * automatically (only a 429 rate limit is): every submission uses one
         * of the 5 attempts, so a 5xx, a 4xx or a network error is thrown on
         * the first attempt; check with {@link #get(String)} before
         * submitting again. Requires a live API key with
         * the {@code whatsapp:write} scope and, in a team workspace, an owner
         * or admin ({@code settings:write}).
         *
         * @param id   The signup's id
         * @param code The code WhatsApp sent the number
         * @return The signup, {@code active} once the code is accepted
         * @throws SendlyException if the request fails: 400
         *         {@code invalid_verification_code} (not 6 digits); 422
         *         {@code whatsapp_verification_code_invalid} (wrong code;
         *         {@code getResponseBody()} carries {@code attemptsRemaining});
         *         409 {@code whatsapp_verification_failed} after 5 wrong
         *         codes (the signup failed and the fee is refunded; thrown on
         *         the first attempt, never retried); 409
         *         {@code whatsapp_verification_busy} (another code for the
         *         number is being checked; try again); 502
         *         {@code whatsapp_verification_unavailable} (WhatsApp couldn't
         *         be reached; the attempt isn't counted); 502
         *         {@code whatsapp_activation_pending} (the code was accepted but
         *         the connection didn't finish; check back with
         *         {@link #get(String)}); 409 {@code signup_not_active} (the
         *         signup isn't waiting for a code, or began more than 3 hours
         *         ago); 404 {@code signup_not_found}.
         */
        public WhatsAppSignup verify(String id, String code) throws SendlyException {
            if (id == null || id.isEmpty()) {
                throw new ValidationException("Signup ID is required");
            }
            if (code == null || code.isEmpty()) {
                throw new ValidationException("Verification code is required");
            }

            Map<String, Object> body = new HashMap<>();
            body.put("code", code);

            JsonObject response = client.post(
                "/whatsapp/signup/" + encodePathParam(id) + "/verify", body, null, true, false);
            return new WhatsAppSignup(response);
        }

        /**
         * Ask WhatsApp to send a new code by SMS. Same as
         * {@link #resend(String, String)} with {@code sms}.
         *
         * @param id The signup's id
         * @return The signup, still {@code verifying}
         * @throws SendlyException if the request fails
         */
        public WhatsAppSignup resend(String id) throws SendlyException {
            return resend(id, null);
        }

        /**
         * Ask WhatsApp to send a new code to a number being added to a
         * connected account.
         * <p>
         * Codes can be sent at most once every 30 seconds, counted from the
         * signup's last change, a code submission included. A signup that is
         * already {@code active} is returned as it is. Requires a live API
         * key with the {@code whatsapp:write} scope and, in a team workspace,
         * an owner or admin ({@code settings:write}).
         *
         * @param id                 The signup's id
         * @param verificationMethod {@code sms} or {@code voice} (see
         *                           {@code WhatsAppVerificationMethod}); null
         *                           sends none, and the API then uses
         *                           {@code sms}
         * @return The signup, still {@code verifying}
         * @throws com.sendly.exceptions.RateLimitException {@code
         *         whatsapp_verification_resend_too_soon} (429) within 30
         *         seconds of the last change, with {@code getRetryAfter()}
         *         the seconds to wait; thrown at once, not retried
         * @throws SendlyException if the request fails:
         *         {@code whatsapp_verification_resend_failed} (a 422 when
         *         WhatsApp wouldn't send another code yet, a 502 when it
         *         couldn't be reached), 409 {@code signup_not_active}, 404
         *         {@code signup_not_found}
         */
        public WhatsAppSignup resend(String id, String verificationMethod) throws SendlyException {
            if (id == null || id.isEmpty()) {
                throw new ValidationException("Signup ID is required");
            }

            Map<String, Object> body = new HashMap<>();
            if (verificationMethod != null) {
                body.put("verificationMethod", verificationMethod);
            }

            JsonObject response = client.post("/whatsapp/signup/" + encodePathParam(id) + "/resend", body);
            return new WhatsAppSignup(response);
        }
    }

    /**
     * WhatsApp senders sub-resource — the numbers connected to WhatsApp and
     * their business profiles.
     */
    public static class Senders {
        private final Sendly client;

        Senders(Sendly client) {
            this.client = client;
        }

        /**
         * List your WhatsApp senders.
         * <p>
         * Returns the numbers connected (or connecting) to WhatsApp on your
         * workspace, newest first. An empty list means no number is connected
         * yet — start one with {@code signup().create()}. Needs the
         * {@code whatsapp:read} scope; test keys work.
         *
         * @return Your senders with connection status and quality rating
         * @throws SendlyException if the request fails
         */
        public WhatsAppSendersResponse list() throws SendlyException {
            JsonObject response = client.get("/whatsapp/senders", null);
            return new WhatsAppSendersResponse(response);
        }

        /**
         * Get a sender's WhatsApp business profile.
         * <p>
         * Returns what recipients see on the sender's contact card: display
         * name, photo, category, about line, description, and contact
         * details. The sender must be actively connected to WhatsApp. Needs
         * the {@code whatsapp:read} scope; test keys work.
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @return The sender's business profile
         * @throws SendlyException if the request fails (404 when the number
         *                         isn't connected)
         */
        public WhatsAppSenderProfile getProfile(String phoneNumber) throws SendlyException {
            validatePhone(phoneNumber);

            JsonObject response = client.get(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/profile", null);
            return new WhatsAppSenderProfile(response);
        }

        /**
         * Update a sender's WhatsApp business profile.
         * <p>
         * Supply only the fields to change (at least one); omitted fields
         * keep their current value. {@code about} is capped at 139 characters
         * and {@code description} at 512. Requires a live API key with the
         * {@code whatsapp:write} scope and, in a team workspace, an owner or
         * admin ({@code settings:write}).
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @param request     The profile fields to change
         * @return The updated business profile
         * @throws SendlyException if the request fails (404 when the number
         *                         isn't connected)
         */
        public WhatsAppSenderProfile updateProfile(String phoneNumber, UpdateWhatsAppSenderProfileRequest request)
                throws SendlyException {
            validatePhone(phoneNumber);
            if (request == null) {
                throw new ValidationException("Request is required");
            }

            JsonObject response = client.patch(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/profile", request);
            return new WhatsAppSenderProfile(response);
        }

        /**
         * Upload a sender's profile photo, replacing the current one.
         * <p>
         * The photo must be a JPEG or PNG (checked from the file's bytes) of
         * at most 5 MB. WhatsApp wants it square and at least 192 pixels
         * wide; 640 pixels is recommended. The upload is not retried
         * automatically (only a 429 rate limit is): a 5xx, a 4xx or a
         * network error is thrown on the first attempt. Requires a live API
         * key with the {@code whatsapp:write} scope and, in a team workspace,
         * an owner or admin ({@code settings:write}).
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @param file        The photo
         * @return The updated business profile
         * @throws SendlyException if the request fails: 400
         *         {@code whatsapp_profile_photo_invalid} (not a JPEG or PNG),
         *         413 {@code whatsapp_profile_photo_too_large} (over 5 MB), 502
         *         {@code whatsapp_profile_update_failed} (WhatsApp refused the
         *         photo or couldn't be reached; fix the image and try again),
         *         404 {@code whatsapp_sender_not_connected}
         */
        public WhatsAppSenderProfile uploadProfilePhoto(String phoneNumber, File file) throws SendlyException {
            validatePhone(phoneNumber);
            if (file == null || !file.exists()) {
                throw new ValidationException("File is required and must exist");
            }
            if (file.length() == 0) {
                throw new ValidationException("Photo file is empty");
            }

            RequestBody fileBody = RequestBody.create(file, MediaType.parse(contentTypeOf(file.getName())));
            return uploadProfilePhoto(phoneNumber, file.getName(), fileBody);
        }

        /**
         * Upload a sender's profile photo from memory. See
         * {@link #uploadProfilePhoto(String, File)}.
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @param data        The photo's bytes, a JPEG or PNG of at most 5 MB
         * @param filename    A file name for the upload, such as
         *                    {@code logo.png}
         * @return The updated business profile
         * @throws SendlyException if the request fails
         */
        public WhatsAppSenderProfile uploadProfilePhoto(String phoneNumber, byte[] data, String filename)
                throws SendlyException {
            validatePhone(phoneNumber);
            if (data == null || data.length == 0) {
                throw new ValidationException("Photo data is required");
            }
            String name = filename == null || filename.isEmpty() ? "photo" : filename;

            RequestBody fileBody = RequestBody.create(data, MediaType.parse(contentTypeOf(name)));
            return uploadProfilePhoto(phoneNumber, name, fileBody);
        }

        private WhatsAppSenderProfile uploadProfilePhoto(String phoneNumber, String filename, RequestBody fileBody)
                throws SendlyException {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", filename, fileBody)
                    .build();

            JsonObject response = client.postMultipart(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/profile/photo", requestBody, false);
            return new WhatsAppSenderProfile(response);
        }

        /**
         * Remove a sender's profile photo. Requires a live API key with the
         * {@code whatsapp:write} scope and, in a team workspace, an owner or
         * admin ({@code settings:write}).
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @return The business profile, with no {@code profilePhotoUrl}
         * @throws SendlyException if the request fails: 502
         *         {@code whatsapp_profile_update_failed}, 404
         *         {@code whatsapp_sender_not_connected}
         */
        public WhatsAppSenderProfile deleteProfilePhoto(String phoneNumber) throws SendlyException {
            validatePhone(phoneNumber);

            JsonObject response = client.delete(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/profile/photo");
            return new WhatsAppSenderProfile(response);
        }

        /**
         * Get a sender's ice breakers (tappable suggestions shown when someone
         * opens a chat with the business for the first time) and commands
         * (shown when the customer types "/"). Needs the
         * {@code whatsapp:read} scope; test keys work.
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @return The ice breakers and commands; empty lists when none are set
         * @throws SendlyException if the request fails: 502
         *         {@code whatsapp_conversational_components_fetch_failed},
         *         404 {@code whatsapp_sender_not_connected}
         */
        public WhatsAppConversationalComponents getConversationalComponents(String phoneNumber)
                throws SendlyException {
            validatePhone(phoneNumber);

            JsonObject response = client.get(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/conversational_components", null);
            return new WhatsAppConversationalComponents(response);
        }

        /**
         * Replace a sender's ice breakers, commands, or both.
         * <p>
         * Each list you set replaces the stored one and an empty list clears
         * it; a list you leave unset is kept. Ice breakers: at most 4, each
         * 1-80 characters after trimming, no two the same ignoring case.
         * Commands: at most 30; each command is letters, digits or
         * underscores, 1-32 characters (a leading "/" is stripped), with a
         * description of 1-256 characters, and no command twice. Requires a
         * live API key with the {@code whatsapp:write} scope and, in a team
         * workspace, an owner or admin ({@code settings:write}).
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @param request     The lists to replace
         * @return The ice breakers and commands now stored
         * @throws SendlyException if the request fails: 400
         *         {@code invalid_request} with a message naming the rule
         *         broken, 502
         *         {@code whatsapp_conversational_components_update_failed},
         *         404 {@code whatsapp_sender_not_connected}
         */
        public WhatsAppConversationalComponents updateConversationalComponents(
                String phoneNumber, UpdateWhatsAppConversationalComponentsRequest request) throws SendlyException {
            validatePhone(phoneNumber);
            if (request == null) {
                throw new ValidationException("Request is required");
            }

            JsonObject response = client.patch(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/conversational_components", request);
            return new WhatsAppConversationalComponents(response);
        }

        /**
         * Switch WhatsApp calling on or off for a sender.
         * <p>
         * Once it is on, a WhatsApp user calling the number rings exactly
         * like a phone call (in the dashboard or to an AI agent, per the
         * number's voice mode), billed at the normal inbound call rate.
         * Turning it on needs voice switched on for the number first, in its
         * voice settings ({@code voice().numbers()}). There is no API for
         * placing WhatsApp calls. Requires a live API key with the
         * {@code whatsapp:write} scope and, in a team workspace, an owner or
         * admin ({@code settings:write}).
         *
         * @param phoneNumber The WhatsApp-connected sender, in E.164 format
         * @param enabled     True to switch calling on, false to switch it off
         * @return Whether calling is on, and whether the business may place
         *         calls from the number
         * @throws SendlyException if the request fails: 409
         *         {@code voice_not_enabled} (switch voice on for the number
         *         first), 422 {@code whatsapp_calling_unavailable} (Meta
         *         refused: the account must be allowed to message at least
         *         2,000 people a day and the display name must be approved),
         *         502 {@code whatsapp_calling_update_failed} (try again), 404
         *         {@code whatsapp_sender_not_connected}
         */
        public WhatsAppCallingSettings setCalling(String phoneNumber, boolean enabled) throws SendlyException {
            validatePhone(phoneNumber);

            Map<String, Object> body = new HashMap<>();
            body.put("enabled", enabled);

            JsonObject response = client.patch(
                "/whatsapp/senders/" + encodePathParam(phoneNumber) + "/calling", body);
            return new WhatsAppCallingSettings(response);
        }
    }

    /**
     * WhatsApp templates sub-resource — Meta-reviewed message templates.
     */
    public static class Templates {
        private final Sendly client;

        Templates(Sendly client) {
            this.client = client;
        }

        /**
         * List your WhatsApp templates. Needs the {@code whatsapp:read}
         * scope; test keys work.
         *
         * @return Your templates with review status and quality rating
         * @throws SendlyException if the request fails
         */
        public WhatsAppTemplateListResponse list() throws SendlyException {
            JsonObject response = client.get("/whatsapp/templates", null);
            return new WhatsAppTemplateListResponse(response);
        }

        /**
         * Create a template and submit it to Meta for review.
         * <p>
         * Review usually takes 24-48h; the template is usable once its status
         * is {@code APPROVED}. Requires a live API key with the
         * {@code whatsapp:write} scope and, in a team workspace, an owner,
         * admin or member ({@code templates:write}). The category is required
         * ({@code UTILITY}, {@code AUTHENTICATION} or {@code MARKETING}) and
         * has no default. A marketing template without an opt-out button is
         * still accepted, with a warning.
         *
         * @param request The template definition
         * @return The created template (status {@code PENDING}), with any
         *         submission warnings
         * @throws SendlyException if the request fails; a template the API
         *         refuses is a 400 {@code ValidationException} whose
         *         {@code getApiErrorCode()} is the {@code template_*} reason
         *         and whose message says what to fix:
         *         {@code template_category_invalid} (category missing or not
         *         one of the three),
         *         {@code template_authentication_otp_button_required},
         *         {@code template_authentication_no_links} (a link in the body
         *         or a URL button on an authentication template) or
         *         {@code template_header_variable_unsupported} for a header
         *         containing {@code {{n}}}. A sender that isn't connected gets
         *         404 {@code whatsapp_sender_not_connected}, checked first.
         */
        public WhatsAppTemplate create(CreateWhatsAppTemplateRequest request) throws SendlyException {
            if (request == null) {
                throw new ValidationException("Request is required");
            }
            validatePhone(request.getSender());
            if (request.getName() == null || request.getName().isEmpty()) {
                throw new ValidationException("Template name is required");
            }
            if (request.getLanguage() == null || request.getLanguage().isEmpty()) {
                throw new ValidationException("Template language is required");
            }
            if (request.getCategory() == null || request.getCategory().isEmpty()) {
                throw new ValidationException("Template category is required");
            }
            if (request.getBody() == null || request.getBody().isEmpty()) {
                throw new ValidationException("Template body is required");
            }

            JsonObject response = client.post("/whatsapp/templates", request);
            return new WhatsAppTemplate(response);
        }

        /**
         * Edit an APPROVED or REJECTED template and resubmit it for review.
         * <p>
         * This is the recovery path for rejections: template names are locked
         * for ~30 days after deletion, so editing a rejected template (rather
         * than deleting and re-creating it) is the way to fix it. The updated
         * template goes back to {@code PENDING} review. The category can't be
         * changed. Requires a live API key with the {@code whatsapp:write}
         * scope and, in a team workspace, an owner, admin or member
         * ({@code templates:write}).
         *
         * @param id      The template's id
         * @param request The fields to change (omitted fields are kept)
         * @return The updated template (status {@code PENDING})
         * @throws SendlyException if the request fails
         */
        public WhatsAppTemplate update(String id, UpdateWhatsAppTemplateRequest request) throws SendlyException {
            if (id == null || id.isEmpty()) {
                throw new ValidationException("Template ID is required");
            }
            if (request == null) {
                throw new ValidationException("Request is required");
            }

            JsonObject response = client.patch("/whatsapp/templates/" + encodePathParam(id), request);
            return new WhatsAppTemplate(response);
        }

        /**
         * Delete a template.
         * <p>
         * Meta locks a deleted template's name for ~30 days — re-creating it
         * fails with {@code template_name_locked} until the lock lifts. To fix
         * a rejected template, prefer
         * {@link #update(String, UpdateWhatsAppTemplateRequest)}. Requires a
         * live API key with the {@code whatsapp:write} scope and, in a team
         * workspace, an owner, admin or member ({@code templates:write}).
         *
         * @param id The template's id
         * @return Deletion confirmation
         * @throws SendlyException if the request fails
         */
        public WhatsAppTemplateDeletedResponse delete(String id) throws SendlyException {
            if (id == null || id.isEmpty()) {
                throw new ValidationException("Template ID is required");
            }

            JsonObject response = client.delete("/whatsapp/templates/" + encodePathParam(id));
            return new WhatsAppTemplateDeletedResponse(response);
        }
    }

    private static void validatePhone(String phone) throws ValidationException {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(
                "Invalid phone number format. Use E.164 format (e.g., +15551234567)"
            );
        }
    }

    private static String contentTypeOf(String filename) {
        String contentType = URLConnection.guessContentTypeFromName(filename);
        return contentType != null ? contentType : "application/octet-stream";
    }

    private static String encodePathParam(String param) {
        return PathParams.encode(param);
    }
}
