# sendly-java

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
