import java.util.regex.Pattern;

/**
 * Validates email addresses using a practical (not full RFC 5322) syntax check.
 * Intended for input sanity-checking, not for confirming deliverability.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 limit

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (isBlank(email)) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
