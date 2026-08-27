import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("test_123@sub-domain.info"));
    }

    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@nodomain.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user name@domain.com"));
    }

    @Test
    void handlesEdgeCases() {
        assertFalse(EmailValidator.isValidEmail("user@@domain.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain..com"));
        assertTrue(EmailValidator.isValidEmail("a@b.co"));
    }
}
