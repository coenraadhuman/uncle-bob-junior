import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "participant@example.com",
        "first.last@sub.example.co.uk",
        "user+tag@example.org"
    })
    void acceptsWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "no-at-sign.example.com",
        "missing-domain@",
        "@missing-local.com",
        "double@@example.com",
        "trailing.dot@example.com."
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String tooLongLocalPart = "a".repeat(255);
        assertFalse(EmailValidator.isValid(tooLongLocalPart + "@example.com"));
    }
}
