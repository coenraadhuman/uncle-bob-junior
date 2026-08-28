**Assumptions:**
- Practical, "good enough" validation via regex — not full RFC 5322 compliance (that grammar is famously impractical to enforce with regex). For contact-critical flows, backing this with a confirmation email is still the only reliable check.
- `null`/blank input is invalid, not an error.
- Tests use JUnit 5.

```java
import java.util.regex.Pattern;

/**
 * Validates email addresses against a practical (not fully RFC 5322)
 * pattern: local-part@domain.tld, no consecutive dots, TLD of 2+ letters.
 */
public final class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^(?!.*\\.\\.)[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(candidate).matches();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "first.last@sub.example.co.uk",
        "user+tag@example.io"
    })
    void isValid_returnsTrue_forWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "no-at-sign.com",
        "user@",
        "@example.com",
        "user@example",
        "user@@example.com",
        "user@example..com"
    })
    void isValid_returnsFalse_forMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void isValid_returnsFalse_forBlankInput(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void isValid_returnsFalse_forNull() {
        assertFalse(EmailValidator.isValid(null));
    }
}
```

`EmailValidator.isValid()` → cleaned: single guard clause + one regex check, blank/null handled explicitly, doc comment states the regex's known limitation → safe to change because tests pin the exact valid/invalid boundary, so any future regex tweak that breaks a case fails loudly.