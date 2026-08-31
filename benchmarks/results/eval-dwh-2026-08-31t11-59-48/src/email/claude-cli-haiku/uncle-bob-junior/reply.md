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

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("user@mail.example.com"));
        assertTrue(EmailValidator.isValidEmail("user+tag@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@example.co.uk"));
    }
    
    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
    }
    
    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
```

**Assumptions:** Guard clause handles null/blank upfront; regex validates format against common patterns (not RFC 5322 compliant, but sufficient for most real-world use). **For production code:** consider Jakarta Mail's `InternetAddress` or Hibernate Validator's `@Email` annotation for more robust validation.