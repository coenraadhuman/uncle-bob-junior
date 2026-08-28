import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical subset of RFC 5322: dot-atom local part, domain labels
    // separated by dots, TLD of at least two letters. Rejects edge cases
    // like quoted local parts and IP-literal domains, which are rarely
    // legitimate in participant-facing systems.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
                    + "[A-Za-z]{2,}$"
    );

    // RFC 5321 limits: 64 chars for the local part, 254 for the whole address
    private static final int MAX_TOTAL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > MAX_TOTAL_LENGTH) {
            return false;
        }
        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex < 1 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
