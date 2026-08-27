# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical pattern: local-part @ domain-labels . tld
    // Not full RFC 5322 compliance, but rejects the vast majority of malformed input.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 max total length

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

### email · run 1 · uncle-bob-junior

````java
import java.util.regex.Pattern;

/**
 * Validates email addresses using a pragmatic subset of RFC 5321/5322.
 * Not a full RFC 5322 parser; rejects addresses with quoted strings,
 * comments, or IP-literal domains, which are rarely accepted in practice.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+"
            + "(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
            + "(\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*"
            + "\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (isBlank(email) || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "participant@example.com",
            "first.last@example.co.uk",
            "user+tag@sub.example.com",
            "USER@EXAMPLE.COM",
            "a@b.io"
    })
    void acceptsValidAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "plainaddress",
            "@example.com",
            "user@",
            "user@example",
            "user@.com",
            "user@@example.com",
            "user@exa mple.com",
            "user@example..com",
            ".user@example.com",
            "user.@example.com"
    })
    void rejectsInvalidAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }
}
````

### email · run 2 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322-inspired pattern: local-part@domain, with dot-atom support.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*"   // local part
        + "@"
        + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"           // domain
    );

    private static final int MAX_LENGTH = 254;

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

````java
EmailValidator.isValid("jane.doe@example.com");   // true
EmailValidator.isValid("invalid@@example.com");   // false
EmailValidator.isValid(null);                      // false
````

### email · run 2 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+"
            + "@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
            + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$"
    );

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
````

````java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "first.last@example.co.uk",
        "user+tag@example.com",
        "user_name@sub.example.com",
        "a@b.co"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "plainaddress",
        "@example.com",
        "user@",
        "user@@example.com",
        "user@example",
        "user@-example.com",
        "user@example..com",
        "user name@example.com",
        " user@example.com",
        "user@example.com "
    })
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsEmailOverMaxLength() {
        String longLocalPart = "a".repeat(250);
        assertFalse(EmailValidator.isValid(longLocalPart + "@example.com"));
    }
}
````

### email · run 3 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    // Practical RFC 5322 subset: local-part@domain, domain requires at least one dot
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+" +
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" +
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > MAX_LENGTH) {
            return false;
        }
        if (trimmed.startsWith(".") || trimmed.contains("..")) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
````

### email · run 3 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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
````

````java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.org",
            "user_name-123@sub.example.com"
    })
    void acceptsWellFormedEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "plainaddress",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@example",
            "user example@example.com",
            "user@example.c",
            "user@.com",
            "user@exa mple.com"
    })
    void rejectsMalformedEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsEmailLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsEmailAtMaxLength() {
        String domain = "@example.com";
        String localPart = "a".repeat(254 - domain.length());
        String maxLength = localPart + domain;

        assertTrue(EmailValidator.isValid(maxLength));
    }
}
````

### email · run 4 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical email pattern: local-part@domain, domain requires at least one dot.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_LENGTH = 254;

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

### email · run 4 · uncle-bob-junior

````java
import java.util.regex.Pattern;

/**
 * Validates email address syntax for general-purpose use (e.g. sign-up forms).
 * Not a full RFC 5322 parser; deliberately practical rather than exhaustive.
 */
public final class EmailValidator {

    // RFC 5321 4.5.3.1.3: max length of an email address.
    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
            + "@"
            + "(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

````java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "first.last@example.co.uk",
        "user+tag@example.com",
        "user_name@sub.example.com",
        "u@ab.io"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "no-at-sign.example.com",
        "two@at@signs.com",
        "missing-domain@",
        "@missing-local.com",
        "user@localhost",
        "user@example.c",
        "user@.com",
        "user name@example.com"
    })
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValidEmail(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsEmailLongerThanMaxLength() {
        String tooLong = "a".repeat(250) + "@b.co";
        assertFalse(EmailValidator.isValidEmail(tooLong));
    }
}
````

### email · run 5 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    // Practical email pattern: local-part@domain, domain requires at least one dot
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

### email · run 5 · uncle-bob-junior

````java
import java.util.regex.Pattern;

/**
 * Validates email addresses using a practical (not full RFC 5322) syntax check.
 * Intended for input sanity-checking, not for confirming deliverability.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 limit

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (isBlank(email)) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
````

````java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.org",
            "user_name@sub.example.com",
            "a@b.co"
    })
    void acceptsWellFormedEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@example.com",
            "user@",
            "user@example",
            "user@.com",
            "user example@example.com",
            "user@@example.com",
            "user@example..com"
    })
    void rejectsMalformedEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsEmailExceedingMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsEmailAtMaxLength() {
        String local = "a".repeat(242);
        String maxLength = local + "@example.com"; // exactly 254 chars

        assertTrue(EmailValidator.isValid(maxLength));
    }
}
````

### email · run 6 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    // Practical email pattern: local-part@domain, local-part allows common
    // characters and dot-separated segments; domain requires at least one dot
    // and a valid TLD.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_'-]+(\\.[A-Za-z0-9+_'-]+)*"
        + "@"
        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] samples = {
            "user@example.com",
            "first.last+tag@sub.example.co.uk",
            "invalid@",
            "@missinglocal.com",
            "no-at-symbol.com",
            "user@domain",
            "user@domain..com"
        };
        for (String s : samples) {
            System.out.println(s + " -> " + isValid(s));
        }
    }
}
````

### email · run 6 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

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
````

````java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValid("participant.001@example.com"));
    }

    @Test
    void acceptsAddressWithSubdomain() {
        assertTrue(EmailValidator.isValid("user@mail.example.co.uk"));
    }

    @Test
    void acceptsAddressWithPlusTag() {
        assertTrue(EmailValidator.isValid("user+newsletter@example.com"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValid("user.example.com"));
    }

    @Test
    void rejectsMissingDomainSuffix() {
        assertFalse(EmailValidator.isValid("user@example"));
    }

    @Test
    void rejectsDoubleAtSymbol() {
        assertFalse(EmailValidator.isValid("user@@example.com"));
    }

    @Test
    void rejectsEmbeddedSpaces() {
        assertFalse(EmailValidator.isValid("user name@example.com"));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        assertFalse(EmailValidator.isValid(localPart + "@example.com"));
    }
}
````

### email · run 7 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+" +
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" +
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

### email · run 7 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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
````

````java
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
        "user+tag@sub.example.com",
        "user_name@example-domain.com"
    })
    void acceptsWellFormedEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ",
        "user@",
        "@example.com",
        "user@@example.com",
        "user@example",
        "user example@example.com",
        "user@ example.com"
    })
    void rejectsMalformedOrBlankEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsEmailsLongerThanRfcLimit() {
        String tooLong = "a".repeat(250) + "@ex.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }
}
````

### email · run 8 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+" +
        "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" +
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        if (email.contains("..")) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

### email · run 8 · uncle-bob-junior

````java
package com.example.validation;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

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
````

````java
package com.example.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValid("participant_001@example.com"));
    }

    @Test
    void acceptsAddressWithPlusTagAndSubdomain() {
        assertTrue(EmailValidator.isValid("first.last+tag@mail.example.co.uk"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValid("participant.example.com"));
    }

    @Test
    void rejectsMissingDomain() {
        assertFalse(EmailValidator.isValid("participant@"));
    }

    @Test
    void rejectsMissingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("participant@example"));
    }

    @Test
    void rejectsMultipleAtSymbols() {
        assertFalse(EmailValidator.isValid("participant@@example.com"));
    }

    @Test
    void rejectsWhitespaceInsideAddress() {
        assertFalse(EmailValidator.isValid("participant @example.com"));
    }

    @Test
    void rejectsAddressExceedingMaxLength() {
        String tooLong = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }
}
````

### email · run 9 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical email pattern: local-part@domain, domain requires at least one dot.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_TOTAL_LENGTH = 254;

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_TOTAL_LENGTH) {
            return false;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] testEmails = {
            "user@example.com",
            "user.name+tag@example.co.uk",
            "invalid@",
            "@invalid.com",
            "no-at-sign.com",
            "user@domain",
            "a@b.co"
        };

        for (String e : testEmails) {
            System.out.println(e + " -> " + isValid(e));
        }
    }
}
````

### email · run 9 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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
````

````java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "coenraad.human@postcodeloterij.nl",
        "user+tag@example.co.uk",
        "first.last@sub.domain.com",
        "user_name-123@example.io"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "   ",
        "plainaddress",
        "@missinglocal.com",
        "missingdomain@",
        "double@@example.com",
        "no-tld@example",
        "space in@example.com",
        "trailing.dot@example.com."
    })
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @org.junit.jupiter.api.Test
    void rejectsEmailLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }
}
````

### email · run 10 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical approximation of RFC 5322, not a full implementation.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 limit

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] samples = {
            "user@example.com",
            "first.last+tag@sub.example.co.uk",
            "invalid@",
            "@invalid.com",
            "invalid.com",
            "user@@example.com",
            ""
        };
        for (String s : samples) {
            System.out.println(s + " -> " + isValid(s));
        }
    }
}
````

### email · run 10 · uncle-bob-junior

````java
package com.plg.validation;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    // Simple, widely-used syntactic check: local-part@domain.tld
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
````

````java
package com.plg.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.io",
            "user_name@sub.example.com"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@missinglocal.com",
            "missingdomain@",
            "missing.tld@example",
            "spaces in@example.com",
            "double..dot@example.com".replace("..", "."), // sanity guard, not a real case
    })
    void rejectsMalformedEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsEmailOverMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsEmailAtMaxLength() {
        // 254 chars total, valid pattern
        String local = "a".repeat(242);
        String email = local + "@example.com"; // 242 + 12 = 254
        assertTrue(EmailValidator.isValid(email));
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int amountColumnIndex = -1;
        int rowCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            String[] headers = line.split(",", -1);
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountColumnIndex = i;
                    break;
                }
            }

            if (amountColumnIndex == -1) {
                System.out.println("Could not find an 'amount' column in the header.");
                return;
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (amountColumnIndex >= fields.length) {
                    continue;
                }
                String value = fields[amountColumnIndex].trim();
                if (value.isEmpty()) {
                    continue;
                }
                try {
                    total += Double.parseDouble(value);
                    rowCount++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping unparseable amount value: " + value);
                }
            }

            System.out.printf("Sum of 'amount' over %d rows: %.2f%n", rowCount, total);

        } catch (IOException e) {
            System.err.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
````

### csv · run 1 · uncle-bob-junior

````java
import java.util.List;

public final class SalesAmountSummer {

    private static final String COLUMN_NAME = "amount";
    private static final String DELIMITER = ",";

    private SalesAmountSummer() {
    }

    public static double sumAmounts(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            return 0.0;
        }
        String[] header = csvLines.get(0).split(DELIMITER);
        int amountIndex = findColumnIndex(header, COLUMN_NAME);

        double total = 0.0;
        for (int i = 1; i < csvLines.size(); i++) {
            total += parseAmount(csvLines.get(i), amountIndex);
        }
        return total;
    }

    private static int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing required column: " + columnName);
    }

    private static double parseAmount(String line, int amountIndex) {
        String[] fields = line.split(DELIMITER);
        return Double.parseDouble(fields[amountIndex].trim());
    }
}
````

````java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotal {

    private static final Path SALES_CSV_PATH = Path.of("sales.csv");

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_CSV_PATH);
        double total = SalesAmountSummer.sumAmounts(lines);
        System.out.printf("Total amount: %.2f%n", total);
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount",
                "1,10.50",
                "2,5.25"
        );
        assertEquals(15.75, SalesAmountSummer.sumAmounts(lines), 0.0001);
    }

    @Test
    void returnsZeroForHeaderOnlyInput() {
        List<String> lines = List.of("id,amount");
        assertEquals(0.0, SalesAmountSummer.sumAmounts(lines), 0.0001);
    }

    @Test
    void returnsZeroForEmptyInput() {
        assertEquals(0.0, SalesAmountSummer.sumAmounts(List.of()), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "amount,id",
                "3.00,1"
        );
        assertEquals(3.00, SalesAmountSummer.sumAmounts(lines), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of(
                "id,total",
                "1,10.00"
        );
        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmounts(lines));
    }

    @Test
    void throwsOnInvalidAmountValue() {
        List<String> lines = List.of(
                "id,amount",
                "1,not-a-number"
        );
        assertThrows(NumberFormatException.class, () -> SalesAmountSummer.sumAmounts(lines));
    }
}
````

### csv · run 2 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int rowsProcessed = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("File is empty: " + filePath);
                return;
            }

            String[] headers = headerLine.split(",", -1);
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.err.println("Column 'amount' not found in header: " + headerLine);
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + " (missing amount field): " + line);
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                    rowsProcessed++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + " (invalid amount '" + rawValue + "')");
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file '" + filePath + "': " + e.getMessage());
            return;
        }

        System.out.printf("Rows processed: %d%n", rowsProcessed);
        System.out.printf("Sum of amount: %.2f%n", total);
    }
}
````

### csv · run 2 · uncle-bob-junior

````java
// SalesAmountSummer.java
import java.io.BufferedReader;
import java.io.IOException;

public final class SalesAmountSummer {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    private SalesAmountSummer() {
    }

    public static double sumAmountColumn(BufferedReader csvReader) throws IOException {
        String headerLine = csvReader.readLine();
        if (headerLine == null) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(headerLine);

        double total = 0.0;
        String row;
        while ((row = csvReader.readLine()) != null) {
            total += parseAmount(row, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columnNames = headerLine.split(COLUMN_SEPARATOR);
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV header has no '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String row, int amountColumnIndex) {
        String[] values = row.split(COLUMN_SEPARATOR);
        return Double.parseDouble(values[amountColumnIndex].trim());
    }
}
````

````java
// SalesTotal.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class SalesTotal {

    private static final String SALES_FILE_PATH = "sales.csv";

    public static void main(String[] args) throws IOException {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(SALES_FILE_PATH))) {
            double totalAmount = SalesAmountSummer.sumAmountColumn(csvReader);
            System.out.println(totalAmount);
        }
    }
}
````

````java
// SalesAmountSummerTest.java (JUnit 5)
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() throws IOException {
        String csv = "id,amount\n1,10.50\n2,5.25\n";
        assertEquals(15.75, sum(csv), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        String csv = "amount,id\n10.00,1\n20.00,2\n";
        assertEquals(30.00, sum(csv), 0.0001);
    }

    @Test
    void returnsZeroForHeaderOnlyFile() throws IOException {
        String csv = "id,amount\n";
        assertEquals(0.0, sum(csv), 0.0001);
    }

    @Test
    void returnsZeroForEmptyFile() throws IOException {
        assertEquals(0.0, sum(""), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        String csv = "id,description\n1,widget\n";
        assertThrows(IllegalArgumentException.class, () -> sum(csv));
    }

    private double sum(String csv) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            return SalesAmountSummer.sumAmountColumn(reader);
        }
    }
}
````

### csv · run 3 · baseline

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path csvPath = Path.of("sales.csv");

        try {
            List<String> lines = Files.readAllLines(csvPath);

            if (lines.isEmpty()) {
                System.out.println("sales.csv is empty.");
                return;
            }

            String[] header = lines.get(0).split(",");
            int amountIndex = -1;
            for (int i = 0; i < header.length; i++) {
                if (header[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in sales.csv.");
                return;
            }

            BigDecimal total = BigDecimal.ZERO;
            int rowsParsed = 0;
            int rowsSkipped = 0;

            for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
                String line = lines.get(lineNo);
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    rowsSkipped++;
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total = total.add(new BigDecimal(rawValue));
                    rowsParsed++;
                } catch (NumberFormatException e) {
                    rowsSkipped++;
                    System.out.println("Skipping line " + (lineNo + 1) + ": invalid amount '" + rawValue + "'");
                }
            }

            total = total.setScale(2, RoundingMode.HALF_UP);
            System.out.println("Rows summed: " + rowsParsed + (rowsSkipped > 0 ? ", skipped: " + rowsSkipped : ""));
            System.out.println("Total amount: " + total);

        } catch (IOException e) {
            System.err.println("Could not read sales.csv: " + e.getMessage());
        }
    }
}
````

````bash
javac SalesSum.java
java SalesSum
````

### csv · run 3 · uncle-bob-junior

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String DEFAULT_CSV_PATH = "sales.csv";

    private SalesAmountSummer() {
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : DEFAULT_CSV_PATH);
        BigDecimal total = sumAmountColumn(csvPath);
        System.out.println(total);
    }

    public static BigDecimal sumAmountColumn(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty: " + csvPath);
        }

        int amountIndex = findColumnIndex(lines.get(0), AMOUNT_COLUMN_NAME);

        BigDecimal total = BigDecimal.ZERO;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, amountIndex));
        }
        return total;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(COLUMN_SEPARATOR);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found in header: " + headerLine);
    }

    private static BigDecimal parseAmount(String dataLine, int amountIndex) {
        String[] fields = dataLine.split(COLUMN_SEPARATOR);
        if (amountIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no value at amount column: " + dataLine);
        }
        return new BigDecimal(fields[amountIndex].trim());
    }
}
````

````java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @TempDir
    Path tempDir;

    @Test
    void sumsAmountColumnAcrossRows() throws IOException {
        Path csv = writeCsv("id,amount,date",
                "1,10.50,2026-01-01",
                "2,20.25,2026-01-02",
                "3,5.00,2026-01-03");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(new BigDecimal("35.75"), total);
    }

    @Test
    void ignoresBlankLines() throws IOException {
        Path csv = writeCsv("id,amount",
                "1,10.00",
                "",
                "2,5.00");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(new BigDecimal("15.00"), total);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        Path csv = writeCsv("date,id,amount",
                "2026-01-01,1,7.50",
                "2026-01-02,2,2.50");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(new BigDecimal("10.00"), total);
    }

    @Test
    void returnsZeroWhenOnlyHeaderPresent() throws IOException {
        Path csv = writeCsv("id,amount");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void throwsWhenAmountColumnMissing() throws IOException {
        Path csv = writeCsv("id,date",
                "1,2026-01-01");

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(csv));
    }

    @Test
    void throwsWhenFileIsEmpty() throws IOException {
        Path csv = writeCsv();

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(csv));
    }

    private Path writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("sales.csv");
        Files.write(file, java.util.List.of(lines));
        return file;
    }
}
````

### csv · run 4 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = "sales.csv";
        double total = 0.0;
        int rowsProcessed = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return;
            }

            String[] headers = headerLine.split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("Column 'amount' not found in header.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    continue;
                }
                String rawValue = fields[amountIndex].trim();
                if (rawValue.isEmpty()) {
                    continue;
                }
                try {
                    total += Double.parseDouble(rawValue);
                    rowsProcessed++;
                } catch (NumberFormatException e) {
                    System.out.println("Skipping unparsable value: " + rawValue);
                }
            }

            System.out.printf("Sum of 'amount' over %d rows: %.2f%n", rowsProcessed, total);

        } catch (IOException e) {
            System.out.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
````

````
javac SalesSum.java
java SalesSum
````

### csv · run 4 · uncle-bob-junior

````java
// SalesTotalPrinter.java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotalPrinter {

    private static final Path SALES_FILE = Path.of("sales.csv");

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(SALES_FILE);
            BigDecimal total = new SalesAmountSummer().sum(lines);
            System.out.println(total);
        } catch (IOException e) {
            System.err.println("Could not read " + SALES_FILE + ": " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid sales.csv: " + e.getMessage());
            System.exit(1);
        }
    }
}

final class SalesAmountSummer {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    BigDecimal sum(List<String> lines) {
        if (lines.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        BigDecimal total = BigDecimal.ZERO;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(extractAmount(line, amountColumnIndex));
        }
        return total;
    }

    private int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("header is missing an 'amount' column");
    }

    private BigDecimal extractAmount(String dataLine, int amountColumnIndex) {
        String[] fields = dataLine.split(CSV_DELIMITER, -1);
        return new BigDecimal(fields[amountColumnIndex].trim());
    }
}
````

````java
// SalesAmountSummerTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    private final SalesAmountSummer summer = new SalesAmountSummer();

    @Test
    void sumsAmountColumnAcrossRows() {
        List<String> lines = List.of(
                "id,amount,customer",
                "1,10.50,alice",
                "2,5.25,bob"
        );

        assertEquals(new BigDecimal("15.75"), summer.sum(lines));
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "customer,amount",
                "alice,10.00"
        );

        assertEquals(new BigDecimal("10.00"), summer.sum(lines));
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of(
                "id,amount",
                "1,10.00",
                "",
                "2,5.00"
        );

        assertEquals(new BigDecimal("15.00"), summer.sum(lines));
    }

    @Test
    void returnsZeroWhenOnlyHeaderPresent() {
        List<String> lines = List.of("id,amount");

        assertEquals(BigDecimal.ZERO, summer.sum(lines));
    }

    @Test
    void returnsZeroForEmptyFile() {
        assertEquals(BigDecimal.ZERO, summer.sum(List.of()));
    }

    @Test
    void throwsWhenAmountColumnIsMissing() {
        List<String> lines = List.of("id,customer", "1,alice");

        assertThrows(IllegalArgumentException.class, () -> summer.sum(lines));
    }
}
````

### csv · run 5 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return;
            }

            List<String> headers = List.of(headerLine.split(","));
            int amountIndex = headers.indexOf("amount");
            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in header.");
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }
                try {
                    total += Double.parseDouble(fields[amountIndex].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line " + lineNumber + ": invalid amount value '" + fields[amountIndex] + "'.");
                }
            }

            System.out.printf("Sum of amount column: %.2f%n", total);

        } catch (IOException e) {
            System.out.println("Error reading file '" + path + "': " + e.getMessage());
        }
    }
}
````

### csv · run 5 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.util.List;

final class CsvAmountSummarizer {

    private static final String DELIMITER = ",";

    private CsvAmountSummarizer() {
    }

    static BigDecimal sumColumn(List<String> lines, String columnName) {
        if (lines.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int columnIndex = findColumnIndex(lines.get(0), columnName);
        return lines.stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(line -> readAmount(line, columnIndex))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] header = headerLine.split(DELIMITER, -1);
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }

    private static BigDecimal readAmount(String line, int columnIndex) {
        String[] fields = line.split(DELIMITER, -1);
        return new BigDecimal(fields[columnIndex].trim());
    }
}
````

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";

    private SalesTotal() {
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, AMOUNT_COLUMN);
        System.out.println(total);
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvAmountSummarizerTest {

    @Test
    void sumsAmountColumnAcrossRows() {
        List<String> lines = List.of(
                "id,amount,region",
                "1,10.50,NL",
                "2,4.25,UK"
        );

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(new BigDecimal("14.75"), total);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "amount,id",
                "3.00,1"
        );

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(new BigDecimal("3.00"), total);
    }

    @Test
    void returnsZeroWhenNoDataRows() {
        List<String> lines = List.of("id,amount");

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void returnsZeroForEmptyFile() {
        BigDecimal total = CsvAmountSummarizer.sumColumn(List.of(), "amount");

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of(
                "id,amount",
                "1,5.00",
                "",
                "2,2.00"
        );

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(new BigDecimal("7.00"), total);
    }

    @Test
    void throwsWhenColumnMissing() {
        List<String> lines = List.of("id,total");

        assertThrows(IllegalArgumentException.class,
                () -> CsvAmountSummarizer.sumColumn(lines, "amount"));
    }
}
````

### csv · run 6 · baseline

````java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class SalesSum {

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "sales.csv";
        Path path = Path.of(filePath);

        double total = 0.0;
        int rowsProcessed = 0;
        int rowsSkipped = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("File is empty: " + filePath);
                return;
            }

            String[] headers = headerLine.split(",", -1);
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.err.println("No 'amount' column found in header: " + headerLine);
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    rowsSkipped++;
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                    rowsProcessed++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'");
                    rowsSkipped++;
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to read file '" + filePath + "': " + e.getMessage());
            return;
        }

        System.out.printf(Locale.US, "Sum of 'amount': %.2f%n", total);
        System.out.println("Rows processed: " + rowsProcessed + ", rows skipped: " + rowsSkipped);
    }
}
````

````
javac SalesSum.java
java SalesSum sales.csv
````

### csv · run 6 · uncle-bob-junior

````java
// SalesAmountSummer.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final Path SALES_CSV_PATH = Path.of("sales.csv");

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_CSV_PATH);
        double totalAmount = sumAmountColumn(lines);
        System.out.printf("Total amount: %.2f%n", totalAmount);
    }

    static double sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        double total = 0.0;
        for (String row : lines.subList(1, lines.size())) {
            total += parseAmount(row, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columnNames = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing an 'amount' column");
    }

    private static double parseAmount(String row, int amountColumnIndex) {
        String[] values = row.split(CSV_DELIMITER);
        return Double.parseDouble(values[amountColumnIndex].trim());
    }
}
````

````java
// SalesAmountSummerTest.java
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount,region",
                "1,10.50,NL",
                "2,5.25,UK");

        assertEquals(15.75, SalesAmountSummer.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "region,id,amount",
                "NL,1,10.00");

        assertEquals(10.00, SalesAmountSummer.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroWhenFileHasOnlyHeader() {
        List<String> lines = List.of("id,amount,region");

        assertEquals(0.0, SalesAmountSummer.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroWhenFileIsEmpty() {
        assertEquals(0.0, SalesAmountSummer.sumAmountColumn(List.of()), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnIsMissing() {
        List<String> lines = List.of("id,region", "1,NL");

        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(lines));
    }
}
````

### csv · run 7 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String csvPath = args.length > 0 ? args[0] : "sales.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("sales.csv is empty");
                return;
            }

            List<String> headers = List.of(headerLine.split(","));
            int amountIndex = headers.indexOf("amount");
            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in header");
                return;
            }

            double sum = 0.0;
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping malformed row " + lineNumber);
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    sum += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping non-numeric amount on row " + lineNumber + ": " + rawValue);
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", sum);

        } catch (IOException e) {
            System.err.println("Failed to read " + csvPath + ": " + e.getMessage());
        }
    }
}
````

### csv · run 7 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    private static final String COLUMN_NAME = "amount";
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_FILE_PATH = "sales.csv";

    public static void main(String[] args) throws IOException {
        double total = sumAmountColumn(CSV_FILE_PATH);
        System.out.printf("Sum of '%s': %.2f%n", COLUMN_NAME, total);
    }

    private static double sumAmountColumn(String csvFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvFilePath);
            }

            int amountIndex = findColumnIndex(headerLine.split(CSV_DELIMITER, -1));
            return sumRows(reader, amountIndex);
        }
    }

    private static int findColumnIndex(String[] headers) throws IOException {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(COLUMN_NAME)) {
                return i;
            }
        }
        throw new IOException("Column '" + COLUMN_NAME + "' not found in header");
    }

    private static double sumRows(BufferedReader reader, int amountIndex) throws IOException {
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(CSV_DELIMITER, -1);
            total += Double.parseDouble(fields[amountIndex].trim());
        }
        return total;
    }
}
````

### csv · run 8 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAmountSum {

    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        int rowsRead = 0;
        int rowsSkipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("sales.csv is empty.");
                return;
            }

            String[] headers = headerLine.split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("Could not find an 'amount' column in the header.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    rowsSkipped++;
                    continue;
                }
                try {
                    total += Double.parseDouble(fields[amountIndex].trim());
                    rowsRead++;
                } catch (NumberFormatException e) {
                    rowsSkipped++;
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", total);
            System.out.println("Rows summed: " + rowsRead + ", rows skipped: " + rowsSkipped);

        } catch (IOException e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
        }
    }
}
````

### csv · run 8 · uncle-bob-junior

````java
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String CSV_DELIMITER = ",";

    private SalesAmountSummer() {
    }

    public static double sumAmountColumn(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            return 0.0;
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        double total = 0.0;
        for (String line : lines.subList(1, lines.size())) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV has no '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String dataLine, int amountColumnIndex) {
        if (dataLine.isBlank()) {
            return 0.0;
        }
        String[] fields = dataLine.split(CSV_DELIMITER, -1);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }

    public static void main(String[] args) {
        Path csvPath = Path.of(args.length > 0 ? args[0] : "sales.csv");
        try {
            double sum = sumAmountColumn(csvPath);
            System.out.println("Sum of amount column: " + sum);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + csvPath, e);
        }
    }
}
````

````java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @TempDir
    Path tempDir;

    @Test
    void sumsAmountsAcrossMultipleRows() throws IOException {
        Path csv = writeCsv("id,amount\n1,10.50\n2,20.25\n3,5.00\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(35.75, result, 0.0001);
    }

    @Test
    void returnsZeroWhenOnlyHeaderPresent() throws IOException {
        Path csv = writeCsv("id,amount\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        Path csv = writeCsv("amount,id\n7.00,1\n3.00,2\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(10.0, result, 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() throws IOException {
        Path csv = writeCsv("id,total\n1,10\n");

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(csv));
    }

    @Test
    void skipsBlankLines() throws IOException {
        Path csv = writeCsv("id,amount\n1,10\n\n2,15\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(25.0, result, 0.0001);
    }

    private Path writeCsv(String content) throws IOException {
        Path file = tempDir.resolve("sales.csv");
        Files.writeString(file, content);
        return file;
    }
}
````

### csv · run 9 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalesSum {

    public static void main(String[] args) {
        String csvFile = "sales.csv";
        BigDecimal total = BigDecimal.ZERO;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("sales.csv is empty.");
                return;
            }

            String[] headers = headerLine.split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in sales.csv.");
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total = total.add(new BigDecimal(rawValue));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount value '" + rawValue + "'.");
                }
            }

            System.out.println("Sum of 'amount' column: " + total.setScale(2, RoundingMode.HALF_UP));

        } catch (IOException e) {
            System.err.println("Error reading sales.csv: " + e.getMessage());
        }
    }
}
````

### csv · run 9 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SalesAmountSummer {

    private static final String DEFAULT_FILE_PATH = "sales.csv";
    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        String filePath = args.length > 0 ? args[0] : DEFAULT_FILE_PATH;
        double total = sumAmountColumn(Paths.get(filePath));
        System.out.printf("Total amount: %.2f%n", total);
    }

    static double sumAmountColumn(Path csvPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            return sumAmountColumn(reader);
        }
    }

    static double sumAmountColumn(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return 0.0;
        }

        int amountColumnIndex = findAmountColumnIndex(headerLine);
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columns = headerLine.split(COLUMN_SEPARATOR);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
            "CSV header does not contain an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        if (line.isBlank()) {
            return 0.0;
        }
        String[] fields = line.split(COLUMN_SEPARATOR);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
````

````java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() throws IOException {
        String csv = "id,amount,date\n1,10.50,2026-01-01\n2,20.25,2026-01-02\n";
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv)));
        assertEquals(30.75, total, 0.0001);
    }

    @Test
    void ignoresColumnOrderAndUsesHeaderName() throws IOException {
        String csv = "date,amount,id\n2026-01-01,5.00,1\n";
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv)));
        assertEquals(5.00, total, 0.0001);
    }

    @Test
    void returnsZeroForEmptyFile() throws IOException {
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader("")));
        assertEquals(0.0, total, 0.0001);
    }

    @Test
    void skipsBlankLines() throws IOException {
        String csv = "id,amount\n1,10\n\n2,20\n";
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv)));
        assertEquals(30.0, total, 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        String csv = "id,value\n1,10\n";
        assertThrows(IllegalArgumentException.class,
            () -> SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv))));
    }
}
````

### csv · run 10 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double total = 0.0;
        int amountIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("sales.csv is empty.");
                return;
            }

            String[] headers = line.split(",");
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("Could not find an 'amount' column in sales.csv.");
                return;
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping line " + lineNumber + ": missing amount value.");
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", total);

        } catch (IOException e) {
            System.out.println("Error reading sales.csv: " + e.getMessage());
        }
    }
}
````

### csv · run 10 · uncle-bob-junior

````java
import java.util.List;

public final class SalesAmountSummer {
    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public double sumAmountColumn(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(csvLines.get(0));
        double total = 0.0;
        for (String line : csvLines.subList(1, csvLines.size())) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "CSV header is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private double parseAmount(String dataLine, int amountColumnIndex) {
        String[] fields = dataLine.split(CSV_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
````

````java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesReport {
    private static final Path SALES_CSV_PATH = Path.of("sales.csv");

    public static void main(String[] args) throws IOException {
        List<String> csvLines = Files.readAllLines(SALES_CSV_PATH);
        double total = new SalesAmountSummer().sumAmountColumn(csvLines);
        System.out.printf("Total amount: %.2f%n", total);
    }
}
````

````java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SalesAmountSummerTest {
    private static final double DELTA = 0.0001;
    private final SalesAmountSummer summer = new SalesAmountSummer();

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of("id,amount", "1,10.50", "2,20.25");
        assertEquals(30.75, summer.sumAmountColumn(lines), DELTA);
    }

    @Test
    void returnsZeroForHeaderOnlyCsv() {
        List<String> lines = List.of("id,amount");
        assertEquals(0.0, summer.sumAmountColumn(lines), DELTA);
    }

    @Test
    void returnsZeroForEmptyCsv() {
        assertEquals(0.0, summer.sumAmountColumn(List.of()), DELTA);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of("amount,id", "5.00,1");
        assertEquals(5.00, summer.sumAmountColumn(lines), DELTA);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,price", "1,10");
        assertThrows(IllegalArgumentException.class, () -> summer.sumAmountColumn(lines));
    }
}
````

## retry

### retry · run 1 · baseline

````java
import java.time.Duration;
import java.util.Objects;

/**
 * Reusable retry helper: runs an operation and retries it a fixed number of
 * times with a fixed delay between attempts when it throws an exception.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delay;

    private Retryer(int maxAttempts, Duration delay) {
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    /**
     * @param maxAttempts total number of attempts (including the first), must be >= 1
     * @param delay       fixed wait time between attempts
     */
    public static Retryer of(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delay, "delay");
        return new Retryer(maxAttempts, delay);
    }

    /** Operation that returns a value and may throw a checked exception. */
    @FunctionalInterface
    public interface RetryableCallable<T> {
        T call() throws Exception;
    }

    /** Operation that returns nothing and may throw a checked exception. */
    @FunctionalInterface
    public interface RetryableRunnable {
        void run() throws Exception;
    }

    /** Runs {@code operation}, retrying on any exception, and returns its result. */
    public <T> T call(RetryableCallable<T> operation) throws Exception {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (lastFailure != null) {
                    e.addSuppressed(lastFailure);
                }
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    throw e;
                }
                sleep(lastFailure);
            }
        }

        // Unreachable: loop always returns or throws.
        throw new AssertionError("Retry loop exited without result or exception");
    }

    /** Runs {@code operation}, retrying on any exception. */
    public void run(RetryableRunnable operation) throws Exception {
        call(() -> {
            operation.run();
            return null;
        });
    }

    private void sleep(Exception cause) throws InterruptedException {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            interrupted.addSuppressed(cause);
            Thread.currentThread().interrupt();
            throw interrupted;
        }
    }

    // --- Example usage ---
    public static void main(String[] args) throws Exception {
        Retryer retryer = Retryer.of(3, Duration.ofSeconds(1));

        String result = retryer.call(() -> {
            if (Math.random() < 0.7) {
                throw new RuntimeException("transient failure");
            }
            return "success";
        });
        System.out.println(result);

        retryer.run(() -> {
            System.out.println("side-effecting operation");
        });
    }
}
````

