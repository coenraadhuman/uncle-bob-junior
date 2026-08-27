import java.util.regex.Pattern;

public final class EmailValidator {

    private EmailValidator() {
    }

    // Practical pattern: dot-atom local part, domain labels separated by dots,
    // TLD of at least two letters. Intentionally stricter than full RFC 5322.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
            + "[A-Za-z]{2,63}$");

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 practical limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    /**
     * Validates an email address for use in forms and data intake.
     *
     * @param email the address to validate, may be null
     * @return true if the address is structurally valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
