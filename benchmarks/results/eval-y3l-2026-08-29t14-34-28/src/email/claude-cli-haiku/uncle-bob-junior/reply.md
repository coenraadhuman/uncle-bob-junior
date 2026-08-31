I'll provide a practical email validator with comprehensive tests.

**Assumptions:**
- Basic format validation (local-part@domain.tld)
- Pattern covers common cases; for RFC 5321 compliance or complex requirements in production, use Apache Commons Validator
- Returns `false` for null/blank input

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

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EmailValidatorTest {
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }

    @Test
    void acceptsComplexLocalPart() {
        assertTrue(EmailValidator.isValid("user.name+tag@example.co.uk"));
    }

    @Test
    void rejectsEmailWithoutAtSign() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    void rejectsIncompleteEmail() {
        assertFalse(EmailValidator.isValid("user@"));
        assertFalse(EmailValidator.isValid("@example.com"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid(""));
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsInvalidFormat() {
        assertFalse(EmailValidator.isValid("user @example.com"));
        assertFalse(EmailValidator.isValid("user@example"));
    }
}
```

Single method, single pattern compiled once, guard clause on entry, tests cover the happy path and edges (null, blank, missing parts, spaces, invalid format).