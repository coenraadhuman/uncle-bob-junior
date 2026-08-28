Assumption: using a standard regex-based check (RFC-5322-lite) is sufficient; not attempting full RFC 5322 compliance since that's impractical with regex alone.

```java
import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static void main(String[] args) {
        String[] tests = {
            "user@example.com",
            "user.name+tag@example.co.uk",
            "invalid-email",
            "missing@domain",
            "@nouser.com",
            "user@.com"
        };

        for (String test : tests) {
            System.out.println(test + " -> " + isValid(test));
        }
    }
}
```