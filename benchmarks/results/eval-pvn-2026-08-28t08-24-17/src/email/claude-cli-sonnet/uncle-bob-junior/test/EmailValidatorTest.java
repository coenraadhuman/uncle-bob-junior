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
        "user+tag@example.io"
    })
    void isValid_returnsTrue_forWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "no-at-sign.com",
        "user@",
        "@example.com",
        "user@example",
        "user@@example.com",
        "user@example..com"
    })
    void isValid_returnsFalse_forMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void isValid_returnsFalse_forBlankInput(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void isValid_returnsFalse_forNull() {
        assertFalse(EmailValidator.isValid(null));
    }
}
