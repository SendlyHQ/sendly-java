package com.sendly.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GET /account/keys lists revoked keys but sends no revokedAt, so the
 * getRevokedAt() doc must not promise a time for every revoked key.
 */
class ApiKeyDocsTest {

    @Test
    void testRevokedAtSaysTheKeyListDoesNotSendIt() throws IOException {
        String text = Files.readString(Paths.get("src/main/java/com/sendly/models/ApiKey.java"));
        int method = text.indexOf("public String getRevokedAt()");
        String doc = text.substring(text.lastIndexOf("/**", method), method).replaceAll("\\s*\\*\\s*", " ");

        assertTrue(doc.contains("listApiKeys()"), doc);
        assertTrue(doc.contains("getApiKey(id)"), doc);
    }
}
