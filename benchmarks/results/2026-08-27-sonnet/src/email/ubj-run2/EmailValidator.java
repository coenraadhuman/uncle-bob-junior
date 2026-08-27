import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    // Local part: letters, digits, and common symbols, dot-separated (no leading/trailing/double dots).
    // Domain: labels of letters/digits/hyphen, dot-separated, final label at least two letters.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    /**
     * Checks whether {@code email} is a syntactically valid email address.
     *
     * <p>This performs format validation only: it does not verify that the
     * domain exists or that the mailbox can receive mail.
     *
     * @param email the candidate address; may be {@code null}
     * @return {@code true} if {@code email} is well-formed, {@code false} otherwise
     */
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
