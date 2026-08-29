```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

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
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("test_123@subdomain.example.com"));
    }

    @Test
    void rejectsInvalidFormats() {
        assertFalse(EmailValidator.isValidEmail("plaintext"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void trims whitespace() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
```

**Assumptions:** The regex validates common email formats without claiming full RFC 5322 compliance (which is impractical with regex alone). For production systems handling regulated participant data, consider wrapping this with a confirmation email step or using a library like Jakarta Mail's `InternetAddress` for stricter validation.