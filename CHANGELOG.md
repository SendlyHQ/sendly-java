# sendly-java

## 4.0.0

### Major Changes

- Every Sendly SDK, the CLI and the MCP server now share one version. No public API was removed or changed in this package; the major aligns the fleet and carries the behaviour change below.

### Security

- **Path parameters are percent-encoded.** Every id you pass is now encoded before it goes into the request path. An id containing `/`, `?` or `#` used to change which endpoint the request reached: an id of `../../account/keys` left its collection and hit another endpoint carrying your API key. Ordinary ids are sent byte-for-byte as before.

## 3.40.0

### Minor Changes

- **Lifecycle webhook payloads are reachable.** `Webhooks.parseEvent(...)` built a `WebhookMessageData` out of every `data.object`, field by field. That is right for `message.*` and wrong for every lifecycle event — `rcs_brand.*`, `rcs_agent.*`, `whatsapp_account.*`, `whatsapp_template.*`, `call.*`, `brand.*`, `campaign.*`, `assignment.*`, `number.*`, `port.*`, `port_out.*`, `contact.*` — which carry a different object entirely. The keys it looks for are absent, so the object came back with every field at its default and **no error was raised**: an integration looked healthy while dropping `agent_id`, `stage` and `name` on the floor. `WebhookEvent` gains two accessors that reach the payload itself:
  - `getRawObject()` returns `data.object` exactly as it arrived, as a Gson `JsonObject`, for every event type. (If a payload carries no `data.object`, it falls back to `data` itself.)
  - `objectAs(Class<T>)` deserializes that object into a type of your choosing.

  ```java
  static class RcsAgentEvent {
      @SerializedName("agent_id") String agentId;
      String stage;
  }

  WebhookEvent event = Webhooks.parseEvent(payload, signature, secret, timestamp);
  if ("rcs_agent.live".equals(event.getType())) {
      RcsAgentEvent agent = event.objectAs(RcsAgentEvent.class);
      System.out.println(agent.agentId + " reached " + agent.stage);
  }
  ```

  `objectAs` uses a plain `Gson` with no field-naming policy, so snake_case keys need `@SerializedName`. It throws `IllegalStateException` if the event carries no object at all. `getData()` is unchanged and still the right accessor for `message.*`.

- **Every webhook event type the API emits is now declared in `WebhookEventType`.** Twenty-one constants were missing, so there was no typed way to subscribe to RCS, WhatsApp or voice: `MESSAGE_READ`, `CONVERSATION_CREATED` / `CONVERSATION_UPDATED`, `DRAFT_CREATED` / `DRAFT_APPROVED` / `DRAFT_REJECTED`, `RCS_BRAND_VERIFIED` / `RCS_BRAND_FAILED`, `RCS_AGENT_TESTING` / `RCS_AGENT_LIVE` / `RCS_AGENT_REJECTED` / `RCS_AGENT_ACTION_REQUIRED`, `NUMBER_RELEASED`, `WHATSAPP_ACCOUNT_CONNECTED` / `WHATSAPP_ACCOUNT_FAILED`, `WHATSAPP_TEMPLATE_APPROVED` / `WHATSAPP_TEMPLATE_REJECTED` / `WHATSAPP_TEMPLATE_PAUSED`, `CALL_STARTED` / `CALL_COMPLETED` / `CALL_RECORDING_READY`. A parity check runs in CI against the server's own list, so this cannot drift again.

- **The README documents webhook event handling for the first time**, including which events `getData()` applies to and how to read a lifecycle payload.

### Deprecated

- `WebhookEventType.MESSAGE_QUEUED`. The API has never emitted `message.queued` and rejects it with a 400 when you subscribe, so drop it from any webhook's event list now. It is kept for one more cycle and will be removed in the next major. The fleet also deprecates `message.undelivered` for the same reason; the Java enum has never carried a constant for it, so there is nothing to deprecate here, but the string is rejected the same way if you pass it to `webhooks().create(...)`.

