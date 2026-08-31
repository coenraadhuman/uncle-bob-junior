Using a regex-based approach with `java.util.regex`, covering standard email format (local-part@domain with TLD).

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
}
```

Assumptions made:
- Validates general structural correctness (local-part@domain.tld), not full RFC 5322 compliance (which is far more permissive and complex).
- Requires a TLD of at least 2 letters.
- Does not perform DNS/MX record lookups to confirm the domain actually exists.
- Rejects `null` and blank strings up front.