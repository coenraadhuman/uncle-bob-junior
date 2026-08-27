import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "coenraad.human@postcodeloterij.nl",
        "a@b.co",
        "user+tag@example.com",
        "first.last@sub.example.org"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "missing-at-sign.com",
        "@no-local-part.com",
        "no-domain@",
        "no-tld@example",
        "spaces in@example.com",
        "double@@example.com"
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
        String longLocalPart = "a".repeat(250);
        assertFalse(EmailValidator.isValidEmail(longLocalPart + "@example.com"));
    }
}
