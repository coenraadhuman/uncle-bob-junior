Assumptions: a pragmatic RFC 5322-inspired regex (not full spec compliance), max length check per RFC 5321 (254 chars), and null-safe handling.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
```

Usage: `EmailValidator.isValid("participant_001@example.com")`.