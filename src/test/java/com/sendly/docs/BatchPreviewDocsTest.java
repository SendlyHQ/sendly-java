package com.sendly.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The batch preview applies a live send's verification and destination checks
 * to every key, and sendBatch() skips them for a test key, so canSend() must
 * say it reads false for a test key on a workspace that is not verified yet.
 */
class BatchPreviewDocsTest {

    @Test
    void testCanSendSaysWhatATestKeySendSkips() throws IOException {
        String text = Files.readString(Paths.get("src/main/java/com/sendly/models/BatchPreviewResponse.java"));
        int method = text.indexOf("public boolean canSend()");
        String doc = text.substring(text.lastIndexOf("/**", method), method).replaceAll("\\s*\\*\\s*", " ");

        assertTrue(doc.contains("no more than 10,000 messages"), doc);
        assertTrue(doc.contains("a test key reads false here although its sandbox send goes through"), doc);
    }
}
