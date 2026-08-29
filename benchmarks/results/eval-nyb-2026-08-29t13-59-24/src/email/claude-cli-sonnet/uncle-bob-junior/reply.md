Assumptions: this validates syntactic format only (not deliverability or DNS/MX checks), uses a simplified pattern rather than the full RFC 5322 grammar (which is impractical to match with regex), and treats `null`/blank input as invalid rather than throwing.

```java
import java.util.regex.Pattern;

/**
 * Validates the syntactic form of an email address.
 *
 * <p>This does not verify that the address exists or is deliverable;
 * it only checks the string against a simplified, widely-used pattern
 * and the RFC 5321 maximum length.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "participant.001@example.com",
        "user+tag@example.co.uk",
        "first.last@sub.example.org",
        "user_name@example-domain.com"
    })
    void acceptsValidAddresses(String candidateEmail) {
        assertTrue(EmailValidator.isValid(candidateEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "plainaddress",
        "@example.com",
        "user@",
        "user@.com",
        "user@example",
        "user name@example.com",
        "user@@example.com",
        "user@example..com"
    })
    void rejectsInvalidAddresses(String candidateEmail) {
        assertFalse(EmailValidator.isValid(candidateEmail));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsNullOrBlank(String candidateEmail) {
        assertFalse(EmailValidator.isValid(candidateEmail));
    }

    @Test
    void acceptsAddressAtMaxLength() {
        String localPart = "a".repeat(242);
        String maxLengthEmail = localPart + "@example.com";
        assertTrue(maxLengthEmail.length() == 254);
        assertTrue(EmailValidator.isValid(maxLengthEmail));
    }

    @Test
    void rejectsAddressExceedingMaxLength() {
        String localPart = "a".repeat(243);
        String tooLongEmail = localPart + "@example.com";
        assertFalse(EmailValidator.isValid(tooLongEmail));
    }
}
```

`[code] → cleaned: single-purpose static validator with named `MAX_EMAIL_LENGTH` constant and no magic values, safe to change because behavior is pinned by tests covering valid/invalid formats and the length boundary.`