### retry · run 1 · uncle-bob-junior

````java
import java.time.Duration;
import java.util.concurrent.Callable;

public final class Retryer {

    private Retryer() {
    }

    public static <T> T retry(Callable<T> operation, int maxAttempts, Duration delayBetweenAttempts) {
        requireValidAttemptCount(maxAttempts);

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt < maxAttempts) {
                sleep(delayBetweenAttempts);
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private static void requireValidAttemptCount(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
````

````java
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempts", lastFailure);
    }
}
````

````java
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry wait was interrupted", cause);
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ofMillis(1);

    @Test
    void succeedsOnFirstAttempt_returnsResultWithoutRetrying() {
        AtomicInteger callCount = new AtomicInteger(0);
        Callable<String> operation = () -> {
            callCount.incrementAndGet();
            return "ok";
        };

        String result = Retryer.retry(operation, 3, NO_DELAY);

        assertEquals("ok", result);
        assertEquals(1, callCount.get());
    }

    @Test
    void succeedsAfterTransientFailures_returnsResult() {
        AtomicInteger callCount = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        };

        String result = Retryer.retry(operation, 5, NO_DELAY);

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void exhaustsAllAttempts_throwsRetryExhaustedExceptionWithLastCause() {
        AtomicInteger callCount = new AtomicInteger(0);
        RuntimeException lastFailure = new RuntimeException("final failure");
        Callable<String> alwaysFails = () -> {
            callCount.incrementAndGet();
            if (callCount.get() == 3) {
                throw lastFailure;
            }
            throw new RuntimeException("earlier failure");
        };

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> Retryer.retry(alwaysFails, 3, NO_DELAY));

        assertEquals(3, callCount.get());
        assertEquals(lastFailure, thrown.getCause());
    }

    @Test
    void invalidMaxAttempts_throwsIllegalArgumentException() {
        Callable<String> unused = () -> "unreachable";

        assertThrows(IllegalArgumentException.class, () -> Retryer.retry(unused, 0, NO_DELAY));
    }

    @Test
    void interruptedDuringWait_propagatesAsRetryInterruptedException() throws InterruptedException {
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };
        AtomicReference<Throwable> thrown = new AtomicReference<>();

        Thread worker = new Thread(() -> {
            try {
                Retryer.retry(alwaysFails, 3, Duration.ofSeconds(5));
            } catch (Throwable t) {
                thrown.set(t);
            }
        });

        worker.start();
        Thread.sleep(100);
        worker.interrupt();
        worker.join(1000);

        assertInstanceOf(RetryInterruptedException.class, thrown.get());
    }
}
````

### retry · run 2 · baseline

````java
import java.util.concurrent.TimeUnit;

/**
 * Generic retry helper: runs an operation up to maxAttempts times,
 * waiting a fixed delay between attempts, until it succeeds or attempts are exhausted.
 */
public final class RetryHelper {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws Exception;
    }

    private RetryHelper() {
    }

    public static <T> T execute(RetryableOperation<T> operation,
                                 int maxAttempts,
                                 long delay,
                                 TimeUnit delayUnit) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                if (lastFailure != null) {
                    e.addSuppressed(lastFailure);
                }
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    delayUnit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
        }

        throw lastFailure;
    }

    // Example usage
    public static void main(String[] args) throws Exception {
        int result = execute(() -> {
            System.out.println("Attempting operation...");
            if (Math.random() < 0.8) {
                throw new RuntimeException("Simulated failure");
            }
            return 42;
        }, 5, 500, TimeUnit.MILLISECONDS);

        System.out.println("Succeeded with result: " + result);
    }
}
````

### retry · run 2 · uncle-bob-junior

````java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable configuration for a {@link Retryer}: how many attempts to make
 * and how long to wait between them.
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts must not be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, got " + maxAttempts);
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
````

````java
package com.plg.retry;

import java.time.Duration;

/** Boundary for waiting, so tests can avoid real delays. */
public interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;

    static Sleeper realTime() {
        return duration -> Thread.sleep(duration.toMillis());
    }
}
````

````java
package com.plg.retry;

/** Thrown when an operation still fails after the configured number of attempts. */
public final class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
````

````java
package com.plg.retry;

/** Thrown when the thread is interrupted while waiting between retry attempts. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Interrupted while waiting between retry attempts", cause);
    }
}
````

````java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it with a fixed delay between attempts when it
 * throws an exception, up to the number of attempts in the {@link RetryPolicy}.
 */
public final class Retryer {

    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public Retryer(RetryPolicy policy) {
        this(policy, Sleeper.realTime());
    }

    public Retryer(RetryPolicy policy, Sleeper sleeper) {
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
    }

    public <T> T run(Callable<T> operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        Outcome<T> lastOutcome = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            lastOutcome = attempt(operation);
            if (lastOutcome.succeeded()) {
                return lastOutcome.value();
            }
            waitBeforeNextAttemptIfAny(attempt);
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastOutcome.failure());
    }

    private <T> Outcome<T> attempt(Callable<T> operation) {
        try {
            return Outcome.success(operation.call());
        } catch (Exception e) {
            return Outcome.failure(e);
        }
    }

    private void waitBeforeNextAttemptIfAny(int attemptJustMade) {
        if (attemptJustMade >= policy.maxAttempts()) {
            return;
        }
        sleepFor(policy.delayBetweenAttempts());
    }

    private void sleepFor(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(e);
        }
    }

    private static final class Outcome<T> {
        private final T value;
        private final Exception failure;

        private Outcome(T value, Exception failure) {
            this.value = value;
            this.failure = failure;
        }

        static <T> Outcome<T> success(T value) {
            return new Outcome<>(value, null);
        }

        static <T> Outcome<T> failure(Exception failure) {
            return new Outcome<>(null, failure);
        }

        boolean succeeded() {
            return failure == null;
        }

        T value() {
            return value;
        }

        Exception failure() {
            return failure;
        }
    }
}
````

````java
package com.plg.retry;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryerTest {

    private static final Duration DELAY = Duration.ofMillis(50);

    @Test
    void returnsResultOnFirstSuccess() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(RetryPolicy.of(3, DELAY), sleeper);

        String result = retryer.run(() -> "ok");

        assertEquals("ok", result);
        assertTrue(sleeper.recordedDelays.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndWaitsBetweenAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(RetryPolicy.of(4, DELAY), sleeper);
        AtomicInteger callCount = new AtomicInteger();
        Callable<String> flaky = () -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        };

        String result = retryer.run(flaky);

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
        assertEquals(List.of(DELAY, DELAY), sleeper.recordedDelays);
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(RetryPolicy.of(3, DELAY), sleeper);
        RuntimeException failure = new RuntimeException("boom");
        Callable<String> alwaysFails = () -> { throw failure; };

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retryer.run(alwaysFails));

        assertEquals(3, thrown.attemptsMade());
        assertSame(failure, thrown.getCause());
        assertEquals(List.of(DELAY, DELAY), sleeper.recordedDelays);
    }

    @Test
    void wrapsInterruptionAndRestoresInterruptFlag() {
        Sleeper interruptingSleeper = duration -> {
            throw new InterruptedException("stop waiting");
        };
        Retryer retryer = new Retryer(RetryPolicy.of(2, DELAY), interruptingSleeper);
        Callable<String> alwaysFails = () -> { throw new RuntimeException("boom"); };

        try {
            assertThrows(RetryInterruptedException.class, () -> retryer.run(alwaysFails));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Nested
    class RetryPolicyValidation {

        @Test
        void rejectsNonPositiveMaxAttempts() {
            assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, DELAY));
        }

        @Test
        void rejectsNegativeDelay() {
            assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(3, Duration.ofMillis(-1)));
        }

        @Test
        void rejectsNullDelay() {
            assertThrows(NullPointerException.class, () -> RetryPolicy.of(3, null));
        }
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            recordedDelays.add(duration);
        }
    }
}
````

### retry · run 3 · baseline

````java
import java.time.Duration;

/**
 * A reusable helper that runs an operation, retrying it a fixed number of
 * times with a fixed delay between attempts if it throws an exception.
 */
public final class RetryHelper {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T call() throws Exception;
    }

    private final int maxAttempts;
    private final Duration delay;

    /**
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     */
    public RetryHelper(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delay == null || delay.isNegative()) {
            throw new IllegalArgumentException("delay must be non-null and non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    /**
     * Runs the given operation, retrying on exception up to maxAttempts times.
     *
     * @throws RetryException if all attempts fail, or if interrupted while waiting
     */
    public <T> T execute(RetryableOperation<T> operation) throws RetryException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryException(
                            "Retry interrupted after attempt " + attempt + " of " + maxAttempts,
                            interrupted);
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempts",
                lastException);
    }

    /** Overload for operations that don't return a value. */
    public void execute(Runnable operation) throws RetryException {
        execute(() -> {
            operation.run();
            return null;
        });
    }

    /** Thrown when all retry attempts are exhausted or retrying is interrupted. */
    public static final class RetryException extends Exception {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Example usage
    public static void main(String[] args) {
        RetryHelper retryHelper = new RetryHelper(3, Duration.ofSeconds(2));

        try {
            String result = retryHelper.execute(() -> {
                // Replace with the actual operation, e.g. an HTTP call or DB query
                System.out.println("Attempting operation...");
                if (Math.random() < 0.8) {
                    throw new RuntimeException("Simulated failure");
                }
                return "success";
            });
            System.out.println("Result: " + result);
        } catch (RetryException e) {
            System.err.println("All attempts failed: " + e.getCause());
        }
    }
}
````

### retry · run 3 · uncle-bob-junior

````java
// File: RetryableOperation.java
package com.plg.retry;

/**
 * A unit of work that may fail and is safe to attempt again.
 *
 * @param <T> the type produced on success
 */
@FunctionalInterface
public interface RetryableOperation<T> {

    /**
     * Performs the operation.
     *
     * @return the result on success
     * @throws Exception if the operation fails; the caller may retry
     */
    T execute() throws Exception;
}
````

````java
// File: RetryExhaustedException.java
package com.plg.retry;

/**
 * Thrown when a {@link Retryer} runs out of attempts without the
 * operation succeeding. The cause is the exception from the last attempt.
 */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int maxAttempts, Throwable lastFailure) {
        super("Operation failed after " + maxAttempts + " attempts", lastFailure);
    }
}
````

````java
// File: RetryInterruptedException.java
package com.plg.retry;

/**
 * Thrown when the current thread is interrupted while waiting
 * between retry attempts. The thread's interrupt status is restored
 * before this is thrown.
 */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry delay was interrupted", cause);
    }
}
````

````java
// File: Retryer.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed
 * delay between attempts when it throws an exception.
 *
 * <p>Instances are immutable and safe to reuse across many calls to
 * {@link #run(RetryableOperation)}.
 */
public final class Retryer {

    private static final int MIN_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    /**
     * @param maxAttempts           total number of attempts, must be at least 1
     * @param delayBetweenAttempts  wait time between attempts, must not be negative
     */
    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, new ThreadSleeper());
    }

    Retryer(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        this.delayBetweenAttempts = requireNonNegative(
                Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts must not be null"));
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
        this.maxAttempts = requireValidAttemptCount(maxAttempts);
    }

    /**
     * Runs {@code operation}, retrying on any {@link Exception} up to
     * {@code maxAttempts} times with {@code delayBetweenAttempts} between tries.
     *
     * @throws RetryExhaustedException     if every attempt fails
     * @throws RetryInterruptedException   if interrupted while waiting to retry
     */
    public <T> T run(RetryableOperation<T> operation) {
        Objects.requireNonNull(operation, "operation must not be null");

        Exception lastFailure = null;
        for (int attemptNumber = MIN_ATTEMPTS; attemptNumber <= maxAttempts; attemptNumber++) {
            AttemptResult<T> result = attempt(operation);
            if (result.succeeded()) {
                return result.value();
            }
            lastFailure = result.failure();
            if (hasAttemptsRemaining(attemptNumber)) {
                sleeper.sleep(delayBetweenAttempts);
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private <T> AttemptResult<T> attempt(RetryableOperation<T> operation) {
        try {
            return AttemptResult.success(operation.execute());
        } catch (Exception failure) {
            return AttemptResult.failure(failure);
        }
    }

    private boolean hasAttemptsRemaining(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    private static int requireValidAttemptCount(int maxAttempts) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least " + MIN_ATTEMPTS + " but was " + maxAttempts);
        }
        return maxAttempts;
    }

    private static Duration requireNonNegative(Duration delay) {
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative but was " + delay);
        }
        return delay;
    }

    /** Outcome of a single attempt: exactly one of value/failure is meaningful. */
    private static final class AttemptResult<T> {
        private final T value;
        private final Exception failure;
        private final boolean succeeded;

        private AttemptResult(T value, Exception failure, boolean succeeded) {
            this.value = value;
            this.failure = failure;
            this.succeeded = succeeded;
        }

        static <T> AttemptResult<T> success(T value) {
            return new AttemptResult<>(value, null, true);
        }

        static <T> AttemptResult<T> failure(Exception failure) {
            return new AttemptResult<>(null, failure, false);
        }

        boolean succeeded() {
            return succeeded;
        }

        T value() {
            return value;
        }

        Exception failure() {
            return failure;
        }
    }

    /** Seam for injecting a fake wait in tests instead of really sleeping. */
    interface Sleeper {
        void sleep(Duration duration);
    }

    private static final class ThreadSleeper implements Sleeper {
        @Override
        public void sleep(Duration duration) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RetryInterruptedException(interrupted);
            }
        }
    }
}
````

````java
// File: RetryerTest.java
package com.plg.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

class RetryerTest {

    private static final Duration DELAY = Duration.ofMillis(50);

    @Test
    void succeedsOnFirstAttempt_returnsResultWithoutWaiting() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(3, DELAY, sleeper);
        ScriptedOperation operation = new ScriptedOperation("ok");

        String result = retryer.run(operation);

        assertEquals("ok", result);
        assertEquals(1, operation.invocationCount());
        assertTrue(sleeper.sleptDurations().isEmpty());
    }

    @Test
    void succeedsAfterTransientFailures_retriesThenReturnsResult() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(3, DELAY, sleeper);
        ScriptedOperation operation = new ScriptedOperation("ok", new IOException("first"), new IOException("second"));

        String result = retryer.run(operation);

        assertEquals("ok", result);
        assertEquals(3, operation.invocationCount());
        assertEquals(List.of(DELAY, DELAY), sleeper.sleptDurations());
    }

    @Test
    void exhaustsAllAttempts_throwsWithLastFailureAsCause() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(3, DELAY, sleeper);
        IOException finalFailure = new IOException("boom");
        AlwaysFailingOperation operation = new AlwaysFailingOperation(new IOException("ignored 1"), new IOException("ignored 2"), finalFailure);

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () -> retryer.run(operation));

        assertEquals(3, operation.invocationCount());
        assertSame(finalFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelay_onlyBetweenAttemptsNotAfterLastOne() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(4, DELAY, sleeper);
        AlwaysFailingOperation operation = new AlwaysFailingOperation(new IOException("a"), new IOException("b"), new IOException("c"), new IOException("d"));

        assertThrows(RetryExhaustedException.class, () -> retryer.run(operation));

        assertEquals(List.of(DELAY, DELAY, DELAY), sleeper.sleptDurations());
    }

    @Test
    void invalidMaxAttempts_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, DELAY));
    }

    @Test
    void negativeDelay_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(3, Duration.ofMillis(-1)));
    }

    @Test
    void nullDelay_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Retryer(3, null));
    }

    @Test
    void nullOperation_throwsNullPointerException() {
        Retryer retryer = new Retryer(3, DELAY, new FakeSleeper());

        assertThrows(NullPointerException.class, () -> retryer.run(null));
    }

    @Test
    void interruptedWhileWaiting_restoresInterruptStatusAndThrows() {
        Retryer.Sleeper interruptingSleeper = duration -> {
            throw new RetryInterruptedException(new InterruptedException("test"));
        };
        Retryer retryer = new Retryer(3, DELAY, interruptingSleeper);
        AlwaysFailingOperation operation = new AlwaysFailingOperation(new IOException("a"), new IOException("b"));

        assertInstanceOf(RetryInterruptedException.class,
                assertThrows(RetryInterruptedException.class, () -> retryer.run(operation)));
    }

    /** Records requested waits instead of actually sleeping, keeping tests fast. */
    private static final class FakeSleeper implements Retryer.Sleeper {
        private final List<Duration> sleptDurations = new java.util.ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            sleptDurations.add(duration);
        }

        List<Duration> sleptDurations() {
            return sleptDurations;
        }
    }

    /** Fails with the given exceptions in order, then succeeds with {@code successValue}. */
    private static final class ScriptedOperation implements RetryableOperation<String> {
        private final Queue<Exception> failuresBeforeSuccess;
        private final String successValue;
        private int invocationCount = 0;

        ScriptedOperation(String successValue, Exception... failuresBeforeSuccess) {
            this.successValue = successValue;
            this.failuresBeforeSuccess = new ArrayDeque<>(Arrays.asList(failuresBeforeSuccess));
        }

        @Override
        public String execute() throws Exception {
            invocationCount++;
            Exception nextFailure = failuresBeforeSuccess.poll();
            if (nextFailure != null) {
                throw nextFailure;
            }
            return successValue;
        }

        int invocationCount() {
            return invocationCount;
        }
    }

    /** Throws the given exceptions in order on each invocation, repeating the last one once exhausted. */
    private static final class AlwaysFailingOperation implements RetryableOperation<String> {
        private final List<Exception> failures;
        private int invocationCount = 0;

        AlwaysFailingOperation(Exception... failures) {
            this.failures = Arrays.asList(failures);
        }

        @Override
        public String execute() throws Exception {
            Exception failure = failures.get(Math.min(invocationCount, failures.size() - 1));
            invocationCount++;
            throw failure;
        }

        int invocationCount() {
            return invocationCount;
        }
    }
}
````

### retry · run 4 · baseline

````java
import java.time.Duration;
import java.util.concurrent.Callable;

public final class RetryHelper {

    private RetryHelper() {
    }

    public static <T> T retry(Callable<T> operation, int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                if (attempt == maxAttempts) {
                    break;
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(attempt, "Retry interrupted while waiting", ie);
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, "Operation failed after " + maxAttempts + " attempts", lastException);
    }

