package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.ShortLink;
import com.sendly.models.ShortLinkDisabledResponse;
import com.sendly.models.ShortLinkListResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Links resource — branded URL shortening.
 * <p>
 * Mint branded short links for a destination URL, list the links your workspace
 * has created (with click analytics), and enable or disable an individual link
 * (a per-link kill switch).
 * </p>
 * <p>
 * Shortening is gated behind the {@code url_shortener} rollout flag
 * (founder-only while dark); calls return a {@code not_found} error until the
 * flag is on for your account.
 * </p>
 */
public class LinksResource {
    private final Sendly client;

    public LinksResource(Sendly client) {
        this.client = client;
    }

    /**
     * Mint a branded short link for a destination URL. Uses your workspace's
     * brand slug when one is configured.
     *
     * @param url Destination URL to shorten (must be an http:// or https:// URL)
     * @return The short link (code, shortUrl, destinationUrl)
     * @throws SendlyException if the request fails
     */
    public ShortLink create(String url) throws SendlyException {
        validateUrl(url);

        Map<String, Object> body = new HashMap<>();
        body.put("url", url);

        JsonObject response = client.post("/links", body);
        return new ShortLink(response);
    }

    /**
     * List the short links your workspace has created, newest first, with click
     * counts and a 14-day daily click histogram.
     *
     * @return The links and a total count
     * @throws SendlyException if the request fails
     */
    public ShortLinkListResponse list() throws SendlyException {
        JsonObject response = client.get("/links", null);
        return new ShortLinkListResponse(response);
    }

    /**
     * List short links with pagination.
     *
     * @param limit  Maximum number of links to return (default 50, max 200)
     * @param offset Number of links to skip
     * @return The links and a total count
     * @throws SendlyException if the request fails
     */
    public ShortLinkListResponse list(int limit, int offset) throws SendlyException {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("offset", String.valueOf(offset));

        JsonObject response = client.get("/links", params);
        return new ShortLinkListResponse(response);
    }

    /**
     * Enable or disable a short link. A disabled link's redirect returns 404
     * until it is re-enabled.
     *
     * @param code     The short link code
     * @param disabled true to disable, false to re-enable
     * @return The link's code and new disabled state
     * @throws SendlyException if the request fails
     */
    public ShortLinkDisabledResponse setDisabled(String code, boolean disabled) throws SendlyException {
        if (code == null || code.isEmpty()) {
            throw new ValidationException("Link code is required");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("disabled", disabled);

        JsonObject response = client.patch("/links/" + encodePathParam(code), body);
        return new ShortLinkDisabledResponse(response);
    }

    /**
     * Disable a short link (its redirect returns 404 until re-enabled).
     * Convenience wrapper over {@link #setDisabled(String, boolean)}.
     *
     * @param code The short link code
     * @return The link's code and new disabled state
     * @throws SendlyException if the request fails
     */
    public ShortLinkDisabledResponse disable(String code) throws SendlyException {
        return setDisabled(code, true);
    }

    /**
     * Re-enable a previously disabled short link. Convenience wrapper over
     * {@link #setDisabled(String, boolean)}.
     *
     * @param code The short link code
     * @return The link's code and new disabled state
     * @throws SendlyException if the request fails
     */
    public ShortLinkDisabledResponse enable(String code) throws SendlyException {
        return setDisabled(code, false);
    }

    private void validateUrl(String url) throws ValidationException {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("A destination 'url' is required");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ValidationException("url must be an http:// or https:// URL");
        }
    }

    private String encodePathParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            return param;
        }
    }
}
