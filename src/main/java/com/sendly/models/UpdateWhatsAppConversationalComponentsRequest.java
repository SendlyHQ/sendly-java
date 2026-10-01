package com.sendly.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for {@code whatsapp().senders().updateConversationalComponents()}.
 * Set {@code iceBreakers}, {@code commands} or both. Each list you set
 * replaces the stored one, an empty list clears it, and a list you leave
 * unset keeps its current value.
 */
public class UpdateWhatsAppConversationalComponentsRequest {
    private final List<String> iceBreakers;
    private final List<WhatsAppCommand> commands;

    private UpdateWhatsAppConversationalComponentsRequest(Builder builder) {
        this.iceBreakers = builder.iceBreakers;
        this.commands = builder.commands;
    }

    public List<String> getIceBreakers() { return iceBreakers; }
    public List<WhatsAppCommand> getCommands() { return commands; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> iceBreakers;
        private List<WhatsAppCommand> commands;

        /**
         * Tappable suggestions shown when someone opens a chat with the
         * business for the first time: at most 4, each 1-80 characters after
         * trimming, no two the same ignoring case.
         */
        public Builder iceBreakers(List<String> iceBreakers) {
            this.iceBreakers = iceBreakers == null ? null : new ArrayList<>(iceBreakers);
            return this;
        }

        /**
         * Commands shown when the customer types "/": at most 30, no two
         * with the same command.
         */
        public Builder commands(List<WhatsAppCommand> commands) {
            this.commands = commands == null ? null : new ArrayList<>(commands);
            return this;
        }

        public UpdateWhatsAppConversationalComponentsRequest build() {
            return new UpdateWhatsAppConversationalComponentsRequest(this);
        }
    }
}
