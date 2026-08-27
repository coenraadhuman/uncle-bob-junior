import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322-inspired pattern: local-part@domain with at least one dot in the domain.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+" +          // local part
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" + // first domain label
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$" // subsequent labels (needs >=1)
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 4.5.3.1.3

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] samples = {
            "user@example.com",
            "user.name+tag@example.co.uk",
            "invalid@",
            "@missinglocal.com",
            "no-at-sign.com",
            "user@localhost", // no dot in domain -> rejected by this pattern
        };
        for (String s : samples) {
            System.out.println(s + " -> " + isValid(s));
        }
    }
}
