import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "participant.001@example.com",
        "user+tag@example.co.uk",
        "first.last@sub.example.org",
        "user_name@example-domain.com"
    })
    void acceptsValidAddresses(String candidateEmail) {
        assertTrue(EmailValidator.isValid(candidateEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "plainaddress",
        "@example.com",
        "user@",
        "user@.com",
        "user@example",
        "user name@example.com",
        "user@@example.com",
        "user@example..com"
    })
    void rejectsInvalidAddresses(String candidateEmail) {
        assertFalse(EmailValidator.isValid(candidateEmail));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsNullOrBlank(String candidateEmail) {
        assertFalse(EmailValidator.isValid(candidateEmail));
    }

    @Test
    void acceptsAddressAtMaxLength() {
        String localPart = "a".repeat(242);
        String maxLengthEmail = localPart + "@example.com";
        assertTrue(maxLengthEmail.length() == 254);
        assertTrue(EmailValidator.isValid(maxLengthEmail));
    }

    @Test
    void rejectsAddressExceedingMaxLength() {
        String localPart = "a".repeat(243);
        String tooLongEmail = localPart + "@example.com";
        assertFalse(EmailValidator.isValid(tooLongEmail));
    }
}