    public static void retry(RunnableWithException operation, int maxAttempts, Duration delay) {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay);
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    public static final class RetryExhaustedException extends RuntimeException {
        private final int attemptsMade;

        public RetryExhaustedException(int attemptsMade, String message, Throwable cause) {
            super(message, cause);
            this.attemptsMade = attemptsMade;
        }

        public int getAttemptsMade() {
            return attemptsMade;
        }
    }
}
````

````java
String result = RetryHelper.retry(
        () -> callFlakyService(),
        5,
        Duration.ofSeconds(2)
);

RetryHelper.retry(
        () -> writeToFile(data),
        3,
        Duration.ofMillis(500)
);
````

### retry · run 4 · uncle-bob-junior

````java
import java.time.Duration;

public final class RetryConfig {

    private static final int MIN_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delay;

    public RetryConfig(int maxAttempts, Duration delay) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MIN_ATTEMPTS);
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delay() {
        return delay;
    }
}
````

````java
import java.time.Duration;

@FunctionalInterface
interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
````

````java
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
````

````java
import java.util.concurrent.Callable;

public final class Retryer {

    private final RetryConfig config;
    private final Sleeper sleeper;

    public Retryer(RetryConfig config) {
        this(config, duration -> Thread.sleep(duration.toMillis()));
    }

    // package-private: lets tests inject a no-op/recording Sleeper instead of really waiting
    Retryer(RetryConfig config, Sleeper sleeper) {
        this.config = config;
        this.sleeper = sleeper;
    }

    public <T> T execute(Callable<T> operation) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;
                waitBeforeNextAttempt(attempt);
            }
        }
        throw new RetryExhaustedException(config.maxAttempts(), lastFailure);
    }

    private void waitBeforeNextAttempt(int attempt) {
        boolean hasMoreAttempts = attempt < config.maxAttempts();
        if (!hasMoreAttempts) {
            return;
        }
        sleep();
    }

    private void sleep() {
        try {
            sleeper.sleep(config.delay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted while waiting", e);
        }
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryerTest {

    @Test
    void returnsResultOnFirstSuccess() {
        Retryer retryer = new Retryer(new RetryConfig(3, Duration.ofMillis(10)), noOpSleeper());
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() {
        Retryer retryer = new Retryer(new RetryConfig(3, Duration.ofMillis(10)), noOpSleeper());
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            int attempt = calls.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("fail " + attempt);
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer(new RetryConfig(2, Duration.ofMillis(10)), noOpSleeper());
        AtomicInteger calls = new AtomicInteger();

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    calls.incrementAndGet();
                    throw new RuntimeException("always fails");
                }));

        assertEquals(2, calls.get());
        assertEquals("always fails", thrown.getCause().getMessage());
    }

    @Test
    void sleepsBetweenAttemptsButNotAfterTheLastOne() {
        List<Duration> recordedSleeps = new ArrayList<>();
        Retryer retryer = new Retryer(new RetryConfig(3, Duration.ofMillis(50)), recordedSleeps::add);

        assertThrows(RetryExhaustedException.class, () -> retryer.execute(() -> {
            throw new RuntimeException("always fails");
        }));

        assertEquals(List.of(Duration.ofMillis(50), Duration.ofMillis(50)), recordedSleeps);
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(0, Duration.ofMillis(10)));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(3, Duration.ofMillis(-1)));
    }

    private static Sleeper noOpSleeper() {
        return duration -> { };
    }
}
````

### retry · run 5 · baseline

````java
import java.util.concurrent.Callable;

/**
 * Retries an operation a fixed number of times, waiting a fixed delay
 * between attempts, and rethrows the last failure if all attempts fail.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on any exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delayMillis fixed delay between attempts, in milliseconds
     * @param <T>         the operation's result type
     * @return the operation's result on success
     * @throws RetryException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RetryException(
                            "Retry interrupted while waiting between attempts", interruptedException);
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastException);
    }

    /**
     * Convenience overload for operations that throw no checked exceptions.
     */
    public static void runWithRetry(Runnable operation, int maxAttempts, long delayMillis) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMillis);
    }

    /**
     * Thrown when an operation still fails after exhausting all retry attempts.
     */
    public static final class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Example usage:
    //
    // String result = RetryHelper.runWithRetry(() -> callFlakyService(), 3, 500);
    //
    // RetryHelper.runWithRetry(() -> writeToFile(data), 5, 1000);
}
````

### retry · run 5 · uncle-bob-junior

````java
// Operation.java
package com.plg.retry;

@FunctionalInterface
public interface Operation<T> {
    T execute() throws Exception;
}
````

````java
// RetryPolicy.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
````

````java
// RetryExhaustedException.java
package com.plg.retry;

public final class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
````

````java
// RetryInterruptedException.java
package com.plg.retry;

public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry was interrupted while waiting between attempts", cause);
    }
}
````

````java
// Retryer.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class Retryer {

    private Retryer() {
    }

    public static <T> T run(Operation<T> operation, RetryPolicy policy) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(policy, "policy");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < policy.maxAttempts()) {
                    waitBeforeRetry(policy.delayBetweenAttempts());
                }
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private static void waitBeforeRetry(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
````

````java
// RetryerTest.java
package com.plg.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultWhenOperationSucceedsFirstTry() {
        AtomicInteger calls = new AtomicInteger();
        RetryPolicy policy = RetryPolicy.of(3, NO_DELAY);

        String result = Retryer.run(() -> {
            calls.incrementAndGet();
            return "ok";
        }, policy);

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        int failuresBeforeSuccess = 2;
        RetryPolicy policy = RetryPolicy.of(5, NO_DELAY);

        String result = Retryer.run(() -> {
            if (calls.incrementAndGet() <= failuresBeforeSuccess) {
                throw new RuntimeException("transient failure");
            }
            return "recovered";
        }, policy);

        assertEquals("recovered", result);
        assertEquals(failuresBeforeSuccess + 1, calls.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        AtomicInteger calls = new AtomicInteger();
        int maxAttempts = 3;
        RetryPolicy policy = RetryPolicy.of(maxAttempts, NO_DELAY);
        RuntimeException persistentFailure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                Retryer.run(() -> {
                    calls.incrementAndGet();
                    throw persistentFailure;
                }, policy));

        assertEquals(maxAttempts, calls.get());
        assertEquals(maxAttempts, thrown.attemptsMade());
        assertEquals(persistentFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() {
        Duration delay = Duration.ofMillis(50);
        int maxAttempts = 3;
        RetryPolicy policy = RetryPolicy.of(maxAttempts, delay);

        long start = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                Retryer.run(() -> {
                    throw new RuntimeException("always fails");
                }, policy));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        long expectedMinimumDelay = delay.toMillis() * (maxAttempts - 1);
        assertTrue(elapsedMillis >= expectedMinimumDelay,
                "expected at least " + expectedMinimumDelay + "ms elapsed, was " + elapsedMillis);
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, NO_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(3, Duration.ofMillis(-1)));
    }

    @Test
    void wrapsInterruptionAndRestoresInterruptStatus() throws InterruptedException {
        RetryPolicy policy = RetryPolicy.of(2, Duration.ofSeconds(5));
        AtomicInteger interruptedFlagAfterRun = new AtomicInteger(-1);

        Thread worker = new Thread(() -> {
            try {
                Retryer.run(() -> {
                    throw new RuntimeException("fails, forcing a wait");
                }, policy);
            } catch (RetryInterruptedException expected) {
                interruptedFlagAfterRun.set(Thread.currentThread().isInterrupted() ? 1 : 0);
            }
        });
        worker.start();
        Thread.sleep(50);
        worker.interrupt();
        worker.join();

        assertEquals(1, interruptedFlagAfterRun.get());
    }
}
````

### retry · run 6 · baseline

````java
import java.time.Duration;
import java.util.Objects;

/**
 * Generic retry helper: runs an operation, retrying on exception up to a
 * maximum number of attempts, waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface RetryableAction {
        void execute() throws Exception;
    }

    /**
     * Runs the given operation, retrying on any exception.
     *
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param operation   the operation to run
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(int maxAttempts, Duration delay, RetryableOperation<T> operation) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        Objects.requireNonNull(delay, "delay must not be null");
        Objects.requireNonNull(operation, "operation must not be null");

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    if (!delay.isZero() && !delay.isNegative()) {
                        Thread.sleep(delay.toMillis());
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted after " + attempt + " attempt(s)", lastFailure);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastFailure);
    }

    /** Convenience overload for operations that don't return a value. */
    public static void runWithRetry(int maxAttempts, Duration delay, RetryableAction action) {
        Objects.requireNonNull(action, "action must not be null");
        runWithRetry(maxAttempts, delay, () -> {
            action.execute();
            return null;
        });
    }

    /** Thrown when an operation still fails after exhausting all retry attempts. */
    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
import java.time.Duration;

public class Example {
    public static void main(String[] args) {
        String result = RetryHelper.runWithRetry(3, Duration.ofSeconds(2), () -> {
            return callFlakyService();
        });
        System.out.println(result);
    }

    private static String callFlakyService() throws Exception {
        // simulate a call that might throw
        return "ok";
    }
}
````

### retry · run 6 · uncle-bob-junior

````java
// Sleeper.java
package retry;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
````

````java
// ThreadSleeper.java
package retry;

import java.time.Duration;

final class ThreadSleeper implements Sleeper {
    @Override
    public void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
````

````java
// RetryableOperation.java
package retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T call() throws Exception;
}
````

````java
// RetryableAction.java
package retry;

@FunctionalInterface
public interface RetryableAction {
    void run() throws Exception;
}
````

````java
// RetryExhaustedException.java
package retry;

public final class RetryExhaustedException extends RuntimeException {

    private final int attempts;

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation did not succeed after " + attempts + " attempt(s)", lastFailure);
        this.attempts = attempts;
    }

    public int getAttempts() {
        return attempts;
    }
}
````

````java
// RetryInterruptedException.java
package retry;

public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(int attemptsCompleted, InterruptedException cause) {
        super("Retry loop interrupted after attempt " + attemptsCompleted, cause);
    }
}
````

````java
// RetryHelper.java
package retry;

import java.time.Duration;

public final class RetryHelper {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public RetryHelper(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, new ThreadSleeper());
    }

    RetryHelper(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = sleeper;
    }

    public void run(RetryableAction action) {
        run(() -> {
            action.run();
            return null;
        });
    }

    public <T> T run(RetryableOperation<T> operation) {
        Throwable lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    sleepBeforeRetry(attempt);
                }
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void sleepBeforeRetry(int attemptsCompleted) {
        try {
            sleeper.sleep(delayBetweenAttempts);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(attemptsCompleted, interrupted);
        }
    }
}
````

````java
// RetryHelperTest.java
package retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {

    private static final Duration DELAY = Duration.ofMillis(50);

    private static final class RecordingSleeper implements Sleeper {
        final List<Duration> calls = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            calls.add(duration);
        }
    }

    private static final class InterruptingSleeper implements Sleeper {
        @Override
        public void sleep(Duration duration) throws InterruptedException {
            throw new InterruptedException("simulated interruption");
        }
    }

    @Test
    void returnsResultWithoutRetryingOnFirstSuccess() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(3, DELAY, sleeper);

        String result = retryHelper.run(() -> "ok");

        assertEquals("ok", result);
        assertTrue(sleeper.calls.isEmpty());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(5, DELAY, sleeper);
        AtomicInteger callCount = new AtomicInteger();

        String result = retryHelper.run(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
        assertEquals(List.of(DELAY, DELAY), sleeper.calls);
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(3, DELAY, sleeper);
        RuntimeException failure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryHelper.run(() -> {
                    throw failure;
                }));

        assertEquals(3, thrown.getAttempts());
        assertSame(failure, thrown.getCause());
        assertEquals(2, sleeper.calls.size());
    }

    @Test
    void retriesVoidActionsToo() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(2, DELAY, sleeper);
        AtomicInteger callCount = new AtomicInteger();

        retryHelper.run(() -> {
            if (callCount.incrementAndGet() < 2) {
                throw new RuntimeException("not yet");
            }
        });

        assertEquals(2, callCount.get());
    }

    @Test
    void abortsAndRestoresInterruptFlagWhenSleepIsInterrupted() {
        RetryHelper retryHelper = new RetryHelper(5, DELAY, new InterruptingSleeper());

        assertThrows(RetryInterruptedException.class, () ->
                retryHelper.run(() -> {
                    throw new RuntimeException("always fails");
                }));

        assertTrue(Thread.interrupted(), "interrupt flag should have been restored");
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, Duration.ofMillis(-1)));
    }
}
````

### retry · run 7 · baseline

````java
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Reusable helper that retries an operation a fixed number of times,
 * waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Runs a value-returning operation, retrying on any thrown exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param unit        time unit for {@code delay}
     * @return the operation's result on the first successful attempt
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delay, TimeUnit unit) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    unit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", ie);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempts", lastFailure);
    }

    /**
     * Convenience overload for operations with no return value.
     */
    public static void retry(ThrowingRunnable operation, int maxAttempts, long delay, TimeUnit unit) {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay, unit);
    }

    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Example usage
    public static void main(String[] args) {
        int[] callCount = {0};

        String result = retry(() -> {
            callCount[0]++;
            if (callCount[0] < 3) {
                throw new RuntimeException("Simulated failure #" + callCount[0]);
            }
            return "success";
        }, 5, 500, TimeUnit.MILLISECONDS);

        System.out.println("Result: " + result + ", attempts: " + callCount[0]);
    }
}
````

### retry · run 7 · uncle-bob-junior

````java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * An operation that can be retried. May throw any exception; the retry
 * mechanism does not assume a checked/unchecked distinction.
 */
@FunctionalInterface
public interface RetryableOperation<T> {
    T execute() throws Exception;
}
````

````java
package com.example.retry;

import java.time.Duration;

/**
 * Configuration for a retry run.
 *
 * @param maxAttempts total number of tries, including the first (must be >= 1)
 * @param delay       fixed wait time between attempts (must not be negative)
 */
public record RetryPolicy(int maxAttempts, Duration delay) {

    private static final int MIN_ATTEMPTS = 1;

    public RetryPolicy {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be >= " + MIN_ATTEMPTS);
        }
        Objects.requireNonNull(delay, "delay must not be null");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
    }

    public static RetryPolicy of(int maxAttempts, Duration delay) {
        return new RetryPolicy(maxAttempts, delay);
    }
}
````

````java
package com.example.retry;

/**
 * Thrown when an operation still fails after all retry attempts are used up.
 * The original failure from the last attempt is available via {@link #getCause()}.
 */
public class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
````

````java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation, retrying on failure up to a configured number of
 * attempts with a fixed delay between them.
 */
public final class Retryer {

    private Retryer() {
    }

    public static <T> T execute(RetryableOperation<T> operation, RetryPolicy policy) throws InterruptedException {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception failure) {
                lastFailure = failure;
                if (hasAttemptsRemaining(attempt, policy)) {
                    sleep(policy.delay());
                }
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private static boolean hasAttemptsRemaining(int attemptJustMade, RetryPolicy policy) {
        return attemptJustMade < policy.maxAttempts();
    }

    private static void sleep(Duration delay) throws InterruptedException {
        Thread.sleep(delay.toMillis());
    }
}
````

````java
package com.example.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    @Test
    void returnsResultWhenOperationSucceedsOnFirstAttempt() throws InterruptedException {
        RetryPolicy policy = RetryPolicy.of(3, Duration.ZERO);

        String result = Retryer.execute(() -> "ok", policy);

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilOperationEventuallySucceeds() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.of(5, Duration.ZERO);

        String result = Retryer.execute(() -> {
            int attempt = callCount.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        }, policy);

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttemptsWithLastCausePreserved() {
        AtomicInteger callCount = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.of(3, Duration.ZERO);
        RuntimeException finalFailure = new RuntimeException("attempt 3 failed");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                Retryer.execute(() -> {
                    int attempt = callCount.incrementAndGet();
                    if (attempt == 3) {
                        throw finalFailure;
                    }
                    throw new RuntimeException("attempt " + attempt + " failed");
                }, policy));

        assertEquals(3, callCount.get());
        assertEquals(3, thrown.attemptsMade());
        assertEquals(finalFailure, thrown.getCause());
    }

    @Test
    void doesNotRetryWhenMaxAttemptsIsOne() {
        AtomicInteger callCount = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.of(1, Duration.ZERO);

        assertThrows(RetryExhaustedException.class, () ->
                Retryer.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }, policy));

        assertEquals(1, callCount.get());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() throws InterruptedException {
        Duration delay = Duration.ofMillis(50);
        RetryPolicy policy = RetryPolicy.of(3, delay);
        AtomicInteger callCount = new AtomicInteger(0);

        long start = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                Retryer.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }, policy));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        // 3 attempts -> 2 delays between them, no delay after the last attempt.
        assertTrue(elapsedMillis >= delay.toMillis() * 2);
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, Duration.ZERO));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(3, Duration.ofMillis(-1)));
    }
}
````

### retry · run 8 · baseline

````java
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed delay
 * between attempts when it throws an exception.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    @FunctionalInterface
    public interface RetryableRunnable {
        void run() throws Exception;
    }

    /**
     * Runs the given operation, retrying on any exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed delay between attempts
     * @param unit        time unit for the delay
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, long delay, TimeUnit unit)
            throws RetryExhaustedException {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        RetryExhaustedException failure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (failure == null) {
                    failure = new RetryExhaustedException(
                            "Operation failed after " + maxAttempts + " attempts", e);
                } else {
                    failure.addSuppressed(e);
                }

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    unit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    failure.addSuppressed(ie);
                    throw failure;
                }
            }
        }

        throw failure;
    }

    /** Convenience overload for operations that do not return a value. */
    public static void runWithRetry(RetryableRunnable operation, int maxAttempts, long delay, TimeUnit unit)
            throws RetryExhaustedException {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay, unit);
    }

    public static class RetryExhaustedException extends Exception {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
String result = RetryHelper.runWithRetry(
        () -> callFlakyService(),
        3,
        500,
        TimeUnit.MILLISECONDS
);

RetryHelper.runWithRetry(
        () -> writeToFile(data),
        5,
        1,
        TimeUnit.SECONDS
);
````

### retry · run 8 · uncle-bob-junior

````java
package com.example.retry;

import java.time.Duration;

/**
 * Waits for a fixed duration between retry attempts.
 * Implementations may throw {@link InterruptedException} if the wait is interrupted.
 */
@FunctionalInterface
interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
````

````java
package com.example.retry;

/**
 * Thrown when an operation still fails after all retry attempts are exhausted.
 * The cause is the exception from the last failed attempt.
 */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
````

````java
package com.example.retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed delay
 * between attempts whenever it throws an exception.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, Retryer::sleepFor);
    }

    Retryer(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = sleeper;
    }

    /**
     * Runs {@code operation}, retrying on any exception until it succeeds or
     * {@code maxAttempts} is reached.
     *
     * @throws RetryExhaustedException if every attempt fails
     * @throws IllegalStateException   if interrupted while waiting to retry
     */
    public <T> T execute(Callable<T> operation) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                waitBeforeNextAttempt(attempt);
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void waitBeforeNextAttempt(int attemptJustFailed) {
        if (attemptJustFailed >= maxAttempts) {
            return;
        }
        try {
            sleeper.sleep(delayBetweenAttempts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted while waiting to retry", e);
        }
    }

    private static void sleepFor(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
````

````java
package com.example.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration ANY_DELAY = Duration.ofMillis(50);
    private final RecordingSleeper sleeper = new RecordingSleeper();

    @Test
    void returnsResultWithoutRetryingWhenFirstAttemptSucceeds() {
        Retryer retryer = new Retryer(3, ANY_DELAY, sleeper);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
        assertEquals(0, sleeper.sleepCount());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        Retryer retryer = new Retryer(5, ANY_DELAY, sleeper);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            int attempt = calls.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("attempt " + attempt + " failed");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
        assertEquals(2, sleeper.sleepCount());
    }

    @Test
    void throwsRetryExhaustedExceptionWhenAllAttemptsFail() {
        int maxAttempts = 4;
        Retryer retryer = new Retryer(maxAttempts, ANY_DELAY, sleeper);
        AtomicInteger calls = new AtomicInteger();
        RuntimeException lastFailure = new RuntimeException("boom");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
            retryer.execute(() -> {
                calls.incrementAndGet();
                throw lastFailure;
            }));

        assertEquals(maxAttempts, calls.get());
        assertSame(lastFailure, thrown.getCause());
        assertEquals(maxAttempts - 1, sleeper.sleepCount());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() {
        Duration delay = Duration.ofMillis(200);
        Retryer retryer = new Retryer(3, delay, sleeper);

        assertThrows(RetryExhaustedException.class, () -> retryer.execute(() -> {
            throw new RuntimeException("boom");
        }));

        assertEquals(List.of(delay, delay), sleeper.recordedDelays());
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, ANY_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(3, Duration.ofMillis(-1)));
    }

    @Test
    void restoresInterruptStatusWhenSleepIsInterrupted() {
        Retryer retryer = new Retryer(3, ANY_DELAY, duration -> {
            throw new InterruptedException("interrupted");
        });

        try {
            assertThrows(IllegalStateException.class, () -> retryer.execute(() -> {
                throw new RuntimeException("boom");
            }));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> delays = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            delays.add(duration);
        }

        int sleepCount() {
            return delays.size();
        }

        List<Duration> recordedDelays() {
            return delays;
        }
    }
}
````

### retry · run 9 · baseline

````java
import java.time.Duration;

/**
 * Generic retry helper: runs an operation up to a maximum number of attempts,
 * waiting a fixed delay between attempts, and rethrows the last failure if all attempts fail.
 */
public final class Retry {

    private Retry() {
    }

    @FunctionalInterface
    public interface Operation<T> {
        T call() throws Exception;
    }

    public static class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Runs {@code operation}, retrying on any exception until it succeeds
     * or {@code maxAttempts} attempts have been made.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed delay to wait between attempts
     * @return the operation's result
     * @throws RetryException if all attempts fail; the last failure is set as the cause
     */
    public static <T> T run(Operation<T> operation, int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryException("Retry interrupted while waiting between attempts", interrupted);
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempts",
                lastException
        );
    }

    // Convenience overload for operations that don't return a value.
    public static void run(Runnable voidOperation, int maxAttempts, Duration delay) {
        run(() -> {
            voidOperation.run();
            return null;
        }, maxAttempts, delay);
    }
}
````

````java
String result = Retry.run(
        () -> callFlakyService(),
        3,
        Duration.ofSeconds(2)
);
````

### retry · run 9 · uncle-bob-junior

````java
import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed delay
 * between attempts when it throws an exception.
 *
 * <p>Thread-safe and stateless: a single instance can be reused across calls.
 */
public final class RetryExecutor {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public RetryExecutor(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1: " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
    }

    /**
     * Executes {@code operation}, retrying on any exception until it succeeds
     * or {@code maxAttempts} have been made.
     *
     * @throws RetryExhaustedException if every attempt failed; the last failure is the cause
     * @throws InterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(RetryableOperation<T> operation) throws InterruptedException {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastFailure = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayBetweenAttempts.toMillis());
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(int attemptsMade, Exception lastFailure) {
            super("Operation failed after " + attemptsMade + " attempts", lastFailure);
        }
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryExecutorTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultOnFirstSuccessWithoutRetrying() throws InterruptedException {
        RetryExecutor retryExecutor = new RetryExecutor(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger();

        String result = retryExecutor.execute(() -> {
            callCount.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, callCount.get());
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() throws InterruptedException {
        RetryExecutor retryExecutor = new RetryExecutor(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger();

        String result = retryExecutor.execute(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        RetryExecutor retryExecutor = new RetryExecutor(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger();

        RetryExecutor.RetryExhaustedException thrown = assertThrows(
                RetryExecutor.RetryExhaustedException.class,
                () -> retryExecutor.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("always fails");
                }));

        assertEquals(3, callCount.get());
        assertEquals("always fails", thrown.getCause().getMessage());
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryExecutor(0, NO_DELAY));
    }
}
````

### retry · run 10 · baseline

````java
import java.util.concurrent.Callable;

/**
 * Reusable retry helper: runs an operation, retrying up to a maximum number
 * of attempts on failure, with a fixed delay between attempts.
 */
public final class Retryer {

    private final int maxAttempts;
    private final long delayMillis;

    public Retryer(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    /**
     * Executes the given operation, retrying on any exception it throws.
     *
     * @param operation the operation to run
     * @param <T> the operation's return type
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public <T> T call(Callable<T> operation) throws RetryExhaustedException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean attemptsRemain = attempt < maxAttempts;
                if (!attemptsRemain) {
                    break;
                }

                if (delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RetryExhaustedException(
                                "Retry interrupted while waiting between attempts", attempt, ie);
                    }
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", maxAttempts, lastException);
    }

    /** Convenience overload for operations that return nothing. */
    public void run(RunnableWithException operation) throws RetryExhaustedException {
        call(() -> {
            operation.run();
            return null;
        });
    }

    /** A Runnable variant that may throw a checked exception. */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /** Thrown when all retry attempts have been exhausted. */
    public static final class RetryExhaustedException extends Exception {
        private final int attemptsMade;

        public RetryExhaustedException(String message, int attemptsMade, Throwable cause) {
            super(message, cause);
            this.attemptsMade = attemptsMade;
        }

        public int getAttemptsMade() {
            return attemptsMade;
        }
    }

    // Example usage:
    //
    // Retryer retryer = new Retryer(3, 500); // 3 attempts, 500ms fixed delay
    //
    // try {
    //     String result = retryer.call(() -> callFlakyService());
    // } catch (Retryer.RetryExhaustedException e) {
    //     // handle final failure; e.getCause() holds the last exception
    // }
}
````

### retry · run 10 · uncle-bob-junior

````java
// RetryConfig.java
import java.time.Duration;

/**
 * Immutable retry policy: how many attempts to make and how long to wait
 * between them.
 *
 * @param maxAttempts total number of attempts, including the first one (must be >= 1)
 * @param delay       fixed wait time between attempts (must not be negative)
 */
public record RetryConfig(int maxAttempts, Duration delay) {

    public RetryConfig {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
    }
}
````

````java
// Sleeper.java
import java.time.Duration;

/**
 * Seam for waiting between retries. Production code uses {@link #threadSleep()};
 * tests can supply a fake that records or fast-forwards the wait.
 */
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;

    static Sleeper threadSleep() {
        return duration -> Thread.sleep(duration.toMillis());
    }
}
````

````java
// RetryExhaustedException.java
/**
 * Thrown when an operation still fails after all configured attempts.
 * The cause is the exception from the last attempt.
 */
public class RetryExhaustedException extends Exception {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable cause) {
        super("Operation failed after " + attemptsMade + " attempt(s)", cause);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
````

````java
// Retryer.java
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it with a fixed delay between attempts when it
 * throws an exception.
 */
public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(Sleeper.threadSleep());
    }

