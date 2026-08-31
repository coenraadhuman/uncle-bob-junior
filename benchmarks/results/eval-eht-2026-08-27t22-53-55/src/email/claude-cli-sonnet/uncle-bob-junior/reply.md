Assumptions: no external validation library (e.g. Commons Validator, jakarta.mail) is confirmed available in the project, so I used `java.util.regex` from the JDK standard library with a practical RFC 5322-style pattern (not full RFC compliance, which needs a parser, not a regex). Enforced RFC 5321's 254-char max length. Test data uses synthetic addresses only.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    /**
     * Checks whether the given candidate is a syntactically valid email address.
     *
     * @param candidateEmail the string to validate, may be null
     * @return true if candidateEmail is non-null, non-blank, at most
     *         {@value #MAX_EMAIL_LENGTH} characters, and matches a practical
     *         email syntax pattern
     */
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.com",
            "user_name@sub.example.com",
            "u@e.io"
    })
    void acceptsValidAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "plainaddress",
            "@example.com",
            "user@",
            "user@.com",
            "user@example",
            "user@@example.com",
            "user name@example.com",
            "user@ex ample.com"
    })
    void rejectsInvalidAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsAddressAtMaxLength() {
        String domain = "@example.com";
        String localPart = "a".repeat(254 - domain.length());
        String atMaxLength = localPart + domain;

        assertTrue(EmailValidator.isValid(atMaxLength));
    }
}
```

`EmailValidator.isValid()` does one job with guard clauses for null/blank/length before the regex check; safe to change because the pattern and length limit are named constants callers never need to touch.