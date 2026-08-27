I'll provide a practical email validation method with tests. Assumptions: Java 8+, validating format and basic structure (not DNS verification), and aiming for real-world accuracy over strict RFC compliance.

```java
public class EmailValidator {
  private static final String EMAIL_REGEX = 
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return email.matches(EMAIL_REGEX);
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
    assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
    assertTrue(EmailValidator.isValidEmail("a@b.co"));
  }

  @Test
  void rejectsInvalidEmails() {
    assertFalse(EmailValidator.isValidEmail(null));
    assertFalse(EmailValidator.isValidEmail(""));
    assertFalse(EmailValidator.isValidEmail("   "));
    assertFalse(EmailValidator.isValidEmail("plaintext"));
    assertFalse(EmailValidator.isValidEmail("@example.com"));
    assertFalse(EmailValidator.isValidEmail("user@"));
    assertFalse(EmailValidator.isValidEmail("user@.com"));
    assertFalse(EmailValidator.isValidEmail("user @example.com"));
    assertFalse(EmailValidator.isValidEmail("user@example"));
  }
}
```

**What it does:** Validates email format with a regex covering local part (alphanumeric, dots, plus signs, hyphens), `@` separator, and domain with extension. Guards against null and blank input first.

**Assumptions:** Format validation only (not checking if mailbox exists); allows `+` addressing common with Gmail; requires valid TLD.