### Upgrade notes

Nothing was removed or renamed and no signature changed, so existing code compiles. Two things are worth checking a handler against.

- **`getData()` still decodes a lifecycle object as a message.** Unlike the SDKs that took a major in this release, Java leaves the message view populated rather than emptying it, because emptying it would be a breaking change and this is a minor. So the old hazard survives until then, and it is worth being precise about: on `contact.auto_flagged` the payload is the **contact**, so `getData().getId()` returns a contact id under a message id's meaning, and a handler keyed on it acts on the wrong record. The message that triggered the flag is the payload's own `message_id` field (`event.getRawObject().get("message_id").getAsString()`). The same applies wherever a lifecycle payload reuses a message field name: `number.activated` carries the number's `id` and `status`, `call.completed` its own `id`, `status`, `from` and `to`. Move every non-`message.*` handler onto `getRawObject()` or `objectAs(...)`.

- **A build with `-Xlint:deprecation -Werror` will now fail on `WebhookEventType.MESSAGE_QUEUED`.** That is the intended signal, but it is a compile break in a strict build rather than a warning, so it is called out here rather than left to be discovered.

## 3.39.0

### Minor Changes

- **Self-serve RCS registration on `rcs()`.** Draft a brand and an agent, invite test devices, submit for review by Sendly, and request launch, all from the API. Sendly reviews the registration and passes it to the carrier network; the API mirrors what the dashboard can do (approval and launch remain with Sendly). Ten new operations, nested the way `agents()` already is: `rcs().registration().get()`, `rcs().dossier().get()`, `rcs().brands().create(...)` / `update(...)`, and `rcs().agents().create(...)` / `get(...)` / `update(...)` / `setTestDevices(...)` / `submit(...)` / `requestLaunch(...)`. Every write also takes an `IdempotentRequestOptions` overload. Registration calls need an API key with the `rcs:read` / `rcs:write` scopes and, like the rest of RCS, answer 404 (`rcs_not_enabled`, a `NotFoundException`) until the `rcs_channel` flag is on for your account.

  Assets can't be uploaded over the API: `logoUrl`, `heroUrl` and `callToActionMediaUrl` must already be public `https://` URLs (422 `rcs_invalid_content` otherwise). Upload files from the dashboard.

  New models: `RcsBrandInput` (with `RcsBrandAddress`, `RcsBrandContact`), `RcsBrand`, `RcsBrandResponse`, `CreateRcsAgentRequest`, `UpdateRcsAgentRequest`, `RcsAgentBasics` (with `RcsAgentPhoneContact`, `RcsAgentWebsiteContact`, `RcsAgentEmailContact`), `RcsCampaign`, `RcsInteraction`, `RcsConsentSettings`, `RcsOptInMethod`, `RcsTesting`, `RcsAgentDetails`, `RcsAgentResponse`, `RcsTestDevice`, `RcsTestDeviceInput`, `RcsTestDevicesResponse`, `RcsLaunchRequest`, `RcsRegistration`, `RcsDossier`, and the string-constant classes `RcsCustomerStage`, `RcsReviewStatus`, `RcsErrorCode`, `RcsLegalEntityType`, `RcsOrganizationType`, `RcsAgentUseCase`, `RcsInteractionType`, `RcsOptInMethodType`.

- **`RcsAgent.getStage()`** on `rcs().agents().list()` items: where each agent sits in the registration journey (`RcsCustomerStage`).

- **`SendlyException.getApiErrorCode()` and `getFieldErrors()`.** Every mapped error now carries the response body's `error` string (e.g. `rcs_field_locked` vs `rcs_launch_not_ready`, both 409s) and its `errors` array as `SendlyException.FieldError` (path + message). `getErrorCode()` is unchanged and still returns the per-class constant. Populated for every resource, not just RCS.

