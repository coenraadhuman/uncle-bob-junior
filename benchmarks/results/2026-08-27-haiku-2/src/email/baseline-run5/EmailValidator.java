public class EmailValidator {
    
    // Simple regex pattern for basic email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates an email address using regex pattern matching.
     * Checks for basic format: local-part@domain.extension
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
    
    /**
     * More strict validation with additional checks
     */
    public static boolean isValidEmailStrict(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim();
        
        // Check basic pattern
        if (!email.matches(EMAIL_PATTERN)) {
            return false;
        }
        
        // Check length constraints
        if (email.length() > 254) {
            return false;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        // Local part shouldn't exceed 64 characters
        if (localPart.length() > 64) {
            return false;
        }
        
        // No consecutive dots allowed
        if (email.contains("..")) {
            return false;
        }
        
        // Shouldn't start or end with dot
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }
        
        // Domain should have at least one dot
        if (!domain.contains(".")) {
            return false;
        }
        
        return true;
    }
}
