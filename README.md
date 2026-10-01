<p align="center">
  <img src="https://raw.githubusercontent.com/SendlyHQ/sendly-java/main/.github/header.svg" alt="Sendly Java SDK" />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/live.sendly/sendly-java"><img src="https://img.shields.io/maven-central/v/live.sendly/sendly-java?style=flat-square" alt="Maven Central" /></a>
  <a href="https://github.com/SendlyHQ/sendly-java/blob/main/LICENSE"><img src="https://img.shields.io/github/license/SendlyHQ/sendly-java?style=flat-square" alt="license" /></a>
</p>

# Sendly Java SDK

Official Java SDK for the Sendly API — SMS and MMS, WhatsApp, RCS, voice calls,
numbers, verification, and everything around them.

## Requirements

- Java 17+
- Maven or Gradle

## Installation

### Maven

```xml
<dependency>
    <groupId>live.sendly</groupId>
    <artifactId>sendly-java</artifactId>
    <version>4.3.0</version>
</dependency>
```

### Gradle (Groovy)

```groovy
implementation 'live.sendly:sendly-java:4.3.0'
```

### Gradle (Kotlin)

```kotlin
implementation("live.sendly:sendly-java:4.3.0")
```

## Quick Start

```java
import com.sendly.Sendly;
import com.sendly.models.Message;

Sendly client = new Sendly("sk_live_v1_your_api_key");

// Send an SMS
Message message = client.messages().send(
    "+12025550143",
    "Hello from Sendly!"
);

System.out.println(message.getId());     // "4a7c1e2f-9b3d-4c8a-91f2-7d5e6a0b3c19"
System.out.println(message.getStatus()); // "queued"
```

The API key is a constructor argument — the SDK does not read one from the
environment. An empty or null key throws `AuthenticationException` before any
request is made.

## Prerequisites for Live Messaging

Before sending live SMS messages, you need:

