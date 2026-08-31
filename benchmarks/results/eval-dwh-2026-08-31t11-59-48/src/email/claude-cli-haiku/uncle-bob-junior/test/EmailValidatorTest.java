import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("user@mail.example.com"));
        assertTrue(EmailValidator.isValidEmail("user+tag@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@example.co.uk"));
    }
    
    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
    }
    
    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