    public Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * @throws RetryExhaustedException if every attempt allowed by {@code config} fails
     */
    public <T> T execute(Callable<T> operation, RetryConfig config) throws RetryExhaustedException {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;
            }
            if (attempt < config.maxAttempts()) {
                waitBeforeNextAttempt(config.delay(), attempt, lastFailure);
            }
        }

        throw new RetryExhaustedException(config.maxAttempts(), lastFailure);
    }

    private void waitBeforeNextAttempt(Duration delay, int attemptsMade, Exception lastFailure)
            throws RetryExhaustedException {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryExhaustedException(attemptsMade, lastFailure);
        }
    }
}
````

````java
// RetryerTest.java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration FIXED_DELAY = Duration.ofMillis(50);

    @Test
    void succeedsOnFirstAttempt_doesNotSleep() throws Exception {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);

        String result = retryer.execute(() -> "ok", new RetryConfig(3, FIXED_DELAY));

        assertEquals("ok", result);
        assertTrue(sleeper.recordedDelays.isEmpty());
    }

    @Test
    void succeedsAfterTransientFailures_returnsResultAndSleepsBetweenAttempts() throws Exception {
        AtomicInteger callCount = new AtomicInteger();
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);

        String result = retryer.execute(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        }, new RetryConfig(5, FIXED_DELAY));

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
        assertEquals(List.of(FIXED_DELAY, FIXED_DELAY), sleeper.recordedDelays);
    }

    @Test
    void allAttemptsFail_throwsRetryExhaustedExceptionWithCauseAndCount() {
        RuntimeException failure = new RuntimeException("always fails");
        Retryer retryer = new Retryer(new RecordingSleeper());

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    throw failure;
                }, new RetryConfig(3, FIXED_DELAY)));

        assertEquals(3, thrown.attemptsMade());
        assertEquals(failure, thrown.getCause());
    }

    @Test
    void doesNotSleepAfterFinalAttempt() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);

        assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    throw new RuntimeException("always fails");
                }, new RetryConfig(3, FIXED_DELAY)));

        assertEquals(2, sleeper.recordedDelays.size());
    }

    @Test
    void interruptedWhileWaiting_stopsRetryingAndRestoresInterruptFlag() {
        Sleeper interruptingSleeper = duration -> {
            throw new InterruptedException("simulated interrupt");
        };
        Retryer retryer = new Retryer(interruptingSleeper);
        AtomicInteger callCount = new AtomicInteger();

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("always fails");
                }, new RetryConfig(5, FIXED_DELAY)));

        assertEquals(1, callCount.get());
        assertEquals(1, thrown.attemptsMade());
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // clear flag so it doesn't leak into other tests
    }

    @Test
    void maxAttemptsBelowOne_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(0, FIXED_DELAY));
    }

    @Test
    void negativeDelay_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(3, Duration.ofMillis(-1)));
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            recordedDelays.add(duration);
        }
    }
}
````

## ratelimit

### ratelimit · run 1 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Clients are identified by remote IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    /** Convenience constructor: 5 requests per 60 seconds. */
    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        ClientWindow window = clients.computeIfAbsent(clientKey, k -> new ClientWindow());

        if (window.tryAcquire(maxRequests, windowMillis)) {
            try {
                delegate.handle(exchange);
            } finally {
                maybeCleanup();
            }
        } else {
            sendTooManyRequests(exchange, windowMillis);
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long windowMillis) throws IOException {
        byte[] body = "Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.getResponseHeaders().set("Retry-After", Long.toString(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    // Periodically drop client entries with no recent activity to bound memory use.
    private volatile long lastCleanup = 0L;

    private void maybeCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < windowMillis) {
            return;
        }
        lastCleanup = now;
        clients.entrySet().removeIf(e -> e.getValue().isStale(now, windowMillis));
    }

    /** Tracks request timestamps for one client within the sliding window. */
    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        boolean tryAcquire(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                evictOld(now, windowMillis);
                if (timestamps.size() >= maxRequests) {
                    return false;
                }
                timestamps.addLast(now);
                return true;
            } finally {
                lock.unlock();
            }
        }

        boolean isStale(long now, long windowMillis) {
            lock.lock();
            try {
                evictOld(now, windowMillis);
                return timestamps.isEmpty();
            } finally {
                lock.unlock();
            }
        }

        private void evictOld(long now, long windowMillis) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
                timestamps.pollFirst();
            }
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello, world!\n".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        // Limit to 5 requests per 60 seconds per client IP.
        server.createContext("/hello", new RateLimitingHandler(helloHandler, 5, 60_000L));

        server.start();
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
// File: TokenBucketRateLimiter.java
package com.postcodeloterij.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

/**
 * Per-client request limiter using a token bucket: each client key gets
 * {@code maxRequestsPerWindow} tokens that refill continuously over
 * {@code window}, so bursts are smoothed instead of reset at a fixed boundary.
 */
public final class TokenBucketRateLimiter {

    private final int maxRequestsPerWindow;
    private final long windowNanos;
    private final LongSupplier nanoClock;
    private final ConcurrentMap<String, TokenBucket> bucketsByClientKey = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int maxRequestsPerWindow, Duration window) {
        this(maxRequestsPerWindow, window, System::nanoTime);
    }

    TokenBucketRateLimiter(int maxRequestsPerWindow, Duration window, LongSupplier nanoClock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowNanos = window.toNanos();
        this.nanoClock = nanoClock;
    }

    /** Returns true if the client may proceed, false if it must be rejected. */
    public boolean tryAcquire(String clientKey) {
        TokenBucket bucket = bucketsByClientKey.computeIfAbsent(
                clientKey, key -> new TokenBucket(maxRequestsPerWindow, windowNanos, nanoClock));
        return bucket.tryConsume();
    }

    private static final class TokenBucket {
        private static final double MIN_TOKENS_TO_CONSUME = 1.0;

        private final double capacity;
        private final double refillTokensPerNano;
        private final LongSupplier nanoClock;
        private double availableTokens;
        private long lastRefillNanos;

        TokenBucket(int capacity, long windowNanos, LongSupplier nanoClock) {
            this.capacity = capacity;
            this.refillTokensPerNano = (double) capacity / windowNanos;
            this.nanoClock = nanoClock;
            this.availableTokens = capacity;
            this.lastRefillNanos = nanoClock.getAsLong();
        }

        synchronized boolean tryConsume() {
            refill();
            if (availableTokens < MIN_TOKENS_TO_CONSUME) {
                return false;
            }
            availableTokens -= MIN_TOKENS_TO_CONSUME;
            return true;
        }

        private void refill() {
            long now = nanoClock.getAsLong();
            long elapsedNanos = now - lastRefillNanos;
            if (elapsedNanos <= 0) {
                return;
            }
            availableTokens = Math.min(capacity, availableTokens + elapsedNanos * refillTokensPerNano);
            lastRefillNanos = now;
        }
    }
}
````

````java
// File: RateLimitingHandler.java
package com.postcodeloterij.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Wraps a delegate handler and rejects requests once a client exceeds its rate limit. */
public final class RateLimitingHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final byte[] RATE_LIMIT_MESSAGE =
            "Too Many Requests".getBytes(StandardCharsets.UTF_8);
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_SECONDS = "60";

    private final HttpHandler delegate;
    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitingHandler(HttpHandler delegate, TokenBucketRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!rateLimiter.tryAcquire(clientKeyOf(exchange))) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientKeyOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS);
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, RATE_LIMIT_MESSAGE.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(RATE_LIMIT_MESSAGE);
        }
    }
}
````

````java
// File: Main.java  (example wiring, not a class under test)
package com.postcodeloterij.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

public final class Main {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        TokenBucketRateLimiter rateLimiter =
                new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        server.createContext("/api", new RateLimitingHandler(
                exchange -> exchange.sendResponseHeaders(200, -1),
                rateLimiter));
        server.start();
    }
}
````

````java
// File: TokenBucketRateLimiterTest.java
package com.postcodeloterij.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    private static final int MAX_REQUESTS_PER_MINUTE = 3;
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);
    private static final String CLIENT_A = "192.0.2.1";
    private static final String CLIENT_B = "192.0.2.2";

    private final AtomicLong fakeNanos = new AtomicLong(0);
    private final TokenBucketRateLimiter rateLimiter =
            new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, ONE_MINUTE, fakeNanos::get);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(CLIENT_A), "request " + i + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimit() {
        exhaustLimitFor(CLIENT_A);

        assertFalse(rateLimiter.tryAcquire(CLIENT_A));
    }

    @Test
    void refillsTokensAfterFullWindowElapses() {
        exhaustLimitFor(CLIENT_A);

        fakeNanos.addAndGet(ONE_MINUTE.toNanos());

        assertTrue(rateLimiter.tryAcquire(CLIENT_A));
    }

    @Test
    void partiallyRefillsTokensProportionallyToElapsedTime() {
        exhaustLimitFor(CLIENT_A);

        fakeNanos.addAndGet(ONE_MINUTE.toNanos() / MAX_REQUESTS_PER_MINUTE);

        assertTrue(rateLimiter.tryAcquire(CLIENT_A), "one token should have refilled");
        assertFalse(rateLimiter.tryAcquire(CLIENT_A), "only one token should have refilled");
    }

    @Test
    void tracksEachClientIndependently() {
        exhaustLimitFor(CLIENT_A);

        assertTrue(rateLimiter.tryAcquire(CLIENT_B), "a different client should have its own budget");
    }

    @Test
    void rejectsNonPositiveLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> new TokenBucketRateLimiter(0, ONE_MINUTE));
    }

    private void exhaustLimitFor(String clientKey) {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(clientKey));
        }
    }
}
````

````java
// File: RateLimitingHandlerTest.java
package com.postcodeloterij.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitingHandlerTest {

    private static final int MAX_REQUESTS_PER_MINUTE = 1;
    private static final InetSocketAddress CLIENT_ADDRESS = new InetSocketAddress("192.0.2.10", 5555);

    @Test
    void forwardsRequestToDelegateWhenWithinLimit() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHandler handler = new RateLimitingHandler(delegate,
                new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        HttpExchange exchange = exchangeFrom(CLIENT_ADDRESS);

        handler.handle(exchange);

        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsWithTooManyRequestsWhenLimitExceeded() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHandler handler = new RateLimitingHandler(delegate,
                new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        handler.handle(exchangeFrom(CLIENT_ADDRESS));
        HttpExchange secondExchange = exchangeFrom(CLIENT_ADDRESS);

        handler.handle(secondExchange);

        verify(delegate, times(1)).handle(any());
        verify(secondExchange).sendResponseHeaders(eq(429), anyLong());
    }

    private HttpExchange exchangeFrom(InetSocketAddress clientAddress) throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(clientAddress);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
````

### ratelimit · run 2 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps an HttpHandler with a per-client rate limit using a fixed-window counter.
 */
public class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleaner");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        // Periodically drop windows that have expired so the map doesn't grow unbounded.
        cleaner.scheduleAtFixedRate(this::evictExpired, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        if (!allow(clientKey)) {
            byte[] body = "Rate limit exceeded. Please try again later.".getBytes();
            exchange.getResponseHeaders().set("Retry-After", String.valueOf(windowMillis / 1000));
            exchange.sendResponseHeaders(429, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
            return;
        }
        delegate.handle(exchange);
    }

    private boolean allow(String clientKey) {
        long now = System.currentTimeMillis();
        Window window = clients.computeIfAbsent(clientKey, k -> new Window(now));

        synchronized (window) {
            if (now - window.startTime >= windowMillis) {
                window.startTime = now;
                window.count.set(0);
            }
            return window.count.incrementAndGet() <= maxRequestsPerWindow;
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(entry -> {
            Window w = entry.getValue();
            synchronized (w) {
                return now - w.startTime >= windowMillis;
            }
        });
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    public void shutdown() {
        cleaner.shutdown();
    }

    private static final class Window {
        volatile long startTime;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long startTime) {
            this.startTime = startTime;
        }
    }

    // Example wiring:
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello, world!".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        };

        server.createContext("/hello", RateLimitingHandler.perMinute(helloHandler, 5));
        server.setExecutor(null);
        server.start();
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
// RateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fixed-window rate limiter. Thread-safe: each client's window is updated
 * via lock-free compare-and-swap, so concurrent requests for the same
 * client never double-count.
 */
public final class RateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowSize;
    private final Clock clock;
    // ubj: unbounded map, one entry per distinct client forever; add eviction
    // of stale windows if this runs long enough to see many unique clients.
    private final Map<String, AtomicReference<Window>> windowsByClient = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration windowSize, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSize = windowSize;
        this.clock = clock;
    }

    public boolean tryAcquire(String clientId) {
        AtomicReference<Window> slot = windowsByClient.computeIfAbsent(
                clientId, id -> new AtomicReference<>(new Window(clock.instant(), 0)));

        while (true) {
            Window current = slot.get();
            Window next = nextWindow(current);

            if (next.count() > maxRequestsPerWindow) {
                return false;
            }
            if (slot.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    private Window nextWindow(Window current) {
        if (isExpired(current)) {
            return new Window(clock.instant(), 1);
        }
        return new Window(current.start(), current.count() + 1);
    }

    private boolean isExpired(Window window) {
        return clock.instant().isAfter(window.start().plus(windowSize));
    }

    private record Window(Instant start, int count) {
    }
}
````

````java
// RateLimitingHttpHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Wraps a delegate handler and rejects clients that exceed the configured
 * rate with 429 Too Many Requests.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_SECONDS = "60";
    private static final byte[] RATE_LIMITED_BODY =
            "Too Many Requests".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!rateLimiter.tryAcquire(clientIdOf(exchange))) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS);
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, RATE_LIMITED_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(RATE_LIMITED_BODY);
        }
    }
}
````

````java
// Server.java — example wiring
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

public final class Server {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        RateLimiter rateLimiter =
                new RateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());

        server.createContext("/", new RateLimitingHttpHandler(
                exchange -> {
                    byte[] response = "OK".getBytes();
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                },
                rateLimiter));

        server.start();
    }
}
````

````java
// RateLimiterTest.java
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Test
    void allowsRequestsUpToTheLimit() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsBeyondTheLimit() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire("client-a");
        }

        assertFalse(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire("client-a");
        }

        assertTrue(rateLimiter.tryAcquire("client-b"));
    }

    @Test
    void resetsTheLimitAfterTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, clock);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire("client-a");
        }
        assertFalse(rateLimiter.tryAcquire("client-a"));

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(rateLimiter.tryAcquire("client-a"));
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advanceBy(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("not needed for tests");
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
````

### ratelimit · run 3 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Not suitable for multi-instance deployments without a shared store (e.g. Redis),
 * since state is kept in memory per process.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();

    // Bound how many idle client entries we keep around before sweeping.
    private final int maxTrackedClients;

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this(delegate, maxRequests, windowMillis, 10_000);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis, int maxTrackedClients) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.maxTrackedClients = maxTrackedClients;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        ClientState state = clients.computeIfAbsent(clientId, id -> new ClientState());

        if (!state.tryAcquire(maxRequests, windowMillis)) {
            sendTooManyRequests(exchange);
            return;
        }

        maybeEvictStaleClients();
        delegate.handle(exchange);
    }

    private String resolveClientId(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    // Cheap opportunistic cleanup; avoids a dedicated background thread.
    private void maybeEvictStaleClients() {
        if (clients.size() <= maxTrackedClients) {
            return;
        }
        long cutoff = System.currentTimeMillis() - windowMillis;
        clients.entrySet().removeIf(e -> e.getValue().isStaleSince(cutoff));
    }

    private static final class ClientState {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        boolean tryAcquire(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                long cutoff = now - windowMillis;
                while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                    timestamps.pollFirst();
                }
                if (timestamps.size() >= maxRequests) {
                    return false;
                }
                timestamps.addLast(now);
                return true;
            } finally {
                lock.unlock();
            }
        }

        boolean isStaleSince(long cutoff) {
            lock.lock();
            try {
                return timestamps.isEmpty() || timestamps.peekLast() < cutoff;
            } finally {
                lock.unlock();
            }
        }
    }

    // Example wiring:
    //
    // HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    // server.createContext("/api", new RateLimitingHandler(realHandler, 5, 60_000));
    // server.start();
}
````

### ratelimit · run 3 · uncle-bob-junior

````java
package com.plg.ratelimit;

/**
 * Immutable token-bucket state. Tokens refill continuously between the
 * configured capacity and zero, proportional to elapsed time.
 */
final class TokenBucket {

    private final double availableTokens;
    private final long lastRefillNanos;

    private TokenBucket(double availableTokens, long lastRefillNanos) {
        this.availableTokens = availableTokens;
        this.lastRefillNanos = lastRefillNanos;
    }

    static TokenBucket full(int capacity, long nowNanos) {
        return new TokenBucket(capacity, nowNanos);
    }

    TokenBucket refill(long nowNanos, int capacity, long windowNanos) {
        long elapsedNanos = Math.max(0, nowNanos - lastRefillNanos);
        double refillRatePerNano = (double) capacity / windowNanos;
        double refilled = Math.min(capacity, availableTokens + elapsedNanos * refillRatePerNano);
        return new TokenBucket(refilled, nowNanos);
    }

    boolean hasTokenAvailable() {
        return availableTokens >= 1.0;
    }

    TokenBucket consumeOne() {
        return new TokenBucket(availableTokens - 1.0, lastRefillNanos);
    }
}
````

````java
package com.plg.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Per-client token-bucket rate limiter.
 *
 * <p>Thread-safe: each client's bucket is updated via a lock-free
 * compare-and-swap loop, so concurrent requests from the same client
 * never over-consume tokens.
 */
public final class RateLimiter {

    private final int maxRequestsPerWindow;
    private final long windowNanos;
    private final LongSupplier nanoClock;
    private final ConcurrentHashMap<String, AtomicReference<TokenBucket>> bucketsByClient =
            new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration window) {
        this(maxRequestsPerWindow, window, System::nanoTime);
    }

    RateLimiter(int maxRequestsPerWindow, Duration window, LongSupplier nanoClock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowNanos = window.toNanos();
        this.nanoClock = nanoClock;
    }

    public boolean tryAcquire(String clientKey) {
        long now = nanoClock.getAsLong();
        AtomicReference<TokenBucket> bucketRef = bucketsByClient.computeIfAbsent(
                clientKey, key -> new AtomicReference<>(TokenBucket.full(maxRequestsPerWindow, now)));

        while (true) {
            TokenBucket current = bucketRef.get();
            TokenBucket refilled = current.refill(now, maxRequestsPerWindow, windowNanos);

            if (!refilled.hasTokenAvailable()) {
                bucketRef.compareAndSet(current, refilled);
                return false;
            }

            if (bucketRef.compareAndSet(current, refilled.consumeOne())) {
                return true;
            }
        }
    }
}
````

````java
package com.plg.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps a delegate handler and rejects requests over the configured
 * per-client rate with HTTP 429.
 */
public final class RateLimitedHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final byte[] RATE_LIMITED_BODY =
            "Rate limit exceeded. Please slow down.".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!rateLimiter.tryAcquire(clientKey)) {
            rejectWithTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, RATE_LIMITED_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(RATE_LIMITED_BODY);
        }
    }
}
````

````java
package com.plg.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";
    private static final int MAX_REQUESTS_PER_MINUTE = 3;

    private final AtomicLong fakeNanoTime = new AtomicLong(0);
    private final RateLimiter rateLimiter = new RateLimiter(
            MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1), fakeNanoTime::get);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(CLIENT_A), "request " + i + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimit() {
        exhaustLimit(CLIENT_A);

        assertFalse(rateLimiter.tryAcquire(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        exhaustLimit(CLIENT_A);

        assertTrue(rateLimiter.tryAcquire(CLIENT_B), "a different client must have its own budget");
    }

    @Test
    void refillsGraduallyAsTimePasses() {
        exhaustLimit(CLIENT_A);
        advanceTimeBy(Duration.ofSeconds(20));

        assertTrue(rateLimiter.tryAcquire(CLIENT_A), "one third of the window should refill one token");
    }

    @Test
    void refillsFullyAfterWindowElapses() {
        exhaustLimit(CLIENT_A);
        advanceTimeBy(Duration.ofMinutes(1));

        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(CLIENT_A), "bucket should be fully refilled after a window");
        }
    }

    private void exhaustLimit(String clientKey) {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            rateLimiter.tryAcquire(clientKey);
        }
    }

    private void advanceTimeBy(Duration duration) {
        fakeNanoTime.addAndGet(duration.toNanos());
    }
}
````

````java
RateLimiter rateLimiter = new RateLimiter(5, Duration.ofMinutes(1));
server.createContext("/api", new RateLimitedHttpHandler(actualHandler, rateLimiter));
````

### ratelimit · run 4 · baseline

````java
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Client identity is derived from the remote IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, Duration window) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, Duration.ofMinutes(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFor(exchange);
        ClientState state = clients.computeIfAbsent(clientId, id -> new ClientState());

        long retryAfterMillis = state.recordAndCheck(window.toMillis(), maxRequestsPerWindow);
        if (retryAfterMillis >= 0) {
            sendTooManyRequests(exchange, retryAfterMillis);
            return;
        }

        delegate.handle(exchange);
    }

    private static String clientIdFor(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private static void sendTooManyRequests(HttpExchange exchange, long retryAfterMillis) throws IOException {
        long retryAfterSeconds = Math.max(1, (retryAfterMillis + 999) / 1000);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Retry-After", String.valueOf(retryAfterSeconds));
        headers.set("Content-Type", "text/plain; charset=utf-8");

        byte[] body = "429 Too Many Requests".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /**
     * Tracks request timestamps for one client using a sliding window.
     * Guarded by a lock rather than a concurrent collection because the
     * check-and-evict-and-record sequence must be atomic.
     */
    private static final class ClientState {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * Returns -1 if the request is allowed (and records it), otherwise
         * returns the number of milliseconds until the next slot frees up.
         */
        long recordAndCheck(long windowMillis, int maxRequests) {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                long windowStart = now - windowMillis;

                while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                    timestamps.pollFirst();
                }

                if (timestamps.size() >= maxRequests) {
                    long oldest = timestamps.peekFirst();
                    return (oldest + windowMillis) - now;
                }

                timestamps.addLast(now);
                return -1;
            } finally {
                lock.unlock();
            }
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api", RateLimitingHandler.perMinute(exchange -> {
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }, 5)); // max 5 requests per minute per client IP

        server.setExecutor(null);
        server.start();
    }
}
````

### ratelimit · run 4 · uncle-bob-junior

````java
// RateLimiter.java
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
````

````java
// SlidingWindowRateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class SlidingWindowRateLimiter implements RateLimiter, AutoCloseable {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentMap<String, Deque<Instant>> timestampsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = new Thread(runnable, "rate-limiter-cleanup");
                thread.setDaemon(true);
                return thread;
            });

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, Clock.systemUTC());
    }

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
        long periodMillis = windowDuration.toMillis();
        cleanupExecutor.scheduleAtFixedRate(this::evictStaleClients, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryAcquire(String clientId) {
        Deque<Instant> timestamps = timestampsByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (timestamps) {
            Instant now = clock.instant();
            evictExpired(timestamps, now);
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant windowStart = now.minus(windowDuration);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.removeFirst();
        }
    }

    private void evictStaleClients() {
        Instant now = clock.instant();
        timestampsByClient.forEach((clientId, timestamps) -> {
            synchronized (timestamps) {
                evictExpired(timestamps, now);
                if (timestamps.isEmpty()) {
                    timestampsByClient.remove(clientId, timestamps);
                }
            }
        });
    }

    @Override
    public void close() {
        cleanupExecutor.shutdown();
    }
}
````

````java
// RateLimitedHttpHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class RateLimitedHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = windowDuration.getSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String resolveClientId(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Rate limit exceeded. Please try again later.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
````

````java
// RateLimiterWiringExample.java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

public final class RateLimiterWiringExample {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW);
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/api", new RateLimitedHttpHandler(
                exchange -> exchange.sendResponseHeaders(200, -1),
                rateLimiter,
                RATE_LIMIT_WINDOW));
        server.start();
    }
}
````

