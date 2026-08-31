import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("name_123@test-domain.com"));
    }
    
    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("invalid.email"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user space@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@@example.com"));
    }
    
    @Test
    void nullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
