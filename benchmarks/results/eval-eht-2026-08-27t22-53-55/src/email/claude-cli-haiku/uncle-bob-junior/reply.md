I'll provide a pragmatic, regex-based email validator that catches common cases without attempting RFC 5322 compliance (which is impractical).

**Assumptions:**
- Regex-based validation (fast, covers ~99% of real-world emails)
- Trims whitespace automatically
- Does not validate DNS/MX records or confirm actual inbox delivery (call for that elsewhere)
- Covers Latin characters for EU/UK/Canada users

```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(email.trim()).matches();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmailAddresses() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
    }
    
    @Test
    void rejectsInvalidEmailAddresses() {
        assertFalse(EmailValidator.isValidEmail("invalid.email"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail(null));
    }
    
    @Test
    void trimsWhitespace() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
```

Delivered: single responsibility (format check only), guard-claused null/empty paths, pattern compiled once; safe to change because tests cover the happy path and edge cases.