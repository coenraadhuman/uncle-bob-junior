import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "coenraad.human@postcodeloterij.nl",
        "user+tag@example.co.uk",
        "first.last@sub.domain.com",
        "user_name-123@example.io"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "   ",
        "plainaddress",
        "@missinglocal.com",
        "missingdomain@",
        "double@@example.com",
        "no-tld@example",
        "space in@example.com",
        "trailing.dot@example.com."
    })
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @org.junit.jupiter.api.Test
    void rejectsEmailLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }
}
