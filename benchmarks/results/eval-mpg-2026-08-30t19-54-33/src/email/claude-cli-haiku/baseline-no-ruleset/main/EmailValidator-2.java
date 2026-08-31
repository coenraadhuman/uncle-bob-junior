import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
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
