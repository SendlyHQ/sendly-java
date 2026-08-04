<p align="center">
  <img src="https://raw.githubusercontent.com/SendlyHQ/sendly-java/main/.github/header.svg" alt="Sendly Java SDK" />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.sendly/sendly-java"><img src="https://img.shields.io/maven-central/v/com.sendly/sendly-java?style=flat-square" alt="Maven Central" /></a>
  <a href="https://github.com/SendlyHQ/sendly-java/blob/main/LICENSE"><img src="https://img.shields.io/github/license/SendlyHQ/sendly-java?style=flat-square" alt="license" /></a>
</p>

# Sendly Java SDK

Official Java SDK for the Sendly SMS API.

## Requirements

- Java 17+
- Maven or Gradle

## Installation

### Maven

```xml
<dependency>
    <groupId>live.sendly</groupId>
    <artifactId>sendly-java</artifactId>
    <version>3.36.0</version>
</dependency>
```

### Gradle (Groovy)

```groovy
implementation 'live.sendly:sendly-java:3.36.0'
```

### Gradle (Kotlin)

```kotlin
implementation("live.sendly:sendly-java:3.36.0")
```

## Quick Start

```java
import com.sendly.Sendly;
import com.sendly.models.Message;

Sendly client = new Sendly("sk_live_v1_your_api_key");

// Send an SMS
Message message = client.messages().send(
    "+15551234567",
    "Hello from Sendly!"
);

System.out.println(message.getId());     // "msg_abc123"
System.out.println(message.getStatus()); // "queued"
```

## Prerequisites for Live Messaging

Before sending live SMS messages, you need:

1. **Business Verification** - Complete verification in the [Sendly dashboard](https://sendly.live/dashboard)
   - **International**: Instant approval (just provide Sender ID)
   - **US/Canada**: Requires carrier approval (3-7 business days)

2. **Credits** - Add credits to your account
   - Test keys (`sk_test_*`) work without credits (sandbox mode)
   - Live keys (`sk_live_*`) require credits for each message

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
import java.time.Duration;

Sendly client = new Sendly("sk_live_v1_xxx", 
    new Sendly.Builder()
        .baseUrl("https://sendly.live/api/v1")
        .timeout(Duration.ofSeconds(60))
        .maxRetries(5)
);
```

## Messages

### Send an SMS

```java
// Marketing message (default)
Message message = client.messages().send("+15551234567", "Check out our new features!");

// Transactional message (bypasses quiet hours)
Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+15551234567")
        .text("Your verification code is: 123456")
        .messageType("transactional")
        .build()
);

// Send from one of your owned numbers (or an alphanumeric sender ID).
// Omit from(...) to use your default sender.
Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+15551234567")
        .text("Hello from our team!")
        .from("+447111111111")
        .build()
);

System.out.println(message.getId());
System.out.println(message.getStatus());
System.out.println(message.getCreditsUsed());
```

### List Messages

```java
// Basic listing
MessageList messages = client.messages().list();

for (Message msg : messages) {
    System.out.println(msg.getTo());
}

// With filters
MessageList messages = client.messages().list(
    ListMessagesRequest.builder()
        .status("delivered")
        .to("+15551234567")
        .limit(50)
        .offset(0)
        .build()
);

// Pagination info
System.out.println(messages.getTotal());
System.out.println(messages.hasMore());
```

### Get a Message

```java
Message message = client.messages().get("msg_abc123");

System.out.println(message.getTo());
System.out.println(message.getText());
System.out.println(message.getStatus());
System.out.println(message.getDeliveredAt());
```

### Scheduling Messages

```java
// Schedule a message for future delivery
ScheduledMessage scheduled = client.messages().schedule(
    ScheduleMessageRequest.builder()
        .to("+15551234567")
        .text("Your appointment is tomorrow!")
        .scheduledAt("2025-01-15T10:00:00Z")
        .build()
);

System.out.println(scheduled.getId());
System.out.println(scheduled.getScheduledAt());

// List scheduled messages
ScheduledMessageList result = client.messages().listScheduled();
for (ScheduledMessage msg : result) {
    System.out.println(msg.getId() + ": " + msg.getScheduledAt());
}

