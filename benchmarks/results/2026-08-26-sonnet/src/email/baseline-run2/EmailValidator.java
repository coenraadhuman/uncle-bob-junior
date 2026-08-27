import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322-inspired pattern: local-part@domain
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+" +
        "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?" +
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)+$"
    );

    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_EMAIL_LENGTH = 254;

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }
}
