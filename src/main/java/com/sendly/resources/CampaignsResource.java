package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;
import com.sendly.models.Campaign;
import com.sendly.models.CampaignList;
import com.sendly.models.CampaignPreview;
import com.sendly.models.CreateCampaignRequest;
import com.sendly.models.ListCampaignsRequest;
import com.sendly.models.ScheduleCampaignRequest;
import com.sendly.models.UpdateCampaignRequest;

import java.util.Map;

public class CampaignsResource {
    private final Sendly client;

    public CampaignsResource(Sendly client) {
        this.client = client;
    }

    public Campaign create(CreateCampaignRequest request) throws SendlyException {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ValidationException("Campaign name is required");
        }
        if (request.getText() == null || request.getText().isEmpty()) {
            throw new ValidationException("Campaign text is required");
        }
        if (request.getContactListIds() == null || request.getContactListIds().isEmpty()) {
            throw new ValidationException("At least one contact list is required");
        }

        JsonObject response = client.post("/campaigns", request);
        return new Campaign(response);
    }

    public CampaignList list() throws SendlyException {
        return list(ListCampaignsRequest.builder().build());
    }

    public CampaignList list(ListCampaignsRequest request) throws SendlyException {
        Map<String, String> params = request.toParams();
        JsonObject response = client.get("/campaigns", params.isEmpty() ? null : params);
        return new CampaignList(response);
    }

    public Campaign get(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        JsonObject response = client.get("/campaigns/" + PathParams.encode(id), null);
        return new Campaign(response);
    }

    public Campaign update(String id, UpdateCampaignRequest request) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        JsonObject response = client.patch("/campaigns/" + PathParams.encode(id), request);
        return new Campaign(response);
    }

    public void delete(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        client.delete("/campaigns/" + PathParams.encode(id));
    }

    public CampaignPreview preview(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        JsonObject response = client.get("/campaigns/" + PathParams.encode(id) + "/preview", null);
        return new CampaignPreview(response);
    }

    public Campaign send(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        JsonObject response = client.post("/campaigns/" + PathParams.encode(id) + "/send", new JsonObject());
        return new Campaign(response);
    }

    public Campaign schedule(String id, ScheduleCampaignRequest request) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        if (request.getScheduledAt() == null || request.getScheduledAt().isEmpty()) {
            throw new ValidationException("Scheduled time is required");
        }
        JsonObject response = client.post("/campaigns/" + PathParams.encode(id) + "/schedule", request);
        return new Campaign(response);
    }

    public Campaign cancel(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        JsonObject response = client.post("/campaigns/" + PathParams.encode(id) + "/cancel", new JsonObject());
        return new Campaign(response);
    }

    public Campaign clone(String id) throws SendlyException {
        if (id == null || id.isEmpty()) {
            throw new ValidationException("Campaign ID is required");
        }
        JsonObject response = client.post("/campaigns/" + PathParams.encode(id) + "/clone", new JsonObject());
        return new Campaign(response);
    }
}
