# sendly-java

## 4.3.0

### Minor Changes

- **Rules take their conditions and actions as objects.** `rules().create(name, conditions, actions)` and `create(name, conditions, actions, priority)` take each as a `Map`, which is how the API stores and evaluates them: conditions `intent`, `sentiment`, `intentConfidenceMin`, `sentimentConfidenceMin`; actions `addLabels` (label IDs) and `closeConversation`. `Rule` gains `getConditionsMap()`, `getActionsMap()` and `isEnabled()`, and reads `createdAt` and `updatedAt`.

  ```java
  Rule rule = client.rules().create("Billing questions",
      Map.of("intent", "billing"),
      Map.of("addLabels", List.of("lbl_123")));
  ```

- **Options the API already supported.** `webhooks().getDeliveries(id, limit, offset, status)` pages and filters a webhook's deliveries. `enterprise().workspaces().inheritVerification(workspaceId, sourceWorkspaceId, true)` copies only the business details, buys the workspace its own toll-free number and submits it; the response has `newNumber: true`. `ListMessagesRequest.Builder.direction("inbound" | "outbound")` filters `messages().list()` and `each()`.

- **New getters for fields the API sends.** `TemplatePreview.getCharacterCount()` and `getSegmentCount()`. `Campaign.getBatchId()`. `CampaignPreview.getCurrentBalance()`, `hasEnoughCredits()`, `getOptedOutCount()` and `getInvalidCount()`. `BatchPreviewResponse.getDuplicates()` and `getWarnings()`. `ApiKey.getRevokedAt()`, set by `getApiKey(id)` and `revokeApiKey(id)`; it is null on the keys `listApiKeys()` returns, revoked ones included, because the list does not send it. `WebhookTestResult.getMessage()`.

- **Error codes.** `CallErrorCode.FROM_NUMBER_NOT_SUPPORTED` (400 when the `from` number is outside the US and Canada). `CallErrorCode` and `RcsErrorCode` gain `TOO_MANY_FAILED_KEY_ATTEMPTS` and `TOO_MANY_CONCURRENT_VERIFICATIONS`, the two 429s any API-key request can answer with. New constructor `RateLimitException(message, retryAfter, apiErrorCode)`; `getErrorCode()` is still `RATE_LIMIT_EXCEEDED`, and the API's code is on `getApiErrorCode()`.

- **WhatsApp extras.** On `whatsapp().senders()`: `uploadProfilePhoto(phoneNumber, File)` and `uploadProfilePhoto(phoneNumber, byte[], filename)` (a JPEG or PNG of at most 5 MB, sent as the multipart field `file`), `deleteProfilePhoto(phoneNumber)`, `getConversationalComponents(phoneNumber)` and `updateConversationalComponents(phoneNumber, UpdateWhatsAppConversationalComponentsRequest)` (ice breakers and commands; each list you set replaces the stored one and an empty list clears it), and `setCalling(phoneNumber, enabled)`, which returns `WhatsAppCallingSettings`. `WhatsAppSender` gains `getBusinessAccountId()`, `getBusinessName()`, `isCallingEnabled()` and `isOutboundCallingAllowed()`. There is no API for placing WhatsApp calls.

  ```java
  client.whatsapp().senders().updateConversationalComponents("+15125550188",
      UpdateWhatsAppConversationalComponentsRequest.builder()
          .iceBreakers(List.of("What are your hours?"))
          .commands(List.of(new WhatsAppCommand("menu", "See today's menu")))
          .build());
  ```

