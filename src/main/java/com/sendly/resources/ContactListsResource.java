package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.ContactList;
import com.sendly.models.ContactListsResponse;
import com.sendly.models.CreateContactListRequest;
import com.sendly.models.UpdateContactListRequest;
import com.sendly.models.AddContactsRequest;

import java.util.List;

public class ContactListsResource {
    private final Sendly client;

    public ContactListsResource(Sendly client) {
        this.client = client;
    }

    public ContactListsResponse list() throws SendlyException {
        JsonObject response = client.get("/contact-lists", null);
        return new ContactListsResponse(response);
    }

    public ContactList get(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact list ID is required");
        }
        JsonObject response = client.get("/contact-lists/" + PathParams.encode(id), null);
        return new ContactList(response);
    }

    public ContactList create(CreateContactListRequest request) throws SendlyException {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ValidationException("Contact list name is required");
        }
        JsonObject response = client.post("/contact-lists", request);
        return new ContactList(response);
    }

    public ContactList update(String id, UpdateContactListRequest request) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact list ID is required");
        }
        JsonObject response = client.patch("/contact-lists/" + PathParams.encode(id), request);
        return new ContactList(response);
    }

    public void delete(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact list ID is required");
        }
        client.delete("/contact-lists/" + PathParams.encode(id));
    }

    public void addContacts(String listId, List<String> contactIds) throws SendlyException {
        if (listId == null || listId.isEmpty()) {
            throw new ValidationException("Contact list ID is required");
        }
        if (contactIds == null || contactIds.isEmpty()) {
            throw new ValidationException("At least one contact ID is required");
        }
        client.post("/contact-lists/" + PathParams.encode(listId) + "/contacts", new AddContactsRequest(contactIds));
    }

    public void removeContact(String listId, String contactId) throws SendlyException {
        if (listId == null || listId.isEmpty()) {
            throw new ValidationException("Contact list ID is required");
        }
        if (contactId == null || contactId.isEmpty()) {
            throw new ValidationException("Contact ID is required");
        }
        client.delete("/contact-lists/" + PathParams.encode(listId) + "/contacts/" + PathParams.encode(contactId));
    }
}
