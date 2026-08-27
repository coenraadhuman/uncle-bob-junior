import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
