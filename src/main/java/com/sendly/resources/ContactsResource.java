package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.Contact;
import com.sendly.models.ContactList;
import com.sendly.models.ContactListResponse;
import com.sendly.models.ContactListsResponse;
import com.sendly.models.CreateContactListRequest;
import com.sendly.models.CreateContactRequest;
import com.sendly.models.ListContactsRequest;
import com.sendly.models.UpdateContactListRequest;
import com.sendly.models.UpdateContactRequest;
import com.sendly.models.ImportContactsRequest;
import com.sendly.models.ImportContactsResponse;

import java.util.List;
import java.util.Map;

public class ContactsResource {
    private final Sendly client;
    private final ContactListsResource lists;

    public ContactsResource(Sendly client) {
        this.client = client;
        this.lists = new ContactListsResource(client);
    }

    public ContactListsResource lists() {
        return lists;
    }

    public ContactListResponse list() throws SendlyException {
        return list(ListContactsRequest.builder().build());
    }

    public ContactListResponse list(ListContactsRequest request) throws SendlyException {
        Map<String, String> params = request.toParams();
        JsonObject response = client.get("/contacts", params.isEmpty() ? null : params);
        return new ContactListResponse(response);
    }

    public Contact get(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact ID is required");
        }
        JsonObject response = client.get("/contacts/" + id, null);
        return new Contact(response);
    }

    public Contact create(CreateContactRequest request) throws SendlyException {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        JsonObject response = client.post("/contacts", request);
        return new Contact(response);
    }

    public Contact update(String id, UpdateContactRequest request) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact ID is required");
        }
        JsonObject response = client.patch("/contacts/" + id, request);
        return new Contact(response);
    }

    public void delete(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact ID is required");
        }
        client.delete("/contacts/" + id);
    }

    public ImportContactsResponse importContacts(ImportContactsRequest request) throws SendlyException {
        if (request.getContacts() == null || request.getContacts().isEmpty()) {
            throw new ValidationException("Contacts list is required");
        }
        JsonObject response = client.post("/contacts/import", request);
        return new ImportContactsResponse(response);
    }
}