1. **Business Verification** - Complete verification in the [Sendly dashboard](https://sendly.live/dashboard)
   - **International**: Instant approval (just provide Sender ID)
   - **US/Canada**: Requires carrier approval

2. **Credits** - Add credits to your account
   - Test keys (`sk_test_v1_*`) work without credits (sandbox mode)
   - Live keys (`sk_live_v1_*`) require credits for each message

3. **Live API Key** - Generate after verification + credits
   - Dashboard → API Keys → Create Live Key

### Test vs Live Keys

| Key Type | Prefix | Credits Required | Verification Required | Use Case |
|----------|--------|------------------|----------------------|----------|
| Test | `sk_test_v1_*` | No | No | Development, testing |
| Live | `sk_live_v1_*` | Yes | Yes | Production messaging |

> **Note**: You can start development immediately with a test key. Messages to sandbox test numbers are free and don't require verification.

## Configuration

```java
import com.sendly.Sendly;
import java.time.Duration;

Sendly client = new Sendly("sk_live_v1_xxx",
    new Sendly.Builder()
        .baseUrl("https://sendly.live/api/v1")   // default
        .timeout(Duration.ofSeconds(60))         // sets read + write timeout
        .connectTimeout(Duration.ofSeconds(10))  // default
        .maxRetries(5)                           // default 3
        .organizationId("org_9f2c")              // default: $SENDLY_ORG_ID
);
```

`readTimeout(...)` and `writeTimeout(...)` set the two halves separately;
`timeout(...)` sets both at once. Defaults are a 10-second connect timeout and
a 30-second read/write timeout (`Sendly.DEFAULT_TIMEOUT`).

When an organization id is set — explicitly or through the `SENDLY_ORG_ID`
environment variable — every request carries it as an `X-Organization-Id`
header. `client.setOrganizationId(id)` changes it afterwards.

### Retries

The client retries failed requests up to `maxRetries` times with exponential
backoff (1s, 2s, 4s, …). `AuthenticationException`, `ValidationException`,
`NotFoundException` and `InsufficientCreditsException` (401, 400, 422, 404 and
402) are never retried. A 429 is retried only in the cases listed under
[Rate Limits](#rate-limits). A 5xx, a timeout, a network error and any other
error status that comes with a JSON body (a 403, 409 or 428, for example) are
retried with the backoff. A POST's retry carries the same idempotency key, and
the API replays a recorded 4xx for that key, so retrying a 403 or 409 returns
the same refusal a few seconds later. `whatsapp_send_unconfirmed` (409), a
WhatsApp send whose outcome is unknown and that may still be delivered, is
thrown on the first attempt instead. Three WhatsApp calls are never retried
except on a 429 rate limit, so a 5xx, a 4xx or a network error from them is
thrown on the first attempt: `whatsapp().signup().verify()` (every submission
uses one of the 5 attempts), `whatsapp().signup().create()` with a
`businessAccountId` (each attempt after a failed one starts a new signup and
charges its fee again) and `whatsapp().senders().uploadProfilePhoto()`. An
error page that is not JSON, such as a proxy's 502 or 524, is retried like any
5xx; a 4xx page that is not JSON (a firewall's 403, a proxy's 413) is thrown at
once. A 5xx that survives every attempt is thrown, so an exception from a 5xx
means the client already retried it on your behalf, except from those three
WhatsApp calls.

## Messages

### Send an SMS

```java
// Marketing message (default)
Message message = client.messages().send("+12025550143", "Check out our new features!");

// Transactional message (bypasses quiet hours)
Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+12025550143")
        .text("Your verification code is: 123456")
        .messageType("transactional")
        .build()
);

// Send from one of your owned numbers (or an alphanumeric sender ID).
// Omit from(...) to use your default sender.
Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+12025550143")
        .text("Hello from our team!")
        .from("+447700900123")
        .build()
);

System.out.println(message.getId());
System.out.println(message.getStatus());
System.out.println(message.getCreditsUsed());
```

Recipients must be in E.164 format and message text is capped at 1600
characters; both are checked client-side and raise a `ValidationException`
before any request is made.

### Send an MMS

Attach media you uploaded with [`media()`](#media). The API accepts only URLs
that `media().upload(...)` returned (up to 10 per message) and refuses any other
URL with a 400 `invalid_request`. MMS must also be enabled for your account
(otherwise a 403 `feature_disabled`).

```java
import java.io.File;
import java.util.List;
import com.sendly.models.MediaFile;

MediaFile receipt = client.media().upload(new File("receipt.jpg"));

Message mms = client.messages().send(
    SendMessageRequest.builder()
        .to("+12025550143")
        .text("Here's your receipt")
        .mediaUrls(List.of(receipt.getUrl()))
        .build()
);

System.out.println(mms.getMediaUrls());
```

### List Messages

```java
// Basic listing
MessageList messages = client.messages().list();

for (Message msg : messages) {
    System.out.println(msg.getTo());
}

// Filtered and paged
MessageList delivered = client.messages().list(
    ListMessagesRequest.builder()
        .status("delivered")
        .to("+12025550143")
        .direction("outbound")   // or "inbound"
        .limit(50)
        .offset(0)
        .build()
);

// Pagination info
System.out.println(delivered.getTotal());
System.out.println(delivered.hasMore());
```

`status`, `to` and `direction` filter on the server. `limit` defaults to 50 and
is capped at 100. A test key lists sandbox messages only, and a live key
production messages only.

### Get a Message

```java
Message message = client.messages().get("4a7c1e2f-9b3d-4c8a-91f2-7d5e6a0b3c19");

System.out.println(message.getTo());
System.out.println(message.getText());
System.out.println(message.getStatus());
System.out.println(message.getDeliveredAt());
```

### Scheduling Messages

```java
import java.time.Instant;
import java.time.temporal.ChronoUnit;

// Schedule a message for future delivery (5 minutes to 5 days ahead)
ScheduledMessage scheduled = client.messages().schedule(
    ScheduleMessageRequest.builder()
        .to("+12025550143")
        .text("Your appointment is tomorrow!")
        .scheduledAt(Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS).toString())
        .build()
);

System.out.println(scheduled.getId());
System.out.println(scheduled.getScheduledAt());

// List scheduled messages, a page at a time
int offset = 0;
ScheduledMessageList result;
do {
    result = client.messages().listScheduled(ListScheduledMessagesRequest.builder()
        .status("scheduled")
        .limit(100)
        .offset(offset)
        .build());
    for (ScheduledMessage msg : result) {
        System.out.println(msg.getId() + ": " + msg.getScheduledAt());
    }
    offset += result.getLimit();
} while (result.hasMore());

// Get a specific scheduled message
ScheduledMessage msg = client.messages().getScheduled("schd_xxx");

// Cancel a scheduled message (refunds the reserved credits)
CancelScheduledMessageResponse cancel = client.messages().cancelScheduled("schd_xxx");
System.out.println("Refunded: " + cancel.getCreditsRefunded() + " credits");
```

`scheduledAt` must be ISO 8601, at least 5 minutes and at most 5 days in the future.

### Batch Messages

```java
// Send multiple messages in one API call
BatchMessageResponse batch = client.messages().sendBatch(
    SendBatchRequest.builder()
        .addMessage("+12025550143", "Hello User 1!")
        .addMessage("+12025550156", "Hello User 2!")
        .addMessage("+12025550172", "Hello User 3!")
        .build()
);

System.out.println(batch.getBatchId());
System.out.println("Total: " + batch.getTotal());
System.out.println("Failed: " + batch.getFailed());
System.out.println("Credits used: " + batch.getCreditsUsed());

// Get batch status
BatchMessageResponse status = client.messages().getBatch("batch_xxx");
for (BatchMessageResult result : status.getMessages()) {
    System.out.println(result.getTo() + ": " + result.getStatus());
}

// List all batches
BatchList batches = client.messages().listBatches();

// Preview batch (dry run) - validates without sending
BatchPreviewResponse preview = client.messages().previewBatch(
    SendBatchRequest.builder()
        .addMessage("+12025550143", "Hello User 1!")
        .addMessage("+447700900123", "Hello UK!")
        .build()
);
System.out.println(preview.getWillSend() + " of " + preview.getTotalMessages() + " sendable");
System.out.println("Credits needed: " + preview.getCreditsNeeded()
    + ", balance " + preview.getCurrentBalance()
    + " (enough: " + preview.hasEnoughCredits() + ")");
System.out.println("Blocked: " + preview.getBlocked() + " " + preview.getBlockReasons());
System.out.println("Duplicates: " + preview.getDuplicates());
preview.getWarnings().forEach(System.out::println);
if (!preview.canSend()) {
    // this batch would be refused
}
```

`canSend()` is true when at least one message is sendable, nothing is blocked
except recipients who opted out (a live send skips those but rejects the whole
batch for any other block), the batch has at most 10,000 messages, the balance
covers a live send, and the key has the `sms:send` scope. The preview checks
the workspace's verification and each destination for a test key too, while a
test-key `sendBatch()` skips both, so on a workspace that is not verified yet a
test key's `canSend()` is false although its sandbox send goes through.
`getBlockReasons()` counts the blocked messages by reason.

The API rejects a batch of more than 10,000 messages with `batch_too_large`;
the preview only warns about it.

### Group MMS

Send a group MMS to 2-8 recipients (US/Canada only). Everyone in `to` shares one
thread and replies fan out to all participants. Requires the `group_mms`
feature, and the sending number must be an MMS-enabled, 10DLC-registered number
you own.

```java
import java.util.List;

GroupMessageResponse group = client.messages().sendGroup(
    SendGroupMessageRequest.builder()
        .to(List.of("+12025550143", "+12025550156"))
        .text("Hey team - quick sync at noon?")
        .build()
);

System.out.println(group.getId());              // msg_xxx
System.out.println(group.getGroupMessageId());  // grp_xxx
System.out.println(group.getStatus());          // sent
for (GroupRecipient r : group.getRecipients()) {
    System.out.println(r.getPhoneNumber() + ": " + r.getStatus());
}
```

`getTo()` holds the recipients' phone numbers. A live send also lists each
recipient with its status in `getRecipients()`; a simulated send
(`isSimulated()`) lists only the numbers, so `getRecipients()` is empty.

### AI Enhance

Rewrite a draft message into a single polished SMS segment (≤160 chars).
Requires the `ai_classification` feature; when AI is unavailable the original
text is returned with an empty explanation. At least one of `text` or
`messageType` is required — with only a `messageType`, a suitable message is
generated instead of rewritten.

```java
EnhanceMessageResponse result = client.messages().enhance(
    EnhanceMessageRequest.builder()
        .text("hey come check out our sale this weekend")
        .messageType("marketing")
        .build()
);

System.out.println(result.getEnhanced());     // polished rewrite
System.out.println(result.getExplanation());  // what changed and why
System.out.println(result.getModel());        // the model that produced it
```

### Iterate All Messages

```java
// Auto-pagination over every message, 100 at a time
for (Message message : client.messages().each()) {
    System.out.println(message.getId() + ": " + message.getTo());
}

// The status, to and direction filters apply to every page
for (Message message : client.messages().each(
        ListMessagesRequest.builder().status("failed").direction("outbound").build())) {
    System.out.println(message.getId() + ": " + message.getErrorMessage());
}
```

`each(...)` ignores the request's `limit` and `offset`. A page request that
fails inside the loop is thrown as a `RuntimeException` whose `getCause()` is
the `SendlyException`.

## Idempotency

POSTs carry an automatically generated `Idempotency-Key`, and the client sends
the same key on every retry of that call: after a 5xx, a timeout, a network
error or a waited-out 429. The API never records a 5xx or a 429 under a key, so
those retries run the request again; when an earlier attempt did finish (a
timeout after the API answered, or a proxy's 502 or 524 in front of a recorded
answer), the retry gets the recorded answer back instead of sending and
charging twice. Pass your own key via `IdempotentRequestOptions` when the
guarantee needs to outlive the process, such as a job queue that re-runs after
a crash or your own retry loop. Reusing a key within 24 hours returns the
original response, and reusing it with a different body is refused with 422
`idempotency_key_mismatch`, so derive keys from something stable in your
domain, like an order id. A 2xx and any 4xx other than a 429 are recorded under
the key, so repeating a refused request with the same key replays the refusal:
use a fresh key to run it again. `sendBatch` sends no automatic key, because
the API already deduplicates identical batches by their contents.

```java
import com.sendly.models.IdempotentRequestOptions;

Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+12025550143")
        .text("Your order has shipped!")
        .build(),
    new IdempotentRequestOptions("order-4821-shipped")
);
```

Keys must be 1-255 printable ASCII characters; anything longer or containing
control characters raises a `ValidationException` up front. PUT, PATCH and
DELETE send a key only when you supply one — nothing is generated for them.

Full details: https://sendly.live/docs/idempotency

## Rate Limits

Requests are counted per API key in a fixed 60-second window that starts with
the key's first request:

| Key | Requests per minute |
|-----|---------------------|
| Test (`sk_test_v1_*`) | 60 |
| Live (`sk_live_v1_*`) | 600 |
| Enterprise master key | 3000 |

Going over returns 429 `rate_limit_exceeded`, which the SDK raises as a
`RateLimitException`. `getRetryAfter()` is the number of seconds to wait, read
from the `Retry-After` header or, when there is none, the body's `retryAfter`;
`getApiErrorCode()` names the limit.

A 429 is waited out and retried within `maxRetries` only when waiting can help
and the wait is 60 seconds or less: an ordinary `rate_limit_exceeded`, a 429
with no code, the per-minute `provision_rate_limit` from enterprise
provisioning, and `too_many_concurrent_verifications` (too many first-time API
key checks running at once, retried after 1 second). The retry goes out right
after the wait, with no backoff added, and keeps the POST's idempotency key.
After the last attempt the exception is thrown without waiting. Every other
429 is thrown at once with `getRetryAfter()` set when the API sent a wait:

- `too_many_failed_key_attempts`: repeated wrong API keys from your address
  locked the account out for up to 5 minutes. Fix the key; until the lockout
  ends the right key is refused too.
- `rate_limit_exceeded` from `verify().send()` or `resend()` once a recipient
  has had 5 codes in 10 minutes or 20 in a day (a wait of a minute or less near
  the end of the window is waited out like any rate limit).
- `max_attempts_exceeded` from `verify().check()`, `daily_call_limit` from
  `calls().create()`, `whatsapp_signup_limit_reached`, and the hourly
  `provision_rate_limit`.

## Media

Upload a file so you can attach it to an MMS. The content type is guessed from
the file name unless you pass one. The API takes a JPEG, PNG or GIF of up to
600 KB: the content type sent must be one of those three, and a file whose
content is not one of them is refused with a 400 `invalid_file`.

```java
import java.io.File;
import com.sendly.models.MediaFile;

MediaFile file = client.media().upload(new File("receipt.jpg"));
System.out.println(file.getUrl());          // pass to mediaUrls(...)
System.out.println(file.getContentType());
System.out.println(file.getSizeBytes());

// Or set the type yourself
MediaFile explicit = client.media().upload(new File("scan"), "image/png");
```

## Contacts & Lists

```java
// Create, read, update, delete
Contact contact = client.contacts().create(CreateContactRequest.builder()
    .phoneNumber("+12025550143")
    .name("Sam Lee")
    .email("sam@acme.example")
    .build());

Contact fetched = client.contacts().get(contact.getId());
// update's result leaves getOptedOut() and getCreatedAt() null; get() returns the whole contact
client.contacts().update(contact.getId(), UpdateContactRequest.builder()
    .name("Sam L.")
    .build());
client.contacts().delete(contact.getId());

// List and search
ContactListResponse page = client.contacts().list(ListContactsRequest.builder()
    .search("sam")
    .limit(50)
    .build());
for (Contact c : page.getContacts()) {
    System.out.println(c.getPhoneNumber() + " " + c.getLineType());
}

// Bulk import
ImportContactsResponse imported = client.contacts().importContacts(
    ImportContactsRequest.builder()
        .contacts(List.of(
            new ImportContactsRequest.ImportContactItem("+12025550143"),
            new ImportContactsRequest.ImportContactItem("+12025550156", "Alex", "alex@acme.example", null)))
        .listId("list_abc123")
        .build());
System.out.println(imported.getImported() + " imported, "
    + imported.getSkippedDuplicates() + " duplicates");
```

Contacts get auto-flagged as invalid when a send fails with a terminal
bad-number error or a carrier lookup says they can't receive SMS. Clear the
flag one at a time or in bulk, and trigger the lookup yourself:

```java
client.contacts().markValid("con_abc123");

BulkMarkValidResponse cleared = client.contacts().bulkMarkValid(
    BulkMarkValidRequest.ofListId("list_abc123"));   // or .ofIds(List.of(...))
System.out.println(cleared.getCleared() + " contacts cleared");

client.contacts().checkNumbers();                      // all un-checked contacts
client.contacts().checkNumbers("list_abc123", true);   // one list, re-check everything
```

Pass `ids` **or** `listId` to `bulkMarkValid`, never both.

### Contact lists

```java
ContactList list = client.contacts().lists().create(CreateContactListRequest.builder()
    .name("Spring campaign")
    .description("Opted in at checkout")
    .build());

ContactListsResponse lists = client.contacts().lists().list();
for (ContactList l : lists.getLists()) {
    System.out.println(l.getName() + ": " + l.getContactCount());
}

client.contacts().lists().addContacts(list.getId(), List.of("con_abc123", "con_def456"));
client.contacts().lists().removeContact(list.getId(), "con_abc123");
client.contacts().lists().update(list.getId(), UpdateContactListRequest.builder()
    .name("Spring campaign 2026")
    .build());
client.contacts().lists().delete(list.getId());
```

## Conversations

Two-way threads with a contact. Gated behind the `conversations_api` feature.

```java
ConversationListResponse conversations = client.conversations().list();
for (Conversation c : conversations) {
    System.out.println(c.getPhoneNumber() + " (" + c.getUnreadCount() + " unread)");
}

// Filtered + paged
ConversationListResponse active = client.conversations().list("active", 25, 0);

// One conversation, with its messages
Conversation conversation = client.conversations().get("conv_abc123", true, 50, 0);
for (Message m : conversation.getMessages()) {
    System.out.println(m.getDirection() + ": " + m.getText());
}

// Reply, and manage state
Message reply = client.conversations().reply("conv_abc123", "On its way!");
client.conversations().markRead("conv_abc123");
client.conversations().close("conv_abc123");
client.conversations().reopen("conv_abc123");

// Labels on a conversation
client.conversations().addLabels("conv_abc123", List.of("lbl_urgent"));
client.conversations().removeLabel("conv_abc123", "lbl_urgent");

// AI helpers (raw JSON)
JsonObject context = client.conversations().getContext("conv_abc123");
JsonObject suggestions = client.conversations().suggestReplies("conv_abc123");
```

## Labels

```java
Label label = client.labels().create("Urgent", "#FF0000", "Needs a reply today");

LabelListResponse labels = client.labels().list();
for (Label l : labels.getData()) {
    System.out.println(l.getName() + " " + l.getColor());
}

client.labels().delete(label.getId());
```

## Drafts

Queue a reply for a human to approve before it sends.

```java
Draft draft = client.drafts().create("conv_abc123", "Thanks — we'll ship today.");

DraftListResponse pending = client.drafts().list("conv_abc123", Draft.STATUS_PENDING, 20, 0);
for (Draft d : pending.getData()) {
    System.out.println(d.getText() + " (" + d.getStatus() + ")");
}

client.drafts().update(draft.getId(), "Thanks — this ships today.");

// Approve it (which sends it) or reject it. Either one settles the draft,
// so calling the other afterwards answers 404.
boolean approved = true;   // your reviewer's decision
if (approved) {
    client.drafts().approve(draft.getId());
} else {
    client.drafts().reject(draft.getId(), "Wrong tone");
}
```

`Draft` carries the status constants `STATUS_PENDING`, `STATUS_APPROVED`,
`STATUS_REJECTED`, `STATUS_SENT` and `STATUS_FAILED`, plus `isPending()`,
`isApproved()` and `isRejected()`.

## Rules

Auto-label rules. When the AI classifier tags an inbound message with an intent
and a sentiment, each enabled rule whose `conditions` match runs its `actions`.
The API reads `conditions` as one object (`intent`, `sentiment`,
`intentConfidenceMin`, `sentimentConfidenceMin`) and `actions` as one object
(`addLabels`, a list of label ids, and `closeConversation`); there is no
text-matching condition and no reply action. `intent` and `sentiment` take a
string or a list of strings, and an empty conditions map matches every
classified message. Rules run in ascending `priority` order.

```java
import java.util.List;
import java.util.Map;

Rule rule = client.rules().create(
    "Label complaints",
    Map.of("intent", "complaint", "intentConfidenceMin", 0.7),
    Map.of("addLabels", List.of("lbl_xxx")),
    10);   // priority

RuleListResponse rules = client.rules().list();
for (Rule r : rules.getData()) {
    System.out.println(r.getName() + " (priority " + r.getPriority()
        + ", enabled " + r.isEnabled() + "): " + r.getConditionsMap() + " -> " + r.getActionsMap());
}

client.rules().update(rule.getId(), Map.of("priority", 20, "enabled", false));
client.rules().delete(rule.getId());
```

The `create(name, List, List)` overloads and `Rule.getConditions()` /
`getActions()` are deprecated: a list's only entry is sent as the object, and a
list with more than one entry throws `ValidationException`. Rules need an API
key with `sms:read` to list and `sms:send` to write.

## Templates

Reusable message bodies with `{{variable}}` placeholders.

```java
Template template = client.templates().create("Order shipped",
    "Hi {{name}}, order {{order}} has shipped!");

TemplateListResponse mine = client.templates().list();
TemplateListResponse presets = client.templates().presets();

Template fetched = client.templates().get(template.getId());
client.templates().update(template.getId(), "Order shipped", "Hi {{name}}, {{order}} is on its way!");
client.templates().publish(template.getId());

// Render it without sending
TemplatePreview preview = client.templates().preview(template.getId(),
    Map.of("name", "Sam", "order", "#4821"));
System.out.println(preview.getPreviewText());
System.out.println(preview.getCharacterCount() + " chars, " + preview.getSegmentCount() + " segment(s)");

// Copy one, optionally under a new name
Template copy = client.templates().clone(template.getId(), "Order shipped (v2)");

// Draft one with AI
GeneratedTemplate generated = client.templates().generate(
    "let customers know their order shipped", "transactional");
System.out.println(generated.getText() + " " + generated.getVariables());

client.templates().delete(template.getId());
```

## Campaigns

Send one message to every contact on one or more lists.

```java
Campaign campaign = client.campaigns().create(CreateCampaignRequest.builder()
    .name("Spring sale")
    .text("Hi {{name}}, 20% off this weekend. Reply STOP to opt out.")
    .contactListIds(List.of("list_abc123"))
    .build());

// Check the cost and blast radius first
CampaignPreview preview = client.campaigns().preview(campaign.getId());
System.out.println(preview.getRecipientCount() + " recipients, "
    + preview.getEstimatedCredits() + " credits, balance " + preview.getCurrentBalance()
    + " (enough: " + preview.hasEnoughCredits() + ")");
System.out.println("Left out: " + preview.getOptedOutCount() + " opted out, "
    + preview.getInvalidCount() + " invalid, " + preview.getBlockedCount() + " blocked");

// Send now, or schedule it instead (a sent campaign can no longer be
// scheduled or cancelled). send() answers with the batch it started.
Campaign started = client.campaigns().send(campaign.getId());
System.out.println(started.getBatchId() + " " + started.getStatus() + ": "
    + started.getSentCount() + "/" + started.getRecipientCount() + " sent, "
    + started.getFailedCount() + " failed, " + started.getCreditsUsed() + " credits");

client.campaigns().schedule(campaign.getId(), ScheduleCampaignRequest.builder()
    .scheduledAt(Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString())
    .timezone("America/Chicago")
    .build());

// Follow it, then stop or copy it
Campaign live = client.campaigns().get(campaign.getId());
System.out.println(live.getSentCount() + "/" + live.getRecipientCount()
    + " sent, " + live.getDeliveredCount() + " delivered");
client.campaigns().cancel(campaign.getId());
Campaign copy = client.campaigns().clone(campaign.getId());

CampaignList page = client.campaigns().list(ListCampaignsRequest.builder()
    .status("completed")
    .limit(20)
    .build());
```

## Verify (OTP)

Send and check one-time passcodes without running your own OTP store.

```java
// Send a code
SendVerificationResponse sent = client.verify().send(
    new SendVerificationRequest("+12025550143")
        .setAppName("Acme")
        .setCodeLength(6)
        .setTimeoutSecs(600));
System.out.println(sent.getId() + " expires " + sent.getExpiresAt());
if (sent.isSandbox()) {
    System.out.println("Sandbox code: " + sent.getSandboxCode());
}

// Check what the user typed. A wrong code is a 400 invalid_code error, and
// its body says how many attempts are left. Once they run out, check throws
// a RateLimitException with code max_attempts_exceeded: send a new code.
try {
    CheckVerificationResponse check = client.verify().check(sent.getId(), "123456");
    System.out.println("Verified at " + check.getVerifiedAt());
} catch (ValidationException e) {
    if (!"invalid_code".equals(e.getApiErrorCode())) throw e;
    System.out.println(e.getResponseBody().get("remaining_attempts").getAsInt() + " attempts left");
}

// Resend, inspect, and list
client.verify().resend(sent.getId());
Verification verification = client.verify().get(sent.getId());
VerificationListResponse recent = client.verify().list(
    new ListVerificationsRequest().setLimit(20).setStatus("verified"));
for (Verification v : recent.getVerifications()) {
    System.out.println(v.getPhone() + ": " + v.getStatus());
}
```

A recipient gets at most 5 codes in 10 minutes and 20 in a day. Past that,
`send()` and `resend()` throw a `RateLimitException` with code
`rate_limit_exceeded` and `getRetryAfter()` set, without waiting (unless the
wait left is a minute or less, which the client waits out).

### Hosted verification sessions

Hand the phone-number step to a Sendly-hosted page and validate the token it
sends you back.

```java
VerifySession session = client.verify().sessions().create(
    CreateSessionRequest.builder()
        .successUrl("https://acme.example/verified")
        .cancelUrl("https://acme.example/cancelled")
        .brandName("Acme")
        .brandColor("#0055FF")
        .build());
System.out.println("Send the user to: " + session.getUrl());

// On your success URL, validate the token you were handed
ValidateSessionResponse validated = client.verify().sessions().validate(token);
if (validated.isValid()) {
    System.out.println("Verified " + validated.getPhone());
}
```

## Numbers

Discover, buy, and manage the phone numbers you own.

```java
// What Sendly can provision, and what's available right now
NumberCountriesResponse countries = client.numbers().listCountries();
for (NumberCountry country : countries.getCountries()) {
    System.out.println(country.getCode() + ": " + country.getNumberTypes());
}

AvailableNumbersResponse available = client.numbers().listAvailable("GB", "mobile");
AvailableNumbersResponse matching = client.numbers().listAvailable("US", "local", "512");
for (AvailableNumber n : available.getNumbers()) {
    System.out.println(n.getPhoneNumber() + " " + n.getMonthlyCost() + " " + n.getCurrency());
}

// Buy one. Prices come back already priced for your account.
AvailableNumber pick = available.getNumbers().get(0);
BuyNumberResponse result = client.numbers().buy(BuyNumberRequest.builder()
    .phoneNumber(pick.getPhoneNumber())
    .countryCode(pick.getCountry())
    .phoneNumberType(pick.getNumberType())
    .monthlyCost(pick.getMonthlyCost())
    .build());

if ("provisioning".equals(result.getStatus())) {
    // The number is being set up — poll list() until it shows as active
} else if (result.getAction() != null) {
    // documents_required / payment_required: a human has to finish this on a
    // hosted Sendly page. Hand them the URL and the short code, wait, then
    // call buy() again with the SAME body plus actionCode(...).
    NumberBuyAction action = result.getAction();
    System.out.println(action.getUrl() + " code " + action.getCode());
}

// List the numbers you own
OwnedNumbersResponse owned = client.numbers().list();
for (OwnedNumber n : owned.getNumbers()) {
    System.out.println(n.getPhoneNumber() + " (" + n.getStatus() + ")");
}

// Get one number, including whether it's your default sender
OwnedNumber number = client.numbers().get("num_abc123");
System.out.println(number.isDefault());
System.out.println(number.isVoiceEnabled() + " " + number.getVoiceMode());

// Make a number your default sender (it must be active)
OwnedNumber updated = client.numbers().update("num_abc123",
    UpdateNumberRequest.builder()
        .isDefault(true)
        .build());

// Cancel a scheduled release ("keep this number")
client.numbers().update("num_abc123",
    UpdateNumberRequest.builder()
        .pendingCancellation(false)
        .build());

// Release a number. A live paid purchase is cancelled at period end;
// everything else is released immediately.
ReleaseNumberResponse release = client.numbers().release("num_abc123");
if (release.isScheduled()) {
    System.out.println("Releases at " + release.getScheduledReleaseAt());
} else {
    System.out.println("Released");
}
```

## 10DLC

Register your business so you can text from local (10-digit) US numbers. Brand,
campaign and assignment writes need a live API key.

```java
// 1. Register a brand, then poll until it's verified
TenDlcBrand brand = client.tenDlc().createBrand(CreateTenDlcBrandRequest.builder()
    .legalName("Acme Holdings LLC")
    .ein("12-3456789")
    .entityType("PRIVATE_PROFIT")
    .website("https://acme.example")
    .email("ops@acme.example")
    .street("1 Main St").city("Chicago").state("IL").postalCode("60601").country("US")
    .build()).getData();

TenDlcBrand status = client.tenDlc().getBrand(brand.getId()).getData();
System.out.println(status.getStatus());          // pending -> verified | failed
System.out.println(status.getFailureReasons());  // why, when it failed

// 2. Pre-check the use case, then create a campaign under the verified brand
TenDlcQualifyResult check = client.tenDlc().qualify(brand.getId(), "MIXED").getData();
if (check.isQualified()) {
    TenDlcCampaign campaign = client.tenDlc().createCampaign(
        CreateTenDlcCampaignRequest.builder()
            .brandId(brand.getId())
            .useCase("MIXED")
            .description("Order updates and support replies for Acme customers")
            .messageFlow("Customers opt in at checkout on acme.example")
            .sampleMessages(List.of("Your order #4821 has shipped!"))
            .build()).getData();

    // Poll until the campaign is active
    TenDlcCampaign live = client.tenDlc().getCampaign(campaign.getId()).getData();
    System.out.println(live.getStatus());                    // pending -> active
    if (live.getThroughput() != null) {                      // null until carriers report
        System.out.println(live.getThroughput().getTier());  // carrier throughput tier
    }

    // 3. Assign a number you own; once the assignment is Active it can send
    TenDlcAssignment assignment = client.tenDlc()
        .assignNumber(campaign.getId(), "+12025550143").getData();
    System.out.println(assignment.getStatus());
}

// Everything you've registered
TenDlcBrandListResponse brands = client.tenDlc().listBrands();
TenDlcCampaignListResponse campaigns = client.tenDlc().listCampaigns();
TenDlcAssignmentListResponse assignments = client.tenDlc().listAssignments();
```

## Short Codes

> **This SDK has no short-code helpers.** There is no `client.shortCodes()`
> resource and no short-code model. Reach the endpoints below through the
> client's generic `get`, `post` and `put` methods (which add your API key,
> retries and the automatic idempotency key on POSTs, and return the raw
> `JsonObject`), or through the Sendly CLI or dashboard.

Short-code applications live at these endpoints:

| Method | Path | Scope |
|--------|------|-------|
| `GET` | `/api/v1/short_codes` | `short_codes:read` |
| `POST` | `/api/v1/short_codes/requests` | `short_codes:write` |
| `GET` | `/api/v1/short_codes/application` | `short_codes:read` |
| `PUT` | `/api/v1/short_codes/application` | `short_codes:write` |
| `POST` | `/api/v1/short_codes/application/preflight` | `short_codes:read` |
| `POST` | `/api/v1/short_codes/application/submit` | `short_codes:write` |

Paths passed to the client are relative to `/api/v1`:

```java
import com.google.gson.JsonObject;

JsonObject shortCodes = client.get("/short_codes", null);
JsonObject application = client.get("/short_codes/application", null);
JsonObject preflight = client.post("/short_codes/application/preflight", null);
```

Check the API reference for the application body before you `put` or submit
one; the SDK does not model it.

You can also follow a short-code application from Java through webhooks: the
`short_code.action_required`, `short_code.rejected`, `short_code.filed` and
`short_code.live` events are declared on `WebhookEventType` and arrive like any
other event. Read their payloads with `getRawObject()` or `objectAs(...)` — see
[Receiving events](#receiving-events).

```java
client.webhooks().create(
    "https://hooks.acme.example/sendly",
    Arrays.asList(
        WebhookEventType.SHORT_CODE_ACTION_REQUIRED.getValue(),
        WebhookEventType.SHORT_CODE_FILED.getValue(),
        WebhookEventType.SHORT_CODE_LIVE.getValue()
    )
);
```

## WhatsApp

Connect a number you own to WhatsApp, create Meta-reviewed message templates,
and send WhatsApp messages through the same `messages().send()` you use for SMS.

> **Note**: WhatsApp is gated behind the `whatsapp_channel` rollout flag
> (default-dark). The flag is per person: the user who owns the API key, not
> the workspace. While it is off, the `/api/v1/whatsapp/*` management routes
> (signup, sender, template and window calls) return 404 `not_found` and
> sends return 403 `whatsapp_not_enabled`.

Sends go through `messages().send()` with a `SendWhatsAppMessageRequest`
(`POST /v1/messages` with channel `whatsapp`) and need `sms:send`, not
`whatsapp:write`. Reads (`signup().get()`, templates, the window, senders,
sender profiles and conversational components) need `whatsapp:read` and
accept test keys. Signup (including `verify()` and `resend()`), template
create/edit/delete, profile edits (including the photo), conversational
components and the calling switch need `whatsapp:write` and a live key
(otherwise 403 `whatsapp_requires_live_key`). Sends need a live key too. In a
team workspace, connecting and sender edits need an owner or admin
(`settings:write`), and template writes need an owner, admin or member
(`templates:write`). A missing role returns 403 `insufficient_permissions`.

```java
// 1. Connect a number ($19 one-time, no monthly fee). The connect URL must be
//    opened by a human: they log in with Facebook in a browser to link their
//    WhatsApp Business Account. Calling again for a number with a signup in
//    flight returns that signup without charging again.
WhatsAppSignupSession signup = client.whatsapp().signup().create("+15125550188");
System.out.println("Have your user open: " + signup.getConnectUrl());

// 2. Poll until active. After the Facebook step the signup stays
//    "registering" while WhatsApp activates the number. Activation usually
//    takes a few minutes but can take hours. If it hasn't finished about 6
//    hours after the session began, the session fails with
//    registration_timeout and the fee is refunded.
WhatsAppSignup status = client.whatsapp().signup().get(signup.getId());
System.out.println(status.getStatus());          // initiated | registering | verifying | active | failed
System.out.println(status.getFailureReasons());  // why, when it failed

// 3. List your WhatsApp senders
WhatsAppSendersResponse senders = client.whatsapp().senders().list();
for (WhatsAppSender s : senders.getSenders()) {
    System.out.println(s.getPhoneNumber() + " (" + s.getStatus() + ", " + s.getQualityRating() + ")");
}

// 4. Create a template (Meta reviews it, usually 24-48h). category is
//    required (UTILITY, AUTHENTICATION or MARKETING) with no default, and an
//    update can't change it.
WhatsAppTemplate template = client.whatsapp().templates().create(
    CreateWhatsAppTemplateRequest.builder()
        .sender("+15125550188")
        .name("order_shipped")
        .language("en_US")
        .category("UTILITY")
        .body("Hi {{1}}, your order {{2}} has shipped!")
        .examples(Map.of("1", "Sam", "2", "#4821"))
        .buttons(List.of(new WhatsAppTemplateButton("quick_reply", "Stop promotions")))
        .build());
System.out.println(template.getStatus());   // PENDING
System.out.println(template.getWarnings()); // submission warnings, if any

// List / edit / delete templates. Editing a rejected template (rather than
// deleting and re-creating it) is the recovery path — deleted template names
// are locked for ~30 days.
WhatsAppTemplateListResponse templates = client.whatsapp().templates().list();
client.whatsapp().templates().update(template.getId(),
    UpdateWhatsAppTemplateRequest.builder()
        .body("Hi {{1}}, your order {{2}} is on its way!")
        .examples(Map.of("1", "Sam", "2", "#4821"))
        .build());
client.whatsapp().templates().delete(template.getId());

// 5. Check the 24-hour customer-service window. Free-form text and media only
//    deliver while a window is open (it opens when the recipient messages you);
//    outside it, send an approved template.
WhatsAppWindow window = client.whatsapp().window("+15125550188", "+12025550143");
if (window.isOpen()) {
    // Free-form reply inside the open window
    WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
        .to("+12025550143")
        .from("+15125550188")
        .text("Your table is ready!")
        .build());
    System.out.println(message.getCreditsUsed()); // 1 for the first 1,000 replies per number each month
} else {
    // Template send — works regardless of the window
    WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
        .to("+12025550143")
        .from("+15125550188")
        .template(new WhatsAppTemplateSendParams("order_shipped", "en_US",
            Map.of("1", "Acme Inc", "2", "#4821")))
        .build());
    System.out.println(message.getWhatsapp().getKind());    // "template"
    System.out.println(message.getCreditsUsed());           // template: priced by country + category
}

// Media send (one attachment; text becomes the caption)
client.messages().send(SendWhatsAppMessageRequest.builder()
    .to("+12025550143")
    .from("+15125550188")
    .text("Here's the menu")
    .mediaUrls(List.of("https://acme.example/menu.jpg"))
    .build());

// 6. Read and edit a sender's business profile — the contact card recipients
//    see. Supply only the fields to change; omitted fields keep their value.
WhatsAppSenderProfile profile = client.whatsapp().senders().getProfile("+15125550188");
System.out.println(profile.getDisplayName() + " — " + profile.getAbout());

client.whatsapp().senders().updateProfile("+15125550188",
    UpdateWhatsAppSenderProfileRequest.builder()
        .about("Fast delivery, friendly service")   // max 139 chars
        .description("Acme sells everything.")      // max 512 chars
        .website("https://acme.example")
        .build());

// Profile photo: a JPEG or PNG of at most 5 MB, square, at least 192 px wide
client.whatsapp().senders().uploadProfilePhoto("+15125550188", new File("logo.png"));
client.whatsapp().senders().deleteProfilePhoto("+15125550188");

// 7. Ice breakers (shown when someone first opens a chat) and commands (shown
//    when the customer types "/"). Each list you set replaces the stored one,
//    an empty list clears it, and a list you leave out is kept.
client.whatsapp().senders().updateConversationalComponents("+15125550188",
    UpdateWhatsAppConversationalComponentsRequest.builder()
        .iceBreakers(List.of("What are your hours?", "Track my order"))
        .commands(List.of(new WhatsAppCommand("menu", "See today's menu")))
        .build());
WhatsAppConversationalComponents components =
    client.whatsapp().senders().getConversationalComponents("+15125550188");

// 8. Add another number to a connected WhatsApp Business account, with no
//    Facebook step. Same $19 one-time fee, refunded if it fails. WhatsApp
//    sends the number a code by SMS (or "voice"). Take the account id from an
//    active sender: the list is newest first and a pending sender has none.
//    A null businessAccountId starts a Facebook connection instead; a blank one is refused.
String accountId = senders.getSenders().stream()
    .filter(s -> "active".equals(s.getStatus()) && s.getBusinessAccountId() != null)
    .findFirst().orElseThrow().getBusinessAccountId();
WhatsAppSignupSession added = client.whatsapp().signup().create(
    CreateWhatsAppSignupRequest.builder()
        .phoneNumber("+15125550199")
        .businessAccountId(accountId)
        .verificationMethod(WhatsAppVerificationMethod.SMS)
        .build());
System.out.println(added.getStatus());   // verifying

// An SMS code arrives on the number itself; get() returns it once it has.
String code = client.whatsapp().signup().get(added.getId()).getVerificationCode();
if (code != null) {
    client.whatsapp().signup().verify(added.getId(), code); // active
}
// No code yet? Ask for another one (at most every 30 seconds).
client.whatsapp().signup().resend(added.getId(), WhatsAppVerificationMethod.VOICE);

// 9. WhatsApp calling. Switch voice on for the number first; then WhatsApp
//    users calling it ring like a phone call. There is no API for placing
//    WhatsApp calls.
WhatsAppCallingSettings calling = client.whatsapp().senders().setCalling("+15125550188", true);
System.out.println(calling.isCallingEnabled());
```

Meta has paused marketing-template delivery to US (+1) numbers.

Pricing: free-form text or media inside the 24-hour window costs 1 credit
each for the first 1,000 per sending number per calendar month (UTC), then
the destination's utility template price; countries without a listed price
use the default utility price of 12 credits. Templates are priced by category
and destination country; countries without a listed price use 33
(marketing), 12 (utility) and 12 (authentication) credits. A failed send
gives its slot back.

The window response is exactly `{ open, expiresAt }`. A window that has closed keeps its past close time in
`getExpiresAt()`; if the contact has never messaged that sender,
`getExpiresAt()` is null. Check `isOpen()`, not the time. A template header cannot contain `{{n}}` variables (400
`template_header_variable_unsupported`).

Refusals to plan for:

- `whatsapp_unavailable` (503), only from `signup().create()`: connections are
  unavailable for now, nothing was charged, and the body carries
  `retryAfter: 3600` with a `Retry-After: 3600` header. The client retries it
  like any 5xx before throwing it. No send returns it.
- A failed connection: if the connection fails, the $19 fee is refunded
  automatically. Once a number has connected there is no refund, and a later
  disconnect gets nothing back.
- `whatsapp_signup_limit_reached` (429): 5 charged signups failed in the last
  24 hours. It is thrown at once; try again the next day.
- `whatsapp_send_failed` on a send: a 422 means the carrier refused the message
  and it is final (cached under the idempotency key and replayed for 24
  hours). A 502 means the message provably never reached the carrier, so it
  was not sent and is safe to send again; it is never cached, and the client
  retries it under the same idempotency key. The credits of a failed send are
  refunded.
- `whatsapp_send_unconfirmed` (409) on a send: the outcome is unknown. The
  message was marked failed and refunded but may still be delivered, so check
  before sending it again (it could arrive twice). It is cached under the
  idempotency key and is not retried automatically.
- `whatsapp_sender_not_connected` (404) from `templates().create()`, checked
  before anything else, and from every `senders()` method that takes a number.
- Profile photo: 400 `whatsapp_profile_photo_invalid` (not a JPEG or PNG), 413
  `whatsapp_profile_photo_too_large` (over 5 MB), 502
  `whatsapp_profile_update_failed` (fix the image and try again).
- Conversational components: 400 `invalid_request` naming the rule broken
  (at most 4 ice breakers of 1-80 characters; at most 30 commands of letters,
  digits or underscores up to 32 characters, each with a 1-256 character
  description; no duplicates), 502
  `whatsapp_conversational_components_fetch_failed` or
  `whatsapp_conversational_components_update_failed`.
- Calling: 409 `voice_not_enabled` (switch voice on for the number first), 422
  `whatsapp_calling_unavailable` (Meta requires the account to be allowed to
  message at least 2,000 people a day and an approved display name), 502
  `whatsapp_calling_update_failed`. `isOutboundCallingAllowed()` is false for
  +1, +20, +84 and +234 numbers.
- Adding a number by code: 404 `whatsapp_business_account_not_found`, 400
  `display_name_required`, 409 `whatsapp_signup_in_progress` or
  `whatsapp_already_enabled`, and `whatsapp_verification_start_failed` (422
  when WhatsApp refused to send the code, 502 when it could not be reached;
  the signup failed and the fee is refunded). Neither this `create()` nor
  `verify()` is retried automatically except on a 429 rate limit. A Facebook
  signup for a number that is verifying gets 409
  `whatsapp_verification_in_progress`.
- `verify()`: 400 `invalid_verification_code` (not 6 digits), 422
  `whatsapp_verification_code_invalid` with `attemptsRemaining` in
  `getResponseBody()`, 409 `whatsapp_verification_failed` after 5 wrong codes
  (the signup failed and the fee is refunded; not retried), 409
  `whatsapp_verification_busy` (try again), 502
  `whatsapp_verification_unavailable` (not counted as an attempt), 502
  `whatsapp_activation_pending` (the code was accepted; check back), 409
  `signup_not_active`.
- `resend()`: 429 `whatsapp_verification_resend_too_soon` with
  `getRetryAfter()` (thrown at once), 422 or 502
  `whatsapp_verification_resend_failed`, 409 `signup_not_active`.
- A failed signup's `getFailureReasons()` can also be
  `verification_start_failed`, `verification_failed` or
  `verification_expired`.
- Template pre-flight refusals (400): `template_category_invalid` (category
  missing or not one of the three), `template_authentication_otp_button_required`,
  `template_authentication_no_links` (a link in the body or a URL button on an
  authentication template) and `template_header_variable_unsupported`. A
  marketing template without an opt-out button only gets a warning.

## RCS

Send branded rich messaging — cards and suggestion chips — through the same
`messages().send()` you use for SMS. Plain-text RCS sends fall back to SMS
automatically for recipients whose device can't receive RCS.

> **Note**: RCS is gated behind the `rcs_channel` rollout flag (default-dark).
> Registration calls and sends return a 404 `rcs_not_enabled` error (listing
> agents and capability checks a 404 `not_found`) until the flag is on for your
> account. Registration needs
> an API key with the `rcs:read` / `rcs:write` scopes; sends and capability
> checks require a live API key.

### Register a brand and agent

Registration is self-serve, from the dashboard or the API. You draft a brand
(your business identity) and an agent (what recipients see), submit them for
review by Sendly, and Sendly passes them to the carrier network. Once the
agent reaches the `testing` stage it can message invited test devices; request
launch when testing is done, and after launch review it reaches everyone.

Assets can't be uploaded over the API: logo, hero and call-to-action media
must already be public `https://` URLs. Upload files from the dashboard
instead.

```java
// 1. Prefill from what's already on file (10DLC brand or toll-free
//    verification), complete it, and draft the brand. Only US businesses for
//    now: a non-US address is refused with rcs_us_only.
RcsDossier dossier = client.rcs().dossier().get();
RcsBrand brand = client.rcs().brands().create(dossier.getBrand().toBuilder()
    .displayName("Acme")
    .legalEntityType(RcsLegalEntityType.LIMITED_LIABILITY_COMPANY)
    .address(RcsBrandAddress.builder()
        .line1("1 Main St").city("Chicago").state("IL").postalCode("60601").countryCode("US")
        .build())
    .contact(RcsBrandContact.builder()
        .firstName("Sam").lastName("Lee").email("sam@acme.example").phoneNumber("+13125550100")
        .build())
    .build()).getBrand();

// 2. Draft an agent under the brand
RcsAgentDetails agent = client.rcs().agents().create(CreateRcsAgentRequest.builder()
    .brandId(brand.getId())
    .displayName("Acme")
    .useCase(RcsAgentUseCase.TRANSACTIONAL)
    .basics(RcsAgentBasics.builder()
        .description("Order updates from Acme")
        .logoUrl("https://acme.example/logo.png")   // public https URL
        .heroUrl("https://acme.example/hero.png")   // public https URL
        .brandColor("#0055FF")
        .privacyPolicyUrl("https://acme.example/privacy")
        .termsAndConditionsUrl("https://acme.example/terms")
        .website(new RcsAgentWebsiteContact("https://acme.example", "Visit us"))
        .build())
    .build()).getAgent();

// Edit a draft; only the groups you set are changed
client.rcs().agents().update(agent.getId(), UpdateRcsAgentRequest.builder()
    .campaign(RcsCampaign.builder()
        .agentOverview("Shipping and delivery updates for Acme orders")
        .interactions(List.of(
            new RcsInteraction(RcsInteractionType.TRANSACTIONAL_UPDATES, "Order status changes")))
        .messageExamples(List.of(
            "Your order #4821 has shipped!",
            "Your order #4821 is out for delivery.",
            "Your order #4821 was delivered."))
        .consentSettings(RcsConsentSettings.builder()
            .optInMethods(List.of(new RcsOptInMethod(RcsOptInMethodType.WEBSITE, "Checkout on acme.example")))
            .callToAction("Text me order updates")
            .callToActionUrl("https://acme.example/checkout")
            .optInMessage("Acme: You're in! Reply STOP to opt out, HELP for help.")
            .helpResponse("Acme: Email support@acme.example for help.")
            .optOutResponse("Acme: You've been unsubscribed.")
            .build())
        .build())
    .build());

// 3. Invite test devices (the list is authoritative; up to 20), then submit
//    for review. Submit lists anything still missing as field errors.
client.rcs().agents().setTestDevices(agent.getId(), List.of(
    new RcsTestDeviceInput("+13125550100", "Sam's phone")));
try {
    RcsAgentResponse submitted = client.rcs().agents().submit(agent.getId());
    System.out.println(submitted.getStage()); // "in_review"
} catch (ValidationException e) {
    if (RcsErrorCode.INVALID_CONTENT.equals(e.getApiErrorCode())) {
        e.getFieldErrors().forEach(f -> System.out.println(f.getPath() + ": " + f.getMessage()));
    }
}

// 4. Poll where the registration stands. Once the stage is "testing", send to
//    your invited devices, then request launch.
RcsRegistration registration = client.rcs().registration().get();
System.out.println(registration.getStage()); // draft, in_review, testing, live, ...
if (RcsCustomerStage.TESTING.equals(registration.getStage())) {
    client.rcs().agents().requestLaunch(agent.getId(), RcsLaunchRequest.builder()
        .testUrl("https://acme.example/rcs-test")
        .build());
}

// Writes accept your own idempotency key, like every other write
client.rcs().agents().submit(agent.getId(), new IdempotentRequestOptions("submit-" + agent.getId()));
```

`RcsCustomerStage` holds every stage a registration passes through: `DRAFT`,
`IN_REVIEW`, `CHANGES_REQUESTED`, `REJECTED`, `BRAND_VERIFICATION`,
`AGENT_REVIEW`, `TESTING`, `LAUNCH_REVIEW`, `LAUNCHING`, `LAUNCH_REJECTED`,
`LIVE`, `SUSPENDED` and `FAILED`. `RcsReviewStatus` holds the review-side
values (`DRAFT`, `AWAITING_REVIEW`, `CHANGES_REQUESTED`,
`APPROVED_FOR_CARRIER`, `REJECTED`, `LAUNCH_REQUESTED`, `LAUNCH_SUBMITTED`,
`LAUNCH_REJECTED`, `FAILED`) returned by `getReviewStatus()`. Both are classes
of `String` constants, so compare with `.equals(...)`.

### Send

```java
// 1. Find your agent. Status "testing" reaches invited test devices only,
//    "approved" reaches everyone.
RcsAgentsResponse agents = client.rcs().agents().list();
for (RcsAgent agent : agents.getAgents()) {
    System.out.println(agent.getName() + " (" + agent.getStatus() + ", sendable=" + agent.isSendable() + ")");
}

// 2. Optional pre-flight: can this recipient receive RCS?
RcsCapability capability = client.rcs().capability("+12025550143");
System.out.println(capability.isCapable());   // false -> text falls back to SMS
System.out.println(capability.getFeatures()); // device features, empty when not capable

// 3. Send text, optionally with suggestion chips. A reply chip's tap comes
//    back as an inbound message carrying your postbackData; an action chip
//    opens a URL.
RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
    .to("+12025550143")
    .text("Your order #4821 has shipped!")
    .suggestions(List.of(
        RcsSuggestion.reply("Thanks", "thanks"),
        RcsSuggestion.action("Track", "track", "https://acme.example/track/4821")))
    .build());

// The response tells you which leg delivered
if (message.getFellBackTo() != null) {
    // Not RCS-capable: sent and billed as SMS, chips dropped
    System.out.println(message.getChannel());                      // "sms"
    System.out.println(message.getRcs().getRequestedChannel());    // "rcs"
    System.out.println(message.getRcs().getSuggestionsDropped());  // true
} else {
    System.out.println(message.getChannel());              // "rcs"
    System.out.println(message.getRcs().getKind());        // "text"
    System.out.println(message.getRcs().getAgentName());   // "Acme Inc"
}

// 4. Send a rich card. Cards have no SMS form — a card to a non-RCS recipient
//    fails with rcs_not_supported_for_recipient rather than falling back.
client.messages().send(SendRcsMessageRequest.builder()
    .to("+12025550143")
    .card(RcsCard.builder()
        .title("Order #4821 shipped")
        .description("Arriving Thursday")
        .mediaUrl("https://acme.example/package.jpg")  // public JPEG, PNG, or GIF
        .orientation("vertical")                       // or "horizontal"
        .suggestions(List.of(
            RcsSuggestion.action("Track", "track", "https://acme.example/track/4821")))
        .build())
    .build());

// Opt out of the SMS fallback to get a 422 instead of an SMS charge
client.messages().send(SendRcsMessageRequest.builder()
    .to("+12025550143")
    .text("RCS only, please")
    .fallbackToSms(false)
    .build());

// Pass agentId when your workspace has more than one agent (otherwise the
// send fails with rcs_agent_ambiguous)
client.messages().send(SendRcsMessageRequest.builder()
    .to("+12025550143")
    .agentId("rag_abc123")
    .text("Your order #4821 has shipped!")
    .build());
```

Send exactly one of `text` or `card`; supplying both, or neither, raises a
`ValidationException` client-side. Suggestion chips ride on text messages —
put card buttons in `card.suggestions` instead.

Refusals name their reason in `getApiErrorCode()`, with constants in
`RcsErrorCode`: `NOT_ENABLED`, `NOT_FOUND`, `FIELD_LOCKED`, `US_ONLY`,
`INVALID_CONTENT`, `BRAND_NOT_VERIFIED`, `LAUNCH_NOT_READY`, `INTERNAL_ERROR`,
`INSUFFICIENT_PERMISSIONS` and `FORBIDDEN`, plus `TOO_MANY_FAILED_KEY_ATTEMPTS`
and `TOO_MANY_CONCURRENT_VERIFICATIONS`, the two 429s the API-key check itself
can answer with (see [Rate Limits](#rate-limits)).

## Branded Short Links

Mint branded short links for a destination URL, list them with click analytics,
and toggle a per-link kill switch.

> **Note**: URL shortening is gated behind the `url_shortener` rollout flag
> (founder-only while dark). Calls return a `not_found` error until the flag is
> on for your account.

```java
// Shorten a URL
ShortLink link = client.links().create("https://acme.example/spring-sale?utm_source=sms");
System.out.println(link.getShortUrl()); // https://sendly.live/l/Ab3xY7
System.out.println(link.getCode());     // Ab3xY7

// List your links with click counts (default 50 per page, max 200)
ShortLinkListResponse listing = client.links().list(20, 0);
for (ShortLinkListItem item : listing.getLinks()) {
    System.out.println(item.getShortUrl() + " -> " + item.getDestinationUrl()
        + " (" + item.getClickCount() + " clicks)");
    System.out.println(item.getSpark());  // 14-day daily click histogram
}

// Kill a link (its redirect returns 404 until re-enabled)
client.links().disable(link.getCode());
client.links().enable(link.getCode());
```

## Voice Calls

Place phone calls that one of your AI agents handles, follow them while they
run, end them, and download recordings. Numbers are set up with `voice()` (see
[Configure voice](#configure-voice)) or in the dashboard under Calls: switch
voice on for a number, choose how it answers, and register its emergency
address. `voice().numbers().list()` reports each number's voice settings so you
can pick a `from` number.

> **Note**: Calls are prepaid from your balance per started minute. An
> agent-handled outbound call costs 10 credits a minute (2 for the call, 8 for
> the agent); an unanswered call costs nothing. Destinations are US and Canada.
> Voice is being enabled workspace by workspace; until it is on for yours every
> method answers 404 `voice_not_enabled`. Reads need the `calls:read` scope,
> writes need `calls:write` and a live API key.

```java
// Place a call. The agent talks; `context` tells it why it is calling.
CreateCallRequest request = CreateCallRequest.builder()
    .to("+12025550143")
    .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
    .from("+15125550147")                 // optional with exactly one voice-enabled number
    .context("You are calling Jordan to confirm the 3pm appointment on Tuesday.")
    .metadata("crmId", "lead_8812")       // echoed on every read and in call.* webhooks
    .build();
Call call = client.calls().create(request);
System.out.println(call.getStatus());     // "ringing"

// List calls, newest first, with filters and paging
CallListResponse page = client.calls().list(ListCallsOptions.builder()
    .status(CallStatus.COMPLETED)
    .direction(CallDirection.OUTBOUND)
    .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
    .limit(20)
    .build());
for (Call c : page.getData()) {
    System.out.println(c.getTo() + " " + c.getDurationSecs() + "s " + c.getCreditsCharged() + " credits");
}
if (page.hasMore()) { /* fetch offset(page.getOffset() + page.getLimit()) */ }

// Inspect one call. Agent-handled calls carry their transcript.
Call finished = client.calls().get(call.getId());
System.out.println(finished.getStatus() + " " + finished.getHangupClass());
if (finished.getTranscript() != null) {
    for (CallTranscriptLine line : finished.getTranscript()) {
        System.out.println(line.getSpeaker() + ": " + line.getText());
    }
}

// End a call early. Ringing -> cancelled, active -> completed; an ended call is returned unchanged.
client.calls().hangup(call.getId());

// Fetch the recording. The URL is signed and works for five minutes.
CallRecording recording = client.calls().recording(call.getId());
if (recording.isReady()) {
    System.out.println(recording.getUrl() + " until " + recording.getExpiresAt());
}

// Writes accept your own idempotency key, like every other write
client.calls().create(request, new IdempotentRequestOptions("call-lead_8812"));
```

Recordings are Ogg/Opus. Agent-handled calls are recorded dual-channel, the
agent on the left channel and the other party on the right.

Refusals name their reason in `getApiErrorCode()` (constants in
`CallErrorCode`):

```java
try {
    client.calls().create(request);
} catch (InsufficientCreditsException e) {
    // 402 insufficient_credits: the balance does not cover one minute
} catch (SendlyException e) {
    String code = e.getApiErrorCode() != null ? e.getApiErrorCode() : "";
    switch (code) {
        case CallErrorCode.E911_REQUIRED -> { /* 428: register the number's emergency address first */ }
        case CallErrorCode.LINES_BUSY -> { /* 409: every line is in use, try again later under a fresh idempotency key */ }
        case CallErrorCode.DAILY_CALL_LIMIT -> { /* 429: try again tomorrow */ }
        case CallErrorCode.FROM_NUMBER_REQUIRED -> { /* 400: more than one voice-enabled number, set from() */ }
        case CallErrorCode.FROM_NUMBER_NOT_SUPPORTED -> { /* 400: from() is outside the US and Canada */ }
        default -> throw e;
    }
}
```

`CallStatus`, `CallDirection`, `CallKind`, `CallChannel`, `CallHandledBy`,
`CallBilling` and `CallRecordingStatus` hold the string values the call object
uses. `getChannel()` is `phone`, `whatsapp` or `browser`, and the `call.*`
webhook objects carry the same `channel` key; compare against the constants,
since a value added later is returned as sent.

### Configure voice

`client.voice()` configures everything a call depends on: which numbers take
calls and how they answer, each number's emergency address, and the AI agents
themselves. Reads need `calls:read` and writes `calls:write` with a live key.
In a team workspace, number and emergency-address writes also need a role that
can change settings, and agent writes a role that can manage API keys (each
agent holds its own scoped sending key); otherwise the API answers 403
`forbidden`. A `number` is the number's id or its E.164 phone number.

Switching voice on for a number changes how real phone calls to it are
answered, and an agent answers real callers on every number pointed at it. A
US or Canadian number needs an emergency address before it can place calls;
the first registration adds $1.50 a month to the number, and registering again
replaces the address without charging twice.

```java
// Numbers and how they answer
VoiceNumberListResponse numbers = client.voice().numbers().list();
for (VoiceNumber n : numbers.getData()) {
    VoiceNumberEmergencyAddress e911 = n.getEmergencyAddress();
    System.out.println(n.getPhoneNumber() + " " + n.getVoiceMode() + " "
        + (e911 != null ? e911.getStatus() : "no emergency address"));
}

VoiceNumber number = client.voice().numbers().get("+15125550147");
System.out.println(number.getRatePerMinute().getOutbound() + " credits a minute outbound");
System.out.println(number.getRatePerMinute().getInbound() + " inbound, "
    + number.getRatePerMinute().getAgent() + " inbound when an agent answers");

// Register the emergency address (country defaults to US)
number = client.voice().numbers().registerEmergencyAddress("+15125550147", EmergencyAddress.builder()
    .street("500 Example Ave")
    .unit("Suite 2")
    .city("Austin")
    .state("TX")
    .zip("78701")
    .build());
System.out.println(number.getEmergencyAddress().getStatus()); // "provisioning", then "active"

// Voices, then an agent
VoiceListResponse voices = client.voice().voices().list();
VoiceAgent agent = client.voice().agents().create(CreateVoiceAgentRequest.builder()
    .name("Front desk")
    .voice(voices.getData().get(0).getId())
    .greeting("Thanks for calling Acme, how can I help?")
    .instructions("Answer questions about opening hours and take a message for anything else.")
    .tools(VoiceAgentTools.builder().sendSms(true).build())
    .build());

// Have the agent answer the number
number = client.voice().numbers().update("+15125550147", UpdateVoiceNumberRequest.builder()
    .voiceEnabled(true)
    .voiceMode(VoiceMode.AGENT)
    .agentId(agent.getId())
    .build());

// Change an agent (only the fields you set are sent)
agent = client.voice().agents().update(agent.getId(), UpdateVoiceAgentRequest.builder()
    .greeting("Thanks for calling Acme. How can I help today?")
    .build());

// Ring the team instead, then delete the agent
client.voice().numbers().update("+15125550147", UpdateVoiceNumberRequest.builder()
    .voiceMode(VoiceMode.RING_DASHBOARD)
    .build());
DeletedVoiceAgent deleted = client.voice().agents().delete(agent.getId());
System.out.println(deleted.isDeleted()); // true
```

A mode alone is enough: `VoiceMode.RING_DASHBOARD` or `VoiceMode.AGENT`
switches voice on, so it can fail the way switching on does (502
`voice_attach_failed`, 503 `voice_unavailable`), and `VoiceMode.NONE` switches
it off. `voiceEnabled(false)` wins over any mode, and `VoiceMode.NONE` with
`voiceEnabled(true)` becomes `ring_dashboard`. An empty
`agentId` clears the stored agent, and an empty `transferTo` clears that tool.
Agents cannot transfer calls yet: while `transferTo` is set, a caller who asks
for a person is told the message will be passed on, and the agent takes their
name and number. A workspace can hold up to 20 agents. Every write also takes
an `IdempotentRequestOptions` overload.

Refusals carry `getApiErrorCode()` as usual: 400 `ValidationException`
(`invalid_request`, `invalid_voice_mode`, `agent_required`, `invalid_address`,
`e911_not_applicable`), 404 `NotFoundException` (`number_not_found`,
`agent_not_found`), 409 `SendlyException` (`agent_disabled`, `agent_limit`,
`agent_in_use`), 422 `ValidationException` (`invalid_address`, the address
couldn't be validated), 502 `SendlyException` (`voice_attach_failed`,
`carrier_refused`) and 503 `SendlyException` (`voice_unavailable`). A 5xx is
thrown only after the client has already retried it on its own. Not every
`carrier_refused` is worth retrying: when the message says the number couldn't
be found for emergency registration, retrying won't help, so contact support;
when it says the address couldn't be registered or emergency calling couldn't
be switched on, try again later.
A 422 is thrown as a `ValidationException` like a 400, and `getStatusCode()`
tells them apart; the 422 `invalid_address` also carries a `suggested` field.
Extra fields are on `getResponseBody()`:

```java
import com.google.gson.JsonElement;

try {
    client.voice().agents().delete(agent.getId());
} catch (SendlyException e) {
    if (!CallErrorCode.AGENT_IN_USE.equals(e.getApiErrorCode())) throw e;
    for (JsonElement n : e.getResponseBody().getAsJsonArray("numbers")) {
        System.out.println("Still answering " + n.getAsString());
    }
}

try {
    client.voice().numbers().registerEmergencyAddress("+15125550147", address);
} catch (ValidationException e) {
    if (!CallErrorCode.INVALID_ADDRESS.equals(e.getApiErrorCode())) throw e;
    if (e.getStatusCode() == 422) {
        System.out.println("Did you mean: " + e.getResponseBody().get("suggested")); // null when no correction was found
    } else {
        System.out.println(e.getMessage()); // 400: a field is missing or malformed
    }
}
```

## Webhooks

### Managing endpoints

```java
// Create a webhook endpoint
WebhookCreatedResponse webhook = client.webhooks().create(
    "https://hooks.acme.example/sendly",
    Arrays.asList("message.delivered", "message.failed")
);

System.out.println(webhook.getId());     // whk_xxx
System.out.println(webhook.getSecret()); // whsec_... — store securely, shown once

// With a description and metadata
client.webhooks().create(
    "https://hooks.acme.example/sendly",
    Arrays.asList("message.delivered"),
    "Delivery receipts",
    Map.of("team", "platform")
);

// List all webhooks
List<Webhook> webhooks = client.webhooks().list();

// Get a specific webhook
Webhook wh = client.webhooks().get("whk_xxx");
System.out.println(wh.getSuccessRate() + " " + wh.getCircuitState());

// Update a webhook (url, events, description, isActive) — pass null to leave one alone
client.webhooks().update("whk_xxx",
    "https://hooks.acme.example/sendly-v2",
    Arrays.asList("message.delivered", "message.failed", "message.sent"),
    null,
    null
);

// Send a test event to it. When your endpoint fails the test, the API answers
// 400, so this throws a ValidationException whose message says why.
WebhookTestResult result = client.webhooks().test("whk_xxx");
System.out.println(result.getStatusCode() + " in " + result.getResponseTimeMs() + "ms: "
    + result.getMessage());

// Rotate the signing secret
WebhookCreatedResponse rotation = client.webhooks().rotateSecret("whk_xxx");
System.out.println(rotation.getSecret());

// Delete a webhook
client.webhooks().delete("whk_xxx");

// List available webhook event types
List<String> eventTypes = client.webhooks().listEventTypes();
```

The URL must be `https://`, at least one event is required, and webhook ids
must start with `whk_` — all three are checked client-side and raise a
`ValidationException` before any request is made.

### Recovering from an outage

Repeated failures trip a circuit breaker (`getCircuitState()`, `isCircuitOpen()`).
Reset it first — redeliver and backfill are both refused with HTTP 409 while the
circuit is open.

```java
// Reset the breaker
client.webhooks().resetCircuit("whk_xxx");

// Re-fire deliveries we recorded but couldn't deliver. Each replay keeps the
// original event_id, so you can dedupe (message events re-queue their original
// delivery row; other events get a new one).
// The window (since to until, until defaults to now) is at most 7 days.
client.webhooks().redeliver("whk_xxx", new WebhooksResource.RedeliverOptions()
    .since(Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString())
    .eventTypes(List.of("message.delivered"))
    .statuses(List.of("failed"))
    .limit(500));

// Synthesize message.sent, message.delivered and message.failed events that
// never got an audit row at all. Each carries the same event_id the original
// dispatch would have used, so dedupe on event_id as usual. Do not dedupe on
// data.object.id: a message's sent and delivered events share it.
client.webhooks().backfill("whk_xxx", new WebhooksResource.BackfillOptions()
    .since(Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString())
    .limit(500));

// Delivery history, newest first, and a single retry
List<WebhookDelivery> deliveries = client.webhooks().getDeliveries("whk_xxx");
List<WebhookDelivery> failedPage = client.webhooks().getDeliveries("whk_xxx",
    50, 0, WebhookDelivery.STATUS_FAILED);   // limit (1-100), offset, status
for (WebhookDelivery d : failedPage) {
    System.out.println(d.getEventType() + " " + d.getResponseStatusCode() + " " + d.getErrorMessage());
}
client.webhooks().retryDelivery("whk_xxx", "del_abc123");
```

Both `redeliver` and `backfill` also have a no-options overload that uses the
server defaults, and both return raw `JsonObject` counts.

### Receiving events

`Webhooks.parseEvent(...)` is a **static helper on `com.sendly.webhooks.Webhooks`**,
not a method on `client.webhooks()`. It verifies the signature and returns a
`Webhooks.WebhookEvent`. There are two ways to read the payload off it:

- `event.getData()` is the **message view** of `data.object`, and is only
  meaningful for `message.*` events.
- `event.getRawObject()` is `data.object` exactly as it arrived, for every
  event type, and `event.objectAs(SomeClass.class)` deserializes it with Gson.

Lifecycle events — `rcs_brand.*`, `rcs_agent.*`, `whatsapp_account.*`,
`whatsapp_template.*`, `call.*`, `short_code.*`, `brand.*`, `campaign.*`,
`assignment.*`, `number.*`, `port.*`, `port_out.*`, `contact.*` — carry a
different object entirely, so read those through `getRawObject()` or
`objectAs(...)`. Reaching for them through `getData()` gives you a message
view that keeps only the keys the object happens to share with a message (a
`call.*` object's `id`, `status`, `to`, `from` and `direction`, for example)
and leaves every other field at its default, and nothing raises an error to
tell you so.

Subscribe with the `WebhookEventType` constants (in `com.sendly.models`) to
keep the wire strings honest — `create(...)` takes them as strings:

```java
client.webhooks().create(
    "https://hooks.acme.example/sendly",
    Arrays.asList(
        WebhookEventType.MESSAGE_DELIVERED.getValue(),
        WebhookEventType.RCS_AGENT_LIVE.getValue(),
        WebhookEventType.CALL_COMPLETED.getValue()
    )
);
```

Then handle both shapes in one endpoint:

```java
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sendly.webhooks.Webhooks;
import com.sendly.webhooks.Webhooks.WebhookEvent;
import com.sendly.webhooks.Webhooks.WebhookSignatureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SendlyWebhookController {

    // data.object for rcs_agent.live:
    // {"agent_id":"rag_abc123","name":"Acme Support","stage":"live",
    //  "organization_id":"org_9f2c"}
    // objectAs uses a plain Gson, which matches field names literally, so
    // snake_case keys need @SerializedName.
    static class RcsAgentEvent {
        @SerializedName("agent_id") String agentId;
        String name;
        String stage;
    }

    @PostMapping("/webhooks/sendly")
    public ResponseEntity<String> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("X-Sendly-Signature") String signature,
        @RequestHeader(value = "X-Sendly-Timestamp", required = false) String timestamp
    ) {
        WebhookEvent event;
        try {
            event = Webhooks.parseEvent(
                payload, signature, System.getenv("SENDLY_WEBHOOK_SECRET"), timestamp);
        } catch (WebhookSignatureException e) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        switch (event.getType()) {
            case "message.delivered":
                // A message event: the message view is the one to read.
                System.out.println("Delivered: " + event.getData().getId());
                break;

            case "rcs_agent.live":
                // A lifecycle event: decode data.object into your own type.
                RcsAgentEvent agent = event.objectAs(RcsAgentEvent.class);
                System.out.println("Agent " + agent.agentId + " reached " + agent.stage);
                break;

            default:
                // Or read data.object straight, without declaring a type.
                JsonObject object = event.getRawObject();
                System.out.println(event.getType() + " -> " + object);
        }

        return ResponseEntity.ok("OK");
    }
}
```

> **Note**: on `contact.auto_flagged` the payload is the **contact**, so its
> `id` is a contact id and `event.getData().getId()` hands it back as though it
> were a message id. A flag raised by a failed send carries that message's id
> as `message_id` (null when the message is unknown); a flag raised by a
> carrier lookup has no `message_id` at all. Check before reading it:
> `JsonElement mid = event.getRawObject().get("message_id");` then
> `String messageId = mid == null || mid.isJsonNull() ? null : mid.getAsString();`

### Signature details

Signatures are `sha256=<hex>`, an HMAC-SHA256 over `<timestamp>.<raw body>`
keyed with your `whsec_...` signing secret. The timestamp comes from the
`X-Sendly-Timestamp` header and is rejected when it is more than 300 seconds
away from now. Pass the **raw** request body — a re-serialized object will not
match.

```java
// Verify by hand, when you parse the body yourself
boolean ok = Webhooks.verifySignature(payload, signature, secret, timestamp);

// Sign a payload so you can exercise your endpoint in tests
String testSignature = Webhooks.generateSignature(payload, secret, timestamp);
```

Both also have three-argument overloads that skip the timestamp, but they are
deprecated: without a timestamp there is no replay protection.

## Account & Credits

```java
// Get account information
Account account = client.account().get();
System.out.println(account.getEmail());

// Check credit balance
Credits credits = client.account().getCredits();
System.out.println("Available: " + credits.getAvailableBalance() + " credits");
System.out.println("Reserved: " + credits.getReservedBalance() + " credits");
System.out.println("Total: " + credits.getBalance() + " credits");

// View credit transaction history
List<CreditTransaction> transactions = client.account().getCreditTransactions(50, 0);
for (CreditTransaction tx : transactions) {
    System.out.println(tx.getType() + ": " + tx.getAmount() + " credits - " + tx.getDescription());
}

// Move credits to another organization you control (raw JSON)
JsonObject transfer = client.account().transferCredits("org_9f2c", 5000);
```

### API keys

```java
// List API keys
List<ApiKey> keys = client.account().listApiKeys();
for (ApiKey key : keys) {
    System.out.println(key.getName() + ": " + key.getPrefix() + "*** (" + key.getType() + ")");
}

// Get a specific API key, with its scopes and whether it was revoked
ApiKey key = client.account().getApiKey("key_xxx");
System.out.println(key.getPermissions() + " revoked: " + key.isRevoked() + " " + key.getRevokedAt());

// Get API key usage stats (raw JSON)
JsonObject usage = client.account().getApiKeyUsage("key_xxx");

// Create a new API key (returns raw JSON; key value shown only once).
// type must be "test" or "live". Leave the scopes out to give the new key the
// scopes of the key making the call.
JsonObject newKey = client.account().createApiKey("Production Key", "live",
    Arrays.asList("sms:send", "sms:read"));
System.out.println("New key: " + newKey.get("key").getAsString());

// Revoke an API key, optionally with a reason
client.account().revokeApiKey("key_xxx", "rotated");

// Rotate an API key. Issues a replacement now and keeps the old key valid for a
// grace period (default 24h; 24-168h) so you can cut over with no downtime. The
// new raw key is shown only once.
JsonObject rotated = client.account().rotateApiKey("key_xxx");
System.out.println(rotated.getAsJsonObject("newKey").get("key").getAsString());

// With a custom 72-hour grace period
JsonObject rotated72 = client.account().rotateApiKey("key_xxx", 72);
System.out.println(rotated72.get("message").getAsString());
```

The scopes a key may carry are `sms:send`, `sms:read`, `verify:send`,
`verify:read`, `webhooks:read`, `webhooks:write`, `templates:read`,
`templates:write`, `campaigns:read`, `campaigns:write`, `campaigns:send`,
`contacts:read`, `contacts:write`, `numbers:read`, `numbers:write`,
`tendlc:read`, `tendlc:write`, `rcs:read`, `rcs:write`, `short_codes:read`,
`short_codes:write`, `calls:read` and `calls:write`, plus `whatsapp:read` and
`whatsapp:write`. A scope outside this list is refused with a 400
`validation_error` (a `ValidationException`), and a scope the calling key does not
hold itself with a 403 `insufficient_permissions`. A live key also needs a
verified business (403 `verification_required`) and credits (402
`credits_required`, an `InsufficientCreditsException`).

`getRevokedAt()` is set by `getApiKey(id)` and `revokeApiKey(id)`; it is null
on the keys `listApiKeys()` returns, revoked ones included, because the list
does not send it.

## Error Handling

```java
import com.sendly.exceptions.*;

try {
    Message message = client.messages().send("+12025550143", "Hello!");
} catch (AuthenticationException e) {
    // 401 - Invalid API key
} catch (RateLimitException e) {
    // 429. A limit worth waiting for was already waited out and retried.
    if ("too_many_failed_key_attempts".equals(e.getApiErrorCode())) {
        // Repeated wrong API keys locked this address out: fix the key, don't retry
    } else {
        System.out.println(e.getApiErrorCode() + ", retry after: " + e.getRetryAfter() + " seconds");
    }
} catch (InsufficientCreditsException e) {
    // 402 - Add more credits
} catch (ValidationException e) {
    // 400 or 422 - Invalid request; getStatusCode() says which
    e.getFieldErrors().forEach(f -> System.out.println(f.getPath() + ": " + f.getMessage()));
} catch (NotFoundException e) {
    // 404 - Resource not found
} catch (NetworkException e) {
    // Transport failure; getStatusCode() is 0
} catch (SendlyException e) {
    // Everything else, including 5xx after the client's own retries
    System.out.println(e.getMessage());
    System.out.println(e.getStatusCode());    // HTTP status
    System.out.println(e.getErrorCode());     // null here; only the subclasses above carry a constant
    System.out.println(e.getApiErrorCode());  // the body's machine-readable `error`
    System.out.println(e.getResponseBody());  // the whole body, as a Gson JsonObject
}
```

Every exception in `com.sendly.exceptions` extends `SendlyException`, which
extends `RuntimeException`, so none of them are checked and `throws` clauses are
advisory. The one checked exception is `Webhooks.WebhookSignatureException`,
thrown by `Webhooks.parseEvent(...)`.

`getErrorCode()` is a fixed constant per exception class
(`AUTHENTICATION_ERROR`, `INSUFFICIENT_CREDITS`, `NOT_FOUND`,
`RATE_LIMIT_EXCEEDED`, `VALIDATION_ERROR`, `NETWORK_ERROR`).
`getApiErrorCode()` is what the API actually said — use that one to tell apart
the different reasons a route returns the same status.

Both 400 and 422 arrive as `ValidationException`, and `getStatusCode()`
reports which one it was (a 422 such as `idempotency_key_mismatch` reports
422). When a route means different things by them, distinguish on
`getStatusCode()` or `getApiErrorCode()`.

An error response that is not JSON, such as an HTML page from a proxy, maps to
the usual exception for its status with the message `HTTP <status>: ` and the
start of the body, and `getResponseBody()` is null. A 2xx whose body is not
JSON throws `SendlyException("Invalid JSON response from API")` and is not
retried, because the request succeeded. An error body with no `message` uses
its `error` sentence as the message, or `HTTP <status>` when it has neither.

Every id put into a request path is percent-encoded, and an empty, null, `.`
or `..` id throws `ValidationException` before any request is made. Ids that
merely contain dots are sent as they are.

## Message Object

```java
message.getId();           // Unique identifier
message.getTo();           // Recipient phone number
message.getFrom();         // Sending number or sender ID
message.getText();         // Message content
message.getStatus();       // see Message Status below
message.getDirection();    // "outbound" or "inbound"
message.getSegments();     // int
message.getCreditsUsed();  // Credits consumed
message.getCreatedAt();    // Instant
message.getUpdatedAt();    // Instant
message.getDeliveredAt();  // Instant (nullable)
message.getErrorCode();    // String (nullable)
message.getErrorMessage(); // String (nullable)
message.getMediaUrls();    // List<String> (nullable, MMS media)
message.getMetadata();     // Map<String, Object>
message.isSandbox();       // boolean
message.getSenderType();   // number_pool | alphanumeric | explicit (live sends only; null otherwise)

// Helper methods
message.isDelivered();     // boolean
message.isFailed();        // boolean
message.isBounced();       // boolean
message.isPending();       // boolean
```

## Message Status

| Status | Constant | Description |
|--------|----------|-------------|
| `queued` | `Message.STATUS_QUEUED` | Message is queued for delivery |
| `sent` | `Message.STATUS_SENT` | Message was sent to the carrier |
| `delivered` | `Message.STATUS_DELIVERED` | Message was delivered |
| `read` | `Message.STATUS_READ` | Recipient read it (RCS and WhatsApp only; SMS never reports this) |
| `failed` | `Message.STATUS_FAILED` | Message delivery failed |
| `bounced` | `Message.STATUS_BOUNCED` | The number rejected it outright |
| `retrying` | `Message.STATUS_RETRYING` | A failed send is being retried |

## Pricing Tiers

1 credit = $0.01. US and Canada are 2 credits per SMS segment; MMS is 4.

| Tier | Credits per SMS | Example countries |
|------|-----------------|-------------------|
| Domestic | 2 | US, CA |
| Tier 1 | 8 | GB, AU, PL, SE |
| Tier 2 | 12 | FR, IT, JP, IN, ES |
| Tier 3 | 16 | DE, NL, MX, BE |
| Tier 4 | 24 | UA, BG, PA, GE |
| Tier 5 | 48 | IL, MY, PH, ID |

Multi-segment messages cost the per-segment price times the segment count.
Enterprise accounts can override rates per country or per tier.

## Sandbox Testing

Use test API keys (`sk_test_v1_xxx`) with these test numbers:

| Number | Behavior |
|--------|----------|
| +15005550000 | Success (instant) |
| +15005550001 | Fails: invalid_number |
| +15005550002 | Fails: unroutable_destination |
| +15005550003 | Fails: queue_full |
| +15005550004 | Fails: rate_limit_exceeded |
| +15005550006 | Fails: carrier_violation |

A successful sandbox send through `messages().send(...)` still fires a
`message.delivered` webhook, so you can exercise your handler end to end without
spending credits. Sends to the failure numbers come back `failed` straight away
and fire no `message.failed` webhook, and sandbox batch sends fire no webhooks.

## Enterprise

The Enterprise API lets you programmatically manage workspaces, verification,
credits, and API keys for multi-tenant platforms. It requires an API key marked
as your organization's enterprise master key — create one from the dashboard.
Master keys look like any other live key (`sk_live_v1_...`); what distinguishes
them is the flag on the key, not the prefix. Calls from a non-master key are
refused with 403 `enterprise_required`, and from a master key whose enterprise
account is inactive with 403 `enterprise_inactive`.

### Quick Provision

Create a fully configured workspace in a single call:

```java
Sendly client = new Sendly("sk_live_v1_your_master_key");

JsonObject options = new JsonObject();
options.addProperty("name", "Acme Insurance - Austin");
options.addProperty("sourceWorkspaceId", "ws_verified");
options.addProperty("creditAmount", 5000);
options.addProperty("creditSourceWorkspaceId", "SOURCE_WORKSPACE_ID");
options.addProperty("keyName", "Production");
options.addProperty("keyType", "live");
options.addProperty("generateOptInPage", true);

JsonObject result = client.enterprise().provision(options);

System.out.println(result.getAsJsonObject("workspace").get("id").getAsString());
System.out.println(result.getAsJsonObject("key").get("key").getAsString());
```

Three provisioning modes:

| Mode | Params | Description |
|------|--------|-------------|
| **Inherit** | `sourceWorkspaceId` | Shares toll-free number from verified workspace |
| **Inherit + New Number** | `sourceWorkspaceId` + `inheritWithNewNumber: true` | Copies business info, purchases new number |
| **Fresh** | `verification` object | Full business details, new number + carrier approval |

`client.enterprise().workspaces().provisionBulk(JsonArray)` runs the same flow for up to 100 workspaces at once; more throws `ValidationException` before anything is sent.

Provisioning is limited per minute and per hour (`provision_rate_limit`). The
client waits out any `provision_rate_limit` whose wait is 60 seconds or less,
which covers the per-minute limit. The hourly one is thrown at once as a
`RateLimitException` whose `getRetryAfter()` says how long to pause, unless
less than a minute of its window is left.

### Workspace Management

```java
JsonObject ws = client.enterprise().workspaces().create("Acme Insurance");
JsonObject list = client.enterprise().workspaces().list();
JsonObject detail = client.enterprise().workspaces().get("ws_xxx");
client.enterprise().workspaces().suspend("ws_xxx", "non-payment");
client.enterprise().workspaces().resume("ws_xxx");
client.enterprise().workspaces().delete("ws_xxx");

// Verification, per workspace. Inherit shares the source's verified toll-free
// number; pass true to copy only the business details, buy the workspace its
// own toll-free number and submit it (the response has newNumber: true).
client.enterprise().workspaces().inheritVerification("ws_xxx", "ws_verified");
client.enterprise().workspaces().inheritVerification("ws_other", "ws_verified", true);
JsonObject verification = client.enterprise().workspaces().getVerification("ws_xxx");

// Invitations and quotas
client.enterprise().workspaces().sendInvitation("ws_xxx", "sam@acme.example", "admin");
client.enterprise().workspaces().setQuota("ws_xxx", 50000);
```

### Credits & API Keys

```java
client.enterprise().workspaces().transferCredits("ws_dest", "ws_source", 5000);
JsonObject balance = client.enterprise().workspaces().getCredits("ws_dest");

JsonObject key = client.enterprise().workspaces().createKey("ws_xxx", "Production", "live");
System.out.println(key.get("key").getAsString());
// createKey("ws_xxx") creates a test key named "API key"

List<JsonObject> keys = client.enterprise().workspaces().listKeys("ws_xxx");
client.enterprise().workspaces().revokeKey("ws_xxx", "key_abc");

// Pooled enterprise credits
JsonObject pool = client.enterprise().credits().get();
client.enterprise().credits().deposit(100000, "Q2 top-up");
```

### Webhooks, Analytics & Billing

```java
client.enterprise().webhooks().set("https://hooks.acme.example/enterprise");
client.enterprise().webhooks().test();
client.enterprise().webhooks().rotateSecret();

JsonObject overview = client.enterprise().analytics().overview();
JsonObject messages = client.enterprise().analytics().messages("30d", null);
JsonObject delivery = client.enterprise().analytics().delivery();   // the rows are under "data"
JsonObject creditUse = client.enterprise().analytics().credits("30d");

JsonObject breakdown = client.enterprise().billing().getBreakdown("30d", 1, 50);
JsonObject autoTopUp = client.enterprise().settings().getAutoTopUp();
```

`listOptInPages(...)`, `listWebhooks(...)`, `listInvitations(...)` and
`analytics().delivery()` call endpoints that answer with a bare JSON array, so
their `JsonObject` holds the list under `data`.

Full enterprise docs: [sendly.live/docs/enterprise](https://sendly.live/docs/enterprise)

## Business Upgrade

Fork a verified workspace onto a new legal entity and number
(`client.businessUpgrade()`): `preflight(...)`, `bestPrefill()`,
`start(workspaceId, params)`, `status(workspaceId)`, `resubmit(...)`,
`cancel(workspaceId)` and `setDisposition(...)`. Every method returns a raw
`JsonObject`; `PreflightCandidate.builder()` and `StartUpgradeParams.builder()`
build the inputs, and `EinDocument.fromFile(...)` attaches the EIN letter.

---

## License

MIT