// Get a specific scheduled message
ScheduledMessage msg = client.messages().getScheduled("sched_xxx");

// Cancel a scheduled message (refunds credits)
CancelScheduledMessageResponse cancel = client.messages().cancelScheduled("sched_xxx");
System.out.println("Refunded: " + cancel.getCreditsRefunded() + " credits");
```

### Batch Messages

```java
// Send multiple messages in one API call (up to 1000)
BatchMessageResponse batch = client.messages().sendBatch(
    SendBatchRequest.builder()
        .addMessage("+15551234567", "Hello User 1!")
        .addMessage("+15559876543", "Hello User 2!")
        .addMessage("+15551112222", "Hello User 3!")
        .build()
);

System.out.println(batch.getBatchId());
System.out.println("Queued: " + batch.getQueued());
System.out.println("Failed: " + batch.getFailed());
System.out.println("Credits used: " + batch.getCreditsUsed());

// Get batch status
BatchMessageResponse status = client.messages().getBatch("batch_xxx");

// List all batches
BatchList batches = client.messages().listBatches();

// Preview batch (dry run) - validates without sending
BatchPreviewResponse preview = client.messages().previewBatch(
    SendBatchRequest.builder()
        .addMessage("+15551234567", "Hello User 1!")
        .addMessage("+447700900123", "Hello UK!")
        .build()
);
System.out.println("Credits needed: " + preview.getCreditsNeeded());
System.out.println("Will send: " + preview.getWillSend() + ", Blocked: " + preview.getBlocked());
```

### Group MMS

Send a group MMS to 2-8 recipients (US/Canada only). Everyone in `to` shares one
thread and replies fan out to all participants. Requires the `group_mms` feature.

```java
import java.util.List;

GroupMessageResponse group = client.messages().sendGroup(
    SendGroupMessageRequest.builder()
        .to(List.of("+14155551234", "+14155555678"))
        .text("Hey team - quick sync at noon?")
        .build()
);

System.out.println(group.getId());              // msg_xxx
System.out.println(group.getGroupMessageId());  // grp_xxx
System.out.println(group.getStatus());          // sent
```

### AI Enhance

Rewrite a draft message into a single polished SMS segment (≤160 chars).
Requires the `ai_classification` feature; when AI is unavailable the original
text is returned with an empty explanation.

```java
EnhanceMessageResponse result = client.messages().enhance(
    EnhanceMessageRequest.builder()
        .text("hey come check out our sale this weekend")
        .messageType("marketing")
        .build()
);

System.out.println(result.getEnhanced());     // polished rewrite
System.out.println(result.getExplanation());  // what changed and why
```

### Iterate All Messages

```java
// Auto-pagination
for (Message message : client.messages().each()) {
    System.out.println(message.getId() + ": " + message.getTo());
}

// With filters
for (Message message : client.messages().each(
    ListMessagesRequest.builder()
        .status("delivered")
        .build()
)) {
    System.out.println("Delivered: " + message.getId());
}
```

## Numbers

Discover, buy, and manage the phone numbers you own.

```java
// List the numbers you own
OwnedNumbersResponse owned = client.numbers().list();
for (OwnedNumber n : owned.getNumbers()) {
    System.out.println(n.getPhoneNumber() + " (" + n.getStatus() + ")");
}

// Get one number, including whether it's your default sender
OwnedNumber number = client.numbers().get("num_abc123");
System.out.println(number.isDefault());

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

## WhatsApp

Connect a number you own to WhatsApp, create Meta-reviewed message templates,
and send WhatsApp messages through the same `messages().send()` you use for SMS.

> **Note**: WhatsApp is gated behind the `whatsapp_channel` rollout flag
> (default-dark). Calls return a `not_found` error until the flag is on for
> your account. Signup, template writes, and sends require a live API key.