- **Add a number to a connected WhatsApp Business account by code.** `whatsapp().signup().create(CreateWhatsAppSignupRequest)` takes `businessAccountId`, `verificationMethod` (`WhatsAppVerificationMethod.SMS` or `VOICE`) and `displayName`; with `businessAccountId` there is no Facebook step, the same $19 one-time fee applies (refunded if it fails), and the signup comes back `verifying`. A `businessAccountId` that is present but empty or whitespace-only throws `ValidationException` before anything is sent, instead of starting a paid Facebook signup. New `signup().verify(id, code)` and `signup().resend(id)` / `resend(id, verificationMethod)`. `WhatsAppSignup` gains `getVerificationMethod()`, `getVerificationAttemptsRemaining()` and `getVerificationCode()` (the code once its text has arrived on the number, else null); `WhatsAppSignupSession` gains `getPhoneNumber()`, `getBusinessAccountId()`, `getVerificationMethod()` and `getVerificationAttemptsRemaining()`. The status can be `verifying`, and the failure reasons include `verification_start_failed`, `verification_failed` and `verification_expired`.

- **Three new WhatsApp calls are not retried automatically.** `signup().verify(id, code)`, `signup().create(CreateWhatsAppSignupRequest)` with a `businessAccountId` and `senders().uploadProfilePhoto(...)` retry only a 429 rate limit; a 5xx, a 4xx or a network error is thrown on the first attempt, and the HTTP client underneath does not re-send them on its own either. Every code submission uses one of the 5 attempts (and after a 502 `whatsapp_activation_pending` WhatsApp has already accepted the code), and each add-by-code attempt after a failed one, such as a 502 `whatsapp_verification_start_failed`, would start a new signup and charge its $19 fee again. `resend()`, the Facebook `create()` and every existing call keep the usual retries. New low-level overloads `post(path, body, idempotencyKey, autoIdempotencyKey, retryErrors)` and `postMultipart(path, body, retryErrors)` on `Sendly`.

- **Calls say how they connect.** `Call.getChannel()` is `phone`, `whatsapp` or `browser` (new constants class `CallChannel`); an unknown value is returned as sent. The `call.started`, `call.completed` and `call.recording.ready` webhook objects carry the same `channel` key.

- **`whatsapp_send_unconfirmed` (409) is thrown on the first attempt.** A WhatsApp send whose outcome is unknown was marked failed and refunded but may still be delivered, so check before sending it again (it could arrive twice). The client retries a 4xx with a JSON body, and would have retried this one; it no longer does, nor `whatsapp_verification_failed` (409 after 5 wrong codes), which a retry would have reported as `signup_not_active`. The javadoc and README now say that a 502 `whatsapp_send_failed` means the message provably never reached the carrier, so it was not sent and is safe to send again, and document the new WhatsApp error codes.

### Fixes

- **A 422 keeps its status.** A `ValidationException` from a 422, such as `idempotency_key_mismatch`, reported `getStatusCode()` as 400. It now reports 422; a 400 is unchanged. `ValidationException` gains a `(message, statusCode)` constructor.
- **Methods that failed on every call now work.** Each was checked against the handler it calls.
  - `webhooks().list()` and the enterprise `listKeys()`, `listOptInPages()`, `listWebhooks()`, `listInvitations()` and `analytics().delivery()` threw Gson's `JsonSyntaxException`, which is not a `SendlyException`, because those endpoints answer with a bare JSON array. An array body is now returned as `{"data": [...]}`: `list()` and `listKeys()` return their items, and the four methods that return a `JsonObject` return the list under `data`.
  - `drafts().approve()`, `templates().publish()`, `templates().clone(id)` and `verify().resend()` sent the body `null`, which the API rejects with a 400 before the request reaches its route. They send `{}`, and so does a POST through the client without a body, such as `post(path, null)`. `put()` and `patch()` still send what they are given, so a null body there is refused as before instead of becoming a write that resets a setting.
  - `campaigns().schedule()` sent `scheduled_at`, and the API answered 400 `scheduledAt is required`. It sends `scheduledAt`.
  - `enterprise().workspaces().createKey(workspaceId)` sent no name, which the API requires. A null or empty name is sent as "API key".
  - `messages().sendGroup()` threw `UnsupportedOperationException`, which is not a `SendlyException`, after a live group send had gone out and been charged, because a live send lists its recipients as objects. `getTo()` still returns the phone numbers, and the new `getRecipients()` returns each `GroupRecipient` with `getPhoneNumber()` and `getStatus()`; it is empty for a simulated send, which lists only numbers.
  - `rules().list()`, `create()` and `update()` threw `JsonSyntaxException` on any rule stored with object conditions, which is every rule the API can evaluate.
