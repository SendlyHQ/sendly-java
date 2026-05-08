# sendly-java

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
- `useCase` validation expanded from 23 entries to the full 43-value Telnyx enum.

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