```java
// 1. Connect a number ($19 one-time, no monthly fee). The connect URL must be
//    opened by a human — they log in with Facebook in a browser to link their
//    WhatsApp Business Account.
WhatsAppSignupSession signup = client.whatsapp().signup().create("+15559876543");
System.out.println("Have your user open: " + signup.getConnectUrl());

// 2. Poll until active
WhatsAppSignup status = client.whatsapp().signup().get(signup.getId());
System.out.println(status.getStatus()); // initiated | registering | active | failed | expired

// 3. List your WhatsApp senders
WhatsAppSendersResponse senders = client.whatsapp().senders().list();
for (WhatsAppSender s : senders.getSenders()) {
    System.out.println(s.getPhoneNumber() + " (" + s.getStatus() + ", " + s.getQualityRating() + ")");
}

// 4. Create a template (Meta reviews it, usually 24-48h)
WhatsAppTemplate template = client.whatsapp().templates().create(
    CreateWhatsAppTemplateRequest.builder()
        .sender("+15559876543")
        .name("order_shipped")
        .language("en_US")
        .category("UTILITY")
        .body("Hi {{1}}, your order {{2}} has shipped!")
        .examples(Map.of("1", "Sam", "2", "#4821"))
        .build());
System.out.println(template.getStatus()); // PENDING

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
WhatsAppWindow window = client.whatsapp().window("+15559876543", "+15551234567");
if (window.isOpen()) {
    // Free-form reply inside the open window
    WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
        .to("+15551234567")
        .from("+15559876543")
        .text("Your table is ready!")
        .build());
} else {
    // Template send — works regardless of the window
    WhatsAppMessage message = client.messages().send(SendWhatsAppMessageRequest.builder()
        .to("+15551234567")
        .from("+15559876543")
        .template(new WhatsAppTemplateSendParams("order_shipped", "en_US",
            Map.of("1", "Acme Inc", "2", "#4821")))
        .build());
    System.out.println(message.getWhatsapp().getKind());    // "template"
    System.out.println(message.getCreditsUsed());           // priced by country + category
}

// Media send (one attachment; text becomes the caption)
client.messages().send(SendWhatsAppMessageRequest.builder()
    .to("+15551234567")
    .from("+15559876543")
    .text("Here's the menu")
    .mediaUrls(List.of("https://example.com/menu.jpg"))
    .build());

// 6. Read and edit a sender's business profile — the contact card recipients
//    see. Supply only the fields to change; omitted fields keep their value.
WhatsAppSenderProfile profile = client.whatsapp().senders().getProfile("+15559876543");
System.out.println(profile.getDisplayName() + " — " + profile.getAbout());

client.whatsapp().senders().updateProfile("+15559876543",
    UpdateWhatsAppSenderProfileRequest.builder()
        .about("Fast delivery, friendly service")   // max 139 chars
        .description("Acme sells everything.")      // max 512 chars
        .website("https://example.com")
        .build());
```

## RCS

Send branded rich messaging — cards and suggestion chips — through the same
`messages().send()` you use for SMS. Plain-text RCS sends fall back to SMS
automatically for recipients whose device can't receive RCS.

> **Note**: RCS is gated behind the `rcs_channel` rollout flag (default-dark).
> Calls return a `not_found` error until the flag is on for your account. Sends
> and capability checks require a live API key.

