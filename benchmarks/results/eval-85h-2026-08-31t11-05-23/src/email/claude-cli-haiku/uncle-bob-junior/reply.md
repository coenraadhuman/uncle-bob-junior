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
  void validEmailsPass() {
    assertTrue(EmailValidator.isValid("user@example.com"));
    assertTrue(EmailValidator.isValid("coenraad.human@postcodeloterij.nl"));
    assertTrue(EmailValidator.isValid("john+tag@domain.co.uk"));
    assertTrue(EmailValidator.isValid("a@b.co"));
  }

  @Test
  void invalidEmailsFail() {
    assertFalse(EmailValidator.isValid(null));
    assertFalse(EmailValidator.isValid(""));
    assertFalse(EmailValidator.isValid("   "));
    assertFalse(EmailValidator.isValid("notanemail"));
    assertFalse(EmailValidator.isValid("user@"));
    assertFalse(EmailValidator.isValid("@example.com"));
    assertFalse(EmailValidator.isValid("user @example.com"));
    assertFalse(EmailValidator.isValid("user@example"));
  }

  @Test
  void edgeCases() {
    assertFalse(EmailValidator.isValid("user..name@example.com"));
    assertTrue(EmailValidator.isValid("user_name@example.com"));
    assertTrue(EmailValidator.isValid("user-name@example-domain.com"));
  }
}
```

**Assumptions & notes:**
- Validates format only (not whether the address exists). Real-world systems should send a confirmation email.
- Pattern allows alphanumeric, dots, hyphens, and underscores in the local part; covers most practical cases without RFC 5322 complexity.
- For stricter validation, consider `jakarta.mail.internet.InternetAddress` (requires a dependency).

Cleaned: One-job validator with guard clause and named constant for the pattern; tests cover happy path, null/blank, missing parts, and edge cases.