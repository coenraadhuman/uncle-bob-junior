import java.util.regex.Pattern;

/**
 * Validates the syntactic form of an email address.
 *
 * <p>This does not verify that the address exists or is deliverable;
 * it only checks the string against a simplified, widely-used pattern
 * and the RFC 5321 maximum length.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private EmailValidator() {
    }

    public static boolean isValid(String candidateEmail) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            return false;
        }
        if (candidateEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(candidateEmail).matches();
    }
}
