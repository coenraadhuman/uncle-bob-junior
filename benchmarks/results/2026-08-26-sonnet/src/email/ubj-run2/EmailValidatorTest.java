import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValid("participant001@example.com"));
    }

    @Test
    void acceptsAddressWithSubdomainAndPlusTag() {
        assertTrue(EmailValidator.isValid("first.last+tag@mail.example.co.uk"));
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
    void rejectsMissingAtSign() {
        assertFalse(EmailValidator.isValid("participant001example.com"));
    }

    @Test
    void rejectsMissingDomain() {
        assertFalse(EmailValidator.isValid("participant001@"));
    }

    @Test
    void rejectsMissingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("participant001@example"));
    }

    @Test
    void rejectsMultipleAtSigns() {
        assertFalse(EmailValidator.isValid("participant001@@example.com"));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        assertFalse(EmailValidator.isValid(localPart + "@example.com"));
    }
}
