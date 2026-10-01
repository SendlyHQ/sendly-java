package com.sendly.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Backfilled message events reuse the event id the original dispatch carried,
 * and a message's sent and delivered events share data.object.id, so the docs
 * must tell integrators to dedupe on event.id.
 */
class BackfillDocsTest {

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(Paths.get("src/main/java"))) {
            return files.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }

    @Test
    void testNoSourceSaysBackfilledEventsGetFreshIds() throws IOException {
        for (Path source : sources()) {
            String text = Files.readString(source);
            assertFalse(text.contains("fresh IDs"), source + " says synthesized events have fresh IDs");
            assertFalse(text.contains("event.data.object.id"), source + " says to dedupe on data.object.id");
        }
    }

    @Test
    void testBackfillSaysToDedupeOnTheEventId() throws IOException {
        String text = Files.readString(Paths.get("src/main/java/com/sendly/resources/WebhooksResource.java"));
        String doc = text.substring(text.indexOf("Backfill missed webhook events"), text.indexOf("public JsonObject backfill("))
                .replaceAll("\\s*\\*\\s*", " ");

        assertTrue(doc.contains("same event id the original dispatch used, so dedupe on event.id"), doc);
        assertTrue(doc.contains("Do not dedupe on data.object.id"), doc);
    }
}
