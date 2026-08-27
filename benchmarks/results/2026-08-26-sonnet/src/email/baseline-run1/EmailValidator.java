import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    // Practical, widely-used pattern covering standard email formats
    // (not full RFC 5322, which is impractical to enforce with regex alone).
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