- **`patch(path, body, idempotencyKey)` and `put(path, body, idempotencyKey)`** on the client send a caller-supplied `Idempotency-Key` on PATCH and PUT. No key is generated automatically for those methods; the two-argument forms behave exactly as before.

### Not changed in this release

- No public members were deprecated, renamed or removed. `rcs().agents().list()` and `rcs().capability(...)` are untouched.

## 3.38.0

### Minor Changes

- **Every POST now carries an idempotency key, generated automatically and reused across the SDK's own retries.** The client attaches an `Idempotency-Key` header per logical POST and sends the same key again when it retries a timeout or a dropped connection. On the endpoints that honour keys (`/messages`, `/messages/group`, `/messages/schedule`, `/messages/batch`, `/verify`, `/enterprise/workspaces/provision`, `/whatsapp/signup`, `/whatsapp/templates`) the server recognises a request that already reached it and returns the original response instead of executing again. The server records a key only once the first attempt has finished, so this narrows the duplicate-send window rather than closing it: a retry that fires while the original is still running is not seen as a repeat. Before this release the retry carried no key, so a send that timed out on the wire but had actually landed went out a second time. Nothing to change in your code, this applies to every resource because they all route through `post(...)`. `GET`, `PUT`, `PATCH` and `DELETE` do not carry a key. Multipart uploads (`media().upload`, the enterprise verification-document upload, `businessUpgrade().start` and `resubmit`) send the header too, though the server does not currently dedupe on those paths.

- **A 5xx rotates the generated key, a timeout does not.** If the server answered with a 5xx it has responded, and may have recorded that response against the key, so the SDK swaps in a fresh generated key before retrying and the retry genuinely re-executes rather than replaying a cached error. A timeout or a network failure leaves the outcome unknown, so the key is kept and the server gets its chance to dedupe. A key you supplied yourself is never rotated.

- **New `IdempotentRequestOptions` for supplying your own key**, with overloads on `messages().send(...)` (SMS, WhatsApp and RCS), `sendGroup(...)`, `schedule(...)` and `sendBatch(...)`. The automatic key only lives for the duration of one call, so use your own when you need idempotency to survive a process restart or your own retry loop.

  ```java
  Message message = client.messages().send(
      SendMessageRequest.builder().to("+15551234567").text("Your order shipped").build(),
      new IdempotentRequestOptions("order-4471-shipped"));
  ```

  Repeating a key within 24 hours returns the original response instead of executing again. That includes a recorded failure, so use a fresh key when you want a failed call to really re-run. Reusing a key with a different request body is rejected by the server rather than silently replayed. Keys are validated as 1 to 255 printable ASCII characters, and an invalid one throws `ValidationException` before any network call is made. Empty and whitespace-only keys are treated as absent, and the automatic key applies as usual.

- **`sendBatch(...)` deliberately sends no automatic key.** The server already dedupes batch retries that arrive without the header by hashing the intent of the request (sender, recipients, text, message type), and attaching a generated key would step around that protection for an identical re-run from a different process. A key you pass through `IdempotentRequestOptions` is still sent and still takes precedence.

- **`BatchMessageResponse.getSent()`**: messages handed to the network so far. This is the count a send response reports on; `queued` only appears on a batch you fetch or list.

### Patch Changes

- **Batch responses decoded to nothing, and now decode.** `BatchMessageResponse` was reading `batch_id`, `credits_used`, `created_at` and `completed_at`, none of which the API sends. `getBatchId()` was always `null` and `getCreditsUsed()` always `0`, on `sendBatch(...)`, `getBatch(...)` and `listBatches(...)` alike. The obvious pattern of sending a batch and then polling it, `client.messages().getBatch(response.getBatchId())`, passed a null id and could never have worked. The model now reads the fields the API actually returns. Batches were always really sent, this only changes what you can read back off the response object, but if you worked around the null id by tracking batches some other way you can now drop that.

