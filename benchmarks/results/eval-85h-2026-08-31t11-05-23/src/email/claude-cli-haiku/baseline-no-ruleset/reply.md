I'll provide a practical email validation solution using regex. This handles common cases without external dependencies.

**Assumptions:**
- Basic validation (format, domain structure) is sufficient
- No need for full RFC 5322 compliance (which is extremely complex)
- Compiled pattern for efficiency over repeated calls

```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
```

**Usage:**
```java
EmailValidator.isValidEmail("user@example.com");        // true
EmailValidator.isValidEmail("invalid.email@");          // false
EmailValidator.isValidEmail(null);                      // false
EmailValidator.isValidEmail("   ");                     // false
```

**Alternative (more robust):** If you need stricter validation, use Apache Commons Email:
```java
import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidator {
    private static final EmailValidator validator = EmailValidator.getInstance();
    
    public static boolean isValidEmail(String email) {
        return validator.isValid(email);
    }
}
```

The first approach is lightweight; the second is more thorough but adds a dependency. For most applications, the regex version above handles typical email formats well.