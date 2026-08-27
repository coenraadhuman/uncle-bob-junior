public class EmailValidator {
    /**
     * Validates email address format using regex pattern.
     * Pattern allows: letters, numbers, dots, hyphens, underscores in local part,
     * and standard domain naming conventions.
     */
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