```java
// 1. Find your agent — the brand identity your messages are sent as. Contact
//    support to register one; "testing" reaches invited test numbers only,
//    "approved" reaches everyone.
RcsAgentsResponse agents = client.rcs().agents().list();
for (RcsAgent agent : agents.getAgents()) {
    System.out.println(agent.getName() + " (" + agent.getStatus() + ", sendable=" + agent.isSendable() + ")");
}

// 2. Optional pre-flight: can this recipient receive RCS?
RcsCapability capability = client.rcs().capability("+15551234567");
System.out.println(capability.isCapable());   // false -> text falls back to SMS
System.out.println(capability.getFeatures()); // device features, empty when not capable

// 3. Send text, optionally with suggestion chips. A reply chip's tap comes
//    back as an inbound message carrying your postbackData; an action chip
//    opens a URL.
RcsMessage message = client.messages().send(SendRcsMessageRequest.builder()
    .to("+15551234567")
    .text("Your order #4821 has shipped!")
    .suggestions(List.of(
        RcsSuggestion.reply("Thanks", "thanks"),
        RcsSuggestion.action("Track", "track", "https://example.com/track/4821")))
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
    .to("+15551234567")
    .card(RcsCard.builder()
        .title("Order #4821 shipped")
        .description("Arriving Thursday")
        .mediaUrl("https://example.com/package.jpg")  // public JPEG, PNG, or GIF
        .orientation("vertical")                      // or "horizontal"
        .suggestions(List.of(
            RcsSuggestion.action("Track", "track", "https://example.com/track/4821")))
        .build())
    .build());

// Opt out of the SMS fallback to get a 422 instead of an SMS charge
client.messages().send(SendRcsMessageRequest.builder()
    .to("+15551234567")
    .text("RCS only, please")
    .fallbackToSms(false)
    .build());

// Pass agentId when your workspace has more than one agent (otherwise the
// send fails with rcs_agent_ambiguous)
client.messages().send(SendRcsMessageRequest.builder()
    .to("+15551234567")
    .agentId("rag_abc123")
    .text("Your order #4821 has shipped!")
    .build());
```

## Branded Short Links

Mint branded short links for a destination URL, list them with click analytics,
and toggle a per-link kill switch.

> **Note**: URL shortening is gated behind the `url_shortener` rollout flag
> (founder-only while dark). Calls return a `not_found` error until the flag is
> on for your account.

```java
// Shorten a URL
ShortLink link = client.links().create("https://example.com/spring-sale?utm_source=sms");
System.out.println(link.getShortUrl()); // https://sendly.live/l/Ab3xY7

// List your links with click counts
ShortLinkListResponse listing = client.links().list(20, 0);
for (ShortLinkListItem item : listing.getLinks()) {
    System.out.println(item.getShortUrl() + " -> " + item.getDestinationUrl()
        + " (" + item.getClickCount() + " clicks)");
}

// Kill a link (returns 404 on redirect until re-enabled)
client.links().disable(link.getCode());
client.links().enable(link.getCode());
```

## Webhooks

```java
// Create a webhook endpoint
WebhookCreatedResponse webhook = client.webhooks().create(
    "https://example.com/webhooks/sendly",
    Arrays.asList("message.delivered", "message.failed")
);

System.out.println(webhook.getId());
System.out.println(webhook.getSecret()); // Store securely!

// List all webhooks
List<Webhook> webhooks = client.webhooks().list();

// Get a specific webhook
Webhook wh = client.webhooks().get("whk_xxx");

// Update a webhook (url, events, description, isActive)
client.webhooks().update("whk_xxx",
    "https://new-endpoint.example.com/webhook",
    Arrays.asList("message.delivered", "message.failed", "message.sent"),
    null,
    null
);

// Test a webhook
WebhookTestResult result = client.webhooks().test("whk_xxx");

// Rotate webhook secret
WebhookCreatedResponse rotation = client.webhooks().rotateSecret("whk_xxx");

// Delete a webhook
client.webhooks().delete("whk_xxx");

// List available webhook event types
List<String> eventTypes = client.webhooks().listEventTypes();
for (String eventType : eventTypes) {
    System.out.println("Event: " + eventType);
}
```

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
List<CreditTransaction> transactions = client.account().getCreditTransactions();
for (CreditTransaction tx : transactions) {
    System.out.println(tx.getType() + ": " + tx.getAmount() + " credits - " + tx.getDescription());
}

// List API keys
List<ApiKey> keys = client.account().listApiKeys();
for (ApiKey key : keys) {
    System.out.println(key.getName() + ": " + key.getPrefix() + "*** (" + key.getType() + ")");
}

// Get a specific API key
ApiKey key = client.account().getApiKey("key_xxx");

// Get API key usage stats (raw JSON)
JsonObject usage = client.account().getApiKeyUsage("key_xxx");
System.out.println("Messages sent: " + usage.get("messagesSent").getAsInt());

// Create a new API key (returns raw JSON; key value shown only once)
JsonObject newKey = client.account().createApiKey("Production Key", "live", Arrays.asList("sms:send", "sms:read"));
System.out.println("New key: " + newKey.get("key").getAsString());

