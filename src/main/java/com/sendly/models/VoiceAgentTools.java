package com.sendly.models;

import com.google.gson.JsonObject;

/**
 * What an AI agent may do on a call. Read back from
 * {@code VoiceAgent.getTools()}, and built with {@link #builder()} to set on
 * {@link CreateVoiceAgentRequest} or {@link UpdateVoiceAgentRequest}, where
 * only the fields you set are sent and, on update, the rest keep their
 * current values.
 *
 * <pre>{@code
 * VoiceAgentTools tools = VoiceAgentTools.builder()
 *     .sendSms(true)
 *     .build();
 * }</pre>
 */
public class VoiceAgentTools {
    private final Boolean sendSms;
    private final String transferTo;

    private VoiceAgentTools(Builder builder) {
        this.sendSms = builder.sendSms;
        this.transferTo = builder.transferTo;
    }

    /**
     * Create a VoiceAgentTools from a JSON object.
     */
    public VoiceAgentTools(JsonObject json) {
        this.sendSms = json.has("sendSms") && !json.get("sendSms").isJsonNull() ? json.get("sendSms").getAsBoolean() : null;
        this.transferTo = json.has("transferTo") && !json.get("transferTo").isJsonNull() ? json.get("transferTo").getAsString() : null;
    }

    /**
     * True when the agent may text the caller during the call. It reads the
     * number back to the caller before sending. False when not set on a
     * request you built.
     */
    public boolean isSendSms() {
        return Boolean.TRUE.equals(sendSms);
    }

    /**
     * A number in E.164 for callers who need a person, or {@code null}.
     * Agents cannot transfer calls yet and never dial or read out this number:
     * while it is set, a caller who asks for a person is told the message will
     * be passed on, and the agent takes their name and number.
     */
    public String getTransferTo() {
        return transferTo;
    }

    /** Serialize to the JSON the API expects. Unset fields are omitted. */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (sendSms != null) o.addProperty("sendSms", sendSms);
        if (transferTo != null) o.addProperty("transferTo", transferTo);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean sendSms;
        private String transferTo;

        /** Whether the agent may text the caller. Defaults to true on a new agent. */
        public Builder sendSms(boolean sendSms) {
            this.sendSms = sendSms;
            return this;
        }

        /**
         * A number in E.164 for callers who need a person; see
         * {@link VoiceAgentTools#getTransferTo()} for what the agent does with
         * it. Pass an empty string to clear it.
         */
        public Builder transferTo(String transferTo) {
            this.transferTo = transferTo;
            return this;
        }

        public VoiceAgentTools build() {
            return new VoiceAgentTools(this);
        }
    }

    @Override
    public String toString() {
        return "VoiceAgentTools{" +
                "sendSms=" + sendSms +
                ", transferTo='" + transferTo + '\'' +
                '}';
    }
}
