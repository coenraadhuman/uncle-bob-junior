import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("first+last@domain.org"));
        assertTrue(EmailValidator.isValidEmail("test_user@example.com"));
    }

    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }

    @Test
    void nullAndBlank() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void whitespaceHandling() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
