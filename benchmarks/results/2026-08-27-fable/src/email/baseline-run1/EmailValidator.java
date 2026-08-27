import java.util.regex.Pattern;

public final class EmailValidator {

    // Pragmatic RFC 5322 subset (matches the HTML5 email input rule):
    // local part of allowed atom characters, then a domain of dot-separated
    // labels that start/end with an alphanumeric and may contain hyphens.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
            + "@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 overall limit
    private static final int MAX_LOCAL_PART_LENGTH = 64; // RFC 5321 local-part limit

    private EmailValidator() {
    }

    /**
     * Validates an email address for syntactic correctness.
     *
     * @param email the address to validate; may be null
     * @return true if the address is syntactically valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        String localPart = trimmed.substring(0, atIndex);
        if (localPart.startsWith(".") || localPart.endsWith(".") || localPart.contains("..")) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
