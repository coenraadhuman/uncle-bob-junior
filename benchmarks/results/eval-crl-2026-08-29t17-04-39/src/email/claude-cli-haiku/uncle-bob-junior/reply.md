I'll provide a practical email validation method that handles common cases without attempting to parse the full RFC 5322 specification.

```java
public class EmailValidator {
    private static final int MIN_LOCAL_PART_LENGTH = 1;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        String trimmed = email.trim();
        if (!PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        String localPart = trimmed.substring(0, atIndex);
        
        if (localPart.length() < MIN_LOCAL_PART_LENGTH) {
            return false;
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }

        if (localPart.contains("..")) {
            return false;
        }

        return true;
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
        assertTrue(EmailValidator.isValidEmail("john.doe+filter@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_email@domain.org"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void rejectsNoAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
    }

    @Test
    void rejectsMultipleAtSymbols() {
        assertFalse(EmailValidator.isValidEmail("user@exam@ple.com"));
    }

    @Test
    void rejectsNoLocalPart() {
        assertFalse(EmailValidator.isValidEmail("@example.com"));
    }

    @Test
    void rejectsNoDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
    }

    @Test
    void rejectsNoTopLevelDomain() {
        assertFalse(EmailValidator.isValidEmail("user@domain"));
    }

    @Test
    void rejectsConsecutiveDots() {
        assertFalse(EmailValidator.isValidEmail("user..name@example.com"));
    }

    @Test
    void rejectsLeadingOrTrailingDots() {
        assertFalse(EmailValidator.isValidEmail(".user@example.com"));
        assertFalse(EmailValidator.isValidEmail("user.@example.com"));
    }

    @Test
    void rejectsExcessiveLength() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValidEmail(longEmail));
    }

    @Test
    void acceptsLeadingAndTrailingWhitespace() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
```

**Assumptions:**
- Uses a simplified pattern suitable for most applications (not full RFC 5322).
- Validates format only; does not verify domain existence.
- Max length of 254 characters per RFC 5321.
- Accepts common special characters in the local part (+, ., %, -).
- Requires at least one character before @ and a valid top-level domain.

Cleaned: Single responsibility (validation logic in one method), intent-revealing names (isValidEmail, EMAIL_PATTERN), guard clauses for error cases, named constants for all magic values, comprehensive test coverage for happy path and boundaries.