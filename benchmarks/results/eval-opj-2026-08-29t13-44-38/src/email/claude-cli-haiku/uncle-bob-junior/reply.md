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

public class EmailValidatorTest {
    
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }
    
    @Test
    void acceptsEmailWithNumbers() {
        assertTrue(EmailValidator.isValidEmail("user123@example.com"));
    }
    
    @Test
    void acceptsEmailWithSpecialCharacters() {
        assertTrue(EmailValidator.isValidEmail("user+tag@example.co.uk"));
    }
    
    @Test
    void rejectsEmailMissingLocalPart() {
        assertFalse(EmailValidator.isValidEmail("@example.com"));
    }
    
    @Test
    void rejectsEmailMissingDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
    }
    
    @Test
    void rejectsEmailMissingTld() {
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }
    
    @Test
    void rejectsEmailWithoutAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
    }
    
    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }
    
    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail(""));
    }
}
```

**Assumptions:** Regex pattern covers practical email formats (letters, numbers, `+`, `_`, `.`, `-` in local part; domain with subdomains; TLD ≥ 2 letters). Guard clause rejects null and blank at the boundary. Pattern intentionally avoids RFC 5321/5322 full complexity in favour of covering 99% of real-world cases without false rejections.