- **Values that were wrong on every call.**
  - Scheduled messages: `getScheduledAt()`, `getCreatedAt()`, `getSentAt()` and `getCancelledAt()` were null and `getCreditsReserved()` and `getCreditsRefunded()` 0, because the API answers in camelCase. `getCancelledAt()` stays null on `cancelScheduled()`, whose response does not carry it; read it from `getScheduled()`.
  - `messages().listScheduled()` reported pagination the API never sent: `getLimit()` was always 20, `getOffset()` always 0 and `hasMore()` always false, so a loop on `hasMore()` stopped after the first page. `GET /messages/scheduled` answers only `data` and `count`, so the limit and offset are now the request's as the API applies them (a limit of at most 100, 50 when unset), and `hasMore()` is true when the page is full; the page after an exactly full last page comes back empty. `getTotal()` is the page's `count`, because the API sends no total. New constructor `ScheduledMessageList(JsonObject, ListScheduledMessagesRequest)`.
  - Campaigns: every count and most dates were null, because the API answers in camelCase. They are read from the camelCase keys, and the text and contact list fall back to `messageText` and `targetListId`. `campaigns().send()` returned a campaign with no ID and no counts; it now has the campaign ID, `getBatchId()`, the recipient, sent and failed counts and credits used, and `getStatus()` is the batch's status. `campaigns().preview()` returned 0 for the recipient count and credit estimate.
  - `messages().each()` stopped after the first 100 messages, because `MessageList.hasMore()` read `has_more` and the API sends `hasMore`. `each(request)` also ignored the request's `status` and `to`; it applies them, and `direction`, to every page.
  - `Message.getErrorMessage()` was always null; the API sends the failure text as `error`.
  - `messages().previewBatch()` read keys the API never sends, so `canSend()` was always false and the totals and balance 0. `canSend()` is true when a message is sendable, nothing is blocked except recipients who opted out (a live send skips those but rejects the whole batch for any other block), the batch has no more than 10,000 messages (the send rejects a larger one with `batch_too_large`, which the preview only warns about), the balance covers a live send, and the key has the `sms:send` scope. Apart from the credits these are a live send's checks: the preview checks the workspace's verification and each destination for a test key too, and a test-key `sendBatch()` skips both, so on a workspace that is not verified yet a test key's `canSend()` is false although its sandbox send goes through. `getBlockReasons()` counts the blocked messages by reason.
  - `webhooks().getDeliveries()` always returned an empty list, and `webhooks().test()` left `getStatusCode()` and `getResponseTimeMs()` null.
  - `account().get()` left `getId()`, `getEmail()` and `getCreatedAt()` null; the API nests them under `user`.
  - `account().getApiKey()` reported every key as active with no permissions, and a key `revokeApiKey()` had just revoked reported `isRevoked()` false. They are read from `scopes`, `isActive`, `revoked` and `revokedAt`.
  - `templates().preview()` left `getPreviewText()` and `getId()` null; the API sends `rendered_text` and `template_id`.
  - `verify().list()` always reported `isHasMore()` false; the API sends `has_more`.
  - `Label.getCreatedAt()` was always null; the API sends `createdAt`.
