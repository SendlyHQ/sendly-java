package com.sendly.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sendly.Sendly;
import com.sendly.exceptions.SendlyException;
import com.sendly.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnterpriseResource {
    private final Sendly client;
    private final Workspaces workspaces;
    private final Webhooks webhooks;
    private final Analytics analytics;
    private final Settings settings;
    private final Billing billing;

    public EnterpriseResource(Sendly client) {
        this.client = client;
        this.workspaces = new Workspaces(client);
        this.webhooks = new Webhooks(client);
        this.analytics = new Analytics(client);
        this.settings = new Settings(client);
        this.billing = new Billing(client);
    }

    public Workspaces workspaces() {
        return workspaces;
    }

    public Webhooks webhooks() {
        return webhooks;
    }

    public Analytics analytics() {
        return analytics;
    }

    public Settings settings() {
        return settings;
    }

    public Billing billing() {
        return billing;
    }

    public JsonObject getAccount() throws SendlyException {
        return client.get("/enterprise/account", null);
    }

    public JsonObject provision(JsonObject options) throws SendlyException {
        if (options == null) {
            throw new ValidationException("Provision options are required");
        }
        if (!options.has("name") || options.get("name").getAsString().trim().isEmpty()) {
            throw new ValidationException("Workspace name is required");
        }
        return client.post("/enterprise/workspaces/provision", options);
    }

    public static class Workspaces {
        private final Sendly client;

        public Workspaces(Sendly client) {
            this.client = client;
        }

        public JsonObject create(String name) throws SendlyException {
            return create(name, null);
        }

        public JsonObject create(String name, String description) throws SendlyException {
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Workspace name is required");
            }

            JsonObject body = new JsonObject();
            body.addProperty("name", name.trim());
            if (description != null && !description.trim().isEmpty()) {
                body.addProperty("description", description.trim());
            }

            return client.post("/enterprise/workspaces", body);
        }

        public JsonObject list() throws SendlyException {
            return client.get("/enterprise/workspaces", null);
        }

        public JsonObject get(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId, null);
        }

        public void delete(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            client.delete("/enterprise/workspaces/" + workspaceId);
        }

        public JsonObject submitVerification(String workspaceId, JsonObject data) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (data == null) {
                throw new ValidationException("Verification data is required");
            }
            return client.post("/enterprise/workspaces/" + workspaceId + "/verification/submit", data);
        }

        public JsonObject inheritVerification(String workspaceId, String sourceWorkspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (sourceWorkspaceId == null || sourceWorkspaceId.isEmpty()) {
                throw new ValidationException("Source workspace ID is required");
            }

            JsonObject body = new JsonObject();
            body.addProperty("source_workspace_id", sourceWorkspaceId);

            return client.post("/enterprise/workspaces/" + workspaceId + "/verification/inherit", body);
        }

        public JsonObject getVerification(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId + "/verification", null);
        }

        public JsonObject transferCredits(String workspaceId, String sourceWorkspaceId, int amount) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (sourceWorkspaceId == null || sourceWorkspaceId.isEmpty()) {
                throw new ValidationException("Source workspace ID is required");
            }
            if (amount <= 0) {
                throw new ValidationException("Amount must be a positive integer");
            }

            JsonObject body = new JsonObject();
            body.addProperty("source_workspace_id", sourceWorkspaceId);
            body.addProperty("amount", amount);

            return client.post("/enterprise/workspaces/" + workspaceId + "/transfer-credits", body);
        }

        public JsonObject getCredits(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId + "/credits", null);
        }

        public JsonObject createKey(String workspaceId) throws SendlyException {
            return createKey(workspaceId, null, null);
        }

        public JsonObject createKey(String workspaceId, String name, String type) throws SendlyException {
            validateWorkspaceId(workspaceId);

            JsonObject body = new JsonObject();
            if (name != null && !name.isEmpty()) {
                body.addProperty("name", name);
            }
            if (type != null && !type.isEmpty()) {
                body.addProperty("type", type);
            }

            return client.post("/enterprise/workspaces/" + workspaceId + "/keys", body);
        }

        public List<JsonObject> listKeys(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            JsonObject response = client.get("/enterprise/workspaces/" + workspaceId + "/keys", null);

            List<JsonObject> keys = new ArrayList<>();
            JsonArray array = null;

            if (response.has("data") && response.get("data").isJsonArray()) {
                array = response.getAsJsonArray("data");
            } else if (response.has("keys") && response.get("keys").isJsonArray()) {
                array = response.getAsJsonArray("keys");
            }

            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    keys.add(array.get(i).getAsJsonObject());
                }
            }

            return keys;
        }

        public void revokeKey(String workspaceId, String keyId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (keyId == null || keyId.isEmpty()) {
                throw new ValidationException("Key ID is required");
            }
            client.delete("/enterprise/workspaces/" + workspaceId + "/keys/" + keyId);
        }

        public JsonObject listOptInPages(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId + "/opt-in-pages", null);
        }

        public JsonObject createOptInPage(String workspaceId, JsonObject options) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (options == null) {
                throw new ValidationException("Opt-in page options are required");
            }
            if (!options.has("businessName") || options.get("businessName").getAsString().trim().isEmpty()) {
                throw new ValidationException("businessName is required");
            }
            return client.post("/enterprise/workspaces/" + workspaceId + "/opt-in-pages", options);
        }

        public JsonObject updateOptInPage(String workspaceId, String pageId, JsonObject options) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (pageId == null || pageId.isEmpty()) {
                throw new ValidationException("Page ID is required");
            }
            if (options == null) {
                throw new ValidationException("Update options are required");
            }
            return client.patch("/enterprise/workspaces/" + workspaceId + "/opt-in-pages/" + pageId, options);
        }

        public void deleteOptInPage(String workspaceId, String pageId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (pageId == null || pageId.isEmpty()) {
                throw new ValidationException("Page ID is required");
            }
            client.delete("/enterprise/workspaces/" + workspaceId + "/opt-in-pages/" + pageId);
        }

        public JsonObject setWebhook(String workspaceId, JsonObject options) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (options == null) {
                throw new ValidationException("Webhook options are required");
            }
            if (!options.has("url") || options.get("url").getAsString().trim().isEmpty()) {
                throw new ValidationException("Webhook URL is required");
            }
            return client.put("/enterprise/workspaces/" + workspaceId + "/webhooks", options);
        }

        public JsonObject listWebhooks(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId + "/webhooks", null);
        }

        public void deleteWebhooks(String workspaceId) throws SendlyException {
            deleteWebhooks(workspaceId, null);
        }

        public void deleteWebhooks(String workspaceId, String webhookId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            String path = "/enterprise/workspaces/" + workspaceId + "/webhooks";
            if (webhookId != null && !webhookId.isEmpty()) {
                path += "?webhookId=" + webhookId;
            }
            client.delete(path);
        }

        public JsonObject testWebhook(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.post("/enterprise/workspaces/" + workspaceId + "/webhooks/test", new JsonObject());
        }

        public JsonObject suspend(String workspaceId) throws SendlyException {
            return suspend(workspaceId, null);
        }

        public JsonObject suspend(String workspaceId, String reason) throws SendlyException {
            validateWorkspaceId(workspaceId);
            JsonObject body = new JsonObject();
            if (reason != null && !reason.isEmpty()) {
                body.addProperty("reason", reason);
            }
            return client.post("/enterprise/workspaces/" + workspaceId + "/suspend", body);
        }

        public JsonObject resume(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.post("/enterprise/workspaces/" + workspaceId + "/resume", new JsonObject());
        }

        public JsonObject provisionBulk(JsonArray workspaces) throws SendlyException {
            if (workspaces == null || workspaces.size() == 0) {
                throw new ValidationException("Workspaces array is required");
            }
            if (workspaces.size() > 50) {
                throw new ValidationException("Maximum 50 workspaces per bulk provision");
            }

            JsonObject body = new JsonObject();
            body.add("workspaces", workspaces);

            return client.post("/enterprise/workspaces/provision/bulk", body);
        }

        public JsonObject setCustomDomain(String workspaceId, String pageId, String domain) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (pageId == null || pageId.isEmpty()) {
                throw new ValidationException("Page ID is required");
            }
            if (domain == null || domain.isEmpty()) {
                throw new ValidationException("Domain is required");
            }

            JsonObject body = new JsonObject();
            body.addProperty("domain", domain);

            return client.put("/enterprise/workspaces/" + workspaceId + "/pages/" + pageId + "/domain", body);
        }

        public JsonObject sendInvitation(String workspaceId, String email, String role) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (email == null || email.isEmpty()) {
                throw new ValidationException("Email is required");
            }
            if (role == null || role.isEmpty()) {
                throw new ValidationException("Role is required");
            }

            JsonObject body = new JsonObject();
            body.addProperty("email", email);
            body.addProperty("role", role);

            return client.post("/enterprise/workspaces/" + workspaceId + "/invitations", body);
        }

        public JsonObject listInvitations(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId + "/invitations", null);
        }

        public void cancelInvitation(String workspaceId, String inviteId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (inviteId == null || inviteId.isEmpty()) {
                throw new ValidationException("Invitation ID is required");
            }
            client.delete("/enterprise/workspaces/" + workspaceId + "/invitations/" + inviteId);
        }

        public JsonObject getQuota(String workspaceId) throws SendlyException {
            validateWorkspaceId(workspaceId);
            return client.get("/enterprise/workspaces/" + workspaceId + "/quota", null);
        }

        public JsonObject setQuota(String workspaceId, Integer monthlyMessageQuota) throws SendlyException {
            validateWorkspaceId(workspaceId);
            if (monthlyMessageQuota == null) {
                throw new ValidationException("Monthly message quota is required");
            }

            JsonObject body = new JsonObject();
            body.addProperty("monthlyMessageQuota", monthlyMessageQuota);

            return client.put("/enterprise/workspaces/" + workspaceId + "/quota", body);
        }

        private void validateWorkspaceId(String workspaceId) {
            if (workspaceId == null || workspaceId.isEmpty()) {
                throw new ValidationException("Workspace ID is required");
            }
        }
    }

    public static class Webhooks {
        private final Sendly client;

        public Webhooks(Sendly client) {
            this.client = client;
        }

        public JsonObject set(String url) throws SendlyException {
            if (url == null || url.isEmpty()) {
                throw new ValidationException("Webhook URL is required");
            }

            JsonObject body = new JsonObject();
            body.addProperty("url", url);

            return client.put("/enterprise/webhooks", body);
        }

        public JsonObject get() throws SendlyException {
            return client.get("/enterprise/webhooks", null);
        }

        public void delete() throws SendlyException {
            client.delete("/enterprise/webhooks");
        }

        public JsonObject test() throws SendlyException {
            return client.post("/enterprise/webhooks/test", new JsonObject());
        }
    }

    public static class Analytics {
        private final Sendly client;

        public Analytics(Sendly client) {
            this.client = client;
        }

        public JsonObject overview() throws SendlyException {
            return client.get("/enterprise/analytics/overview", null);
        }

        public JsonObject messages() throws SendlyException {
            return messages(null, null);
        }

        public JsonObject messages(String period, String workspaceId) throws SendlyException {
            Map<String, String> params = new HashMap<>();
            if (period != null && !period.isEmpty()) {
                params.put("period", period);
            }
            if (workspaceId != null && !workspaceId.isEmpty()) {
                params.put("workspaceId", workspaceId);
            }
            return client.get("/enterprise/analytics/messages", params.isEmpty() ? null : params);
        }

        public JsonObject delivery() throws SendlyException {
            return client.get("/enterprise/analytics/delivery", null);
        }

        public JsonObject credits() throws SendlyException {
            return credits(null);
        }

        public JsonObject credits(String period) throws SendlyException {
            Map<String, String> params = new HashMap<>();
            if (period != null && !period.isEmpty()) {
                params.put("period", period);
            }
            return client.get("/enterprise/analytics/credits", params.isEmpty() ? null : params);
        }
    }

    public static class Settings {
        private final Sendly client;

        public Settings(Sendly client) {
            this.client = client;
        }

        public JsonObject getAutoTopUp() throws SendlyException {
            return client.get("/enterprise/settings/auto-top-up", null);
        }

        public JsonObject updateAutoTopUp(JsonObject options) throws SendlyException {
            if (options == null) {
                throw new ValidationException("Auto top-up options are required");
            }
            return client.put("/enterprise/settings/auto-top-up", options);
        }
    }

    public static class Billing {
        private final Sendly client;

        public Billing(Sendly client) {
            this.client = client;
        }

        public JsonObject getBreakdown() throws SendlyException {
            return getBreakdown(null, null, null);
        }

        public JsonObject getBreakdown(String period, Integer page, Integer limit) throws SendlyException {
            Map<String, String> params = new HashMap<>();
            if (period != null && !period.isEmpty()) {
                params.put("period", period);
            }
            if (page != null) {
                params.put("page", String.valueOf(page));
            }
            if (limit != null) {
                params.put("limit", String.valueOf(limit));
            }
            return client.get("/enterprise/billing/workspace-breakdown", params.isEmpty() ? null : params);
        }
    }
}
