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
    <version>3.7.0</version>
</dependency>
```

### Gradle (Groovy)

```groovy
implementation 'live.sendly:sendly-java:3.7.0'
```

### Gradle (Kotlin)

```kotlin
implementation("live.sendly:sendly-java:3.7.0")
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
System.out.println("Total credits needed: " + preview.getTotalCredits());
System.out.println("Valid: " + preview.getValid() + ", Invalid: " + preview.getInvalid());
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

## Webhooks

```java
// Create a webhook endpoint
Webhook webhook = client.webhooks().create(
    CreateWebhookRequest.builder()
        .url("https://example.com/webhooks/sendly")
        .events(Arrays.asList("message.delivered", "message.failed"))
        .build()
);

System.out.println(webhook.getId());
System.out.println(webhook.getSecret()); // Store securely!

// List all webhooks
List<Webhook> webhooks = client.webhooks().list();

// Get a specific webhook
Webhook wh = client.webhooks().get("whk_xxx");

// Update a webhook
client.webhooks().update("whk_xxx",
    UpdateWebhookRequest.builder()
        .url("https://new-endpoint.example.com/webhook")
        .events(Arrays.asList("message.delivered", "message.failed", "message.sent"))
        .build()
);

// Test a webhook
WebhookTestResult result = client.webhooks().test("whk_xxx");

// Rotate webhook secret
WebhookSecretRotation rotation = client.webhooks().rotateSecret("whk_xxx");

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
CreditTransactionList transactions = client.account().getCreditTransactions();
for (CreditTransaction tx : transactions) {
    System.out.println(tx.getType() + ": " + tx.getAmount() + " credits - " + tx.getDescription());
}

// List API keys
ApiKeyList keys = client.account().listApiKeys();
for (ApiKey key : keys) {
    System.out.println(key.getName() + ": " + key.getPrefix() + "*** (" + key.getType() + ")");
}

// Get a specific API key
ApiKey key = client.account().getApiKey("key_xxx");

// Get API key usage stats
ApiKeyUsage usage = client.account().getApiKeyUsage("key_xxx");
System.out.println("Messages sent: " + usage.getMessagesSent());

// Create a new API key
ApiKey newKey = client.account().createApiKey(
    CreateApiKeyRequest.builder()
        .name("Production Key")
        .type("live")
        .scopes(Arrays.asList("sms:send", "sms:read"))
        .build()
);
System.out.println("New key: " + newKey.getKey()); // Only shown once!

// Revoke an API key
client.account().revokeApiKey("key_xxx");
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
| Domestic | US, CA | 1 |
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

## License

MIT
