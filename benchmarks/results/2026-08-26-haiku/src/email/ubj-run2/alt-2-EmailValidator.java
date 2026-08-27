import javax.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
