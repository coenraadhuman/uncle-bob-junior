public class EmailValidator {
    // Simple regex pattern for basic email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates an email address using a regex pattern.
     * Allows: alphanumerics, dots, hyphens, underscores, plus signs
     * Rejects: spaces, invalid characters, missing @, missing domain
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
