import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322 subset: dot-separated atoms in the local part,
    // dot-separated alphanumeric labels (hyphens allowed internally) in the
    // domain, ending in a TLD of at least two letters.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
          + "@"
          + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
          + "[A-Za-z]{2,63}$"
    );

    private static final int MAX_EMAIL_LENGTH = 254;  // RFC 5321 overall limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    private EmailValidator() {
    }

    /**
     * Validates the syntax of an email address.
     *
     * @param email the address to validate; may be null
     * @return true if the address is syntactically valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
