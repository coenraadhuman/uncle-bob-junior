import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean validateEmail(String email) {
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
