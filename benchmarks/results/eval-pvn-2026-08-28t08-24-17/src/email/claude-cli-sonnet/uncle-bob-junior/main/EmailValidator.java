import java.util.regex.Pattern;

/**
 * Validates email addresses against a practical (not fully RFC 5322)
 * pattern: local-part@domain.tld, no consecutive dots, TLD of 2+ letters.
 */
public final class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^(?!.*\\.\\.)[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(candidate).matches();
    }
}
