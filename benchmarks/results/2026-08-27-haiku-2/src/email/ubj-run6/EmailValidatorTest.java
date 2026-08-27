import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmailsPasses() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+tag@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_123@test-domain.org"));
    }

    @Test
    void invalidEmailsFail() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("plaintext"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
    }

    @Test
    void excessivelyLongEmailFails() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValidEmail(longEmail));
    }
}