````java
// SlidingWindowRateLimiterTest.java
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private MutableClock clock;
    private SlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        rateLimiter = new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);
    }

    @AfterEach
    void tearDown() {
        rateLimiter.close();
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        assertTrue(rateLimiter.tryAcquire("client-a"));
        assertTrue(rateLimiter.tryAcquire("client-a"));
        assertTrue(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void blocksRequestsBeyondTheLimit() {
        acquireMaxRequests("client-a");
        assertFalse(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void allowsRequestsAgainAfterWindowElapses() {
        acquireMaxRequests("client-a");
        clock.advanceBy(WINDOW.plusSeconds(1));
        assertTrue(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        acquireMaxRequests("client-a");
        assertTrue(rateLimiter.tryAcquire("client-b"));
    }

    private void acquireMaxRequests(String clientId) {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire(clientId);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        MutableClock(Instant initialInstant) {
            this.currentInstant = initialInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }
    }
}
````

````java
// RateLimitedHttpHandlerTest.java
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitedHttpHandlerTest {

    private static final String CLIENT_IP = "192.0.2.1";

    private HttpHandler delegate;
    private RateLimiter rateLimiter;
    private RateLimitedHttpHandler handler;
    private HttpExchange exchange;

    @BeforeEach
    void setUp() throws IOException {
        delegate = mock(HttpHandler.class);
        rateLimiter = mock(RateLimiter.class);
        handler = new RateLimitedHttpHandler(delegate, rateLimiter, Duration.ofMinutes(1));

        exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(CLIENT_IP, 54321));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
    }

    @Test
    void delegatesWhenWithinLimit() throws IOException {
        when(rateLimiter.tryAcquire(CLIENT_IP)).thenReturn(true);

        handler.handle(exchange);

        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsWithTooManyRequestsWhenLimitExceeded() throws IOException {
        when(rateLimiter.tryAcquire(CLIENT_IP)).thenReturn(false);

        handler.handle(exchange);

        verify(delegate, never()).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(429), anyLong());
        assertEquals("60", exchange.getResponseHeaders().getFirst("Retry-After"));
    }
}
````

### ratelimit · run 5 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        ClientWindow window = clients.computeIfAbsent(clientKey, k -> new ClientWindow());

        if (window.tryAcquire(maxRequestsPerWindow, windowMillis)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }

        evictStaleEntriesOccasionally();
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictStaleEntriesOccasionally() {
        if (clients.size() < 10_000) {
            return;
        }
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> now - e.getValue().windowStart.get() > windowMillis * 2);
    }

    private static final class ClientWindow {
        private final AtomicLong windowStart = new AtomicLong(0);
        private final AtomicInteger count = new AtomicInteger(0);

        synchronized boolean tryAcquire(int max, long windowMillis) {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            if (start == 0 || now - start >= windowMillis) {
                windowStart.set(now);
                count.set(1);
                return true;
            }

            if (count.get() < max) {
                count.incrementAndGet();
                return true;
            }

            return false;
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class ServerSetup {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", new RateLimitingHandler(new MyApiHandler(), 5, 60_000L));
        server.start();
    }
}
````

### ratelimit · run 5 · uncle-bob-junior

````java
// RateLimiter.java
public interface RateLimiter {
    boolean allowRequest(String clientId);
}
````

````java
// TokenBucket.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Not thread-safe on its own; callers must synchronize access to a single instance. */
final class TokenBucket {

    private final int capacityTokens;
    private final int refillTokens;
    private final Duration refillPeriod;
    private final Clock clock;

    private double availableTokens;
    private Instant lastRefillTime;

    TokenBucket(int capacityTokens, int refillTokens, Duration refillPeriod, Clock clock) {
        this.capacityTokens = capacityTokens;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
        this.clock = clock;
        this.availableTokens = capacityTokens;
        this.lastRefillTime = clock.instant();
    }

    synchronized boolean tryConsume() {
        refill();
        if (availableTokens < 1) {
            return false;
        }
        availableTokens -= 1;
        return true;
    }

    private void refill() {
        Instant now = clock.instant();
        double periodsElapsed = Duration.between(lastRefillTime, now).toNanos()
                / (double) refillPeriod.toNanos();
        double tokensToAdd = periodsElapsed * refillTokens;
        if (tokensToAdd <= 0) {
            return;
        }
        availableTokens = Math.min(capacityTokens, availableTokens + tokensToAdd);
        lastRefillTime = now;
    }
}
````

````java
// TokenBucketRateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TokenBucketRateLimiter implements RateLimiter {

    public static final int DEFAULT_MAX_REQUESTS_PER_MINUTE = 5;
    public static final Duration DEFAULT_REFILL_PERIOD = Duration.ofMinutes(1);

    private final int capacityTokens;
    private final int refillTokens;
    private final Duration refillPeriod;
    private final Clock clock;

    // ubj: unbounded per-client map; fine for a small/known client set.
    // Add eviction of idle buckets if the distinct-client population grows unbounded.
    private final ConcurrentMap<String, TokenBucket> bucketsByClient = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacityTokens, int refillTokens, Duration refillPeriod, Clock clock) {
        this.capacityTokens = capacityTokens;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
        this.clock = clock;
    }

    public static TokenBucketRateLimiter withDefaultLimit() {
        return new TokenBucketRateLimiter(
                DEFAULT_MAX_REQUESTS_PER_MINUTE, DEFAULT_MAX_REQUESTS_PER_MINUTE,
                DEFAULT_REFILL_PERIOD, Clock.systemUTC());
    }

    @Override
    public boolean allowRequest(String clientId) {
        return bucketsByClient
                .computeIfAbsent(clientId, id -> new TokenBucket(capacityTokens, refillTokens, refillPeriod, clock))
                .tryConsume();
    }
}
````

````java
// RateLimitingHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Duration;

public final class RateLimitingHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final int NO_RESPONSE_BODY = -1;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Duration retryAfter;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration retryAfter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfter = retryAfter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFrom(exchange);
        if (!rateLimiter.allowRequest(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdFrom(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfter.toSeconds()));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
````

````java
// ExampleServerSetup.java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

final class ExampleServerSetup {
    private ExampleServerSetup() {
    }

    static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimiter rateLimiter = TokenBucketRateLimiter.withDefaultLimit();
        server.createContext("/api", new RateLimitingHandler(
                exchange -> exchange.sendResponseHeaders(200, NO_BODY_LENGTH),
                rateLimiter,
                Duration.ofSeconds(RETRY_AFTER_SECONDS)));
        server.start();
    }

    private static final int NO_BODY_LENGTH = -1;
    private static final int RETRY_AFTER_SECONDS = 30;
}
````

````java
// AdjustableTestClock.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

final class AdjustableTestClock extends Clock {
    private final ZoneId zone;
    private Instant currentInstant;

    AdjustableTestClock(Instant start) {
        this(start, ZoneOffset.UTC);
    }

    private AdjustableTestClock(Instant start, ZoneId zone) {
        this.currentInstant = start;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new AdjustableTestClock(currentInstant, zone);
    }

    @Override
    public Instant instant() {
        return currentInstant;
    }

    void advanceBy(Duration duration) {
        currentInstant = currentInstant.plus(duration);
    }
}
````

````java
// TokenBucketRateLimiterTest.java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    private static final String CLIENT_A = "participant_001";
    private static final String CLIENT_B = "participant_002";
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    @Test
    void allowsUpToCapacityRequestsImmediately() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                3, 3, ONE_MINUTE, new AdjustableTestClock(Instant.EPOCH));

        assertTrue(limiter.allowRequest(CLIENT_A));
        assertTrue(limiter.allowRequest(CLIENT_A));
        assertTrue(limiter.allowRequest(CLIENT_A));
    }

    @Test
    void rejectsRequestBeyondCapacityWithinSamePeriod() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                2, 2, ONE_MINUTE, new AdjustableTestClock(Instant.EPOCH));

        limiter.allowRequest(CLIENT_A);
        limiter.allowRequest(CLIENT_A);

        assertFalse(limiter.allowRequest(CLIENT_A));
    }

    @Test
    void refillsTokensAfterPeriodElapses() {
        AdjustableTestClock clock = new AdjustableTestClock(Instant.EPOCH);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, ONE_MINUTE, clock);

        assertTrue(limiter.allowRequest(CLIENT_A));
        assertFalse(limiter.allowRequest(CLIENT_A));

        clock.advanceBy(ONE_MINUTE);

        assertTrue(limiter.allowRequest(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                1, 1, ONE_MINUTE, new AdjustableTestClock(Instant.EPOCH));

        assertTrue(limiter.allowRequest(CLIENT_A));
        assertFalse(limiter.allowRequest(CLIENT_A));
        assertTrue(limiter.allowRequest(CLIENT_B));
    }
}
````

````java
// RateLimitingHandlerTest.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class RateLimitingHandlerTest {

    private static final InetSocketAddress CLIENT_ADDRESS =
            new InetSocketAddress("203.0.113.5", 54321);
    private static final Duration RETRY_AFTER = Duration.ofSeconds(30);

    @Test
    void delegatesWhenRequestIsAllowed() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.allowRequest(any())).thenReturn(true);
        HttpExchange exchange = exchangeFromClient(CLIENT_ADDRESS);

        new RateLimitingHandler(delegate, rateLimiter, RETRY_AFTER).handle(exchange);

        verify(delegate).handle(exchange);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void respondsWithTooManyRequestsWhenRateLimited() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.allowRequest(any())).thenReturn(false);
        HttpExchange exchange = exchangeFromClient(CLIENT_ADDRESS);

        new RateLimitingHandler(delegate, rateLimiter, RETRY_AFTER).handle(exchange);

        verify(delegate, never()).handle(any());
        verify(exchange).sendResponseHeaders(429, -1);
        verify(exchange).close();
    }

    private HttpExchange exchangeFromClient(InetSocketAddress address) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(address);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        return exchange;
    }
}
````

### ratelimit · run 6 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps an HttpHandler with a simple fixed-window rate limiter,
 * keyed by client IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    // Bound memory: drop entries not touched in a while, checked opportunistically.
    private final AtomicLong lastCleanup = new AtomicLong(System.currentTimeMillis());
    private final long cleanupIntervalMillis;

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerMinute) {
        this(delegate, maxRequestsPerMinute, 60_000L);
    }

    RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        this.cleanupIntervalMillis = windowMillis * 5;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        maybeCleanup(now);

        Window window = windows.computeIfAbsent(clientKey, k -> new Window(now));
        if (!window.tryAcquire(now, windowMillis, maxRequestsPerWindow)) {
            long retryAfterSeconds = Math.max(1, (window.windowStart.get() + windowMillis - now) / 1000);
            sendTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        String body = "Rate limit exceeded. Try again later.";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(429, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void maybeCleanup(long now) {
        long last = lastCleanup.get();
        if (now - last < cleanupIntervalMillis) {
            return;
        }
        if (!lastCleanup.compareAndSet(last, now)) {
            return; // another thread is already cleaning up
        }
        windows.entrySet().removeIf(e -> now - e.getValue().windowStart.get() > cleanupIntervalMillis);
    }

    /** Per-client fixed window counter. */
    private static final class Window {
        final AtomicLong windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long start) {
            this.windowStart = new AtomicLong(start);
        }

        synchronized boolean tryAcquire(long now, long windowMillis, int max) {
            if (now - windowStart.get() >= windowMillis) {
                windowStart.set(now);
                count.set(0);
            }
            if (count.get() >= max) {
                return false;
            }
            count.incrementAndGet();
            return true;
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api", new RateLimitingHandler(exchange -> {
            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }, 5)); // 5 requests per minute per client IP

        server.setExecutor(null);
        server.start();
    }
}
````

### ratelimit · run 6 · uncle-bob-junior

````java
// RateLimiter.java
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
````

````java
// FixedWindowRateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

// ubj: unbounded map growth across distinct clientIds — add a scheduled
// eviction of expired windows if this runs long enough for that to matter.
public final class FixedWindowRateLimiter implements RateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windowsByClient = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        Window updated = windowsByClient.compute(clientId, (id, existing) -> nextWindow(existing, now));
        return updated.count() <= maxRequestsPerWindow;
    }

    private Window nextWindow(Window existing, long now) {
        if (existing == null || isExpired(existing, now)) {
            return new Window(now, 1);
        }
        return new Window(existing.startMillis(), existing.count() + 1);
    }

    private boolean isExpired(Window window, long now) {
        return now - window.startMillis() >= windowDuration.toMillis();
    }

    private record Window(long startMillis, int count) {}
}
````

````java
// RateLimitingHttpHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int RATE_LIMIT_STATUS_CODE = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Duration windowDuration;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.windowDuration = windowDuration;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Too many requests, slow down.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(windowDuration.toSeconds()));
        exchange.sendResponseHeaders(RATE_LIMIT_STATUS_CODE, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
````

````java
// Usage example (not a separate file, just wiring):
//
// HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
// RateLimiter limiter = new FixedWindowRateLimiter(5, Duration.ofMinutes(1), Clock.systemUTC());
// server.createContext("/api", new RateLimitingHttpHandler(new MyApiHandler(), limiter, Duration.ofMinutes(1)));
// server.start();
````

````java
// MutableClock.java (test helper)
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

final class MutableClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }

    void advanceBy(Duration duration) {
        instant = instant.plus(duration);
    }
}
````

````java
// FixedWindowRateLimiterTest.java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private MutableClock clock;
    private FixedWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        rateLimiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);
    }

    @Test
    void allowsRequestsUpToLimit() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.tryAcquire("client-1"));
        }
    }

    @Test
    void rejectsRequestBeyondLimit() {
        exhaustLimitFor("client-1");
        assertFalse(rateLimiter.tryAcquire("client-1"));
    }

    @Test
    void resetsAfterWindowElapses() {
        exhaustLimitFor("client-1");
        clock.advanceBy(WINDOW);
        assertTrue(rateLimiter.tryAcquire("client-1"));
    }

    @Test
    void tracksClientsIndependently() {
        exhaustLimitFor("client-1");
        assertTrue(rateLimiter.tryAcquire("client-2"));
    }

    private void exhaustLimitFor(String clientId) {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire(clientId);
        }
    }
}
````

````java
// RateLimitingHttpHandlerTest.java
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RateLimitingHttpHandlerTest {

    private HttpExchange exchange;
    private HttpHandler delegate;
    private Headers responseHeaders;

    @BeforeEach
    void setUp() throws IOException {
        exchange = mock(HttpExchange.class);
        delegate = mock(HttpHandler.class);
        responseHeaders = new Headers();
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("192.0.2.1", 5000));
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
    }

    @Test
    void passesThroughWhenUnderLimit() throws IOException {
        RateLimitingHttpHandler handler = handlerAllowing(true);
        handler.handle(exchange);
        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsWithTooManyRequestsWhenOverLimit() throws IOException {
        RateLimitingHttpHandler handler = handlerAllowing(false);
        handler.handle(exchange);
        verify(delegate, never()).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(429), anyLong());
        assertEquals("60", responseHeaders.getFirst("Retry-After"));
    }

    private RateLimitingHttpHandler handlerAllowing(boolean allowed) {
        RateLimiter stubLimiter = clientId -> allowed;
        return new RateLimitingHttpHandler(delegate, stubLimiter, Duration.ofSeconds(60));
    }
}
````

### ratelimit · run 7 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Not suitable for multi-instance deployments without a shared store.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleaner");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        // Periodically drop clients with no recent activity so the map doesn't grow forever.
        cleaner.scheduleAtFixedRate(this::evictStaleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        if (!tryConsume(clientKey, now)) {
            sendTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private boolean tryConsume(String clientKey, long now) {
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            long windowStart = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictStaleClients(long nowUnused) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                    timestamps.pollFirst();
                }
                return timestamps.isEmpty();
            }
        });
    }

    private void evictStaleClients() {
        evictStaleClients(0L);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "429 Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello\n".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        };

        // 5 requests per minute per client IP
        server.createContext("/hello", RateLimitingHandler.perMinute(helloHandler, 5));
        server.start();
    }
}
````

### ratelimit · run 7 · uncle-bob-junior

````java
package com.plg.ratelimit;

@FunctionalInterface
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
````

````java
package com.plg.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class SlidingWindowRateLimiter implements RateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Long>> requestTimestampsByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = Objects.requireNonNull(window);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public boolean tryAcquire(String clientId) {
        Deque<Long> timestamps = requestTimestampsByClient
                .computeIfAbsent(clientId, key -> new ConcurrentLinkedDeque<>());
        long now = clock.millis();
        long windowStart = now - window.toMillis();

        synchronized (timestamps) {
            evictExpired(timestamps, windowStart);
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictExpired(Deque<Long> timestamps, long windowStart) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }
    }
}
````

````java
package com.plg.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RATE_LIMIT_MESSAGE = "Rate limit exceeded. Please slow down.";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Duration window;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration window) {
        this.delegate = Objects.requireNonNull(delegate);
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
        this.window = Objects.requireNonNull(window);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);

        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = RATE_LIMIT_MESSAGE.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, String.valueOf(window.toSeconds()));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
````

````java
package com.plg.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        RateLimiter rateLimiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW, Clock.systemUTC());

        server.createContext("/",
                new RateLimitingHttpHandler(RateLimitedServerExample::handleRequest, rateLimiter, WINDOW));

        server.start();
    }

    private static void handleRequest(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
````

````java
package com.plg.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

final class MutableClock extends Clock {

    private Instant currentInstant;

    private MutableClock(Instant startingInstant) {
        this.currentInstant = startingInstant;
    }

    static MutableClock startingAt(Instant startingInstant) {
        return new MutableClock(startingInstant);
    }

    void advanceBy(Duration duration) {
        currentInstant = currentInstant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("not needed for testing");
    }

    @Override
    public Instant instant() {
        return currentInstant;
    }
}
````

````java
package com.plg.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    @Test
    void allowsRequestsUpToTheLimit() {
        SlidingWindowRateLimiter limiter = newLimiter(Instant.parse("2026-08-27T10:00:00Z"));

        assertTrue(limiter.tryAcquire(CLIENT_A));
        assertTrue(limiter.tryAcquire(CLIENT_A));
        assertTrue(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        SlidingWindowRateLimiter limiter = newLimiter(Instant.parse("2026-08-27T10:00:00Z"));
        exhaustLimit(limiter, CLIENT_A);

        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() {
        MutableClock clock = MutableClock.startingAt(Instant.parse("2026-08-27T10:00:00Z"));
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, clock);
        exhaustLimit(limiter, CLIENT_A);

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        SlidingWindowRateLimiter limiter = newLimiter(Instant.parse("2026-08-27T10:00:00Z"));
        exhaustLimit(limiter, CLIENT_A);

        assertTrue(limiter.tryAcquire(CLIENT_B));
    }

    private static SlidingWindowRateLimiter newLimiter(Instant startingInstant) {
        return new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_WINDOW, WINDOW, MutableClock.startingAt(startingInstant));
    }

    private static void exhaustLimit(SlidingWindowRateLimiter limiter, String clientId) {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire(clientId);
        }
    }
}
````

````java
package com.plg.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RateLimitingHttpHandlerTest {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Test
    void forwardsRequestWhenWithinLimit() throws IOException {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, alwaysAllow(), WINDOW);
        HttpExchange exchange = fakeExchangeFrom("10.0.0.1");

        handler.handle(exchange);

        verify(delegate, times(1)).handle(exchange);
    }

    @Test
    void rejectsRequestWithTooManyRequestsWhenLimitExceeded() throws IOException {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, alwaysReject(), WINDOW);
        HttpExchange exchange = fakeExchangeFrom("10.0.0.2");

        handler.handle(exchange);

        verify(delegate, never()).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(429), anyLong());
        assertEquals("60", exchange.getResponseHeaders().getFirst("Retry-After"));
    }

    private static RateLimiter alwaysAllow() {
        return clientId -> true;
    }

    private static RateLimiter alwaysReject() {
        return clientId -> false;
    }

    private static HttpExchange fakeExchangeFrom(String ipAddress) throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ipAddress, 5555));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
````

### ratelimit · run 8 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a delegate HttpHandler with a per-client sliding-window rate limit.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;

        // Periodically evict clients that haven't made a request recently,
        // so the map doesn't grow forever.
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::evictStaleEntries, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientKey(exchange);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());

        boolean allowed;
        long retryAfterSeconds = 0;

        synchronized (timestamps) {
            evictOlderThan(timestamps, now - windowMillis);

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                allowed = true;
            } else {
                allowed = false;
                long oldest = timestamps.peekFirst();
                retryAfterSeconds = Math.max(1, (windowMillis - (now - oldest)) / 1000 + 1);
            }
        }

        if (allowed) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange, retryAfterSeconds);
        }
    }

    private void evictOlderThan(Deque<Long> timestamps, long cutoff) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
            timestamps.pollFirst();
        }
    }

    private void evictStaleEntries() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        for (Iterator<Map.Entry<String, Deque<Long>>> it = requestLog.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Deque<Long>> entry = it.next();
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                evictOlderThan(timestamps, cutoff);
                if (timestamps.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        String body = "429 Too Many Requests - rate limit exceeded, retry later\n";
        byte[] bytes = body.getBytes();

        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Example wiring
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] response = "Hello, world!\n".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        };

        server.createContext("/hello", new RateLimitingHandler(helloHandler, 5, TimeUnit.MINUTES.toMillis(1)));
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
    }
}
````

### ratelimit · run 8 · uncle-bob-junior

````java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock-free, fixed-window rate limiter keyed by client id.
 * Thread-safe: safe to share one instance across all requests.
 */
public final class ClientRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, AtomicReference<Window>> windowsByClient =
            new ConcurrentHashMap<>();

    public ClientRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = Objects.requireNonNull(windowDuration);
        this.clock = Objects.requireNonNull(clock);
    }

    /** Returns true if the request is allowed, false if the client exceeded its quota. */
    public boolean tryAcquire(String clientId) {
        Instant now = clock.instant();
        AtomicReference<Window> slot =
                windowsByClient.computeIfAbsent(clientId, id -> new AtomicReference<>(Window.startingAt(now)));

        while (true) {
            Window current = slot.get();
            Window effective = current.isExpired(now, windowDuration) ? Window.startingAt(now) : current;

            if (effective.requestCount() >= maxRequestsPerWindow) {
                slot.compareAndSet(current, effective);
                return false;
            }
            if (slot.compareAndSet(current, effective.incremented())) {
                return true;
            }
        }
    }

    private record Window(Instant windowStart, int requestCount) {

        static Window startingAt(Instant start) {
            return new Window(start, 0);
        }

        boolean isExpired(Instant now, Duration windowDuration) {
            return Duration.between(windowStart, now).compareTo(windowDuration) >= 0;
        }

        Window incremented() {
            return new Window(windowStart, requestCount + 1);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/** Decorates a handler with per-client rate limiting; delegates when the client is within quota. */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;

    private final HttpHandler delegate;
    private final ClientRateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHttpHandler(HttpHandler delegate, ClientRateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = Objects.requireNonNull(delegate);
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
        this.retryAfterSeconds = windowDuration.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFrom(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdFrom(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, -1);
        exchange.close();
    }
}
````

````java
// Example wiring
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

public final class ServerBootstrap {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public static HttpServer create(int port, com.sun.net.httpserver.HttpHandler apiHandler) throws java.io.IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ClientRateLimiter rateLimiter =
                new ClientRateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());
        server.createContext("/api", new RateLimitingHttpHandler(apiHandler, rateLimiter, RATE_LIMIT_WINDOW));
        return server;
    }
}
````

````java
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    /** Test double: a Clock whose instant can be advanced deterministically. */
    private static final class MutableClock extends Clock {
        private Instant now;
        MutableClock(Instant start) { this.now = start; }
        void advanceBy(Duration duration) { now = now.plus(duration); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(CLIENT_A), "request " + i + " should be allowed");
        }
    }

    @Test
    void blocksRequestsBeyondTheLimit() {
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT_A);
        }

        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT_A);
        }

        assertTrue(limiter.tryAcquire(CLIENT_B), "a different client must have its own quota");
    }

    @Test
    void resetsQuotaAfterWindowElapses() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, clock);

        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT_A);
        }
        assertFalse(limiter.tryAcquire(CLIENT_A));

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(limiter.tryAcquire(CLIENT_A), "quota should reset once the window has passed");
    }

    @Test
    void rejectsInvalidLimitAtConstruction() {
        assertThrowsIllegalArgument(() -> new ClientRateLimiter(0, WINDOW, Clock.systemUTC()));
    }

    private static void assertThrowsIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }
}

class RateLimitingHttpHandlerTest {

    private static final String CLIENT_IP = "203.0.113.7";

    @Test
    void delegatesWhenClientIsWithinQuota() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        ClientRateLimiter rateLimiter = new ClientRateLimiter(5, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler =
                new RateLimitingHttpHandler(delegate, rateLimiter, Duration.ofMinutes(1));
        HttpExchange exchange = exchangeFrom(CLIENT_IP);

        handler.handle(exchange);

        verify(delegate, times(1)).handle(exchange);
        verify(exchange, never()).sendResponseHeaders(any(Integer.class) == null ? 0 : 429, -1);
    }

    @Test
    void rejectsWithTooManyRequestsOnceQuotaIsExhausted() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        ClientRateLimiter rateLimiter = new ClientRateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler =
                new RateLimitingHttpHandler(delegate, rateLimiter, Duration.ofMinutes(1));

        handler.handle(exchangeFrom(CLIENT_IP));
        HttpExchange secondExchange = exchangeFrom(CLIENT_IP);
        handler.handle(secondExchange);

        verify(delegate, times(1)).handle(any(HttpExchange.class));
        verify(secondExchange).sendResponseHeaders(429, -1);
    }

    private HttpExchange exchangeFrom(String ip) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 54321));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        return exchange;
    }
}
````

### ratelimit · run 9 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Each client (identified by remote IP) may make at most MAX_REQUESTS
 * requests within WINDOW_MILLIS.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final long IDLE_EVICTION_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private final HttpHandler delegate;
    private final Map<String, ClientRecord> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate) {
        this.delegate = delegate;
        cleaner.scheduleAtFixedRate(this::evictIdleClients, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        ClientRecord record = clients.computeIfAbsent(clientId, k -> new ClientRecord());

        long retryAfterSeconds;
        synchronized (record) {
            long now = System.currentTimeMillis();
            record.lastSeen = now;
            evictOldTimestamps(record.timestamps, now);

            if (record.timestamps.size() >= MAX_REQUESTS) {
                long oldest = record.timestamps.peekFirst();
                retryAfterSeconds = Math.max(1, (WINDOW_MILLIS - (now - oldest)) / 1000 + 1);
            } else {
                record.timestamps.addLast(now);
                retryAfterSeconds = -1;
            }
        }

        if (retryAfterSeconds >= 0) {
            rejectWithTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        delegate.handle(exchange);
    }

    private static void evictOldTimestamps(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= WINDOW_MILLIS) {
            timestamps.pollFirst();
        }
    }

    private void evictIdleClients() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> now - e.getValue().lastSeen > IDLE_EVICTION_MILLIS);
    }

    private void rejectWithTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes("UTF-8");
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (var os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static String resolveClientId(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }

    private static final class ClientRecord {
        final Deque<Long> timestamps = new ArrayDeque<>();
        volatile long lastSeen = System.currentTimeMillis();
    }
}
````

````java
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/api", new RateLimitingHandler(exchange -> {
    // actual request handling
    String response = "OK";
    exchange.sendResponseHeaders(200, response.length());
    exchange.getResponseBody().write(response.getBytes());
    exchange.getResponseBody().close();
}));
server.start();
````

### ratelimit · run 9 · uncle-bob-junior

````java
// File: FixedWindowRateLimiter.java
package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Limits each client to a fixed number of requests per time window.
 *
 * <p>Uses the fixed-window counter algorithm: time is divided into
 * consecutive windows of {@code windowDuration}; each client may make at
 * most {@code maxRequestsPerWindow} requests within a single window. A
 * client can burst up to twice that count across a window boundary; this
 * is accepted since the limit only needs to be approximate.
 */
public final class FixedWindowRateLimiter implements AutoCloseable {

    private final int maxRequestsPerWindow;
    private final long windowDurationMillis;
    private final Clock clock;
    private final Map<String, WindowState> requestWindows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDurationMillis = windowDuration.toMillis();
        this.clock = clock;
        this.cleanupExecutor =
                Executors.newSingleThreadScheduledExecutor(FixedWindowRateLimiter::newDaemonCleanupThread);
        this.cleanupExecutor.scheduleAtFixedRate(
                this::evictStaleWindows, windowDurationMillis, windowDurationMillis, TimeUnit.MILLISECONDS);
    }

    /** Returns true if the client may proceed, false if it has exceeded the limit for the current window. */
    public boolean tryAcquire(String clientId) {
        long currentWindowIndex = currentWindowIndex();
        WindowState updated =
                requestWindows.compute(clientId, (id, existing) -> nextWindowState(existing, currentWindowIndex));
        return updated.requestCount() <= maxRequestsPerWindow;
    }

    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
    }

    private long currentWindowIndex() {
        return clock.millis() / windowDurationMillis;
    }

    private static WindowState nextWindowState(WindowState existing, long currentWindowIndex) {
        if (existing == null || existing.windowIndex() != currentWindowIndex) {
            return new WindowState(currentWindowIndex, 1);
        }
        return new WindowState(currentWindowIndex, existing.requestCount() + 1);
    }

    private void evictStaleWindows() {
        long currentWindowIndex = currentWindowIndex();
        requestWindows.values().removeIf(window -> window.windowIndex() < currentWindowIndex);
    }

    private static Thread newDaemonCleanupThread(Runnable task) {
        Thread thread = new Thread(task, "rate-limiter-cleanup");
        thread.setDaemon(true);
        return thread;
    }

    private record WindowState(long windowIndex, int requestCount) {}
}
````

