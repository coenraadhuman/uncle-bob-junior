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
            "user+tag@example.org",
            "o'connor@example.ie",
            "a@b.co"
    })
    void acceptsWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "plainaddress",
            "@example.com",
            "user@",
            "user@localhost",
            "user@example.",
            "user@.example.com",
            ".user@example.com",
            "user.@example.com",
            "us..er@example.com",
            "user@exa mple.com",
            "user@-example.com",
            "user@example-.com",
            "us er@example.com",
            "üser@example.com"
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValidEmail(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void acceptsLocalPartAtLengthLimit() {
        String localPart = "a".repeat(64);
        assertTrue(EmailValidator.isValidEmail(localPart + "@example.com"));
    }

    @Test
    void rejectsLocalPartOverLengthLimit() {
        String localPart = "a".repeat(65);
        assertFalse(EmailValidator.isValidEmail(localPart + "@example.com"));
    }

    @Test
    void rejectsEmailOverTotalLengthLimit() {
        String longDomain = ("a".repeat(63) + ".").repeat(4) + "com";
        assertFalse(EmailValidator.isValidEmail("user@" + longDomain));
    }

    @Test
    void rejectsDomainLabelOverLengthLimit() {
        String longLabel = "a".repeat(64);
        assertFalse(EmailValidator.isValidEmail("user@" + longLabel + ".com"));
    }
}
