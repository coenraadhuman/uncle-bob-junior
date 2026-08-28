Assumptions: pragmatic RFC 5321-style validation (dot-atom local part, hostname domain with a TLD, length limits), not full RFC 5322 with quoted strings or IP literals — that covers real-world addresses without accepting garbage. If your project can take a dependency, Apache Commons Validator's `EmailValidator` is the better choice; this is the self-contained version.

```java
import java.util.regex.Pattern;

/**
 * Validates email addresses against a pragmatic subset of RFC 5321:
 * a dot-atom local part and a multi-label hostname domain.
 * Quoted local parts and IP-literal domains are deliberately rejected,
 * since they are almost never legitimate participant input.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;      // RFC 5321 path limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;  // RFC 5321 local-part limit
    private static final int MAX_DOMAIN_LENGTH = 253;

    private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*");

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}");

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1 || atIndex == email.length() - 1) {
            return false;
        }
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    private static boolean isValidLocalPart(String localPart) {
        return localPart.length() <= MAX_LOCAL_PART_LENGTH
                && LOCAL_PART_PATTERN.matcher(localPart).matches();
    }

    private static boolean isValidDomain(String domain) {
        return domain.length() <= MAX_DOMAIN_LENGTH
                && DOMAIN_PATTERN.matcher(domain).matches();
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
            "participant_001@example.com",
            "first.last@example.co.uk",
            "user+tag@sub.example.org",
            "x@example.nl",
            "o'brien@example.ie"
    })
    void acceptsWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "plainaddress",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@example",            // no TLD
            ".user@example.com",       // leading dot in local part
            "user.@example.com",       // trailing dot in local part
            "us..er@example.com",      // consecutive dots
            "user@-example.com",       // label starts with hyphen
            "user@example-.com",       // label ends with hyphen
            "user@example.c",          // single-character TLD
            "user name@example.com",   // space in local part
            "user@exa mple.com"
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsOverlongEmail() {
        String email = "a".repeat(250) + "@example.com"; // exceeds 254 total
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsOverlongLocalPart() {
        String email = "a".repeat(65) + "@example.com"; // exceeds 64-char local part
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void acceptsLocalPartAtExactLengthLimit() {
        String email = "a".repeat(64) + "@example.com";
        assertTrue(EmailValidator.isValid(email));
    }
}
```

Validation is split into `isValidLocalPart` and `isValidDomain` with named RFC limits, so tightening either side later is a one-place change; tests pin the happy path, structural rejects, and the length boundaries. If this feeds participant registration, treat it as format checking only — deliverability (e.g. a confirmation email) is the real proof an address works.