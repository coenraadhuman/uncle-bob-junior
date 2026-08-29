```java
public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
```

**Assumptions:**
- Accepts standard email formats with alphanumeric characters, plus signs, underscores, dots, and hyphens
- Requires a domain with at least one dot and a 2+ character TLD
- Returns `false` for null or empty strings

**Alternative (more robust, using built-in Java mail utilities):**

```java
import javax.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            new InternetAddress(email);
            return email.contains("@");
        } catch (Exception e) {
            return false;
        }
    }
}
```

The regex approach is faster and suitable for most use cases. The `InternetAddress` approach is more RFC-compliant but requires the `javax.mail` library (included in many Java distributions but not guaranteed).