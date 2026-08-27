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
    
    @Test
    public void validEmailsShouldPass() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
    }
    
    @Test
    public void invalidEmailsShouldFail() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@nodomain.com"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }
}
```

**Assumptions:**
- Validates basic RFC-like format with local part, `@`, domain, and TLD
- Rejects null and blank strings with early guard clause
- Uses regex for simplicity without external dependencies
- For stricter validation (RFC 5322 compliance), consider Jakarta Mail's `InternetAddress` class or the `EmailValidator` from Apache Commons