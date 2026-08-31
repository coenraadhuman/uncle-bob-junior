I'll provide a practical email validation method with tests. I'm assuming you want something suitable for business use (realistic, not RFC 5322 perfection) that validates common email formats.

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

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("coenraad.human@postcodeloterij.nl"));
    }

    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("plaintext"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }

    @Test
    void nullAndBlank() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
```

**Assumptions:** This uses a regex pattern suitable for most business applications—it validates the basic structure (local-part@domain.tld) without attempting full RFC 5322 compliance, which is complex and rarely needed for user registration. The pattern rejects spaces, requires a valid domain with at least one dot, and accepts common special characters (+, -, _) in the local part. For stricter validation (confirming the address actually receives mail), consider adding verification via a confirmation email.