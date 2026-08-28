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
