package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.CreateWhatsAppTemplateRequest;
import com.sendly.models.UpdateWhatsAppSenderProfileRequest;
import com.sendly.models.UpdateWhatsAppTemplateRequest;
import com.sendly.models.WhatsAppSenderProfile;
import com.sendly.models.WhatsAppSendersResponse;
import com.sendly.models.WhatsAppSignup;
import com.sendly.models.WhatsAppSignupSession;
import com.sendly.models.WhatsAppTemplate;
import com.sendly.models.WhatsAppTemplateDeletedResponse;
import com.sendly.models.WhatsAppTemplateListResponse;
import com.sendly.models.WhatsAppWindow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
 * Connecting a number is a one-time $19 setup (no monthly fee) and always ends
 * with a human step: {@code signup().create()} returns a {@code connectUrl}
 * that a person must open in a browser and log in with Facebook to link their
 * WhatsApp Business Account. Hand the URL to your user — the connection cannot
 * be completed programmatically.
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
     * Connect numbers to WhatsApp. Starting a signup returns a
     * {@code connectUrl} a human must complete in a browser.
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
     * message). Outside a window, send an approved template.
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
         * Requires a live API key.
         *
         * @param phoneNumber The number to connect, in E.164 format. Must be an
         *                    active number in your workspace (provisioned,
         *                    purchased, or ported into Sendly).
         * @return The signup with its {@code connectUrl}
         * @throws SendlyException if the request fails
         */
        public WhatsAppSignupSession create(String phoneNumber) throws SendlyException {
            validatePhone(phoneNumber);

            Map<String, Object> body = new HashMap<>();
            body.put("phoneNumber", phoneNumber);

            JsonObject response = client.post("/whatsapp/signup", body);
            return new WhatsAppSignupSession(response);
        }

        /**
         * Get the status of a WhatsApp signup.
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
         * yet — start one with {@code signup().create()}.
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
         * details. The sender must be actively connected to WhatsApp.
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
         * and {@code description} at 512. Requires a live API key.
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
         * List your WhatsApp templates.
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
         * is {@code APPROVED}. Requires a live API key.
         *
         * @param request The template definition
         * @return The created template (status {@code PENDING}), with any
         *         submission warnings
         * @throws SendlyException if the request fails
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
         * template goes back to {@code PENDING} review. Requires a live API
         * key.
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
         * live API key.
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

    private static String encodePathParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            return param;
        }
    }
}
