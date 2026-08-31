```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public static boolean isValid(String email) {
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
    void validBasicEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }
    
    @Test
    void validEmailWithPlus() {
        assertTrue(EmailValidator.isValid("user+tag@example.com"));
    }
    
    @Test
    void validEmailWithDot() {
        assertTrue(EmailValidator.isValid("first.last@example.com"));
    }
    
    @Test
    void validEmailWithSubdomain() {
        assertTrue(EmailValidator.isValid("user@mail.example.co.uk"));
    }
    
    @Test
    void nullEmail() {
        assertFalse(EmailValidator.isValid(null));
    }
    
    @Test
    void blankEmail() {
        assertFalse(EmailValidator.isValid("   "));
    }
    
    @Test
    void missingAtSign() {
        assertFalse(EmailValidator.isValid("user.example.com"));
    }
    
    @Test
    void missingLocalPart() {
        assertFalse(EmailValidator.isValid("@example.com"));
    }
    
    @Test
    void missingDomain() {
        assertFalse(EmailValidator.isValid("user@"));
    }
    
    @Test
    void missingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("user@example"));
    }
    
    @Test
    void invalidCharactersInLocalPart() {
        assertFalse(EmailValidator.isValid("user#name@example.com"));
    }
}
```

Cleaned: Pattern extracted to named constant, null/blank handled with guard clause before regex matching. Safe to change because: the regex pattern is explicit and testable, tests cover valid formats (basic, plus addressing, dots, subdomains) and invalid cases (missing parts, wrong characters).

**Assumption:** This validates basic email format (practical for most use cases), not full RFC 5321/5322 compliance. For production systems requiring strict compliance or delivery verification, use a dedicated library like Jakarta Bean Validation or Apache Commons Validator.