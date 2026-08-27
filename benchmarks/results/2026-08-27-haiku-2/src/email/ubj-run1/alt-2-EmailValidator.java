import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            new InternetAddress(email, true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