- `getBatchId()` falls back to `id` when `batchId` is absent, because the two payloads name the identifier differently: a send response calls it `batchId`, while a fetched or listed batch calls it `id`. Either way the value is safe to hand straight back to `getBatch(...)`.

- The two payloads are otherwise not the same shape, which is worth knowing before you read a count that is quietly zero. A send response has no `queued` count and no timestamps, so `getQueued()` returns `0` and `getCreatedAt()` returns `null` there. Read `getSent()` off a send response, and `getQueued()`, `getCreatedAt()` and `getCompletedAt()` off a batch you fetched or listed.

### Not changed in this release

- No public members were deprecated, renamed or removed. Existing code compiles against 3.38.0 unchanged and will not emit new deprecation warnings.
- `templates().clone(...)` still posts to `/templates/:id/clone` under the versioned base URL. Only an unversioned, session-authenticated clone route exists, so this call returns 404 under an API key and always has. `campaigns().clone(...)` is unaffected and works.
- `webhooks().retryDelivery(...)` still posts to a per-delivery retry path that the API does not serve at any version, so it cannot succeed. Use `webhooks().redeliver(webhookId, options)`, which is live, to replay deliveries after an endpoint outage.
- 400 and 422 both surface as `ValidationException`, whose `getStatusCode()` reports 400 in either case. This matters more now that keys are in play: reusing an idempotency key with a different body comes back from the server as a 422, but reaches you as a `ValidationException` you can only distinguish by its message.

## 3.32.0

### Minor Changes

- New resource **`businessUpgrade()`** — entity-upgrade (a.k.a. fork-with-new-number) flow for toll-free numbers. When a customer forms a new legal entity (e.g. an LLC), this resource reserves a new toll-free number under the new entity, submits it for carrier review, and atomically swaps to it on approval — without disrupting outbound SMS during the 1-2 week review window. Mirrors the same resource on our Node SDK.

  7 methods: `preflight`, `bestPrefill`, `start`, `status`, `cancel`, `resubmit`, `setDisposition`. `start` and `resubmit` accept an optional `EinDocument` (built via `EinDocument.fromFile(File)` or `EinDocument.fromBytes(byte[], filename)`) uploaded as multipart form data.

  ```java
  // Preview validation
  JsonObject preview = client.businessUpgrade().preflight(
      BusinessUpgradeResource.PreflightCandidate.builder()
          .businessName("Acme Holdings LLC")
          .brn("12-3456789")
          .brnType("EIN")
          .brnCountry("US")
          .entityType("PRIVATE_PROFIT")
          .build());

  // Submit with IRS letter
  JsonObject result = client.businessUpgrade().start(
      "ws_abc",
      BusinessUpgradeResource.StartUpgradeParams.builder()
          .businessName("Acme Holdings LLC")
          .brn("12-3456789")
          .brnType("EIN")
          .brnCountry("US")
          .entityType("PRIVATE_PROFIT")
          .build(),
      BusinessUpgradeResource.EinDocument.fromFile(new File("./CP-575.pdf")));
  ```

## 3.31.0

### Minor Changes

- New method **`conversations().suggestReplies(id)`** — returns AI-generated reply suggestions for a conversation. Mirrors the same method on our Node, Python, Ruby, Go, and C# SDKs (closes a feature gap).

  ```java
  JsonObject response = client.conversations().suggestReplies("conv_abc");
  // response.suggestions[] each has .text and .tone
  ```

## 3.30.0

### Minor Changes

