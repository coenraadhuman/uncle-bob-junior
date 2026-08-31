import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EmailValidatorTest {
    @Test
    void acceptsStandardEmail() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }
    
    @Test
    void acceptsEmailWithSubdomain() {
        assertTrue(EmailValidator.isValidEmail("user@mail.example.co.uk"));
    }
    
    @Test
    void acceptsEmailWithPlusAndHyphens() {
        assertTrue(EmailValidator.isValidEmail("first-last+tag@my-domain.com"));
    }
    
    @Test
    void rejectsEmailWithoutAtSign() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
    }
    
    @Test
    void rejectsEmailWithoutDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
    }
    
    @Test
    void rejectsEmailWithoutTopLevelDomain() {
        assertFalse(EmailValidator.isValidEmail("user@localhost"));
    }
    
    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }
    
    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail("   "));
    }
}
