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
public class EmailValidatorTest {
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }

    @Test
    void acceptsEmailWithSubdomain() {
        assertTrue(EmailValidator.isValid("user@mail.example.co.uk"));
    }

    @Test
    void acceptsEmailWithPlus() {
        assertTrue(EmailValidator.isValid("user+tag@example.com"));
    }

    @Test
    void rejectsEmailWithoutAtSymbol() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    void rejectsEmailWithoutDomain() {
        assertFalse(EmailValidator.isValid("user@"));
    }

    @Test
    void rejectsEmailWithoutTopLevelDomain() {
        assertFalse(EmailValidator.isValid("user@example"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlankString() {
        assertFalse(EmailValidator.isValid("   "));
    }
}
```

**Assumptions:** Pragmatic RFC 5322-adjacent validation (handles most real-world cases); null and blank inputs rejected with guard clause; pattern accepts alphanumerics, plus, dot, and hyphen in local part; at least one dot required in domain with 2+ character TLD. For stricter validation, consider the `email-validator` library or send confirmation emails.