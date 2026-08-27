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
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
```

```java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("first+last@domain.org"));
        assertTrue(EmailValidator.isValidEmail("test_user@example.com"));
    }

    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }

    @Test
    void nullAndBlank() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void whitespaceHandling() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
```

**Assumptions:**
- Pattern matches practical email formats (not strict RFC 5322 compliance, which is rarely needed).
- Rejects null, empty, and whitespace-only inputs.
- Trims leading/trailing whitespace before validation.
- Pattern compiled as static final to avoid recompilation on each call.