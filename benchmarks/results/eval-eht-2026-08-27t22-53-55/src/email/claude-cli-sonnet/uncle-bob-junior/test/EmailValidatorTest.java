import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.com",
            "user_name@sub.example.com",
            "u@e.io"
    })
    void acceptsValidAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "plainaddress",
            "@example.com",
            "user@",
            "user@.com",
            "user@example",
            "user@@example.com",
            "user name@example.com",
            "user@ex ample.com"
    })
    void rejectsInvalidAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsAddressAtMaxLength() {
        String domain = "@example.com";
        String localPart = "a".repeat(254 - domain.length());
        String atMaxLength = localPart + domain;

        assertTrue(EmailValidator.isValid(atMaxLength));
    }
}
