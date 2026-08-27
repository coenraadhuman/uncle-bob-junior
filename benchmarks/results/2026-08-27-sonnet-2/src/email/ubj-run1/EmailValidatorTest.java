import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "participant@example.com",
            "first.last@example.co.uk",
            "user+tag@sub.example.com",
            "USER@EXAMPLE.COM",
            "a@b.io"
    })
    void acceptsValidAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "plainaddress",
            "@example.com",
            "user@",
            "user@example",
            "user@.com",
            "user@@example.com",
            "user@exa mple.com",
            "user@example..com",
            ".user@example.com",
            "user.@example.com"
    })
    void rejectsInvalidAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }
}
