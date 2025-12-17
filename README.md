# Sendly Java SDK

Official Java SDK for the Sendly SMS API.

## Requirements

- Java 17+
- Maven or Gradle

## Installation

### Maven

```xml
<dependency>
    <groupId>com.sendly</groupId>
    <artifactId>sendly-java</artifactId>
    <version>3.0.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.sendly:sendly-java:3.0.1'
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
        .baseUrl("https://api.sendly.live/v1")
        .timeout(Duration.ofSeconds(60))
        .maxRetries(5)
);
```

## Messages

### Send an SMS

```java
// Simple
Message message = client.messages().send("+15551234567", "Hello!");

// With builder
Message message = client.messages().send(
    SendMessageRequest.builder()
        .to("+15551234567")
        .text("Hello from Sendly!")
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
| +15550001234 | Success |
| +15550001001 | Invalid number |
| +15550001002 | Carrier rejected |
| +15550001003 | No credits |
| +15550001004 | Rate limited |

## License

MIT
