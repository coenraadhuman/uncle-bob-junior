public class EmailValidator {
    
    /**
     * Validates an email address using a regex pattern.
     * Handles most common email formats while avoiding overly complex RFC 5322 compliance.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(pattern);
    }
    
    /**
     * More permissive validation allowing additional special characters.
     */
    public static boolean isValidEmailLenient(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String pattern = "^[A-Za-z0-9+_'.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(pattern);
    }
}
