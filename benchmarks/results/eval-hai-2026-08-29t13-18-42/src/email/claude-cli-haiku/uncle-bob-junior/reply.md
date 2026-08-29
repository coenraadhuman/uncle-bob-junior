**Assumptions:**
- Basic email format validation (local-part@domain.tld)
- Accepts common formats including subdomains, plus addressing, and hyphens
- No external dependencies beyond Java standard library

```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    public static boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && EMAIL_PATTERN.matcher(email).matches();
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EmailValidatorTest {
    @Test
    void acceptsStandardEmail() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }
    
    @Test
    void acceptsEmailWithSubdomain() {
        assertTrue(EmailValidator.isValidEmail("user@mail.example.co.uk"));
    }
    
    @Test
    void acceptsEmailWithPlusAndHyphens() {
        assertTrue(EmailValidator.isValidEmail("first-last+tag@my-domain.com"));
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
    void rejectsEmailWithoutTopLevelDomain() {
        assertFalse(EmailValidator.isValidEmail("user@localhost"));
    }
    
    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }
    
    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
```

Pattern compiled once and reused across calls; null and blank handled upfront with guard clause. Tests cover happy path and key edge cases (missing parts, no TLD, null, whitespace).