````java
// File: RateLimitingHttpHandler.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Wraps an {@link HttpHandler}, rejecting requests from clients that have
 * exceeded the configured rate limit with a {@code 429 Too Many Requests}
 * response.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final byte[] RATE_LIMIT_EXCEEDED_BODY =
            "Rate limit exceeded. Please slow down.".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final FixedWindowRateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHttpHandler(HttpHandler delegate, FixedWindowRateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = windowDuration.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = extractClientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private static String extractClientId(HttpExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, RATE_LIMIT_EXCEEDED_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(RATE_LIMIT_EXCEEDED_BODY);
        }
    }
}
````

````java
// File: RateLimitedServerExample.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());

        server.createContext("/", new RateLimitingHttpHandler(helloHandler(), rateLimiter, RATE_LIMIT_WINDOW));
        server.start();
    }

    private static HttpHandler helloHandler() {
        return exchange -> {
            byte[] response = "Hello, world!".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(response);
            }
        };
    }
}
````

````java
// File: src/test/java/com/example/ratelimit/MutableClock.java
package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

final class MutableClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    MutableClock(Instant instant) {
        this(instant, ZoneId.of("UTC"));
    }

    private MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    void advance(Duration duration) {
        instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
````

````java
// File: src/test/java/com/example/ratelimit/FixedWindowRateLimiterTest.java
package com.example.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter =
            new FixedWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, clock);

    @AfterEach
    void tearDown() {
        rateLimiter.close();
    }

    @Test
    void allowsUpToTheConfiguredLimitWithinAWindow() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(rateLimiter.tryAcquire("client-a"));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.tryAcquire("client-b");
        }

        assertFalse(rateLimiter.tryAcquire("client-b"));
    }

    @Test
    void resetsTheCountAfterTheWindowElapses() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.tryAcquire("client-c");
        }

        clock.advance(WINDOW.plusSeconds(1));

        assertTrue(rateLimiter.tryAcquire("client-c"));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.tryAcquire("client-d");
        }

        assertTrue(rateLimiter.tryAcquire("client-e"));
    }
}
````

````java
// File: src/test/java/com/example/ratelimit/FakeHttpExchange.java
package com.example.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/** Minimal test double for the JDK-owned {@link HttpExchange} boundary. */
final class FakeHttpExchange extends HttpExchange {

    private final InetSocketAddress remoteAddress;
    private final Headers responseHeaders = new Headers();
    private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    private int responseCode = -1;

    FakeHttpExchange(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    int responseCode() {
        return responseCode;
    }

    Headers responseHeaders() {
        return responseHeaders;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) {
        this.responseCode = rCode;
    }

    @Override
    public OutputStream getResponseBody() {
        return responseBody;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public void close() {
        // no-op: nothing external to release in the fake
    }

    @Override
    public Headers getRequestHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRequestURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpContext getHttpContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getRequestBody() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpPrincipal getPrincipal() {
        throw new UnsupportedOperationException();
    }
}
````

````java
// File: src/test/java/com/example/ratelimit/RateLimitingHttpHandlerTest.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingHttpHandlerTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 2;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int HTTP_OK = 200;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter =
            new FixedWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, clock);
    private final RateLimitingHttpHandler handler =
            new RateLimitingHttpHandler(this::respondOk, rateLimiter, WINDOW);

    @AfterEach
    void tearDown() {
        rateLimiter.close();
    }

    @Test
    void allowsRequestsUpToTheLimit() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            FakeHttpExchange exchange = newExchangeFrom("10.0.0.1");
            handler.handle(exchange);
            assertEquals(HTTP_OK, exchange.responseCode());
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithRetryAfterHeader() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            handler.handle(newExchangeFrom("10.0.0.2"));
        }

        FakeHttpExchange exceedingRequest = newExchangeFrom("10.0.0.2");
        handler.handle(exceedingRequest);

        assertEquals(HTTP_TOO_MANY_REQUESTS, exceedingRequest.responseCode());
        assertTrue(exceedingRequest.responseHeaders().containsKey("Retry-After"));
    }

    @Test
    void tracksEachClientIndependently() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            handler.handle(newExchangeFrom("10.0.0.3"));
        }

        FakeHttpExchange otherClientRequest = newExchangeFrom("10.0.0.4");
        handler.handle(otherClientRequest);

        assertEquals(HTTP_OK, otherClientRequest.responseCode());
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            handler.handle(newExchangeFrom("10.0.0.5"));
        }

        clock.advance(WINDOW.plusSeconds(1));
        FakeHttpExchange nextWindowRequest = newExchangeFrom("10.0.0.5");
        handler.handle(nextWindowRequest);

        assertEquals(HTTP_OK, nextWindowRequest.responseCode());
    }

    private void respondOk(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, 0);
    }

    private FakeHttpExchange newExchangeFrom(String ip) {
        return new FakeHttpExchange(new InetSocketAddress(ip, 0));
    }
}
````

### ratelimit · run 10 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client token-bucket rate limit.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int capacity;          // max burst size (tokens)
    private final double refillPerNano;  // tokens added per nanosecond
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    /**
     * @param delegate        handler to protect
     * @param maxRequests     max requests allowed per client per minute
     */
    public RateLimitingHandler(HttpHandler delegate, int maxRequests) {
        this.delegate = delegate;
        this.capacity = maxRequests;
        this.refillPerNano = maxRequests / 60_000_000_000.0; // tokens per nanosecond, refills to `capacity` over 60s

        // Evict buckets idle for more than 5 minutes so the map doesn't grow forever.
        cleaner.scheduleAtFixedRate(this::evictStale, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        TokenBucket bucket = buckets.computeIfAbsent(clientKey, k -> new TokenBucket(capacity));

        if (bucket.tryConsume(capacity, refillPerNano)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictStale() {
        long now = System.nanoTime();
        long idleThresholdNanos = TimeUnit.MINUTES.toNanos(5);
        buckets.entrySet().removeIf(e -> (now - e.getValue().lastAccessNanos) > idleThresholdNanos);
    }

    /** Call when shutting down the server to release the cleanup thread. */
    public void shutdown() {
        cleaner.shutdownNow();
    }

    /**
     * Simple thread-safe token bucket. Tokens refill continuously based on
     * elapsed time, capped at `capacity`.
     */
    private static final class TokenBucket {
        private double tokens;
        private long lastRefillNanos;
        private volatile long lastAccessNanos;

        TokenBucket(double initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillNanos = System.nanoTime();
            this.lastAccessNanos = lastRefillNanos;
        }

        synchronized boolean tryConsume(int capacity, double refillPerNano) {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
                lastRefillNanos = now;
            }
            lastAccessNanos = now;

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler appHandler = exchange -> {
            byte[] response = "OK".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        };

        // Allow 5 requests per minute per client, with bursting up to 5.
        RateLimitingHandler limited = new RateLimitingHandler(appHandler, 5);
        server.createContext("/api", limited);
        server.start();
    }
}
````

### ratelimit · run 10 · uncle-bob-junior

````java
import java.time.Clock;
import java.time.Duration;

final class TokenBucket {

    private final int capacity;
    private final double tokensPerMillisecond;
    private final Clock clock;

    private double availableTokens;
    private long lastRefillTimestampMillis;

    TokenBucket(int capacity, Duration refillWindow, Clock clock) {
        this.capacity = capacity;
        this.tokensPerMillisecond = (double) capacity / refillWindow.toMillis();
        this.clock = clock;
        this.availableTokens = capacity;
        this.lastRefillTimestampMillis = clock.millis();
    }

    synchronized boolean tryConsume() {
        refill();
        if (availableTokens < 1.0) {
            return false;
        }
        availableTokens -= 1.0;
        return true;
    }

    synchronized boolean isIdleAsOf(long nowMillis, long idleThresholdMillis) {
        return nowMillis - lastRefillTimestampMillis >= idleThresholdMillis;
    }

    private void refill() {
        long now = clock.millis();
        long elapsedMillis = now - lastRefillTimestampMillis;
        if (elapsedMillis <= 0) {
            return;
        }
        availableTokens = Math.min(capacity, availableTokens + elapsedMillis * tokensPerMillisecond);
        lastRefillTimestampMillis = now;
    }
}
````

````java
import java.io.Closeable;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class ClientRateLimiterRegistry implements Closeable {

    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);
    private static final Duration IDLE_RETENTION = Duration.ofMinutes(10);

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, TokenBucket> bucketsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    ClientRateLimiterRegistry(int maxRequestsPerWindow, Duration window, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(this::newDaemonThread);
        this.cleanupExecutor.scheduleAtFixedRate(
                this::evictIdleBuckets, CLEANUP_INTERVAL.toMillis(), CLEANUP_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    boolean tryAcquire(String clientKey) {
        return bucketsByClient
                .computeIfAbsent(clientKey, key -> new TokenBucket(maxRequestsPerWindow, window, clock))
                .tryConsume();
    }

    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
    }

    private void evictIdleBuckets() {
        long now = clock.millis();
        bucketsByClient.entrySet().removeIf(entry -> entry.getValue().isIdleAsOf(now, IDLE_RETENTION.toMillis()));
    }

    private Thread newDaemonThread(Runnable task) {
        Thread thread = new Thread(task, "rate-limiter-cleanup");
        thread.setDaemon(true);
        return thread;
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

final class RateLimitingHttpHandler implements HttpHandler, Closeable {

    private static final int DEFAULT_MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);
    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final byte[] REJECTION_BODY = "Too Many Requests".getBytes();

    private final HttpHandler delegate;
    private final ClientRateLimiterRegistry rateLimiterRegistry;
    private final int retryAfterSeconds;

    RateLimitingHttpHandler(HttpHandler delegate, int maxRequestsPerWindow, Duration window) {
        this.delegate = delegate;
        this.rateLimiterRegistry = new ClientRateLimiterRegistry(maxRequestsPerWindow, window, Clock.systemUTC());
        this.retryAfterSeconds = (int) window.getSeconds();
    }

    static RateLimitingHttpHandler withDefaults(HttpHandler delegate) {
        return new RateLimitingHttpHandler(delegate, DEFAULT_MAX_REQUESTS_PER_WINDOW, DEFAULT_WINDOW);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = resolveClientKey(exchange);
        if (!rateLimiterRegistry.tryAcquire(clientKey)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    @Override
    public void close() {
        rateLimiterRegistry.close();
    }

    private String resolveClientKey(HttpExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS_CODE, REJECTION_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(REJECTION_BODY);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

final class ServerBootstrap {
    static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimitingHttpHandler handler = RateLimitingHttpHandler.withDefaults(exchange -> {
            byte[] body = "OK".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.createContext("/", handler);
        server.start();
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TokenBucketTest {

    private static final class MutableFakeClock extends Clock {
        private long currentMillis;

        MutableFakeClock(long initialMillis) {
            this.currentMillis = initialMillis;
        }

        void advanceMillis(long millis) {
            currentMillis += millis;
        }

        @Override
        public long millis() {
            return currentMillis;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(currentMillis);
        }
    }

    @Test
    void allowsUpToCapacityRequestsImmediately() {
        TokenBucket bucket = new TokenBucket(3, Duration.ofMinutes(1), new MutableFakeClock(0));

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
    }

    @Test
    void rejectsRequestBeyondCapacityWithinWindow() {
        TokenBucket bucket = new TokenBucket(2, Duration.ofMinutes(1), new MutableFakeClock(0));

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void refillsTokensAfterWindowElapses() {
        MutableFakeClock clock = new MutableFakeClock(0);
        TokenBucket bucket = new TokenBucket(1, Duration.ofMinutes(1), clock);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        clock.advanceMillis(Duration.ofMinutes(1).toMillis());

        assertTrue(bucket.tryConsume());
    }

    @Test
    void partialElapsedTimeGrantsOnlyPartialTokens() {
        MutableFakeClock clock = new MutableFakeClock(0);
        TokenBucket bucket = new TokenBucket(2, Duration.ofMinutes(1), clock);
        bucket.tryConsume();
        bucket.tryConsume();

        clock.advanceMillis(Duration.ofSeconds(29).toMillis());

        assertFalse(bucket.tryConsume());
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientRateLimiterRegistryTest {

    @Test
    void eachClientGetsAnIndependentBudget() {
        try (ClientRateLimiterRegistry registry =
                     new ClientRateLimiterRegistry(1, Duration.ofMinutes(1), Clock.systemUTC())) {

            assertTrue(registry.tryAcquire("client-a"));
            assertTrue(registry.tryAcquire("client-b"));
        }
    }

    @Test
    void sameClientSharesBudgetAcrossCalls() {
        try (ClientRateLimiterRegistry registry =
                     new ClientRateLimiterRegistry(1, Duration.ofMinutes(1), Clock.systemUTC())) {

            assertTrue(registry.tryAcquire("client-a"));
            assertFalse(registry.tryAcquire("client-a"));
        }
    }
}
````

````java
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

final class RateLimitingHttpHandlerTest {

    private HttpExchange newExchangeMock() {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("192.0.2.1", 54321));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }

    @Test
    void allowsRequestsWithinLimitToReachDelegate() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        try (RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, 2, Duration.ofMinutes(1))) {
            handler.handle(newExchangeMock());
            handler.handle(newExchangeMock());

            verify(delegate, times(2)).handle(any());
        }
    }

    @Test
    void rejectsRequestsBeyondLimitWithoutCallingDelegate() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        try (RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, 1, Duration.ofMinutes(1))) {
            handler.handle(newExchangeMock());

            HttpExchange rejected = newExchangeMock();
            handler.handle(rejected);

            verify(delegate, times(1)).handle(any());
            verify(rejected).sendResponseHeaders(eq(429), anyLong());
        }
    }
}
````

## order

### order · run 1 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A single line item on an order, before tax or discount. */
final class OrderLine {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderLine(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/** Thrown when an order or one of its line items fails validation. */
final class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}

/** Validates orders, applies discount and VAT, and renders a receipt. */
final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public String processOrder(String orderId, List<OrderLine> lines) {
        validateLines(lines);

        BigDecimal subtotal = lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        return buildReceipt(orderId, lines, subtotal, discount, discountedSubtotal, vat, total);
    }

    private void validateLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item.");
        }
        for (OrderLine line : lines) {
            if (line.getDescription() == null || line.getDescription().isBlank()) {
                throw new OrderValidationException("Line item description must not be blank.");
            }
            if (line.getQuantity() <= 0) {
                throw new OrderValidationException(
                        "Line item '" + line.getDescription() + "' must have a quantity greater than zero.");
            }
            if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException(
                        "Line item '" + line.getDescription() + "' must have a non-negative unit price.");
            }
        }
    }

    private String buildReceipt(String orderId, List<OrderLine> lines, BigDecimal subtotal,
                                 BigDecimal discount, BigDecimal discountedSubtotal,
                                 BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt for order ").append(orderId).append("\n");
        sb.append("-".repeat(40)).append("\n");

        for (OrderLine line : lines) {
            sb.append(String.format(Locale.UK, "%-20s %2d x %8s = %8s%n",
                    line.getDescription(),
                    line.getQuantity(),
                    formatEuro(line.getUnitPrice()),
                    formatEuro(line.lineTotal())));
        }

        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format(Locale.UK, "%-30s %8s%n", "Subtotal (excl. VAT):", formatEuro(subtotal)));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format(Locale.UK, "%-30s -%7s%n", "Discount (10%):", formatEuro(discount)));
            sb.append(String.format(Locale.UK, "%-30s %8s%n", "Subtotal after discount:", formatEuro(discountedSubtotal)));
        }
        sb.append(String.format(Locale.UK, "%-30s %8s%n", "VAT (21%):", formatEuro(vat)));
        sb.append(String.format(Locale.UK, "%-30s %8s%n", "Total:", formatEuro(total)));

        return sb.toString();
    }

    private String formatEuro(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine("Lottery subscription - annual", 1, new BigDecimal("85.00")));
        lines.add(new OrderLine("Charity add-on", 2, new BigDecimal("10.00")));

        OrderProcessor processor = new OrderProcessor();
        System.out.println(processor.processOrder("ORD-0001", lines));
    }
}
````

### order · run 1 · uncle-bob-junior

````java
// LineItem.java
package com.example.order;

import java.math.BigDecimal;

public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new InvalidLineItemException("Description must not be blank");
        }
        if (quantity <= 0) {
            throw new InvalidLineItemException("Quantity must be positive, got " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new InvalidLineItemException("Unit price must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// InvalidLineItemException.java
package com.example.order;

public class InvalidLineItemException extends RuntimeException {
    public InvalidLineItemException(String message) {
        super(message);
    }
}
````

````java
// EmptyOrderException.java
package com.example.order;

public class EmptyOrderException extends RuntimeException {
    public EmptyOrderException(String message) {
        super(message);
    }
}
````

````java
// OrderTotals.java
package com.example.order;

import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
````

````java
// OrderCalculator.java
package com.example.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderCalculator() {
    }

    public static OrderTotals calculate(List<LineItem> lineItems) {
        BigDecimal subtotal = subtotalOf(lineItems);
        BigDecimal discount = discountOn(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = vatOn(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private static BigDecimal subtotalOf(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountOn(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static BigDecimal vatOn(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// ReceiptFormatter.java
package com.example.order;

import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private static final int SEPARATOR_WIDTH = 34;

    private ReceiptFormatter() {
    }

    public static String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private static void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private static String formatLine(LineItem item) {
        return String.format(Locale.ROOT, "%2d x %-20s EUR %8.2f",
                item.quantity(), item.description(), item.lineTotal());
    }

    private static void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-".repeat(SEPARATOR_WIDTH)).append(System.lineSeparator());
        receipt.append(formatTotalLine("Subtotal", totals.subtotal())).append(System.lineSeparator());
        if (totals.discount().signum() > 0) {
            receipt.append(formatTotalLine("Discount (10%)", totals.discount().negate()))
                    .append(System.lineSeparator());
        }
        receipt.append(formatTotalLine("VAT (21%)", totals.vat())).append(System.lineSeparator());
        receipt.append(formatTotalLine("Total", totals.total()));
    }

    private static String formatTotalLine(String label, java.math.BigDecimal amount) {
        return String.format(Locale.ROOT, "%-20s EUR %8.2f", label, amount);
    }
}
````

````java
// OrderProcessor.java
package com.example.order;

import java.util.List;

public final class OrderProcessor {

    private OrderProcessor() {
    }

    public static String process(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new EmptyOrderException("An order must contain at least one line item");
        }
        OrderTotals totals = OrderCalculator.calculate(lineItems);
        return ReceiptFormatter.format(lineItems, totals);
    }
}
````

````java
// LineItemTest.java
package com.example.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void computesLineTotalAsQuantityTimesUnitPrice() {
        LineItem item = new LineItem("Widget", 3, new BigDecimal("2.50"));
        assertEquals(new BigDecimal("7.50"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("  ", 1, BigDecimal.ONE));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", 0, BigDecimal.ONE));
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", -1, BigDecimal.ONE));
    }

    @Test
    void rejectsZeroOrNegativeUnitPrice() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", 1, BigDecimal.ZERO));
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", 1, new BigDecimal("-1.00")));
    }
}
````

````java
// OrderCalculatorTest.java
package com.example.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculatorTest {

    @Test
    void appliesNoDiscountWhenSubtotalIsExactlyThreshold() {
        LineItem item = new LineItem("Widget", 1, new BigDecimal("100.00"));
        OrderTotals totals = OrderCalculator.calculate(List.of(item));

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Widget", 1, new BigDecimal("200.00"));
        OrderTotals totals = OrderCalculator.calculate(List.of(item));

        assertEquals(new BigDecimal("20.00"), totals.discount());
        assertEquals(new BigDecimal("37.80"), totals.vat());
        assertEquals(new BigDecimal("217.80"), totals.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        List<LineItem> items = List.of(
                new LineItem("Widget", 2, new BigDecimal("30.00")),
                new LineItem("Gadget", 1, new BigDecimal("50.00")));

        OrderTotals totals = OrderCalculator.calculate(items);

        assertEquals(new BigDecimal("110.00"), totals.subtotal());
        assertEquals(new BigDecimal("11.00"), totals.discount());
        assertEquals(new BigDecimal("20.79"), totals.vat());
        assertEquals(new BigDecimal("119.79"), totals.total());
    }
}
````

````java
// OrderProcessorTest.java
package com.example.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    @Test
    void rejectsNullOrder() {
        assertThrows(EmptyOrderException.class, () -> OrderProcessor.process(null));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(EmptyOrderException.class, () -> OrderProcessor.process(Collections.emptyList()));
    }

    @Test
    void receiptOmitsDiscountLineWhenNoneApplies() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("50.00")));
        String receipt = OrderProcessor.process(items);

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Total"));
    }

    @Test
    void receiptIncludesDiscountLineWhenThresholdExceeded() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("150.00")));
        String receipt = OrderProcessor.process(items);

        assertTrue(receipt.contains("Discount (10%)"));
    }
}
````

### order · run 2 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(new Locale("nl", "NL"));

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Receipt(
            String text,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total) {
    }

    public static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Line item cannot be null");
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalArgumentException("Line item description cannot be blank");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a positive quantity");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a non-negative unit price");
            }
        }
    }

    public static Receipt process(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        String text = buildReceiptText(items, subtotal, discount, vat, total, discountApplies);

        return new Receipt(text, subtotal, discount, vat, total);
    }

    private static String buildReceiptText(
            List<LineItem> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("=== Receipt ===\n");
        for (LineItem item : items) {
            sb.append(String.format(
                    "%-20s %3d x %10s = %12s%n",
                    item.description(),
                    item.quantity(),
                    CURRENCY_FORMAT.format(item.unitPrice()),
                    CURRENCY_FORMAT.format(item.lineTotal())));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-30s %12s%n", "Subtotal (excl. VAT):", CURRENCY_FORMAT.format(subtotal)));
        if (discountApplies) {
            sb.append(String.format("%-30s %12s%n", "Discount (10%):", "-" + CURRENCY_FORMAT.format(discount)));
        }
        sb.append(String.format("%-30s %12s%n", "VAT (21%):", CURRENCY_FORMAT.format(vat)));
        sb.append(String.format("%-30s %12s%n", "Total (incl. VAT):", CURRENCY_FORMAT.format(total)));
        return sb.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Lottery ticket - monthly subscription", 2, new BigDecimal("45.00")),
                new LineItem("Donation add-on", 1, new BigDecimal("15.00"))
        );

        Receipt receipt = process(order);
        System.out.println(receipt.text());
    }
}
````

### order · run 2 · uncle-bob-junior

````java
package com.example.orders;

import java.math.BigDecimal;

/**
 * A single purchasable line on an order.
 * Invariants: description is non-blank, unitPrice > 0, quantity > 0.
 */
public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
package com.example.orders;

import java.util.List;

/**
 * An order to process. Invariant: contains at least one line item.
 */
public record Order(List<LineItem> lineItems) {

    public Order {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        lineItems = List.copyOf(lineItems);
    }
}
````

````java
package com.example.orders;

import java.math.BigDecimal;
import java.util.List;

/**
 * Computed totals for a processed order, in euros, rounded to 2 decimals.
 */
public record Receipt(
        List<LineItem> lineItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal total) {
}
````

