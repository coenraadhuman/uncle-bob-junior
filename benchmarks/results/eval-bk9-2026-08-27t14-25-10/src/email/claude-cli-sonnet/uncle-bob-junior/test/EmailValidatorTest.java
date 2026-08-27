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
