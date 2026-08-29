import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("test_123@subdomain.example.com"));
    }

    @Test
    void rejectsInvalidFormats() {
        assertFalse(EmailValidator.isValidEmail("plaintext"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void trims whitespace() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
