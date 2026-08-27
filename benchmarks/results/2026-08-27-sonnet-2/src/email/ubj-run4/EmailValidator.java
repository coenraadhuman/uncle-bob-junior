import java.util.regex.Pattern;

/**
 * Validates email address syntax for general-purpose use (e.g. sign-up forms).
 * Not a full RFC 5322 parser; deliberately practical rather than exhaustive.
 */
public final class EmailValidator {

    // RFC 5321 4.5.3.1.3: max length of an email address.
    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
            + "@"
            + "(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
