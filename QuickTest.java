import com.sendly.Sendly;
import com.sendly.models.Message;
import com.sendly.models.Credits;

public class QuickTest {
    public static void main(String[] args) {
        try {
            Sendly client = new Sendly("sk_test_v1_CifaHSb7eq2s4jKb60I9xz0lnqsJv7Hg");

            System.out.println("Testing Sendly Java SDK...\n");

            Credits credits = client.account().getCredits();
            System.out.println("✓ Credits: " + credits.getBalance());

            Message message = client.messages().send("+15005550000", "Test message");
            System.out.println("✓ Message sent: " + message.getId());
            System.out.println("✓ Status: " + message.getStatus());

            System.out.println("\n✓ All tests passed!");

        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
