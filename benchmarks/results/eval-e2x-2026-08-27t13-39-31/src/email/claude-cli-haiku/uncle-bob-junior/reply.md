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
public class EmailValidatorTest {
    private static final EmailValidator validator = new EmailValidator();

    @Test
    public void acceptsValidEmails() {
        assertTrue(validator.isValidEmail("user@example.com"));
        assertTrue(validator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(validator.isValidEmail("test+tag@domain.org"));
    }

    @Test
    public void rejectsInvalidEmails() {
        assertFalse(validator.isValidEmail(null));
        assertFalse(validator.isValidEmail(""));
        assertFalse(validator.isValidEmail("notanemail"));
        assertFalse(validator.isValidEmail("user@"));
        assertFalse(validator.isValidEmail("@example.com"));
        assertFalse(validator.isValidEmail("user @example.com"));
    }

    @Test
    public void rejectsEmailsWithoutTopLevelDomain() {
        assertFalse(validator.isValidEmail("user@localhost"));
    }
}
```

**Assumptions:** Regex-based validation for practical use; accepts common formats (alphanumeric, dots, hyphens, plus signs). If you need strict RFC 5322 compliance or expect exotic domains, use `InternetAddress` from `javax.mail` instead:

```java
import javax.mail.internet.InternetAddress;

public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
        return false;
    }
    try {
        new InternetAddress(email).validate();
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

The regex approach is faster and has no external dependencies; the `InternetAddress` approach is more RFC-compliant but requires the `javax.mail` library.