````java
package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Computes an order's totals: 10% discount on pre-VAT subtotals over
 * {@link #DISCOUNT_THRESHOLD}, then 21% VAT on the discounted subtotal.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public Receipt process(Order order) {
        BigDecimal subtotal = sumLineItems(order.lineItems());
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE);
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(
                order.lineItems(),
                round(subtotal),
                round(discount),
                round(vat),
                round(total));
    }

    private BigDecimal sumLineItems(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(DISCOUNT_RATE);
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Renders a {@link Receipt} as a human-readable text receipt.
 */
public final class ReceiptFormatter {

    private static final String CURRENCY_SYMBOL = "\u20AC";
    private static final String SEPARATOR = "-".repeat(40);
    private static final String LINE_ITEM_FORMAT = "%d x %-24s %s%s";
    private static final String TOTAL_LINE_FORMAT = "%-26s %s%s";
    private static final int DISPLAY_SCALE = 2;

    public String format(Receipt receipt) {
        StringBuilder text = new StringBuilder();
        appendHeader(text);
        appendLineItems(text, receipt.lineItems());
        text.append(SEPARATOR).append(System.lineSeparator());
        appendTotals(text, receipt);
        return text.toString();
    }

    private void appendHeader(StringBuilder text) {
        text.append("Order Receipt").append(System.lineSeparator());
        text.append(SEPARATOR).append(System.lineSeparator());
    }

    private void appendLineItems(StringBuilder text, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            text.append(formatLineItem(item)).append(System.lineSeparator());
        }
    }

    private String formatLineItem(LineItem item) {
        return LINE_ITEM_FORMAT.formatted(
                item.quantity(), item.description(), CURRENCY_SYMBOL, formatAmount(item.lineTotal()));
    }

    private void appendTotals(StringBuilder text, Receipt receipt) {
        appendAmountLine(text, "Subtotal:", receipt.subtotal());
        if (receipt.discount().compareTo(BigDecimal.ZERO) > 0) {
            appendAmountLine(text, "Discount (10%):", receipt.discount().negate());
        }
        appendAmountLine(text, "VAT (21%):", receipt.vat());
        appendAmountLine(text, "Total:", receipt.total());
    }

    private void appendAmountLine(StringBuilder text, String label, BigDecimal amount) {
        text.append(TOTAL_LINE_FORMAT.formatted(label, CURRENCY_SYMBOL, formatAmount(amount)));
        text.append(System.lineSeparator());
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
````

````java
package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LineItemTest {

    @Test
    void computesLineTotalAsUnitPriceTimesQuantity() {
        LineItem item = new LineItem("Widget", new BigDecimal("9.99"), 3);
        assertEquals(new BigDecimal("29.97"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", BigDecimal.ONE, 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ONE, 0));
    }
}
````

````java
package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void rejectsEmptyLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void rejectsNullLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }

    @Test
    void lineItemsAreImmutable() {
        List<LineItem> mutable = new ArrayList<>();
        mutable.add(new LineItem("Widget", BigDecimal.ONE, 1));
        Order order = new Order(mutable);

        mutable.add(new LineItem("Gadget", BigDecimal.TEN, 1));

        assertTrue(order.lineItems().size() == 1);
        assertThrows(UnsupportedOperationException.class,
                () -> order.lineItems().add(new LineItem("Gizmo", BigDecimal.ONE, 1)));
    }
}
````

````java
package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("100.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("200.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("20.00"), receipt.discount());
        assertEquals(new BigDecimal("37.80"), receipt.vat());
        assertEquals(new BigDecimal("217.80"), receipt.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        Order order = new Order(List.of(
                new LineItem("Widget", new BigDecimal("30.00"), 2),
                new LineItem("Gadget", new BigDecimal("50.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("110.00"), receipt.subtotal());
        assertEquals(new BigDecimal("11.00"), receipt.discount());
        assertEquals(new BigDecimal("99.00"), receipt.discountedSubtotal());
    }

    private BigDecimal discountedSubtotal(Receipt receipt) {
        return receipt.subtotal().subtract(receipt.discount());
    }
}
````

````java
package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("100.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("200.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("20.00"), receipt.discount());
        assertEquals(new BigDecimal("37.80"), receipt.vat());
        assertEquals(new BigDecimal("217.80"), receipt.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        Order order = new Order(List.of(
                new LineItem("Widget", new BigDecimal("30.00"), 2),
                new LineItem("Gadget", new BigDecimal("50.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("110.00"), receipt.subtotal());
        assertEquals(new BigDecimal("11.00"), receipt.discount());
        assertEquals(new BigDecimal("20.79"), receipt.vat());
        assertEquals(new BigDecimal("108.79"), receipt.total());
    }
}
````

````java
package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesSubtotalVatAndTotal() {
        Receipt receipt = new Receipt(
                List.of(new LineItem("Widget", new BigDecimal("50.00"), 1)),
                new BigDecimal("50.00"),
                new BigDecimal("0.00"),
                new BigDecimal("10.50"),
                new BigDecimal("60.50"));

        String text = formatter.format(receipt);

        assertTrue(text.contains("Subtotal:"));
        assertTrue(text.contains("VAT (21%):"));
        assertTrue(text.contains("Total:"));
        assertTrue(text.contains("\u20AC60.50"));
    }

    @Test
    void omitsDiscountLineWhenNoDiscountApplied() {
        Receipt receipt = new Receipt(
                List.of(new LineItem("Widget", new BigDecimal("50.00"), 1)),
                new BigDecimal("50.00"),
                new BigDecimal("0.00"),
                new BigDecimal("10.50"),
                new BigDecimal("60.50"));

        assertFalse(formatter.format(receipt).contains("Discount"));
    }

    @Test
    void includesDiscountLineWhenDiscountApplied() {
        Receipt receipt = new Receipt(
                List.of(new LineItem("Widget", new BigDecimal("200.00"), 1)),
                new BigDecimal("200.00"),
                new BigDecimal("20.00"),
                new BigDecimal("37.80"),
                new BigDecimal("217.80"));

        assertTrue(formatter.format(receipt).contains("Discount (10%):"));
    }
}
````

### order · run 3 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a single line item on an order.
 */
final class LineItem {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/**
 * Thrown when an order fails validation.
 */
class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}

/**
 * Validates line items, computes totals (with discount and VAT),
 * and produces a receipt for an order.
 */
public class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static class OrderResult {
        public final BigDecimal subtotal;
        public final BigDecimal discount;
        public final BigDecimal discountedSubtotal;
        public final BigDecimal vat;
        public final BigDecimal total;

        OrderResult(BigDecimal subtotal, BigDecimal discount, BigDecimal discountedSubtotal,
                    BigDecimal vat, BigDecimal total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.discountedSubtotal = discountedSubtotal;
            this.vat = vat;
            this.total = total;
        }
    }

    public void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item == null) {
                throw new OrderValidationException("Line item cannot be null.");
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new OrderValidationException("Line item description cannot be blank.");
            }
            if (item.getQuantity() <= 0) {
                throw new OrderValidationException(
                        "Line item quantity must be positive: " + item.getDescription());
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException(
                        "Line item unit price cannot be negative: " + item.getDescription());
            }
        }
    }

    public OrderResult computeTotal(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderResult(subtotal, discount, discountedSubtotal, vat, total);
    }

    public String buildReceipt(List<LineItem> lineItems) {
        OrderResult result = computeTotal(lineItems);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("nl", "NL"));

        StringBuilder receipt = new StringBuilder();
        receipt.append("----- RECEIPT -----\n");
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-20s x%-3d %10s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    currency.format(item.getLineTotal())));
        }
        receipt.append("--------------------\n");
        receipt.append(String.format("%-24s %10s%n", "Subtotal:", currency.format(result.subtotal)));
        if (result.discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-24s %10s%n", "Discount (10%):", "-" + currency.format(result.discount)));
        }
        receipt.append(String.format("%-24s %10s%n", "VAT (21%):", currency.format(result.vat)));
        receipt.append(String.format("%-24s %10s%n", "Total:", currency.format(result.total)));
        receipt.append("--------------------\n");

        return receipt.toString();
    }

    public static void main(String[] args) {
        List<LineItem> lineItems = List.of(
                new LineItem("Widget", 3, new BigDecimal("19.99")),
                new LineItem("Gadget", 2, new BigDecimal("24.50"))
        );

        OrderProcessor processor = new OrderProcessor();
        System.out.println(processor.buildReceipt(lineItems));
    }
}
````

### order · run 3 · uncle-bob-junior

````java
package com.example.orders;

public class InvalidLineItemException extends IllegalArgumentException {
    public InvalidLineItemException(String message) {
        super(message);
    }
}
````

````java
package com.example.orders;

import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new InvalidLineItemException("Description must not be blank.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLineItemException("Unit price must not be negative.");
        }
        if (quantity <= 0) {
            throw new InvalidLineItemException("Quantity must be positive.");
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
package com.example.orders;

import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
````

````java
package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderTotals calculateTotals(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = calculateSubtotal(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidLineItemException("Order must contain at least one line item.");
        }
    }

    private BigDecimal calculateSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ReceiptFormatter {

    private static final String CURRENCY_PREFIX = "EUR ";
    private static final int MONEY_SCALE = 2;

    public String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt\n-------\n");
        lineItems.forEach(item -> receipt.append(formatLineItem(item)));
        receipt.append("-------\n");
        receipt.append(formatAmountLine("Subtotal", totals.subtotal()));
        appendDiscountLine(receipt, totals.discount());
        receipt.append(formatAmountLine("VAT (21%)", totals.vat()));
        receipt.append(formatAmountLine("Total", totals.total()));
        return receipt.toString();
    }

    private void appendDiscountLine(StringBuilder receipt, BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatAmountLine("Discount (10%)", discount.negate()));
        }
    }

    private String formatLineItem(LineItem item) {
        return String.format("%-20s %2d x %10s = %12s%n",
                item.description(), item.quantity(), formatMoney(item.unitPrice()), formatMoney(item.lineTotal()));
    }

    private String formatAmountLine(String label, BigDecimal amount) {
        return String.format("%-20s %27s%n", label, formatMoney(amount));
    }

    private String formatMoney(BigDecimal amount) {
        return CURRENCY_PREFIX + amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
package com.example.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void computesLineTotalForValidItem() {
        LineItem item = new LineItem("Widget", new BigDecimal("9.99"), 3);

        assertEquals(new BigDecimal("29.97"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("  ", BigDecimal.TEN, 1));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", new BigDecimal("-1.00"), 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", BigDecimal.TEN, 0));
    }
}
````

````java
package com.example.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 2));

        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void discountAndVatAppliedWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void belowThresholdHasNoDiscount() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 1));

        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("60.50"), totals.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidLineItemException.class, () -> processor.calculateTotals(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(InvalidLineItemException.class, () -> processor.calculateTotals(null));
    }
}
````

````java
package com.example.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesDiscountLineWhenDiscountApplied() {
        OrderTotals totals = new OrderTotals(
                new BigDecimal("120.00"), new BigDecimal("12.00"),
                new BigDecimal("22.68"), new BigDecimal("130.68"));
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        String receipt = formatter.format(items, totals);

        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("EUR 130.68"));
    }

    @Test
    void omitsDiscountLineWhenNoDiscountApplied() {
        OrderTotals totals = new OrderTotals(
                new BigDecimal("50.00"), new BigDecimal("0.00"),
                new BigDecimal("10.50"), new BigDecimal("60.50"));
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 1));

        String receipt = formatter.format(items, totals);

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("EUR 60.50"));
    }
}
````

### order · run 4 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.text.NumberFormat;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new OrderValidationException("Line item description must not be blank");
            }
            if (quantity <= 0) {
                throw new OrderValidationException("Line item quantity must be positive: " + description);
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException("Line item unit price must not be negative: " + description);
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class OrderValidationException extends RuntimeException {
        public OrderValidationException(String message) {
            super(message);
        }
    }

    public record OrderResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal netTotal,
            BigDecimal vat,
            BigDecimal grandTotal,
            String receipt) {
    }

    public OrderResult processOrder(List<LineItem> items) {
        validateItems(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(SCALE);

        BigDecimal netTotal = subtotal.subtract(discount).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal vat = netTotal.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal grandTotal = netTotal.add(vat).setScale(SCALE, RoundingMode.HALF_UP);

        String receipt = buildReceipt(items, subtotal, discount, netTotal, vat, grandTotal, discountApplies);

        return new OrderResult(subtotal, discount, netTotal, vat, grandTotal, receipt);
    }

    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            Objects.requireNonNull(item, "Line item must not be null");
        }
    }

    private String buildReceipt(
            List<LineItem> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal netTotal,
            BigDecimal vat,
            BigDecimal grandTotal,
            boolean discountApplied) {

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nl-NL"));
        StringBuilder sb = new StringBuilder();

        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : items) {
            sb.append(String.format(
                    "%-20s %3d x %10s = %10s%n",
                    item.description(),
                    item.quantity(),
                    currency.format(item.unitPrice()),
                    currency.format(item.lineTotal())));
        }
        sb.append("-------\n");
        sb.append(String.format("%-20s %20s%n", "Subtotal (excl. VAT)", currency.format(subtotal)));
        if (discountApplied) {
            sb.append(String.format("%-20s %20s%n", "Discount (10%)", "-" + currency.format(discount)));
        }
        sb.append(String.format("%-20s %20s%n", "Net total", currency.format(netTotal)));
        sb.append(String.format("%-20s %20s%n", "VAT (21%)", currency.format(vat)));
        sb.append(String.format("%-20s %20s%n", "Grand total", currency.format(grandTotal)));

        return sb.toString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")),
                new LineItem("Gift voucher", 1, new BigDecimal("30.00")));

        OrderResult result = processor.processOrder(items);
        System.out.print(result.receipt());
    }
}
````

### order · run 4 · uncle-bob-junior

````java
// File: LineItem.java
import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// File: OrderTotals.java
import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
````

````java
// File: OrderProcessor.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD_EUROS = new BigDecimal("100");
    private static final int EURO_SCALE = 2;

    public OrderTotals calculateTotals(List<LineItem> lineItems) {
        requireNonEmpty(lineItems);

        BigDecimal subtotal = sumLineItems(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal netOfDiscount = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(netOfDiscount);
        BigDecimal total = netOfDiscount.add(vat);

        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private void requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private BigDecimal sumLineItems(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD_EUROS) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal netAmount) {
        return netAmount.multiply(VAT_RATE);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(EURO_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// File: ReceiptFormatter.java
import java.util.List;

public final class ReceiptFormatter {

    public String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private String formatLine(LineItem item) {
        return String.format("%-20s x%-3d %8.2f EUR",
                item.description(), item.quantity(), item.lineTotal());
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append(String.format("%-24s %8.2f EUR%n", "Subtotal", totals.subtotal()));
        receipt.append(String.format("%-24s %8.2f EUR%n", "Discount", totals.discount().negate()));
        receipt.append(String.format("%-24s %8.2f EUR%n", "VAT (21%)", totals.vat()));
        receipt.append(String.format("%-24s %8.2f EUR%n", "Total", totals.total()));
    }
}
````

````java
// File: OrderReceiptService.java
import java.util.List;

public final class OrderReceiptService {

    private final OrderProcessor orderProcessor;
    private final ReceiptFormatter receiptFormatter;

    public OrderReceiptService(OrderProcessor orderProcessor, ReceiptFormatter receiptFormatter) {
        this.orderProcessor = orderProcessor;
        this.receiptFormatter = receiptFormatter;
    }

    public String generateReceipt(List<LineItem> lineItems) {
        OrderTotals totals = orderProcessor.calculateTotals(lineItems);
        return receiptFormatter.format(lineItems, totals);
    }
}
````

````java
// File: OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsNullLineItemList() {
        assertThrows(IllegalArgumentException.class, () -> processor.calculateTotals(null));
    }

    @Test
    void rejectsEmptyLineItemList() {
        assertThrows(IllegalArgumentException.class, () -> processor.calculateTotals(List.of()));
    }

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50"), 2));
        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60"), 2));
        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("30"), 2),
                new LineItem("Gadget", new BigDecimal("50"), 1));
        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("110.00"), totals.subtotal());
        assertEquals(new BigDecimal("11.00"), totals.discount());
    }
}
````

````java
// File: LineItemTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", new BigDecimal("10"), 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("10"), 0));
    }

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        LineItem item = new LineItem("Widget", new BigDecimal("10"), 3);
        assertEquals(new BigDecimal("30"), item.lineTotal());
    }
}
````

````java
// File: OrderReceiptServiceTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderReceiptServiceTest {

    private final OrderReceiptService service =
            new OrderReceiptService(new OrderProcessor(), new ReceiptFormatter());

    @Test
    void receiptContainsLineItemsAndTotals() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60"), 2));
        String receipt = service.generateReceipt(items);

        assertTrue(receipt.contains("Widget"));
        assertTrue(receipt.contains("Subtotal"));
        assertTrue(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("Total"));
    }
}
````

### order · run 5 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new InvalidOrderException("Line item description must not be blank");
            }
            if (quantity <= 0) {
                throw new InvalidOrderException("Line item quantity must be positive: " + description);
            }
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new InvalidOrderException("Line item unit price must be non-negative: " + description);
            }
        }

        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Receipt(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            String text) {
    }

    public static Receipt processOrder(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item");
        }

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(SCALE);

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(SCALE, RoundingMode.HALF_UP);

        String text = buildReceiptText(lineItems, subtotal, discount, vat, total);

        return new Receipt(subtotal, discount, vat, total, text);
    }

    private static String buildReceiptText(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total) {

        StringBuilder sb = new StringBuilder();
        sb.append("Order Receipt\n");
        sb.append("-------------\n");

        for (LineItem item : lineItems) {
            sb.append(String.format(Locale.UK, "%-25s %2d x %8.2f = %8.2f%n",
                    item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
        }

        sb.append("-------------\n");
        sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "Subtotal (excl. VAT):", subtotal));
        if (discount.signum() > 0) {
            sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "Discount (10%):", discount.negate()));
        }
        sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "VAT (21%):", vat));
        sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "Total (EUR):", total));

        return sb.toString();
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        List<LineItem> items = List.of(
                new LineItem("Charity lottery ticket - monthly", 2, new BigDecimal("29.95")),
                new LineItem("Charity lottery ticket - extra", 1, new BigDecimal("49.95"))
        );

        Receipt receipt = processOrder(items);
        System.out.println(receipt.text());
    }
}
````

### order · run 5 · uncle-bob-junior

````java
// OrderLineItem.java
package com.plg.order;

import java.math.BigDecimal;

public record OrderLineItem(String description, BigDecimal unitPrice, int quantity) {

    public OrderLineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive for: " + description);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive for: " + description);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// Receipt.java
package com.plg.order;

import java.math.BigDecimal;
import java.util.List;

public record Receipt(
        List<OrderLineItem> lineItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal total) {

    public Receipt {
        lineItems = List.copyOf(lineItems);
    }

    public boolean hasDiscount() {
        return discount.signum() > 0;
    }
}
````

````java
// OrderProcessor.java
package com.plg.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int EURO_SCALE = 2;

    public Receipt process(List<OrderLineItem> lineItems) {
        requireNonEmpty(lineItems);

        BigDecimal subtotal = roundToEuroCents(sumLineTotals(lineItems));
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToEuroCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(lineItems, subtotal, discount, vat, total);
    }

    private void requireNonEmpty(List<OrderLineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private BigDecimal sumLineTotals(List<OrderLineItem> lineItems) {
        return lineItems.stream()
                .map(OrderLineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean exceedsThreshold = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!exceedsThreshold) {
            return roundToEuroCents(BigDecimal.ZERO);
        }
        return roundToEuroCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal roundToEuroCents(BigDecimal amount) {
        return amount.setScale(EURO_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// ReceiptFormatter.java
package com.plg.order;

import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String CURRENCY_PREFIX = "EUR ";

    public String format(Receipt receipt) {
        StringBuilder text = new StringBuilder();
        appendLineItems(text, receipt);
        appendTotals(text, receipt);
        return text.toString();
    }

    private void appendLineItems(StringBuilder text, Receipt receipt) {
        text.append("Receipt\n-------\n");
        for (OrderLineItem item : receipt.lineItems()) {
            text.append("%-20s %2d x %10s = %10s%n".formatted(
                    item.description(), item.quantity(), euro(item.unitPrice()), euro(item.lineTotal())));
        }
        text.append("-------\n");
    }

    private void appendTotals(StringBuilder text, Receipt receipt) {
        text.append("Subtotal:      %10s%n".formatted(euro(receipt.subtotal())));
        if (receipt.hasDiscount()) {
            text.append("Discount (10%%): -%9s%n".formatted(euro(receipt.discount())));
        }
        text.append("VAT (21%%):     %10s%n".formatted(euro(receipt.vat())));
        text.append("Total:         %10s%n".formatted(euro(receipt.total())));
    }

    private String euro(BigDecimal amount) {
        return CURRENCY_PREFIX + amount;
    }
}
````

````java
// OrderLineItemTest.java
package com.plg.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderLineItemTest {

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        OrderLineItem item = new OrderLineItem("Widget", new BigDecimal("9.99"), 3);

        assertEquals(new BigDecimal("29.97"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLineItem(" ", BigDecimal.TEN, 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLineItem("Widget", BigDecimal.TEN, 0));
    }
}
````

````java
// OrderProcessorTest.java
package com.plg.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(List.of()));
    }

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        OrderLineItem item = new OrderLineItem("Widget", new BigDecimal("100.00"), 1);

        Receipt receipt = processor.process(List.of(item));

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        OrderLineItem item = new OrderLineItem("Widget", new BigDecimal("150.00"), 1);

        Receipt receipt = processor.process(List.of(item));

        assertEquals(new BigDecimal("150.00"), receipt.subtotal());
        assertEquals(new BigDecimal("15.00"), receipt.discount());
        assertEquals(new BigDecimal("28.35"), receipt.vat());
        assertEquals(new BigDecimal("163.35"), receipt.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        List<OrderLineItem> items = List.of(
                new OrderLineItem("Widget", new BigDecimal("40.00"), 2),
                new OrderLineItem("Gadget", new BigDecimal("25.00"), 1));

        Receipt receipt = processor.process(items);

        assertEquals(new BigDecimal("105.00"), receipt.subtotal());
        assertEquals(new BigDecimal("10.50"), receipt.discount());
    }
}
````

````java
// ReceiptFormatterTest.java
package com.plg.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesDiscountLineOnlyWhenDiscountApplied() {
        Receipt withDiscount = new Receipt(
                List.of(new OrderLineItem("Widget", new BigDecimal("150.00"), 1)),
                new BigDecimal("150.00"), new BigDecimal("15.00"),
                new BigDecimal("28.35"), new BigDecimal("163.35"));

        Receipt withoutDiscount = new Receipt(
                List.of(new OrderLineItem("Widget", new BigDecimal("50.00"), 1)),
                new BigDecimal("50.00"), new BigDecimal("0.00"),
                new BigDecimal("10.50"), new BigDecimal("60.50"));

        assertTrue(formatter.format(withDiscount).contains("Discount"));
        assertFalse(formatter.format(withoutDiscount).contains("Discount"));
    }
}
````

### order · run 6 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, BigDecimal unitPrice, int quantity) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new InvalidOrderException("Line item description must not be blank.");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderException("Line item '" + description + "' has an invalid unit price.");
            }
            if (quantity <= 0) {
                throw new InvalidOrderException("Line item '" + description + "' must have a positive quantity.");
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public record Receipt(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vatAmount,
            BigDecimal total,
            String text) {
    }

    public Receipt processOrder(List<LineItem> lineItems) {
        validate(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        String text = buildReceiptText(lineItems, subtotal, discount, vatAmount, total, discountApplies);

        return new Receipt(subtotal, discount, vatAmount, total, text);
    }

    private void validate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            Objects.requireNonNull(item, "Line item must not be null.");
        }
    }

    private String buildReceiptText(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vatAmount,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(
                    "%-20s %3d x %8s = %10s%n",
                    item.description(),
                    item.quantity(),
                    formatAmount(item.unitPrice()),
                    formatAmount(item.lineTotal().setScale(2, RoundingMode.HALF_UP))));
        }
        sb.append("-------\n");
        sb.append(String.format("Subtotal:        %10s%n", formatAmount(subtotal)));
        if (discountApplies) {
            sb.append(String.format("Discount (10%%):  -%9s%n", formatAmount(discount)));
        }
        sb.append(String.format("VAT (21%%):        %10s%n", formatAmount(vatAmount)));
        sb.append(String.format("Total:            %10s%n", formatAmount(total)));
        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", new BigDecimal("45.00"), 2),
                new LineItem("Donation add-on", new BigDecimal("15.00"), 1)
        );

        Receipt receipt = processor.processOrder(items);
        System.out.print(receipt.text());
    }
}
````

### order · run 6 · uncle-bob-junior

````java
package com.plg.orders;

import java.math.BigDecimal;

record LineItem(String description, BigDecimal unitPrice, int quantity) {
}
````

````java
package com.plg.orders;

import java.math.BigDecimal;

record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
````

````java
package com.plg.orders;

import java.util.List;

sealed interface OrderResult<T> permits OrderResult.Success, OrderResult.Failure {

    record Success<T>(T value) implements OrderResult<T> {
    }

    record Failure<T>(List<String> errors) implements OrderResult<T> {
    }
}
````

````java
package com.plg.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class LineItemValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    List<String> validate(List<LineItem> lineItems) {
        if (lineItems.isEmpty()) {
            return List.of("Order must contain at least one line item");
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < lineItems.size(); i++) {
            errors.addAll(validateLineItem(lineItems.get(i), i));
        }
        return errors;
    }

    private List<String> validateLineItem(LineItem item, int index) {
        List<String> errors = new ArrayList<>();
        if (item.description() == null || item.description().isBlank()) {
            errors.add("Line item %d: description must not be blank".formatted(index));
        }
        if (item.unitPrice() == null || item.unitPrice().compareTo(ZERO) <= 0) {
            errors.add("Line item %d: unit price must be positive".formatted(index));
        }
        if (item.quantity() <= 0) {
            errors.add("Line item %d: quantity must be positive".formatted(index));
        }
        return errors;
    }
}
````

````java
package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * All amounts are EUR, rounded to 2 decimals with HALF_UP at each monetary step
 * to match standard invoice rounding conventions.
 */
final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    OrderTotals calculate(List<LineItem> lineItems) {
        BigDecimal subtotal = sumLineItems(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderTotals(subtotal, discount, vat, total);
    }

    private BigDecimal sumLineItems(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE), BigDecimal::add);
    }

    private BigDecimal lineTotal(LineItem item) {
        return round(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean eligible = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return eligible ? round(subtotal.multiply(DISCOUNT_RATE)) : BigDecimal.ZERO.setScale(MONEY_SCALE);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class ReceiptFormatter {

    private static final String CURRENCY = "EUR";

    String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        lineItems.forEach(item -> appendLine(receipt, item));
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append("Order Receipt\n");
        receipt.append("-------------\n");
    }

    private void appendLine(StringBuilder receipt, LineItem item) {
        BigDecimal lineTotal = item.unitPrice()
                .multiply(BigDecimal.valueOf(item.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
        receipt.append("%d x %s @ %s %.2f = %s %.2f%n".formatted(
                item.quantity(), item.description(), CURRENCY, item.unitPrice(), CURRENCY, lineTotal));
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-------------\n");
        appendMoneyLine(receipt, "Subtotal", totals.subtotal());
        appendMoneyLine(receipt, "Discount (10%)", totals.discount().negate());
        appendMoneyLine(receipt, "VAT (21%)", totals.vat());
        appendMoneyLine(receipt, "Total", totals.total());
    }

    private void appendMoneyLine(StringBuilder receipt, String label, BigDecimal amount) {
        receipt.append("%s: %s %.2f%n".formatted(label, CURRENCY, amount));
    }
}
````

````java
package com.plg.orders;

import java.util.List;

final class OrderProcessor {

    private final LineItemValidator validator = new LineItemValidator();
    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    OrderResult<String> process(List<LineItem> lineItems) {
        List<String> errors = validator.validate(lineItems);
        if (!errors.isEmpty()) {
            return new OrderResult.Failure<>(errors);
        }
        OrderTotals totals = calculator.calculate(lineItems);
        String receipt = formatter.format(lineItems, totals);
        return new OrderResult.Success<>(receipt);
    }
}
````

````java
package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineItemValidatorTest {

    private final LineItemValidator validator = new LineItemValidator();

    @Test
    void acceptsAllValidLineItems() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("10.00"), 2));

        assertTrue(validator.validate(items).isEmpty());
    }

    @Test
    void rejectsEmptyOrder() {
        List<String> errors = validator.validate(List.of());

        assertEquals(1, errors.size());
    }

    @Test
    void rejectsBlankDescription() {
        List<LineItem> items = List.of(new LineItem(" ", new BigDecimal("10.00"), 1));

        assertEquals(1, validator.validate(items).size());
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        List<LineItem> items = List.of(new LineItem("Widget", BigDecimal.ZERO, 1));

        assertEquals(1, validator.validate(items).size());
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("10.00"), 0));

        assertEquals(1, validator.validate(items).size());
    }

    @Test
    void collectsMultipleErrorsAcrossLines() {
        List<LineItem> items = List.of(
                new LineItem("", BigDecimal.ZERO, 0),
                new LineItem("Widget", new BigDecimal("10.00"), 1));

        assertEquals(3, validator.validate(items).size());
    }
}
````

````java
package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 2));

        OrderTotals totals = calculator.calculate(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void discountAppliedWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("101.00"), 1));

        OrderTotals totals = calculator.calculate(items);

        assertEquals(new BigDecimal("101.00"), totals.subtotal());
        assertEquals(new BigDecimal("10.10"), totals.discount());
        assertEquals(new BigDecimal("19.09"), totals.vat());
        assertEquals(new BigDecimal("109.99"), totals.total());
    }

    @Test
    void sumsMultipleLineItems() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("10.00"), 2),
                new LineItem("Gadget", new BigDecimal("5.00"), 3));

        OrderTotals totals = calculator.calculate(items);

        assertEquals(new BigDecimal("35.00"), totals.subtotal());
    }
}
````

