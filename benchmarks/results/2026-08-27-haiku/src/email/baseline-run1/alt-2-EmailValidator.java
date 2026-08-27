import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    
    /**
     * Validates an email address using Jakarta Mail library
     * More compliant with RFC 5321/5322 standards
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
