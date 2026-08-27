import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    // Practical RFC 5322 subset: dot-atom local part, domain labels of
    // letters/digits/hyphens (no leading or trailing hyphen), TLD of 2+ letters.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

    private EmailValidator() {
    }

    /**
     * Validates an email address syntactically.
     *
     * @param email the address to check, may be null
     * @return true if the address is structurally valid
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
