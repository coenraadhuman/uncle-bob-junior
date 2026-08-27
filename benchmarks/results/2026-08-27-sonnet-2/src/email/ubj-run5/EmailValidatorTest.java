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
            "user+tag@example.org",
            "user_name@sub.example.com",
            "a@b.co"
    })
    void acceptsWellFormedEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@example.com",
            "user@",
            "user@example",
            "user@.com",
            "user example@example.com",
            "user@@example.com",
            "user@example..com"
    })
    void rejectsMalformedEmails(String email) {
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
    void rejectsEmailExceedingMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsEmailAtMaxLength() {
        String local = "a".repeat(242);
        String maxLength = local + "@example.com"; // exactly 254 chars

        assertTrue(EmailValidator.isValid(maxLength));
    }
}
