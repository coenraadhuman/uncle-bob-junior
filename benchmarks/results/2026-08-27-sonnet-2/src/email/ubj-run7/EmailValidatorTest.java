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
        "user+tag@sub.example.com",
        "user_name@example-domain.com"
    })
    void acceptsWellFormedEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ",
        "user@",
        "@example.com",
        "user@@example.com",
        "user@example",
        "user example@example.com",
        "user@ example.com"
    })
    void rejectsMalformedOrBlankEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsEmailsLongerThanRfcLimit() {
        String tooLong = "a".repeat(250) + "@ex.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }
}
