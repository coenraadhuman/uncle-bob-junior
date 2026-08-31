**Assumptions:**
- Java 8+
- Simple regex-based validation for common email formats (RFC 5322 simplified pattern)
- Rejection of null, empty, and whitespace-only inputs
- Testing with JUnit 5 (standard for modern Java)
- No external validation libraries required

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

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("name_123@test-domain.com"));
    }
    
    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("invalid.email"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user space@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@@example.com"));
    }
    
    @Test
    void nullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
```

**Cleaned:** Single-job validator with named constant, guard clause for empty/null, flat control flow. Safe to change because intent is clear and behavior is fully tested against common cases and edge cases.