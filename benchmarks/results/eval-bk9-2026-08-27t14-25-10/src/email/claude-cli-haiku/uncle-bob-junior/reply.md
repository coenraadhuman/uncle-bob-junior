I'll provide a practical email validation method using regex, plus tests. Here are my assumptions:

- **Covers RFC 5322 basics** but not every edge case (which are rare in practice)
- **Rejects empty/null** input
- **Allows common formats**: `user@example.com`, `user+tag@sub.domain.co.uk`
- **Rejects invalid patterns**: missing @, no domain, spaces, consecutive dots

```java
public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
```

**Tests:**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("info+tag@sub.domain.org"));
    }
    
    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("  "));
    }
    
    @Test
    void rejectsInvalidFormats() {
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user..name@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }
}
```

**For stricter validation**, use Apache Commons Validator (add dependency: `org.apache.commons:commons-validator3:3.4`):

```java
import org.apache.commons.validator.routines.EmailValidator;

public class RobustEmailValidator {
    private static final EmailValidator validator = EmailValidator.getInstance();
    
    public static boolean isValidEmail(String email) {
        return email != null && validator.isValid(email);
    }
}
```

The simple regex version handles ~99% of real-world cases; use the library if you need RFC 5322 compliance or allow international domains.