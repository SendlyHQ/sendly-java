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
    <version>4.1.0</version>
</dependency>
```

### Gradle (Groovy)

```groovy
implementation 'live.sendly:sendly-java:4.1.0'
```

### Gradle (Kotlin)

```kotlin
implementation("live.sendly:sendly-java:4.1.0")
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

## Idempotency

POSTs carry an automatically generated `Idempotency-Key`, reused across the
SDK's own timeout and network-error retries, so a retry of a request that
already reached the API returns the original result instead of sending and
charging again. Pass your own key via `IdempotentRequestOptions` when the
guarantee needs to outlive the process, such as a job queue that re-runs after
a crash or your own retry loop. Reusing a key within 24 hours returns the
original response, so derive keys from something stable in your domain, like an
order id. `sendBatch` sends no automatic key, because the API already
deduplicates identical batches by their contents.

```java
import com.sendly.models.IdempotentRequestOptions;

Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+15551234567")
        .text("Your order has shipped!")
        .build(),
    new IdempotentRequestOptions("order-4821-shipped")
);
```

Full details: https://sendly.live/docs/idempotency

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
> Registration calls return a 404 `rcs_not_enabled` error and sends a
> `not_found` error until the flag is on for your account. Registration needs
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

### Send

```java
// 1. Find your agent — "testing" reaches invited test devices only,
//    "approved" reaches everyone.
RcsAgentsResponse agents = client.rcs().agents().list();
for (RcsAgent agent : agents.getAgents()) {
    System.out.println(agent.getName() + " (" + agent.getStage() + ", sendable=" + agent.isSendable() + ")");
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
    .to("+15555550123")
    .agentId("3c4d5e6f-7081-4293-a4b5-c6d7e8f90a1b")
    .from("+15555550188")                 // optional with exactly one voice-enabled number
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
        case CallErrorCode.LINES_BUSY -> { /* 409: every line is in use, retry with backoff */ }
        case CallErrorCode.DAILY_CALL_LIMIT -> { /* 429: try again tomorrow */ }
        case CallErrorCode.FROM_NUMBER_REQUIRED -> { /* 400: more than one voice-enabled number, set from() */ }
        default -> throw e;
    }
}
```

`CallStatus`, `CallDirection`, `CallKind`, `CallHandledBy`, `CallBilling` and
`CallRecordingStatus` hold the string values the call object uses.

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

VoiceNumber number = client.voice().numbers().get("+15555550188");
System.out.println(number.getRatePerMinute().getOutbound() + " credits a minute outbound");

// Register the emergency address (country defaults to US)
number = client.voice().numbers().registerEmergencyAddress("+15555550188", EmergencyAddress.builder()
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
number = client.voice().numbers().update("+15555550188", UpdateVoiceNumberRequest.builder()
    .voiceEnabled(true)
    .voiceMode(VoiceMode.AGENT)
    .agentId(agent.getId())
    .build());

// Change an agent (only the fields you set are sent)
agent = client.voice().agents().update(agent.getId(), UpdateVoiceAgentRequest.builder()
    .greeting("Thanks for calling Acme. How can I help today?")
    .build());

// Ring the team instead, then delete the agent
client.voice().numbers().update("+15555550188", UpdateVoiceNumberRequest.builder()
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
name and number. Every write also takes an `IdempotentRequestOptions`
overload.

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
A 422 is thrown as a `ValidationException` like a 400, so tell the two
`invalid_address` refusals apart by the `suggested` field the 422 carries.
Extra fields are on `getResponseBody()`:

```java
try {
    client.voice().agents().delete(agent.getId());
} catch (SendlyException e) {
    if (!CallErrorCode.AGENT_IN_USE.equals(e.getApiErrorCode())) throw e;
    for (JsonElement n : e.getResponseBody().getAsJsonArray("numbers")) {
        System.out.println("Still answering " + n.getAsString());
    }
}

try {
    client.voice().numbers().registerEmergencyAddress("+15555550188", address);
} catch (ValidationException e) {
    if (!CallErrorCode.INVALID_ADDRESS.equals(e.getApiErrorCode())) throw e;
    JsonObject body = e.getResponseBody();
    if (body != null && body.has("suggested")) {
        System.out.println("Did you mean: " + body.get("suggested")); // 422: null when no correction was found
    } else {
        System.out.println(e.getMessage());                          // 400: a field is missing or malformed
    }
}
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

### Receiving events

`Webhooks.parseEvent(...)` verifies the signature and returns a
`Webhooks.WebhookEvent`. There are two ways to read the payload off it:

- `event.getData()` is the **message view** of `data.object`, and is only
  meaningful for `message.*` events.
- `event.getRawObject()` is `data.object` exactly as it arrived, for every
  event type, and `event.objectAs(SomeClass.class)` deserializes it with Gson.

Lifecycle events — `rcs_brand.*`, `rcs_agent.*`, `whatsapp_account.*`,
`whatsapp_template.*`, `call.*`, `brand.*`, `campaign.*`, `assignment.*`,
`number.*`, `port.*`, `port_out.*`, `contact.*` — carry a different object
entirely, so read those through `getRawObject()` or `objectAs(...)`. Reaching
for them through `getData()` gives you a message whose every field sits at its
default (`null`, `0`, `""`), and nothing raises an error to tell you so.

Subscribe with the `WebhookEventType` constants (in `com.sendly.models`) to
keep the wire strings honest — `create(...)` takes them as strings:

```java
client.webhooks().create(
    "https://example.com/webhooks/sendly",
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
> were a message id. The message that triggered the flag is the payload's own
> `message_id`: read it with
> `event.getRawObject().get("message_id").getAsString()`.

`Webhooks.verifySignature(payload, signature, secret, timestamp)` is available
on its own if you parse the body yourself, and
`Webhooks.generateSignature(payload, secret, timestamp)` signs a payload so you
can exercise your endpoint in tests.

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
