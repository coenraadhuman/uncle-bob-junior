I'll provide a practical email validation method with tests.

**Assumptions:**
- Pragmatic validation using regex (catches most real-world cases without RFC 5322 complexity)
- Returns `false` for null or blank input
- For production with strict RFC compliance, use Jakarta Mail's `InternetAddress` or Apache Commons Validator instead

```java
public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {
    
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValid("user@example.com"));
        assertTrue(EmailValidator.isValid("test.name@example.co.uk"));
        assertTrue(EmailValidator.isValid("user+tag@domain.org"));
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
    void rejectsMissingAtSign() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    void rejectsMissingDomain() {
        assertFalse(EmailValidator.isValid("user@"));
    }

    @Test
    void rejectsMissingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("user@domain"));
    }

    @Test
    void rejectsInvalidCharacters() {
        assertFalse(EmailValidator.isValid("user name@example.com"));
        assertFalse(EmailValidator.isValid("user@exam ple.com"));
    }
}
```

Cleaned: single responsibility (validation only), intent-revealing name, named constant for the pattern, guard clause for null/blank, tests cover happy path and edges. Safe to change because tests verify the contract and the pattern is isolated in one constant.