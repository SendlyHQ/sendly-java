package examples;

import com.sendly.Sendly;
import com.sendly.exceptions.*;
import com.sendly.models.Message;

/**
 * Example: Sending an SMS with error handling.
 */
public class SendSmsExample {
    public static void main(String[] args) {
        // Get API key from environment or use test key
        String apiKey = System.getenv("SENDLY_API_KEY");
        if (apiKey == null) {
            apiKey = "sk_test_v1_example";
        }

        // Create client
        Sendly client = new Sendly(apiKey);

        try {
            // Send an SMS
            Message message = client.messages().send(
                "+15125550123",
                "Hello from Sendly Java SDK!"
            );

            System.out.println("Message sent successfully!");
            System.out.println("  ID: " + message.getId());
            System.out.println("  To: " + message.getTo());
            System.out.println("  Status: " + message.getStatus());
            System.out.println("  Credits used: " + message.getCreditsUsed());

        } catch (AuthenticationException e) {
            System.err.println("Authentication failed: " + e.getMessage());
        } catch (InsufficientCreditsException e) {
            System.err.println("Insufficient credits: " + e.getMessage());
        } catch (RateLimitException e) {
            if ("too_many_failed_key_attempts".equals(e.getApiErrorCode())) {
                System.err.println("Locked out after repeated wrong API keys. Fix the key; do not retry.");
            } else {
                System.err.println("Rate limited (" + e.getApiErrorCode() + "). Retry after: "
                    + e.getRetryAfter() + " seconds");
            }
        } catch (ValidationException e) {
            System.err.println("Validation error: " + e.getMessage());
        } catch (SendlyException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
