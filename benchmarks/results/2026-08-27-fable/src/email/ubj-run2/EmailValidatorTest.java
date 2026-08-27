import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "participant_001@example.com",
            "first.last@sub.example.co.uk",
            "user+tag@example.org",
            "o'brien@example.ie"
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
            ".user@example.com",
            "user.@example.com",
            "user..name@example.com",
            "user@-example.com",
            "user@example..com",
            "user name@example.com"
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValidEmail(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsOverlongLocalPart() {
        String overlongLocalPart = "a".repeat(65);
        assertFalse(EmailValidator.isValidEmail(overlongLocalPart + "@example.com"));
    }
}