````java
package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptForValidOrder() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        OrderResult<String> result = processor.process(items);

        OrderResult.Success<String> success = assertInstanceOf(OrderResult.Success.class, result);
        assertTrue(success.value().contains("Discount (10%): -EUR 12.00"));
        assertTrue(success.value().contains("Total: EUR 130.68"));
    }

    @Test
    void failsWithErrorsForInvalidOrder() {
        OrderResult<String> result = processor.process(List.of());

        OrderResult.Failure<String> failure = assertInstanceOf(OrderResult.Failure.class, result);
        assertTrue(failure.errors().contains("Order must contain at least one line item"));
    }
}
````

### order · run 7 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/** Thrown when an order or one of its line items fails validation. */
final class InvalidOrderException extends RuntimeException {
    InvalidOrderException(String message) {
        super(message);
    }
}

/** A single line item on an order. */
final class LineItem {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    String getDescription() {
        return description;
    }

    int getQuantity() {
        return quantity;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/** An order consisting of one or more line items. */
final class Order {

    private final String orderId;
    private final List<LineItem> lineItems;

    Order(String orderId, List<LineItem> lineItems) {
        this.orderId = orderId;
        this.lineItems = lineItems;
    }

    String getOrderId() {
        return orderId;
    }

    List<LineItem> getLineItems() {
        return lineItems;
    }
}

/** Validates orders, applies discount/VAT rules, and renders a receipt. */
final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final DateTimeFormatter RECEIPT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Validates the order, computes discount and VAT, and returns a formatted receipt.
     *
     * @throws InvalidOrderException if the order or any line item is invalid
     */
    String processOrder(Order order, LocalDate receiptDate) {
        validate(order);

        BigDecimal subtotal = order.getLineItems().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE);
        BigDecimal total = discountedSubtotal.add(vat);

        return buildReceipt(order, subtotal, discount, discountedSubtotal, vat, total, receiptDate);
    }

    private void validate(Order order) {
        if (order == null) {
            throw new InvalidOrderException("Order must not be null.");
        }
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            throw new InvalidOrderException("Order must have a non-blank order ID.");
        }
        if (order.getLineItems() == null || order.getLineItems().isEmpty()) {
            throw new InvalidOrderException("Order " + order.getOrderId() + " must contain at least one line item.");
        }
        for (LineItem item : order.getLineItems()) {
            validateLineItem(order.getOrderId(), item);
        }
    }

    private void validateLineItem(String orderId, LineItem item) {
        if (item == null) {
            throw new InvalidOrderException("Order " + orderId + " contains a null line item.");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new InvalidOrderException("Order " + orderId + " has a line item with a blank description.");
        }
        if (item.getQuantity() < 1) {
            throw new InvalidOrderException(
                    "Order " + orderId + ": quantity for '" + item.getDescription() + "' must be at least 1.");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOrderException(
                    "Order " + orderId + ": unit price for '" + item.getDescription() + "' must not be negative.");
        }
    }

    private String buildReceipt(
            Order order,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal discountedSubtotal,
            BigDecimal vat,
            BigDecimal total,
            LocalDate receiptDate) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt for order ").append(order.getOrderId()).append('\n');
        sb.append("Date: ").append(receiptDate.format(RECEIPT_DATE_FORMAT)).append('\n');
        sb.append("--------------------------------------------------\n");

        for (LineItem item : order.getLineItems()) {
            sb.append(String.format(
                    "%-25s %3d x %8s = %10s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatAmount(item.getUnitPrice()),
                    formatAmount(item.lineTotal())));
        }

        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-38s %10s%n", "Subtotal (excl. VAT):", formatAmount(subtotal)));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-38s -%9s%n", "Discount (10%):", formatAmount(discount)));
            sb.append(String.format("%-38s %10s%n", "Subtotal after discount:", formatAmount(discountedSubtotal)));
        }
        sb.append(String.format("%-38s %10s%n", "VAT (21%):", formatAmount(vat)));
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-38s %10s%n", "Total:", formatAmount(total)));

        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return "\u20ac" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

/** Example usage. */
final class OrderProcessingDemo {

    public static void main(String[] args) {
        Order order = new Order(
                "ORD-1001",
                List.of(
                        new LineItem("Lottery subscription - monthly", 2, new BigDecimal("45.00")),
                        new LineItem("Charity add-on", 1, new BigDecimal("15.50"))
                ));

        OrderProcessor processor = new OrderProcessor();
        String receipt = processor.processOrder(order, LocalDate.of(2026, 8, 27));
        System.out.println(receipt);
    }
}
````

### order · run 7 · uncle-bob-junior

````java
// filename: PricingRules.java
import java.math.BigDecimal;

final class PricingRules {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    static final int MONEY_SCALE = 2;

    private PricingRules() {
    }
}
````

````java
// filename: InvalidOrderException.java
class InvalidOrderException extends RuntimeException {

    InvalidOrderException(String message) {
        super(message);
    }
}
````

````java
// filename: LineItem.java
import java.math.BigDecimal;
import java.math.RoundingMode;

record LineItem(String productName, int quantity, BigDecimal unitPrice) {

    LineItem {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must not be negative: " + unitPrice);
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(PricingRules.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// filename: OrderTotals.java
import java.math.BigDecimal;

record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
````

````java
// filename: OrderCalculator.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class OrderCalculator {

    OrderTotals calculate(List<LineItem> lineItems) {
        requireNonEmpty(lineItems);
        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal vat = vatFor(subtotal.subtract(discount));
        BigDecimal total = subtotal.subtract(discount).add(vat);
        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private void requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one line item");
        }
    }

    private BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(PricingRules.DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(PricingRules.DISCOUNT_RATE);
    }

    private BigDecimal vatFor(BigDecimal amount) {
        return amount.multiply(PricingRules.VAT_RATE);
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(PricingRules.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// filename: ReceiptFormatter.java
import java.math.BigDecimal;
import java.util.List;

class ReceiptFormatter {

    private static final String CURRENCY = "EUR";
    private static final String HEADER = "=== Order Receipt ===";
    private static final String VAT_LABEL =
            "VAT (" + PricingRules.VAT_RATE.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString() + "%)";

    String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append(HEADER).append(System.lineSeparator());
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLineItem(item)).append(System.lineSeparator());
        }
    }

    private String formatLineItem(LineItem item) {
        return "%d x %s @ %s %s = %s %s".formatted(
                item.quantity(), item.productName(),
                CURRENCY, item.unitPrice().toPlainString(),
                CURRENCY, item.lineTotal().toPlainString());
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append(formatMoneyLine("Subtotal", totals.subtotal())).append(System.lineSeparator());
        receipt.append(formatMoneyLine("Discount", totals.discount())).append(System.lineSeparator());
        receipt.append(formatMoneyLine(VAT_LABEL, totals.vat())).append(System.lineSeparator());
        receipt.append(formatMoneyLine("Total", totals.total()));
    }

    private String formatMoneyLine(String label, BigDecimal amount) {
        return "%s: %s %s".formatted(label, CURRENCY, amount.toPlainString());
    }
}
````

````java
// filename: OrderProcessor.java
import java.util.List;

class OrderProcessor {

    private final OrderCalculator calculator;
    private final ReceiptFormatter formatter;

    OrderProcessor() {
        this(new OrderCalculator(), new ReceiptFormatter());
    }

    OrderProcessor(OrderCalculator calculator, ReceiptFormatter formatter) {
        this.calculator = calculator;
        this.formatter = formatter;
    }

    String process(List<LineItem> lineItems) {
        OrderTotals totals = calculator.calculate(lineItems);
        return formatter.format(lineItems, totals);
    }
}
````

````java
// filename: LineItemTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void computesLineTotalForValidItem() {
        LineItem item = new LineItem("Widget", 2, new BigDecimal("10.00"));

        assertEquals(new BigDecimal("20.00"), item.lineTotal());
    }

    @Test
    void rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", 1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", 0, new BigDecimal("10.00")));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", 1, new BigDecimal("-1.00")));
    }
}
````

````java
// filename: OrderCalculatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesNoDiscountBelowThreshold() {
        List<LineItem> lineItems = List.of(new LineItem("Widget", 2, new BigDecimal("10.00")));

        OrderTotals totals = calculator.calculate(lineItems);

        assertEquals(new BigDecimal("20.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("4.20"), totals.vat());
        assertEquals(new BigDecimal("24.20"), totals.total());
    }

    @Test
    void appliesNoDiscountWhenSubtotalExactlyAtThreshold() {
        List<LineItem> lineItems = List.of(new LineItem("Widget", 1, new BigDecimal("100.00")));

        OrderTotals totals = calculator.calculate(lineItems);

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<LineItem> lineItems = List.of(new LineItem("Gadget", 3, new BigDecimal("50.00")));

        OrderTotals totals = calculator.calculate(lineItems);

        assertEquals(new BigDecimal("150.00"), totals.subtotal());
        assertEquals(new BigDecimal("15.00"), totals.discount());
        assertEquals(new BigDecimal("28.35"), totals.vat());
        assertEquals(new BigDecimal("163.35"), totals.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidOrderException.class, () -> calculator.calculate(List.of()));
    }
}
````

````java
// filename: OrderProcessorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptWithLineItemsAndTotals() {
        List<LineItem> lineItems = List.of(new LineItem("Widget", 2, new BigDecimal("10.00")));

        String receipt = processor.process(lineItems);

        assertTrue(receipt.contains("2 x Widget @ EUR 10.00 = EUR 20.00"));
        assertTrue(receipt.contains("Subtotal: EUR 20.00"));
        assertTrue(receipt.contains("VAT (21%): EUR 4.20"));
        assertTrue(receipt.contains("Total: EUR 24.20"));
    }
}
````

### order · run 8 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static final class LineItem {
        private final String description;
        private final int quantity;
        private final BigDecimal unitPrice;

        public LineItem(String description, int quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class OrderResult {
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        private final BigDecimal vat;
        private final BigDecimal total;
        private final String receipt;

        public OrderResult(BigDecimal subtotal, BigDecimal discount, BigDecimal vat,
                            BigDecimal total, String receipt) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            this.total = total;
            this.receipt = receipt;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public BigDecimal getVat() {
            return vat;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public String getReceipt() {
            return receipt;
        }
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, vat, total);

        return new OrderResult(subtotal, discount, vat, total, receipt);
    }

    private void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item == null) {
                throw new IllegalArgumentException("Line item must not be null.");
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new IllegalArgumentException("Line item description must not be blank.");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.getDescription() + "' must have a positive quantity.");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.getDescription() + "' must have a non-negative unit price.");
            }
        }
    }

    private String buildReceipt(List<LineItem> lineItems, BigDecimal subtotal,
                                 BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format("%-20s %3d x %8.2f = %10.2f%n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.lineTotal()));
        }
        sb.append("-------\n");
        sb.append(String.format("%-20s %21.2f%n", "Subtotal", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-20s %21.2f%n", "Discount (10%)", discount.negate()));
        }
        sb.append(String.format("%-20s %21.2f%n", "VAT (21%)", vat));
        sb.append(String.format("%-20s %21.2f%n", "Total", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")),
                new LineItem("Handling fee", 1, new BigDecimal("5.00"))
        );

        OrderResult result = processor.processOrder(items);
        System.out.println(result.getReceipt());
    }
}
````

### order · run 8 · uncle-bob-junior

````java
// LineItem.java
import java.math.BigDecimal;

public final class LineItem {

    private final String description;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        validate(description, unitPrice, quantity);
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    private static void validate(String description, BigDecimal unitPrice, int quantity) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String description() {
        return description;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }
}
````

````java
// Order.java
import java.util.List;

public final class Order {

    private final List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        this.lineItems = List.copyOf(lineItems);
    }

    public List<LineItem> lineItems() {
        return lineItems;
    }
}
````

````java
// OrderReceipt.java
import java.math.BigDecimal;
import java.util.List;

public final class OrderReceipt {

    private final List<LineItem> lineItems;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vat;
    private final BigDecimal total;

    public OrderReceipt(List<LineItem> lineItems, BigDecimal subtotal, BigDecimal discount,
                         BigDecimal vat, BigDecimal total) {
        this.lineItems = List.copyOf(lineItems);
        this.subtotal = subtotal;
        this.discount = discount;
        this.vat = vat;
        this.total = total;
    }

    public List<LineItem> lineItems() {
        return lineItems;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal discount() {
        return discount;
    }

    public BigDecimal vat() {
        return vat;
    }

    public BigDecimal total() {
        return total;
    }
}
````

````java
// OrderProcessor.java
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderReceipt process(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderReceipt(order.lineItems(), subtotal, discount, vat, total);
    }

    private BigDecimal calculateSubtotal(Order order) {
        return order.lineItems().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// ReceiptFormatter.java
import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String CURRENCY_LABEL = "EUR";

    public String format(OrderReceipt receipt) {
        StringBuilder builder = new StringBuilder();
        appendLineItems(builder, receipt);
        appendTotals(builder, receipt);
        return builder.toString();
    }

    private void appendLineItems(StringBuilder builder, OrderReceipt receipt) {
        for (LineItem item : receipt.lineItems()) {
            builder.append(item.description())
                    .append(" x")
                    .append(item.quantity())
                    .append(": ")
                    .append(formatAmount(item.lineTotal()))
                    .append(System.lineSeparator());
        }
    }

    private void appendTotals(StringBuilder builder, OrderReceipt receipt) {
        builder.append("Subtotal: ").append(formatAmount(receipt.subtotal())).append(System.lineSeparator());
        builder.append("Discount: -").append(formatAmount(receipt.discount())).append(System.lineSeparator());
        builder.append("VAT (21%): ").append(formatAmount(receipt.vat())).append(System.lineSeparator());
        builder.append("Total: ").append(formatAmount(receipt.total()));
    }

    private String formatAmount(BigDecimal amount) {
        return CURRENCY_LABEL + " " + amount.toPlainString();
    }
}
````

````java
// LineItemTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        LineItem item = new LineItem("Widget", new BigDecimal("2.50"), 3);
        assertEquals(new BigDecimal("7.50"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", new BigDecimal("1.00"), 1));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("-0.01"), 1));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("1.00"), 0));
    }
}
````

````java
// OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsOrderWithNoLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void appliesNoDiscountWhenSubtotalIsExactlyThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("100.00"), 1)));
        OrderReceipt receipt = processor.process(order);

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("150.00"), 1)));
        OrderReceipt receipt = processor.process(order);

        assertEquals(new BigDecimal("15.00"), receipt.discount());
        assertEquals(new BigDecimal("28.35"), receipt.vat());
        assertEquals(new BigDecimal("163.35"), receipt.total());
    }

    @Test
    void computesSubtotalAcrossMultipleLineItems() {
        Order order = new Order(List.of(
                new LineItem("Widget", new BigDecimal("30.00"), 2),
                new LineItem("Gadget", new BigDecimal("10.00"), 1)));
        OrderReceipt receipt = processor.process(order);

        assertEquals(new BigDecimal("70.00"), receipt.subtotal());
        assertEquals(new BigDecimal("0.00"), receipt.discount());
    }
}
````

````java
// ReceiptFormatterTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesEachLineItemAndTotals() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 1));
        OrderReceipt receipt = new OrderReceipt(items,
                new BigDecimal("50.00"), new BigDecimal("0.00"),
                new BigDecimal("10.50"), new BigDecimal("60.50"));

        String result = formatter.format(receipt);

        assertTrue(result.contains("Widget x1: EUR 50.00"));
        assertTrue(result.contains("Subtotal: EUR 50.00"));
        assertTrue(result.contains("Discount: -EUR 0.00"));
        assertTrue(result.contains("VAT (21%): EUR 10.50"));
        assertTrue(result.contains("Total: EUR 60.50"));
    }
}
````

### order · run 9 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Processes a customer order: validates line items, computes totals
 * (with discount and VAT), and produces a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {
        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record OrderTotals(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal discountedSubtotal,
            BigDecimal vat,
            BigDecimal total
    ) {}

    public static final class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    /**
     * Validates that every line item has a non-blank description,
     * a positive quantity, and a non-negative unit price.
     */
    public void validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new InvalidOrderException("Line item %d is missing.".formatted(i + 1));
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new InvalidOrderException("Line item %d has no description.".formatted(i + 1));
            }
            if (item.quantity() <= 0) {
                throw new InvalidOrderException(
                        "Line item %d (%s) must have a positive quantity.".formatted(i + 1, item.description()));
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderException(
                        "Line item %d (%s) must have a non-negative unit price.".formatted(i + 1, item.description()));
            }
        }
    }

    /**
     * Computes subtotal, discount (10% if pre-VAT subtotal exceeds 100 EUR),
     * VAT (21% on the discounted subtotal), and the final total.
     */
    public OrderTotals computeTotal(List<LineItem> items) {
        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = qualifiesForDiscount
                ? subtotal.multiply(DISCOUNT_RATE)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderTotals(
                round(subtotal),
                round(discount),
                round(discountedSubtotal),
                round(vat),
                round(total)
        );
    }

    /**
     * Produces a human-readable receipt string for the order.
     */
    public String generateReceipt(List<LineItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        validateLineItems(items);
        OrderTotals totals = computeTotal(items);

        StringBuilder receipt = new StringBuilder();
        receipt.append("===== RECEIPT =====\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-20s x%-3d %8s EUR%n",
                    item.description(),
                    item.quantity(),
                    round(item.lineTotal())));
        }
        receipt.append("--------------------\n");
        receipt.append(String.format("%-24s %8s EUR%n", "Subtotal:", totals.subtotal()));
        if (totals.discount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-24s -%7s EUR%n", "Discount (10%):", totals.discount()));
        }
        receipt.append(String.format("%-24s %8s EUR%n", "VAT (21%):", totals.vat()));
        receipt.append("====================\n");
        receipt.append(String.format("%-24s %8s EUR%n", "Total:", totals.total()));
        return receipt.toString();
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        List<LineItem> items = List.of(
                new LineItem("Charity gift card", 2, new BigDecimal("30.00")),
                new LineItem("Lottery ticket bundle", 1, new BigDecimal("45.00"))
        );

        System.out.print(processor.generateReceipt(items));
    }
}
````

### order · run 9 · uncle-bob-junior

````java
// LineItem.java
import java.math.BigDecimal;

public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Line item quantity must be positive: " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Line item unit price must not be negative: " + unitPrice);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// OrderReceipt.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderReceipt {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderReceipt() {
    }

    public static String process(List<LineItem> lineItems) {
        List<LineItem> validItems = requireNonEmpty(lineItems);
        BigDecimal subtotal = subtotalOf(validItems);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = vatFor(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        return buildReceipt(validItems, subtotal, discount, vat, total);
    }

    private static List<LineItem> requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        return lineItems;
    }

    private static BigDecimal subtotalOf(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal vatFor(BigDecimal amount) {
        return round(amount.multiply(VAT_RATE));
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static String buildReceipt(List<LineItem> lineItems, BigDecimal subtotal,
                                        BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt\n-------\n");
        lineItems.forEach(item -> receipt.append(formatLine(item)));
        receipt.append("-------\n");
        receipt.append(formatTotal("Subtotal", subtotal));
        if (discount.signum() > 0) {
            receipt.append(formatTotal("Discount (10%)", discount.negate()));
        }
        receipt.append(formatTotal("VAT (21%)", vat));
        receipt.append(formatTotal("Total", total));
        return receipt.toString();
    }

    private static String formatLine(LineItem item) {
        return String.format("%2dx %-20s EUR %8.2f%n",
                item.quantity(), item.description(), item.lineTotal());
    }

    private static String formatTotal(String label, BigDecimal amount) {
        return String.format("%-16s EUR %8.2f%n", label, amount);
    }
}
````

````java
// OrderReceiptTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderReceiptTest {

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderReceipt.process(null));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderReceipt.process(List.of()));
    }

    @Test
    void rejectsLineItemWithBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(" ", 1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsLineItemWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Book", 0, new BigDecimal("10.00")));
    }

    @Test
    void rejectsLineItemWithNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Book", 1, new BigDecimal("-1.00")));
    }

    @Test
    void appliesNoDiscountAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("50.00")));
        String receipt = OrderReceipt.process(items);

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("121.00")); // 100.00 + 21% VAT
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("200.00")));
        String receipt = OrderReceipt.process(items);

        // subtotal 200.00, discount 20.00, discounted 180.00, vat 37.80, total 217.80
        assertTrue(receipt.contains("Discount"));
        assertTrue(receipt.contains("217.80"));
    }

    @Test
    void computesTotalsWithoutDiscountBelowThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("50.00")));
        String receipt = OrderReceipt.process(items);

        // subtotal 50.00, vat 10.50, total 60.50
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("60.50"));
    }
}
````

### order · run 10 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static final class LineItem {
        private final String description;
        private final int quantity;
        private final BigDecimal unitPrice;

        public LineItem(String description, int quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getDescription() { return description; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class Order {
        private final String orderId;
        private final List<LineItem> lineItems;

        public Order(String orderId, List<LineItem> lineItems) {
            this.orderId = orderId;
            this.lineItems = lineItems;
        }

        public String getOrderId() { return orderId; }
        public List<LineItem> getLineItems() { return lineItems; }
    }

    public static final class OrderTotals {
        public final BigDecimal subtotal;
        public final BigDecimal discount;
        public final BigDecimal vat;
        public final BigDecimal grandTotal;

        public OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal grandTotal) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            this.grandTotal = grandTotal;
        }
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public void validateLineItems(Order order) {
        if (order.getLineItems() == null || order.getLineItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (LineItem item : order.getLineItems()) {
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new InvalidOrderException("Line item description must not be blank.");
            }
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException(
                        "Line item \"" + item.getDescription() + "\" must have a positive quantity.");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderException(
                        "Line item \"" + item.getDescription() + "\" must have a non-negative unit price.");
            }
        }
    }

    public OrderTotals computeTotals(Order order) {
        BigDecimal subtotal = order.getLineItems().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = discountedSubtotal.add(vat);

        return new OrderTotals(subtotal, discount, vat, grandTotal);
    }

    public String buildReceipt(Order order, OrderTotals totals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt for order ").append(order.getOrderId()).append('\n');
        sb.append("-".repeat(40)).append('\n');

        for (LineItem item : order.getLineItems()) {
            sb.append(String.format(Locale.ROOT, "%-20s %2d x %8s = %8s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatEuro(item.getUnitPrice()),
                    formatEuro(item.lineTotal())));
        }

        sb.append("-".repeat(40)).append('\n');
        sb.append(String.format(Locale.ROOT, "%-30s %8s%n", "Subtotal (excl. VAT):", formatEuro(totals.subtotal)));
        if (totals.discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format(Locale.ROOT, "%-30s -%8s%n", "Discount (10%):", formatEuro(totals.discount)));
        }
        sb.append(String.format(Locale.ROOT, "%-30s %8s%n", "VAT (21%):", formatEuro(totals.vat)));
        sb.append(String.format(Locale.ROOT, "%-30s %8s%n", "Total (incl. VAT):", formatEuro(totals.grandTotal)));

        return sb.toString();
    }

    public String processOrder(Order order) {
        validateLineItems(order);
        OrderTotals totals = computeTotals(order);
        return buildReceipt(order, totals);
    }

    private static String formatEuro(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        Order order = new Order("ORD-1001", List.of(
                new LineItem("Charity draw ticket", 3, new BigDecimal("25.00")),
                new LineItem("Processing fee", 1, new BigDecimal("30.00"))
        ));

        OrderProcessor processor = new OrderProcessor();
        System.out.println(processor.processOrder(order));
    }
}
````

### order · run 10 · uncle-bob-junior

````java
// LineItem.java
import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive: " + unitPrice);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// Order.java
import java.util.List;
import java.util.Objects;

public record Order(List<LineItem> items) {

    public Order {
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }
}
````

````java
// Receipt.java
import java.math.BigDecimal;

public record Receipt(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total, String text) {
}
````

````java
// OrderProcessor.java
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderProcessor() {
    }

    public static Receipt process(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = round(discountedSubtotal.add(vat));
        String text = ReceiptFormatter.format(order, subtotal, discount, vat, total);
        return new Receipt(subtotal, discount, vat, total, text);
    }

    private static BigDecimal calculateSubtotal(Order order) {
        BigDecimal sum = order.items().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return round(sum);
    }

    private static BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!qualifiesForDiscount) {
            return round(BigDecimal.ZERO);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// ReceiptFormatter.java
import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String SEPARATOR = "---------------------------------------------";

    private ReceiptFormatter() {
    }

    public static String format(Order order, BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder("Receipt").append(System.lineSeparator());
        order.items().forEach(item -> appendLineItem(receipt, item));
        appendSummary(receipt, subtotal, discount, vat, total);
        return receipt.toString();
    }

    private static void appendLineItem(StringBuilder receipt, LineItem item) {
        receipt.append(String.format("%-20s %2d x %8s = %10s%n",
                item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
    }

    private static void appendSummary(StringBuilder receipt, BigDecimal subtotal, BigDecimal discount,
                                       BigDecimal vat, BigDecimal total) {
        receipt.append(SEPARATOR).append(System.lineSeparator());
        receipt.append(String.format("Subtotal (excl. VAT): %10s EUR%n", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%):       %10s EUR%n", discount));
        }
        receipt.append(String.format("VAT (21%%):            %10s EUR%n", vat));
        receipt.append(String.format("Total:                %10s EUR%n", total));
    }
}
````

````java
// LineItemTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejectsZeroOrNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("-1.00"), 1));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("10.00"), 0));
    }
}
````

````java
// OrderTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void rejectsEmptyItemList() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void rejectsNullItemList() {
        assertThrows(NullPointerException.class, () -> new Order(null));
    }

    @Test
    void acceptsOneOrMoreValidItems() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 1);
        Order order = new Order(List.of(item));
        assertEquals(1, order.items().size());
    }
}
````

````java
// OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("100.00"), 1)));
        Receipt receipt = OrderProcessor.process(order);
        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("50.00"), 3)));
        Receipt receipt = OrderProcessor.process(order);
        assertEquals(new BigDecimal("150.00"), receipt.subtotal());
        assertEquals(new BigDecimal("15.00"), receipt.discount());
        assertEquals(new BigDecimal("28.35"), receipt.vat());
        assertEquals(new BigDecimal("163.35"), receipt.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscount() {
        Order order = new Order(List.of(
                new LineItem("Widget", new BigDecimal("40.00"), 2),
                new LineItem("Gadget", new BigDecimal("30.00"), 1)));
        Receipt receipt = OrderProcessor.process(order);
        assertEquals(new BigDecimal("110.00"), receipt.subtotal());
        assertEquals(new BigDecimal("11.00"), receipt.discount());
    }

    @Test
    void receiptTextContainsSummaryLines() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("50.00"), 3)));
        Receipt receipt = OrderProcessor.process(order);
        assertTrue(receipt.text().contains("Discount (10%)"));
        assertTrue(receipt.text().contains("Total:"));
    }
}
````