// Revoke an API key
client.account().revokeApiKey("key_xxx");

// Rotate an API key. Issues a replacement now and keeps the old key valid for a
// grace period (default 24h; 24-168h) so you can cut over with no downtime. The
// new raw key is shown only once.
JsonObject rotated = client.account().rotateApiKey("key_xxx");
System.out.println(rotated.getAsJsonObject("newKey").get("key").getAsString());

// With a custom 72-hour grace period
JsonObject rotated72 = client.account().rotateApiKey("key_xxx", 72);
System.out.println(rotated72.get("message").getAsString());
```

## Error Handling

```java
import com.sendly.exceptions.*;

try {
    Message message = client.messages().send("+15551234567", "Hello!");
} catch (AuthenticationException e) {
    // Invalid API key
} catch (RateLimitException e) {
    // Rate limit exceeded
    System.out.println("Retry after: " + e.getRetryAfter() + " seconds");
} catch (InsufficientCreditsException e) {
    // Add more credits
} catch (ValidationException e) {
    // Invalid request
} catch (NotFoundException e) {
    // Resource not found
} catch (NetworkException e) {
    // Network error
} catch (SendlyException e) {
    // Other error
    System.out.println(e.getMessage());
    System.out.println(e.getErrorCode());
    System.out.println(e.getStatusCode());
}
```

## Message Object

```java
message.getId();           // Unique identifier
message.getTo();           // Recipient phone number
message.getText();         // Message content
message.getStatus();       // queued, sending, sent, delivered, failed
message.getCreditsUsed();  // Credits consumed
message.getCreatedAt();    // Instant
message.getUpdatedAt();    // Instant
message.getDeliveredAt();  // Instant (nullable)
message.getErrorCode();    // String (nullable)
message.getErrorMessage(); // String (nullable)
message.getMediaUrls();    // List<String> (nullable, MMS media)

// Helper methods
message.isDelivered();     // boolean
message.isFailed();        // boolean
message.isPending();       // boolean
```

## Message Status

| Status | Description |
|--------|-------------|
| `queued` | Message is queued for delivery |
| `sending` | Message is being sent |
| `sent` | Message was sent to carrier |
| `delivered` | Message was delivered |
| `failed` | Message delivery failed |

## Pricing Tiers

| Tier | Countries | Credits per SMS |
|------|-----------|-----------------|
| Domestic | US, CA | 2 |
| Tier 1 | GB, PL, IN, etc. | 8 |
| Tier 2 | FR, JP, AU, etc. | 12 |
| Tier 3 | DE, IT, MX, etc. | 16 |

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

## Enterprise

The Enterprise API lets you programmatically manage workspaces, verification, credits, and API keys for multi-tenant platforms. Requires an enterprise master key (`sk_live_v1_master_*`).

### Quick Provision

Create a fully configured workspace in a single call:

```java
Sendly client = new Sendly("sk_live_v1_master_YOUR_KEY");

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

### Workspace Management

```java
JsonObject ws = client.enterprise().workspaces().create("Acme Insurance");
JsonObject list = client.enterprise().workspaces().list();
JsonObject detail = client.enterprise().workspaces().get("ws_xxx");
client.enterprise().workspaces().delete("ws_xxx");
```

### Credits & API Keys

```java
client.enterprise().workspaces().transferCredits("ws_dest", "ws_source", 5000);

JsonObject key = client.enterprise().workspaces().createKey("ws_xxx", "Production", "live");
System.out.println(key.get("key").getAsString());

client.enterprise().workspaces().revokeKey("ws_xxx", "key_abc");
```

### Webhooks & Analytics

```java
client.enterprise().webhooks().set("https://yourapp.com/webhooks");
JsonObject overview = client.enterprise().analytics().overview();
JsonObject messages = client.enterprise().analytics().messages("30d", null);
JsonObject delivery = client.enterprise().analytics().delivery();
```

Full enterprise docs: [sendly.live/docs/enterprise](https://sendly.live/docs/enterprise)

---

## License

MIT
