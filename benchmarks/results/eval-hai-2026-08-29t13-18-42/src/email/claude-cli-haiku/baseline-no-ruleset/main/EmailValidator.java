public class EmailValidator {
    
    /**
     * Validates email addresses using a regex pattern that covers most common cases.
     * Not fully RFC 5322 compliant but suitable for typical business use.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Stricter validation using InternetAddress (requires javax.mail dependency).
     * More robust but has external dependency.
     */
    public static boolean isValidEmailStrict(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            new javax.mail.internet.InternetAddress(email).validate();
            return true;
        } catch (javax.mail.internet.AddressException e) {
            return false;
        }
    }
}
