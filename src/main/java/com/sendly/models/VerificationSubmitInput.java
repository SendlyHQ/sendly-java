package com.sendly.models;

import com.google.gson.annotations.SerializedName;

/**
 * Payload for {@code enterprise.workspaces().submitVerification(...)} and
 * {@code resubmitVerification(...)}.
 *
 * <p>All fields are nullable. Null fields are stripped before sending so a
 * resubmit only carries the values you want to change — the server merges
 * the partial body with the existing verification record.</p>
 *
 * <p>For an initial submit on a workspace with no existing record, the
 * server validator requires: {@code businessName}, {@code website},
 * {@code address}, {@code contact}, {@code useCase}, {@code useCaseSummary},
 * {@code sampleMessages}, {@code optInWorkflow}.</p>
 *
 * <p>For sole proprietors leave {@code brn}, {@code brnType},
 * {@code brnCountry} unset — the server strips them before forwarding to
 * the carrier.</p>
 *
 * <pre>{@code
 * // Full submit
 * VerificationSubmitInput input = VerificationSubmitInput.builder()
 *     .businessName("Acme LLC")
 *     .website("https://acme.com")
 *     .address(VerificationSubmitInput.Address.builder()
 *         .street("123 Main St").city("Los Angeles")
 *         .state("California").zip("90001").country("US").build())
 *     .contact(VerificationSubmitInput.Contact.builder()
 *         .firstName("Ada").lastName("Lovelace")
 *         .email("ada@acme.com").phone("+15551234567").build())
 *     .useCase("Insurance Services")
 *     .useCaseSummary("...")
 *     .sampleMessages("...")
 *     .optInWorkflow("...")
 *     .entityType("SOLE_PROPRIETOR")
 *     .build();
 *
 * // Partial-update resubmit (only changing email)
 * VerificationSubmitInput patch = VerificationSubmitInput.builder()
 *     .contact(VerificationSubmitInput.Contact.builder()
 *         .email("new@email.com").build())
 *     .build();
 * }</pre>
 */
public class VerificationSubmitInput {
    @SerializedName("businessName")
    private String businessName;

    @SerializedName("doingBusinessAs")
    private String doingBusinessAs;

    @SerializedName("website")
    private String website;

    @SerializedName("address")
    private Address address;

    @SerializedName("contact")
    private Contact contact;

    @SerializedName("brn")
    private String brn;

    @SerializedName("brnType")
    private String brnType;

    @SerializedName("brnCountry")
    private String brnCountry;

    @SerializedName("entityType")
    private String entityType;

    @SerializedName("useCase")
    private String useCase;

    @SerializedName("useCaseSummary")
    private String useCaseSummary;

    @SerializedName("sampleMessages")
    private String sampleMessages;

    @SerializedName("optInWorkflow")
    private String optInWorkflow;

    @SerializedName("optInImageUrls")
    private String optInImageUrls;

    @SerializedName("monthlyVolume")
    private String monthlyVolume;

    @SerializedName("additionalInformation")
    private String additionalInformation;

    @SerializedName("ageGatedContent")
    private Boolean ageGatedContent;

    @SerializedName("isvReseller")
    private String isvReseller;

    @SerializedName("privacyUrl")
    private String privacyUrl;

    @SerializedName("termsUrl")
    private String termsUrl;

    public VerificationSubmitInput() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getDoingBusinessAs() { return doingBusinessAs; }
    public void setDoingBusinessAs(String doingBusinessAs) { this.doingBusinessAs = doingBusinessAs; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }

    public String getBrn() { return brn; }
    public void setBrn(String brn) { this.brn = brn; }

    public String getBrnType() { return brnType; }
    public void setBrnType(String brnType) { this.brnType = brnType; }

