import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValid("participant.001@example.com"));
    }

    @Test
    void acceptsAddressWithSubdomain() {
        assertTrue(EmailValidator.isValid("user@mail.example.co.uk"));
    }

    @Test
    void acceptsAddressWithPlusTag() {
        assertTrue(EmailValidator.isValid("user+newsletter@example.com"));
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
    void rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValid("user.example.com"));
    }

    @Test
    void rejectsMissingDomainSuffix() {
        assertFalse(EmailValidator.isValid("user@example"));
    }

    @Test
    void rejectsDoubleAtSymbol() {
        assertFalse(EmailValidator.isValid("user@@example.com"));
    }

    @Test
    void rejectsEmbeddedSpaces() {
        assertFalse(EmailValidator.isValid("user name@example.com"));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        assertFalse(EmailValidator.isValid(localPart + "@example.com"));
    }
}
