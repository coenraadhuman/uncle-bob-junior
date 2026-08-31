import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validBasicEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }
    
    @Test
    void validEmailWithPlus() {
        assertTrue(EmailValidator.isValid("user+tag@example.com"));
    }
    
    @Test
    void validEmailWithDot() {
        assertTrue(EmailValidator.isValid("first.last@example.com"));
    }
    
    @Test
    void validEmailWithSubdomain() {
        assertTrue(EmailValidator.isValid("user@mail.example.co.uk"));
    }
    
    @Test
    void nullEmail() {
        assertFalse(EmailValidator.isValid(null));
    }
    
    @Test
    void blankEmail() {
        assertFalse(EmailValidator.isValid("   "));
    }
    
    @Test
    void missingAtSign() {
        assertFalse(EmailValidator.isValid("user.example.com"));
    }
    
    @Test
    void missingLocalPart() {
        assertFalse(EmailValidator.isValid("@example.com"));
    }
    
    @Test
    void missingDomain() {
        assertFalse(EmailValidator.isValid("user@"));
    }
    
    @Test
    void missingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("user@example"));
    }
    
    @Test
    void invalidCharactersInLocalPart() {
        assertFalse(EmailValidator.isValid("user#name@example.com"));
    }
}
