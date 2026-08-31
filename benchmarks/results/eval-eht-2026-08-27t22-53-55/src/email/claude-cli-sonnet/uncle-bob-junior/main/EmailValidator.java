import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    /**
     * Checks whether the given candidate is a syntactically valid email address.
     *
     * @param candidateEmail the string to validate, may be null
     * @return true if candidateEmail is non-null, non-blank, at most
     *         {@value #MAX_EMAIL_LENGTH} characters, and matches a practical
     *         email syntax pattern
     */
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