- `enterprise.workspaces().submitVerification(workspaceId, VerificationSubmitInput)`: rewritten to match the actual API shape (camelCase top-level, nested `address`/`contact` objects, `entityType` + `brn`/`brnType`/`brnCountry` instead of the previous flat `businessType`/`ein` shape). The previous shape didn't match the server endpoint and produced 400s.
- **Partial-update friendly:** for resubmits on existing workspaces, set only the fields you want to change — everything else is filled from the existing record. Null fields on `VerificationSubmitInput` are stripped before serialization. Hosted page URLs (`/biz/`, `/opt-in/`, `/legal/`) generated during provision are auto-preserved.
- `enterprise.workspaces().resubmitVerification(workspaceId, partial)`: convenience alias for resubmits — same as `submitVerification` but reads more naturally for one-field-change use cases.
- New `VerificationSubmitInput` model (with nested `Address` and `Contact` builders) — type-safe payload shape with all fields documented.
- The legacy `submitVerification(String, JsonObject)` overload is preserved for callers that build the payload by hand.

### Server-side fixes paired with this release

- `/api/v1/enterprise/workspaces/:id/verification/submit` now returns specific missing-field errors (e.g. `"Missing required fields: website"`) instead of listing every required field whether present or not.
- Endpoint accepts both flat and `{ verification: {...} }` wrapped shapes (matches `/enterprise/provision`).
- `useCase` validation expanded from 23 entries to the full 43-value carrier use-case enum.

## 3.29.0

### Minor Changes

- `contacts().bulkMarkValid(BulkMarkValidRequest)`: clear the invalid flag on many contacts at once (up to 10,000 per call). Escape hatch for when auto-mark misclassifies at scale. Use `BulkMarkValidRequest.ofIds(list)` or `BulkMarkValidRequest.ofListId("lst_xxx")`.
- New `WebhookEventType` enum exposes all event type string literals, including four new list-health values: `CONTACT_AUTO_FLAGGED`, `CONTACT_MARKED_VALID`, `CONTACTS_LOOKUP_COMPLETED`, `CONTACTS_BULK_MARKED_VALID`.
- New `ListHealthEventSource` enum (frozen): `SEND_FAILURE | CARRIER_LOOKUP | USER_ACTION | BULK_MARK_VALID` — the `source` field on auto-flag and mark-valid webhooks.
- `Contact` gains `userMarkedValidAt` — when a user manually cleared an auto-flag. Carrier re-checks respect this timestamp and leave the contact clean.

## 3.28.0

### Minor Changes

- `contacts().markValid(id)`: clear the auto-exclusion flag on a contact.
- `contacts().checkNumbers(listId, force)`: trigger a background carrier lookup.
- `Contact` model gains optedOut, lineType, carrierName, lineTypeCheckedAt, invalidReason, invalidatedAt (accepts snake_case or camelCase from server).

## 3.18.1

### Patch Changes

- fix: webhook signature verification and payload parsing now match server implementation
  - `verifySignature()` accepts `String timestamp` parameter for HMAC on `timestamp.payload` format (3-arg overload deprecated)
  - `parseEvent()` handles `data.object` JSON nesting (with flat `data` fallback for backwards compat)
  - `WebhookEvent` adds `boolean livemode`, `JsonElement created` fields
  - `WebhookMessageData` renamed `messageId` to `id` (with `getMessageId()` deprecated alias)
  - Added `direction`, `organizationId`, `text`, `messageFormat` fields
  - `generateSignature()` accepts `String timestamp` parameter (2-arg overload deprecated)
  - 5-minute timestamp tolerance check prevents replay attacks

## 3.18.0

### Minor Changes

- Add MMS support for US/CA domestic messaging

## 3.17.0

### Minor Changes

- Add structured error classification and automatic message retry
- New `errorCode` field with 13 structured codes (E001-E013, E099)
- New `retryCount` field tracks retry attempts
- New `retrying` status and `message.retrying` webhook event

## 3.16.0

### Minor Changes

- Add `transferCredits()` for moving credits between workspaces

## 3.15.2

### Patch Changes

- Add metadata support to batch message items

## 3.13.0

### Minor Changes

- Campaigns, Contacts & Contact Lists resources with full CRUD
- Template clone method
