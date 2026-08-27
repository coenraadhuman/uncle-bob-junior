import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical pattern: dot-separated atoms in the local part (no leading/trailing/double dots),
    // dot-separated domain labels that don't start or end with a hyphen, alphabetic TLD of 2+ chars.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+"
            + "[A-Za-z]{2,63}$");

    private static final int MAX_TOTAL_LENGTH = 254; // RFC 5321 practical limit
    private static final int MAX_LOCAL_LENGTH = 64;  // RFC 5321 local-part limit

    private EmailValidator() {
    }

    /**
     * Validates an email address for typical application use (e.g. registration forms).
     *
     * @param email the address to validate; may be null
     * @return true if the address is structurally valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_TOTAL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
