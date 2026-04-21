package com.sendly.resources;

import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.BulkMarkValidRequest;
import com.sendly.models.BulkMarkValidResponse;
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

    /**
     * Clear the invalid flag on a contact so future campaigns include it again.
     * Contacts get auto-flagged when a send fails with a terminal bad-number
     * error (landline, invalid number) or when a carrier lookup reports they
     * can't receive SMS.
     */
    public Contact markValid(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Contact ID is required");
        }
        JsonObject response = client.post("/contacts/" + id + "/mark-valid", new JsonObject());
        return new Contact(response);
    }

    /**
     * Clear the invalid flag on many contacts at once — the escape hatch for
     * when auto-flag misclassifies at scale. Pass either {@code ids} (up to
     * 10,000 per call) OR {@code listId} on the request, not both. Foreign
     * ids silently no-op via the per-organization filter.
     *
     * @return number of contacts whose flag was actually cleared
     */
    public BulkMarkValidResponse bulkMarkValid(BulkMarkValidRequest request) throws SendlyException {
        boolean hasIds = request != null && request.getIds() != null && !request.getIds().isEmpty();
        boolean hasListId = request != null && request.getListId() != null && !request.getListId().isEmpty();

        if (!hasIds && !hasListId) {
            throw new ValidationException("bulkMarkValid requires either ids or listId");
        }
        if (hasIds && hasListId) {
            throw new ValidationException("bulkMarkValid accepts ids OR listId, not both");
        }

        JsonObject body = new JsonObject();
        if (hasIds) {
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            for (String id : request.getIds()) {
                arr.add(id);
            }
            body.add("ids", arr);
        } else {
            body.addProperty("listId", request.getListId());
        }

        JsonObject response = client.post("/contacts/bulk-mark-valid", body);
        return new BulkMarkValidResponse(response);
    }

    /**
     * Trigger a background carrier lookup across your contacts. Landlines
     * and other non-SMS-capable numbers are auto-excluded from future
     * campaigns. Idempotent: re-triggering while a lookup is running for
     * the same scope is a no-op.
     *
     * @param listId scope to a single list (or null for all un-checked contacts)
     * @param force re-check contacts even if previously looked up
     */
    public JsonObject checkNumbers(String listId, boolean force) throws SendlyException {
        JsonObject body = new JsonObject();
        if (listId != null) body.addProperty("listId", listId);
        body.addProperty("force", force);
        return client.post("/contacts/lookup", body);
    }

    /** Check all un-checked contacts. */
    public JsonObject checkNumbers() throws SendlyException {
        return checkNumbers(null, false);
    }

    public ImportContactsResponse importContacts(ImportContactsRequest request) throws SendlyException {
        if (request.getContacts() == null || request.getContacts().isEmpty()) {
            throw new ValidationException("Contacts list is required");
        }
        JsonObject response = client.post("/contacts/import", request);
        return new ImportContactsResponse(response);
    }
}
