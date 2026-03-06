# sendly-java

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
