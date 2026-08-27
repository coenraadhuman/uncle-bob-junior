import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+tag@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_123@test-domain.org"));
    }

    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("invalid.email"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
    }

    @Test
    void nullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void tooLong() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValidEmail(longEmail));
    }
}
