// Simple and practical email validation
public class EmailValidator {
    
    // RFC 5322 simplified regex pattern
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    
    /**
     * Validates email address using regex pattern.
     * @param email the email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
