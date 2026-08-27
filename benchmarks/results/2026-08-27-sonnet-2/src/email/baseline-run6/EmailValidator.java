import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    // Practical email pattern: local-part@domain, local-part allows common
    // characters and dot-separated segments; domain requires at least one dot
    // and a valid TLD.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_'-]+(\\.[A-Za-z0-9+_'-]+)*"
        + "@"
        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

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
            "@missinglocal.com",
            "no-at-symbol.com",
            "user@domain",
            "user@domain..com"
        };
        for (String s : samples) {
            System.out.println(s + " -> " + isValid(s));
        }
    }
}
