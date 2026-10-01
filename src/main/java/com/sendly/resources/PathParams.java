package com.sendly.resources;

import com.sendly.exceptions.ValidationException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Encodes a value being interpolated into a request path.
 *
 * <p>Without this an id such as {@code ../../account/keys} escapes its
 * collection and the request lands on a different endpoint, carrying the
 * caller's credentials.
 */
final class PathParams {

    private PathParams() {
    }

    static String encode(String value) {
        if (value == null || value.isEmpty() || value.equals(".") || value.equals("..")) {
            throw new ValidationException("Path parameter must not be empty, \".\" or \"..\"");
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
