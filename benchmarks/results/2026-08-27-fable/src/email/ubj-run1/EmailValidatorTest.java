import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "participant_001@example.com",
            "first.last@example.co.uk",
            "user+tag@sub.example.org",
            "o'brien@example.ie",
            "a@b.co"
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
            "user@localhost",
            ".leadingdot@example.com",
            "trailingdot.@example.com",
            "double..dot@example.com",
            "user@-example.com",
            "user@example-.com",
            "user@exa mple.com",
            "user name@example.com"
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @org.junit.jupiter.api.Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @org.junit.jupiter.api.Test
    void enforcesRfcLengthLimits() {
        String local64 = "a".repeat(64);
        assertTrue(EmailValidator.isValid(local64 + "@example.com"));
        assertFalse(EmailValidator.isValid("a".repeat(65) + "@example.com"));

        String overallTooLong = "a@" + ("b".repeat(63) + ".").repeat(4) + "com";
        assertFalse(EmailValidator.isValid(overallTooLong.length() > 254
                ? overallTooLong
                : "a".repeat(250) + "@x.co"));
    }
}
