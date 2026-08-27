import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical pattern: local-part @ domain-labels . tld
    // Not full RFC 5322 compliance, but rejects the vast majority of malformed input.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 max total length

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
