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
