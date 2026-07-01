package com.sendly.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for {@code tenDlc().createCampaign()}.
 *
 * <pre>{@code
 * CreateTenDlcCampaignRequest request = CreateTenDlcCampaignRequest.builder()
 *     .brandId("brd_xxx")
 *     .useCase("MIXED")
 *     .description("Order updates and support replies for Acme customers")
 *     .messageFlow("Customers opt in at checkout on acme.example")
 *     .sampleMessages(List.of("Your order #123 has shipped!"))
 *     .optOutKeywords("STOP")
 *     .build();
 * }</pre>
 */
public class CreateTenDlcCampaignRequest {
    private final String brandId;
    private final String useCase;
    private final String description;
    private final String messageFlow;
    private final List<String> sampleMessages;
    private final List<String> subUseCases;
    private final String optInKeywords;
    private final String optOutKeywords;
    private final String helpKeywords;
    private final String optInMessage;
    private final String optOutMessage;
    private final String helpMessage;
    private final Boolean embeddedLink;
    private final Boolean embeddedPhone;

    private CreateTenDlcCampaignRequest(Builder builder) {
        this.brandId = builder.brandId;
        this.useCase = builder.useCase;
        this.description = builder.description;
        this.messageFlow = builder.messageFlow;
        this.sampleMessages = builder.sampleMessages;
        this.subUseCases = builder.subUseCases;
        this.optInKeywords = builder.optInKeywords;
        this.optOutKeywords = builder.optOutKeywords;
        this.helpKeywords = builder.helpKeywords;
        this.optInMessage = builder.optInMessage;
        this.optOutMessage = builder.optOutMessage;
        this.helpMessage = builder.helpMessage;
        this.embeddedLink = builder.embeddedLink;
        this.embeddedPhone = builder.embeddedPhone;
    }

    public String getBrandId() { return brandId; }
    public String getUseCase() { return useCase; }
    public String getDescription() { return description; }
    public String getMessageFlow() { return messageFlow; }
    public List<String> getSampleMessages() { return sampleMessages; }
    public List<String> getSubUseCases() { return subUseCases; }
    public String getOptInKeywords() { return optInKeywords; }
    public String getOptOutKeywords() { return optOutKeywords; }
    public String getHelpKeywords() { return helpKeywords; }
    public String getOptInMessage() { return optInMessage; }
    public String getOptOutMessage() { return optOutMessage; }
    public String getHelpMessage() { return helpMessage; }
    public Boolean getEmbeddedLink() { return embeddedLink; }
    public Boolean getEmbeddedPhone() { return embeddedPhone; }

    /** Serialize to the JSON body the API expects (camelCase keys). */
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        if (brandId != null) o.addProperty("brandId", brandId);
        if (useCase != null) o.addProperty("useCase", useCase);
        if (description != null) o.addProperty("description", description);
        if (messageFlow != null) o.addProperty("messageFlow", messageFlow);
        if (sampleMessages != null) {
            JsonArray arr = new JsonArray();
            sampleMessages.forEach(arr::add);
            o.add("sampleMessages", arr);
        }
        if (subUseCases != null) {
            JsonArray arr = new JsonArray();
            subUseCases.forEach(arr::add);
            o.add("subUseCases", arr);
        }
        if (optInKeywords != null) o.addProperty("optInKeywords", optInKeywords);
        if (optOutKeywords != null) o.addProperty("optOutKeywords", optOutKeywords);
        if (helpKeywords != null) o.addProperty("helpKeywords", helpKeywords);
        if (optInMessage != null) o.addProperty("optInMessage", optInMessage);
        if (optOutMessage != null) o.addProperty("optOutMessage", optOutMessage);
        if (helpMessage != null) o.addProperty("helpMessage", helpMessage);
        if (embeddedLink != null) o.addProperty("embeddedLink", embeddedLink);
        if (embeddedPhone != null) o.addProperty("embeddedPhone", embeddedPhone);
        return o;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String brandId;
        private String useCase;
        private String description;
        private String messageFlow;
        private List<String> sampleMessages;
        private List<String> subUseCases;
        private String optInKeywords;
        private String optOutKeywords;
        private String helpKeywords;
        private String optInMessage;
        private String optOutMessage;
        private String helpMessage;
        private Boolean embeddedLink;
        private Boolean embeddedPhone;

        /** The verified brand to create the campaign under (required). */
        public Builder brandId(String brandId) {
            this.brandId = brandId;
            return this;
        }

        /** Primary use-case code (required, e.g. "MIXED", "MARKETING"). */
        public Builder useCase(String useCase) {
            this.useCase = useCase;
            return this;
        }

        /** What the campaign sends and why (required). */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /** How recipients opt in to receive messages (required). */
        public Builder messageFlow(String messageFlow) {
            this.messageFlow = messageFlow;
            return this;
        }

        /** Example messages the campaign sends (required; the first 5 are used). */
        public Builder sampleMessages(List<String> sampleMessages) {
            this.sampleMessages = sampleMessages != null ? new ArrayList<>(sampleMessages) : null;
            return this;
        }

        /** Sub-use-case codes. */
        public Builder subUseCases(List<String> subUseCases) {
            this.subUseCases = subUseCases != null ? new ArrayList<>(subUseCases) : null;
            return this;
        }

        /** Comma-separated keywords that opt a recipient in. */
        public Builder optInKeywords(String optInKeywords) {
            this.optInKeywords = optInKeywords;
            return this;
        }

        /** Comma-separated keywords that opt a recipient out. */
        public Builder optOutKeywords(String optOutKeywords) {
            this.optOutKeywords = optOutKeywords;
            return this;
        }

        /** Comma-separated keywords that request help. */
        public Builder helpKeywords(String helpKeywords) {
            this.helpKeywords = helpKeywords;
            return this;
        }

        /** Auto-reply sent on opt-in. */
        public Builder optInMessage(String optInMessage) {
            this.optInMessage = optInMessage;
            return this;
        }

        /** Auto-reply sent on opt-out. */
        public Builder optOutMessage(String optOutMessage) {
            this.optOutMessage = optOutMessage;
            return this;
        }

        /** Auto-reply sent on a help request. */
        public Builder helpMessage(String helpMessage) {
            this.helpMessage = helpMessage;
            return this;
        }

        /** Whether messages may contain links; defaults to true. */
        public Builder embeddedLink(Boolean embeddedLink) {
            this.embeddedLink = embeddedLink;
            return this;
        }

        /** Whether messages may contain phone numbers; defaults to false. */
        public Builder embeddedPhone(Boolean embeddedPhone) {
            this.embeddedPhone = embeddedPhone;
            return this;
        }

        public CreateTenDlcCampaignRequest build() {
            return new CreateTenDlcCampaignRequest(this);
        }
    }
}
