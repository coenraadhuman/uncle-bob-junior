```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
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

class EmailValidatorTest {
    
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("user_name@example.io"));
    }
    
    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("no-at-sign.com"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user@domain.c"));
    }
}
```

**Assumptions:**
- Format validation only (no DNS lookup or SMTP verification).
- Local part allows alphanumeric, dots, plus, hyphen, underscore, and percent; domain requires at least one dot and a 2+ letter TLD.
- For production, prefer **Jakarta Validation** (`@Email` annotation on fields) or **Apache Commons Validator** (`EmailValidator.getInstance().isValid(email)`) — they're maintained and handle edge cases beyond this regex.