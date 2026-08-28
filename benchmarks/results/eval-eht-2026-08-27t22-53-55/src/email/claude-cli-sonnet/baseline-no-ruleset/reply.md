Uses a practical regex that covers standard email formats (not full RFC 5322 compliance, which is impractically complex for real-world validation).

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
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] testCases = {
            "user@example.com",
            "first.last+tag@sub.example.co.uk",
            "invalid@",
            "@missinglocal.com",
            "no-at-sign.com",
            "spaces not@allowed.com"
        };

        for (String email : testCases) {
            System.out.println(email + " -> " + isValid(email));
        }
    }
}
```

Assumptions:
- Validates format only, not deliverability (no DNS/MX lookup or SMTP check).
- Requires a TLD of at least 2 letters.
- Rejects whitespace, missing `@`, or missing domain/local part.