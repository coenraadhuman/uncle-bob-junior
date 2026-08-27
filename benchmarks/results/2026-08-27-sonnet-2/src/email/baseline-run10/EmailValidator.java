import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical approximation of RFC 5322, not a full implementation.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 limit

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
            "first.last+tag@sub.example.co.uk",
            "invalid@",
            "@invalid.com",
            "invalid.com",
            "user@@example.com",
            ""
        };
        for (String s : samples) {
            System.out.println(s + " -> " + isValid(s));
        }
    }
}
