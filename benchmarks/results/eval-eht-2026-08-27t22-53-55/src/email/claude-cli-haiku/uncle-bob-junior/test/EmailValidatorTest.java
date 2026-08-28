import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmailAddresses() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
    }
    
    @Test
    void rejectsInvalidEmailAddresses() {
        assertFalse(EmailValidator.isValidEmail("invalid.email"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail(null));
    }
    
    @Test
    void trimsWhitespace() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