    public String getBrnCountry() { return brnCountry; }
    public void setBrnCountry(String brnCountry) { this.brnCountry = brnCountry; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getUseCase() { return useCase; }
    public void setUseCase(String useCase) { this.useCase = useCase; }

    public String getUseCaseSummary() { return useCaseSummary; }
    public void setUseCaseSummary(String useCaseSummary) { this.useCaseSummary = useCaseSummary; }

    public String getSampleMessages() { return sampleMessages; }
    public void setSampleMessages(String sampleMessages) { this.sampleMessages = sampleMessages; }

    public String getOptInWorkflow() { return optInWorkflow; }
    public void setOptInWorkflow(String optInWorkflow) { this.optInWorkflow = optInWorkflow; }

    public String getOptInImageUrls() { return optInImageUrls; }
    public void setOptInImageUrls(String optInImageUrls) { this.optInImageUrls = optInImageUrls; }

    public String getMonthlyVolume() { return monthlyVolume; }
    public void setMonthlyVolume(String monthlyVolume) { this.monthlyVolume = monthlyVolume; }

    public String getAdditionalInformation() { return additionalInformation; }
    public void setAdditionalInformation(String additionalInformation) { this.additionalInformation = additionalInformation; }

    public Boolean getAgeGatedContent() { return ageGatedContent; }
    public void setAgeGatedContent(Boolean ageGatedContent) { this.ageGatedContent = ageGatedContent; }

    public String getIsvReseller() { return isvReseller; }
    public void setIsvReseller(String isvReseller) { this.isvReseller = isvReseller; }

    public String getPrivacyUrl() { return privacyUrl; }
    public void setPrivacyUrl(String privacyUrl) { this.privacyUrl = privacyUrl; }

    public String getTermsUrl() { return termsUrl; }
    public void setTermsUrl(String termsUrl) { this.termsUrl = termsUrl; }

    /**
     * Returns true when at least one field has been set. Used by the
     * resource layer to reject empty payloads before they hit the network.
     */
    public boolean isEmpty() {
        return businessName == null
                && doingBusinessAs == null
                && website == null
                && address == null
                && contact == null
                && brn == null
                && brnType == null
                && brnCountry == null
                && entityType == null
                && useCase == null
                && useCaseSummary == null
                && sampleMessages == null
                && optInWorkflow == null
                && optInImageUrls == null
                && monthlyVolume == null
                && additionalInformation == null
                && ageGatedContent == null
                && isvReseller == null
                && privacyUrl == null
                && termsUrl == null;
    }

    public static class Builder {
        private final VerificationSubmitInput input = new VerificationSubmitInput();

        public Builder businessName(String v) { input.businessName = v; return this; }
        public Builder doingBusinessAs(String v) { input.doingBusinessAs = v; return this; }
        public Builder website(String v) { input.website = v; return this; }
        public Builder address(Address v) { input.address = v; return this; }
        public Builder contact(Contact v) { input.contact = v; return this; }
        public Builder brn(String v) { input.brn = v; return this; }
        public Builder brnType(String v) { input.brnType = v; return this; }
        public Builder brnCountry(String v) { input.brnCountry = v; return this; }
        public Builder entityType(String v) { input.entityType = v; return this; }
        public Builder useCase(String v) { input.useCase = v; return this; }
        public Builder useCaseSummary(String v) { input.useCaseSummary = v; return this; }
        public Builder sampleMessages(String v) { input.sampleMessages = v; return this; }
        public Builder optInWorkflow(String v) { input.optInWorkflow = v; return this; }
        public Builder optInImageUrls(String v) { input.optInImageUrls = v; return this; }
        public Builder monthlyVolume(String v) { input.monthlyVolume = v; return this; }
        public Builder additionalInformation(String v) { input.additionalInformation = v; return this; }
        public Builder ageGatedContent(Boolean v) { input.ageGatedContent = v; return this; }
        public Builder isvReseller(String v) { input.isvReseller = v; return this; }
        public Builder privacyUrl(String v) { input.privacyUrl = v; return this; }
        public Builder termsUrl(String v) { input.termsUrl = v; return this; }

        public VerificationSubmitInput build() { return input; }
    }

    /** Mailing/registered address. All fields camelCase on the wire. */
    public static class Address {
        @SerializedName("street")
        private String street;

        @SerializedName("address1")
        private String address1;

        @SerializedName("address2")
        private String address2;

        @SerializedName("city")
        private String city;

        @SerializedName("state")
        private String state;

        @SerializedName("zip")
        private String zip;

        @SerializedName("country")
        private String country;

        public Address() {}

        public static Builder builder() { return new Builder(); }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getAddress1() { return address1; }
        public void setAddress1(String address1) { this.address1 = address1; }

        public String getAddress2() { return address2; }
        public void setAddress2(String address2) { this.address2 = address2; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public static class Builder {
            private final Address a = new Address();
            public Builder street(String v) { a.street = v; return this; }
            public Builder address1(String v) { a.address1 = v; return this; }
            public Builder address2(String v) { a.address2 = v; return this; }
            public Builder city(String v) { a.city = v; return this; }
            public Builder state(String v) { a.state = v; return this; }
            public Builder zip(String v) { a.zip = v; return this; }
            public Builder country(String v) { a.country = v; return this; }
            public Address build() { return a; }
        }
    }

    /** Authorized representative contact. All fields camelCase on the wire. */
    public static class Contact {
        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        public Contact() {}

        public static Builder builder() { return new Builder(); }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public static class Builder {
            private final Contact c = new Contact();
            public Builder firstName(String v) { c.firstName = v; return this; }
            public Builder lastName(String v) { c.lastName = v; return this; }
            public Builder email(String v) { c.email = v; return this; }
            public Builder phone(String v) { c.phone = v; return this; }
            public Contact build() { return c; }
        }
    }
}