- **Errors are `SendlyException`s and say what went wrong.** A response that is not JSON, such as an HTML 502 or 524 page from a proxy, escaped as Gson's `JsonSyntaxException` and was never retried. It now maps to the usual exception for its status with the message `HTTP <status>: ` and the start of the body, so a 5xx is retried with the usual backoff; `getResponseBody()` is null for such a body. A 4xx page that is not JSON, such as a firewall's 403 or a proxy's 413, is thrown at once, as before, because a retry cannot change it. A 2xx whose body is not JSON throws `SendlyException("Invalid JSON response from API")` and is not retried, because the request succeeded. An error body with the sentence only under `error`, as every enterprise endpoint sends, surfaced as "Unknown error"; the sentence is now the message, and a body with neither is "HTTP <status>".
- **A retry after a 5xx keeps the POST's automatic `Idempotency-Key`.** The client gave that retry a new key. When a proxy's 502 or 524 hid a request the API had finished, the retry ran it again: a second message or OTP, a second number purchase or credit transfer. The proxy in front of the API sends such errors as JSON to a client that accepts JSON, as this one does, so 4.2.0 did this too, and the HTML form of the page, newly retried in this release, would have done it as well. The API never saves a 5xx under a key, so after its own 5xx the same key runs the request again as a new one would, and after a proxy's it returns the saved result. The key is kept as after a timeout; a key you supply was already kept.
- **Only a rate limit worth waiting for is waited out.** A 429 is retried only when its code is `rate_limit_exceeded`, `too_many_concurrent_verifications`, `rate_limited` or `provision_rate_limit` (or it has none) and its retry-after is 60 seconds or less; it keeps its idempotency key. So the enterprise `provision()` and `provisionBulk()` calls still wait out the per-minute provisioning limit, while its hourly limit, whose wait runs up to an hour, is thrown at once. Anything else is thrown at once with `getRetryAfter()` set: `too_many_failed_key_attempts` (the key is wrong; its lockout lasts up to 5 minutes, and the client used to sleep through it and retry, blocking for up to 20 minutes), a verification limit (`retryAfter` up to 10 minutes or a day, which verify send used to retry three times and report with `getRetryAfter()` 0), `max_attempts_exceeded` and daily limits. `getRetryAfter()` comes from the `Retry-After` header, else the body's `retryAfter`; a date in the header no longer throws `NumberFormatException`. A 429 with no attempts left is thrown without first waiting out its `Retry-After`.
- **A waited-out 429 is retried right after its `Retry-After`, with no added backoff.** The client slept the exponential backoff on top of the wait, so the one-second busy key check (`too_many_concurrent_verifications`) waited 2, 3 and 5 seconds, and a 60-second limit 61 to 64. A 429 without a retry-after still gets the backoff. A thread interrupted during the wait still throws `NetworkException("Request interrupted")`.
- **`enterprise().workspaces().provisionBulk()` accepts up to 100 workspaces, the API's limit.** It refused more than 50 with a `ValidationException` before sending anything.
- **An id of `.` or `..` is refused before anything is sent.** Percent-encoding leaves dots as they are, so such an id was resolved as a path segment and the request reached a different endpoint: `enterprise().workspaces().revokeKey("ws_1", "..")` sent `DELETE /api/v1/enterprise/workspaces/ws_1/`, which deletes the workspace, and `contacts().lists().removeContact(listId, "..")` sent `DELETE` to the list, which deletes it. Every id put into a request path is now checked in one place, and an empty, null, `.` or `..` id throws `ValidationException` without a request. Ids that merely contain dots are sent as before.
- **`webhooks().backfill()` says to dedupe on `event.id`.** Its javadoc said synthesized events get fresh IDs and to dedupe on `data.object.id`. A backfilled message event carries the event id the original dispatch used, and a message's sent and delivered events share `data.object.id`.
- **WhatsApp javadoc matches the API.** It now says that the API never sends the `expired` signup status, that a closed window returns its past `getExpiresAt()` rather than null, that a media send returns its caption as `getText()`, how in-window replies are priced (1 credit for the first 1,000 per sending number each month, then the destination's utility price), which roles and scopes connecting and editing need, the `waba_mismatch` and `registration_timeout` failure reasons, `template_header_variable_unsupported`, `whatsapp_unavailable` (503), `whatsapp_signup_limit_reached` (429), and `whatsapp_send_failed` as a final 422 or a retried 502. Nothing changes at runtime.

### Deprecated

- `rules().create(name, List, List)` and `create(name, List, List, priority)`, and `Rule.getConditions()` and `getActions()`. Conditions and actions are objects, so use the `Map` overloads and `getConditionsMap()` and `getActionsMap()`. The List overloads now send a list's only entry as the object, which makes the rule work, and throw `ValidationException` for a list with more than one entry; before, they stored a rule that matched every message and did nothing. The getters return the object as a one-entry list.

### Upgrade notes

Nothing was removed or renamed and no signature changed. A few behaviours differ:

- `messages().each()` walks every page, as its javadoc says, so it makes more requests than before.
- `messages().listScheduled()` reports `hasMore()` true for a full page, so a loop on it fetches the next page where it used to stop.
- `enterprise().workspaces().listOptInPages()`, `listWebhooks()`, `listInvitations()` and `analytics().delivery()` return `{"data": [...]}` where they used to throw.
- A POST retried after a 5xx sends the same automatic `Idempotency-Key` as its first attempt instead of a new one.
- A 429 that is not retried is thrown at once rather than after sleeping through its `Retry-After`, and codes that cannot change by waiting (`too_many_failed_key_attempts`, `max_attempts_exceeded`, daily limits) are no longer retried at all.
- Exception messages for bodies without a `message` are the API's `error` sentence or "HTTP <status>" instead of "Unknown error".
- `inheritVerification()` sends the source as `sourceWorkspaceId`, the key the API documents.
- `whatsapp().signup().create(null)` no longer compiles, because both `create` overloads match; it always threw `ValidationException`. Cast the argument, as in `create((String) null)`.
- A build with `-Xlint:deprecation -Werror` fails on the deprecated `rules()` List overloads and getters; move to the `Map` forms. A call that passes `null` literals as both `rules().create(...)` conditions and actions no longer compiles, because both overloads match; it always threw `ValidationException`.

## 4.2.0

### Minor Changes

- **Configure voice from code on `voice()`.** Everything a call depends on used to be dashboard-only; it is now on three sub-resources. `voice().numbers()`: `list()`, `get(number)`, `update(number, UpdateVoiceNumberRequest)` (switch voice on or off, choose how the number answers, point it at an agent) and `registerEmergencyAddress(number, EmergencyAddress)`. `voice().agents()`: `list()`, `create(CreateVoiceAgentRequest)`, `get(id)`, `update(id, UpdateVoiceAgentRequest)` and `delete(id)`. `voice().voices()`: `list()`. A `number` is the number's id or its E.164 phone number, percent-encoded in the path (`+15555550188` is sent as `%2B15555550188`). Every write takes an `IdempotentRequestOptions` overload, and the two POSTs send an `Idempotency-Key` automatically like every other POST. Blank ids and numbers, a blank agent `name` on create, and a blank `street`, `city`, `state` or `zip` are refused client-side with a `ValidationException` before any request is made; phone formats and modes are left to the server.

  These endpoints use the existing `calls:read` / `calls:write` scopes and need a live key for writes. In a team workspace, number and emergency-address writes also need a role that can change settings and agent writes a role that can manage API keys (403 `forbidden` otherwise). Switching voice on changes how real calls to the number are answered; the first emergency address on a number adds $1.50 a month. Deleting an agent that still answers a number is refused with 409 `agent_in_use`.

  New models: `VoiceNumber`, `VoiceNumberListResponse`, `VoiceNumberEmergencyAddress`, `EmergencyAddress` (a builder for registering, and the address read back), `VoiceNumberRates`, `UpdateVoiceNumberRequest`, `VoiceAgent`, `VoiceAgentListResponse`, `VoiceAgentTools` (read back, or built to set on a request), `CreateVoiceAgentRequest`, `UpdateVoiceAgentRequest`, `DeletedVoiceAgent`, `Voice`, `VoiceListResponse`, and the string-constant class `VoiceMode` (`NONE`, `RING_DASHBOARD`, `AGENT`).

- **`CallErrorCode` covers voice configuration**: `AGENT_IN_USE`, `AGENT_LIMIT`, `INVALID_VOICE_MODE`, `INVALID_ADDRESS`, `E911_NOT_APPLICABLE`, `VOICE_ATTACH_FAILED` and `CARRIER_REFUSED` are new; `NUMBER_NOT_FOUND`, `AGENT_REQUIRED`, `AGENT_DISABLED` and `FORBIDDEN` now also describe their voice-configuration meanings.

- **`SendlyException.getResponseBody()`** returns the API response body an error was mapped from, as a Gson `JsonObject`, for every resource. It reaches fields beyond `error` and `message`, such as `numbers` on 409 `agent_in_use` and `suggested` on 422 `invalid_address`. It is null for errors that did not come from a response.

- **`delete(path, idempotencyKey)`** on the client sends a caller-supplied `Idempotency-Key` on DELETE, like the PATCH and PUT overloads. No key is generated automatically; `delete(path)` behaves exactly as before.

### Fixes

- **Recording channels are documented the right way round.** `calls().recording()` described agent-call recordings as caller on the left and agent on the right. Agent-handled calls are recorded with the agent on the left channel and the other party on the right.

## 4.1.0

### Minor Changes

- **Voice calls on `calls()`.** Place a phone call that one of your AI agents handles, list and inspect calls, end one, and fetch its recording: `calls().create(CreateCallRequest)`, `list()` / `list(ListCallsOptions)`, `get(id)`, `hangup(id)` and `recording(id)`. `create` and `hangup` also take an `IdempotentRequestOptions` overload; both send an `Idempotency-Key` automatically like every other POST. `create` refuses client-side (a `ValidationException`, no request made) when `to` or `agentId` is blank, and the id methods when the id is blank; phone-number format is left to the server. Calls need an API key with the `calls:read` / `calls:write` scopes, a live key for writes, and answer 404 `voice_not_enabled` (a `NotFoundException`) until voice is enabled for your workspace.

  Calls are prepaid from the workspace balance per started minute (an agent-handled outbound call is 10 credits a minute; an unanswered call costs nothing) and can only be placed to US and Canadian numbers. The number you call from must have voice switched on and an emergency address registered in the dashboard.

  New models: `Call` (with `getTranscript()`, populated by `get(id)` for agent-handled calls and `null` otherwise), `CallTranscriptLine`, `CallListResponse` (`getData()`, `getTotal()`, `getLimit()`, `getOffset()`, `hasMore()`), `CallRecording` (`getUrl()` and `getExpiresAt()` are `null` until the status is `ready`; the URL works for five minutes), `CreateCallRequest` and `ListCallsOptions` builders, and the string-constant classes `CallStatus`, `CallDirection`, `CallKind`, `CallHandledBy`, `CallBilling`, `CallRecordingStatus` and `CallErrorCode` (every `error` code the calls endpoints answer with, e.g. `e911_required`, `lines_busy`, `from_number_required`, `call_not_found`).

- **`OwnedNumber.isVoiceEnabled()` and `getVoiceMode()`** on `numbers().list()` and `numbers().get(id)`: whether the number can take and place calls, and how it answers (`none`, `ring_dashboard` or `agent`), so you can find a `from` number for `calls().create`.

- **Call webhooks carry `billing` and `metadata`.** The `call.started`, `call.completed` and `call.recording.ready` payloads (already declared in `WebhookEventType`) now also include the call's `billing` state and the `metadata` pairs you attached on create. Read them through `getRawObject()` or `objectAs(...)`; no type changed. Nothing in this release was deprecated, renamed or removed.

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
