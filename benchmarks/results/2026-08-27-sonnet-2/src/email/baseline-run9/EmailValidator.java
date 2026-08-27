import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical email pattern: local-part@domain, domain requires at least one dot.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_TOTAL_LENGTH = 254;

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_TOTAL_LENGTH) {
            return false;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] testEmails = {
            "user@example.com",
            "user.name+tag@example.co.uk",
            "invalid@",
            "@invalid.com",
            "no-at-sign.com",
            "user@domain",
            "a@b.co"
        };

        for (String e : testEmails) {
            System.out.println(e + " -> " + isValid(e));
        }
    }
}
