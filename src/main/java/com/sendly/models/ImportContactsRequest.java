package com.sendly.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImportContactsRequest {
    private final List<ImportContactItem> contacts;
    @SerializedName("listId")
    private final String listId;
    @SerializedName("optedInAt")
    private final String optedInAt;

    private ImportContactsRequest(Builder builder) {
        this.contacts = builder.contacts;
        this.listId = builder.listId;
        this.optedInAt = builder.optedInAt;
    }

    public List<ImportContactItem> getContacts() { return contacts; }
    public String getListId() { return listId; }
    public String getOptedInAt() { return optedInAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class ImportContactItem {
        private final String phone;
        private final String name;
        private final String email;
        @SerializedName("optedInAt")
        private final String optedInAt;

        public ImportContactItem(String phone) {
            this(phone, null, null, null);
        }

        public ImportContactItem(String phone, String name, String email, String optedInAt) {
            this.phone = phone;
            this.name = name;
            this.email = email;
            this.optedInAt = optedInAt;
        }

        public String getPhone() { return phone; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getOptedInAt() { return optedInAt; }
    }

    public static class Builder {
        private List<ImportContactItem> contacts;
        private String listId;
        private String optedInAt;

        public Builder contacts(List<ImportContactItem> contacts) {
            this.contacts = contacts;
            return this;
        }

        public Builder listId(String listId) {
            this.listId = listId;
            return this;
        }

        public Builder optedInAt(String optedInAt) {
            this.optedInAt = optedInAt;
            return this;
        }

        public ImportContactsRequest build() {
            return new ImportContactsRequest(this);
        }
    }
}
