import java.util.regex.Pattern;

/**
 * Validates email addresses using a pragmatic subset of RFC 5321/5322.
 * Not a full RFC 5322 parser; rejects addresses with quoted strings,
 * comments, or IP-literal domains, which are rarely accepted in practice.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+"
            + "(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
            + "(\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*"
            + "\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (isBlank(email) || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
