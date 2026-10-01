package com.sendly.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The RateLimitException class doc must describe the 429s the client waits
 * out and retries: every code in the retry set, and a 429 that has no code.
 */
class RateLimitDocsTest {

    private static String classDoc() throws IOException {
        String text = Files.readString(Paths.get("src/main/java/com/sendly/exceptions/RateLimitException.java"));
        return text.substring(text.indexOf("/**"), text.indexOf("public class RateLimitException"))
                .replaceAll("\\s*\\*\\s*", " ");
    }

    @Test
    void testClassDocNamesEveryCodeTheClientRetries() throws IOException {
        String doc = classDoc();

        for (String code : new String[] {"rate_limit_exceeded", "rate_limited", "too_many_concurrent_verifications",
                "provision_rate_limit"}) {
            assertTrue(doc.contains("{@code " + code + "}"), "does not name " + code + ": " + doc);
        }
    }

    @Test
    void testClassDocSaysA429WithoutACodeIsRetried() throws IOException {
        String doc = classDoc();

        assertTrue(doc.contains("a 429 without a code"), doc);
        assertFalse(doc.contains("retries only {@code rate_limit_exceeded} and {@code too_many_concurrent_verifications}"), doc);
    }
}
