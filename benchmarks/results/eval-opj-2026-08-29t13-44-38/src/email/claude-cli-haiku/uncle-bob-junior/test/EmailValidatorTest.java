import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {
    
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }
    
    @Test
    void acceptsEmailWithNumbers() {
        assertTrue(EmailValidator.isValidEmail("user123@example.com"));
    }
    
    @Test
    void acceptsEmailWithSpecialCharacters() {
        assertTrue(EmailValidator.isValidEmail("user+tag@example.co.uk"));
    }
    
    @Test
    void rejectsEmailMissingLocalPart() {
        assertFalse(EmailValidator.isValidEmail("@example.com"));
    }
    
    @Test
    void rejectsEmailMissingDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
    }
    
    @Test
    void rejectsEmailMissingTld() {
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }
    
    @Test
    void rejectsEmailWithoutAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
    }
    
    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }
    
    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail(""));
    }
}
