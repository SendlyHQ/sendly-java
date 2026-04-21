# sendly-java

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
