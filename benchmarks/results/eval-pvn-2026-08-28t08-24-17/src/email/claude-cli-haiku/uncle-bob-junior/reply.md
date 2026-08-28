```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern COMPILED_PATTERN = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return COMPILED_PATTERN.matcher(email).matches();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }

    @Test
    void acceptsEmailWithSubdomain() {
        assertTrue(EmailValidator.isValidEmail("user@mail.example.com"));
    }

    @Test
    void acceptsEmailWithPlus() {
        assertTrue(EmailValidator.isValidEmail("user+tag@example.com"));
    }

    @Test
    void acceptsEmailWithDot() {
        assertTrue(EmailValidator.isValidEmail("first.last@example.com"));
    }

    @Test
    void rejectsNullEmail() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsBlankEmail() {
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void rejectsEmailWithoutAtSign() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
    }

    @Test
    void rejectsEmailWithoutDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
    }

    @Test
    void rejectsEmailWithoutTld() {
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }

    @Test
    void rejectsEmailWithSpaces() {
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
        assertFalse(EmailValidator.isValidEmail("user@ example.com"));
    }

    @Test
    void rejectsEmailWithMultipleAtSigns() {
        assertFalse(EmailValidator.isValidEmail("user@mail@example.com"));
    }
}
```

**Assumptions:** Practical regex validation suitable for front-end checks; handles common formats (alphanumeric, plus, underscore, dot, hyphen); rejects null/blank, missing parts, and spaces. Note: true RFC-compliant validation requires sending a confirmation message—this provides lightweight input validation.