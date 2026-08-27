import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.org",
            "user_name-123@sub.example.com"
    })
    void acceptsWellFormedEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "plainaddress",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@example",
            "user example@example.com",
            "user@example.c",
            "user@.com",
            "user@exa mple.com"
    })
    void rejectsMalformedEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsEmailLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsEmailAtMaxLength() {
        String domain = "@example.com";
        String localPart = "a".repeat(254 - domain.length());
        String maxLength = localPart + domain;

        assertTrue(EmailValidator.isValid(maxLength));
    }
}
