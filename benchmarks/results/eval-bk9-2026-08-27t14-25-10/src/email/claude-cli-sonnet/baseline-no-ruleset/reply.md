Assumptions: syntactic validation only (not deliverability), using a practical regex (not full RFC 5322), max length 254 chars per RFC 5321, and returning a boolean with a null/blank check.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical pattern: local-part@domain, domain has at least one dot,
    // and a TLD of at least two letters. Not full RFC 5322, but covers
    // the vast majority of real-world addresses without pathological
    // backtracking.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 limit

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
```

Usage:

```java
boolean ok = EmailValidator.isValid("participant_001@example.com");
```

Note: for stricter RFC 5322 compliance or deliverability checks (MX lookup, disposable-domain detection), use a maintained library such as Apache Commons Validator's `EmailValidator` or `javax.mail.internet.InternetAddress` rather than a hand-rolled regex.