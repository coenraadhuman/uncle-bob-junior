Assumptions: practical (not full RFC 5322) validation via regex, since PLG's real email checks for participant/marketing data should ultimately be verified by your platform's mailer or an existing library — this is a reasonable general-purpose guard, not a compliance control. Max length capped at 254 per RFC 5321. No external dependencies used.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
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
        "user+tag@example.org",
        "user_name-123@example-domain.com"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "no-at-sign.example.com",
        "user@",
        "@example.com",
        "user@example",
        "user@.com",
        "user name@example.com"
    })
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsEmailLongerThanMaxLength() {
        String longLocalPart = "a".repeat(250);
        String tooLongEmail = longLocalPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLongEmail));
    }
}
```

`EmailValidator.isValid()` does one job with a guard clause for null/blank input and a named `MAX_EMAIL_LENGTH` constant, safe to change because the regex and length rule are isolated and covered by tests for both acceptance and rejection paths.