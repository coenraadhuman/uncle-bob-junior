import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }

    @Test
    void acceptsAddressWithSubdomainAndPlusTag() {
        assertTrue(EmailValidator.isValidEmail("first.last+tag@mail.example.co.uk"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("user.example.com"));
    }

    @Test
    void rejectsMissingDomainDot() {
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }

    @Test
    void rejectsDoubleAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("user@@example.com"));
    }

    @Test
    void rejectsAddressExceedingMaxLength() {
        String longLocalPart = "a".repeat(255);
        assertFalse(EmailValidator.isValidEmail(longLocalPart + "@example.com"));
    }
}
