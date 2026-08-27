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
