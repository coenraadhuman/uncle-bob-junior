Assumptions: syntactic validation only (RFC 5322-style, not full spec, no DNS/MX lookup), max length capped at 254 per RFC 5321, `null`/blank input is invalid rather than an error.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String candidateEmail) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            return false;
        }
        if (candidateEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(candidateEmail).matches();
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "participant@example.com",
        "first.last@sub.example.co.uk",
        "user+tag@example.org"
    })
    void acceptsWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "no-at-sign.example.com",
        "missing-domain@",
        "@missing-local.com",
        "double@@example.com",
        "trailing.dot@example.com."
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String tooLongLocalPart = "a".repeat(255);
        assertFalse(EmailValidator.isValid(tooLongLocalPart + "@example.com"));
    }
}
```

`EmailValidator.isValid()` does one job, uses a named constant for the length limit, guard clauses handle null/blank/too-long first, and tests cover happy path plus empty, malformed, null, and boundary-length edges. Safe to change because the regex and length rule are isolated and fully covered.