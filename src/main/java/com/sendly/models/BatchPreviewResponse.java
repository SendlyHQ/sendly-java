package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response from previewing a batch (dry run).
 */
public class BatchPreviewResponse {
    private static final int MAX_BATCH_MESSAGES = 10000;

    private final boolean canSend;
    private final int totalMessages;
    private final int willSend;
    private final int blocked;
    private final int creditsNeeded;
    private final int currentBalance;
    private final boolean hasEnoughCredits;
    private final int duplicates;
    private final List<String> warnings;
    private final List<BatchPreviewItem> messages;
    private final Map<String, Integer> blockReasons;

    /**
     * Create a BatchPreviewResponse from a JSON object.
     */
    public BatchPreviewResponse(JsonObject json) {
        this.totalMessages = intOf(json, "totalMessages", "total");
        this.willSend = intOf(json, "willSend", "sendable");
        this.blocked = intOf(json, "blocked");
        this.creditsNeeded = intOf(json, "creditsNeeded");
        this.currentBalance = intOf(json, "currentBalance", "creditBalance");
        this.hasEnoughCredits = boolOf(json, "hasEnoughCredits", "hasSufficientCredits");
        this.duplicates = intOf(json, "duplicates");
        this.canSend = present(json, "canSend") != null ? boolOf(json, "canSend") : canSendOf(json);

        this.warnings = new ArrayList<>();
        JsonElement warningList = present(json, "warnings");
        if (warningList != null && warningList.isJsonArray()) {
            for (JsonElement warning : warningList.getAsJsonArray()) {
                if (!warning.isJsonNull()) warnings.add(warning.getAsString());
            }
        }

        this.messages = new ArrayList<>();
        if (json.has("messages") && json.get("messages").isJsonArray()) {
            JsonArray messagesArray = json.getAsJsonArray("messages");
            for (JsonElement element : messagesArray) {
                messages.add(new BatchPreviewItem(element.getAsJsonObject()));
            }
        }

        this.blockReasons = new HashMap<>();
        if (json.has("blockReasons") && json.get("blockReasons").isJsonObject()) {
            JsonObject reasons = json.getAsJsonObject("blockReasons");
            for (String key : reasons.keySet()) {
                blockReasons.put(key, reasons.get(key).getAsInt());
            }
        } else if (json.has("blockedMessages") && json.get("blockedMessages").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("blockedMessages")) {
                if (element.isJsonObject()) {
                    JsonElement reason = present(element.getAsJsonObject(), "reason");
                    if (reason != null) blockReasons.merge(reason.getAsString(), 1, Integer::sum);
                }
            }
        }
    }

    private boolean canSendOf(JsonObject json) {
        JsonElement compliance = present(json, "compliance");
        int optedOut = compliance != null && compliance.isJsonObject()
                ? intOf(compliance.getAsJsonObject(), "optedOutBlocked") : 0;
        JsonElement keyType = present(json, "keyType");
        boolean testKey = keyType != null && "test".equals(keyType.getAsString());
        return willSend > 0
                && blocked == optedOut
                && totalMessages <= MAX_BATCH_MESSAGES
                && (testKey || hasEnoughCredits)
                && boolOf(json, "hasWriteScope");
    }

    private static JsonElement present(JsonObject json, String... keys) {
        for (String key : keys) {
            JsonElement value = json.get(key);
            if (value != null && !value.isJsonNull()) return value;
        }
        return null;
    }

    private static int intOf(JsonObject json, String... keys) {
        JsonElement value = present(json, keys);
        return value != null ? (int) value.getAsDouble() : 0;
    }

    private static boolean boolOf(JsonObject json, String... keys) {
        JsonElement value = present(json, keys);
        return value != null && value.getAsBoolean();
    }

    // Getters

    /**
     * Whether sending this batch would go through: at least one message is
     * sendable, nothing is blocked except recipients who opted out (a live
     * send skips those but rejects the whole batch for any other block), the
     * batch has no more than 10,000 messages, the balance covers the credits
     * with a live key (test sends are free), and the key has the
     * {@code sms:send} scope.
     * <p>
     * Apart from the credits, these are the checks a live send applies. The
     * preview checks the workspace's verification and each destination for
     * every key, and {@code sendBatch()} skips both for a test key, so on a
     * workspace that is not verified yet a test key reads false here although
     * its sandbox send goes through; {@link #getBlockReasons()} says why.
     * </p>
     */
    public boolean canSend() {
        return canSend;
    }

    /** Messages in the batch, before duplicates are removed. */
    public int getTotalMessages() {
        return totalMessages;
    }

    /** Messages that would be sent. */
    public int getWillSend() {
        return willSend;
    }

    public int getBlocked() {
        return blocked;
    }

    public int getCreditsNeeded() {
        return creditsNeeded;
    }

    /** The credit balance of the workspace, or of its pool. */
    public int getCurrentBalance() {
        return currentBalance;
    }

    /** Whether the balance, with any overage allowance, covers the credits needed. */
    public boolean hasEnoughCredits() {
        return hasEnoughCredits;
    }

    /** Duplicate numbers that the send would remove. */
    public int getDuplicates() {
        return duplicates;
    }

    /** Non-blocking warnings about the batch. */
    public List<String> getWarnings() {
        return warnings;
    }

    /** The preview does not return per-message rows, so this is empty. */
    public List<BatchPreviewItem> getMessages() {
        return messages;
    }

    /** How many messages are blocked, by reason. */
    public Map<String, Integer> getBlockReasons() {
        return blockReasons;
    }

    @Override
    public String toString() {
        return "BatchPreviewResponse{" +
                "canSend=" + canSend +
                ", totalMessages=" + totalMessages +
                ", willSend=" + willSend +
                ", blocked=" + blocked +
                ", creditsNeeded=" + creditsNeeded +
                ", currentBalance=" + currentBalance +
                ", hasEnoughCredits=" + hasEnoughCredits +
                '}';
    }

    /**
     * Represents a single message in a batch preview.
     */
    public static class BatchPreviewItem {
        private final String to;
        private final String text;
        private final int segments;
        private final int credits;
        private final boolean canSend;
        private final String blockReason;
        private final String country;
        private final String pricingTier;

        public BatchPreviewItem(JsonObject json) {
            this.to = getStringOrNull(json, "to");
            this.text = getStringOrNull(json, "text");
            this.segments = json.has("segments") ? json.get("segments").getAsInt() : 1;
            this.credits = json.has("credits") ? json.get("credits").getAsInt() : 0;
            this.canSend = json.has("canSend") && json.get("canSend").getAsBoolean();
            this.blockReason = getStringOrNull(json, "blockReason");
            this.country = getStringOrNull(json, "country");
            this.pricingTier = getStringOrNull(json, "pricingTier");
        }

        private String getStringOrNull(JsonObject json, String key) {
            return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
        }

        public String getTo() {
            return to;
        }

        public String getText() {
            return text;
        }

        public int getSegments() {
            return segments;
        }

        public int getCredits() {
            return credits;
        }

        public boolean canSend() {
            return canSend;
        }

        public String getBlockReason() {
            return blockReason;
        }

        public String getCountry() {
            return country;
        }

        public String getPricingTier() {
            return pricingTier;
        }

        @Override
        public String toString() {
            return "BatchPreviewItem{" +
                    "to='" + to + '\'' +
                    ", segments=" + segments +
                    ", credits=" + credits +
                    ", canSend=" + canSend +
                    '}';
        }
    }
}
