import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {
    
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValid("user@example.com"));
        assertTrue(EmailValidator.isValid("test.name@example.co.uk"));
        assertTrue(EmailValidator.isValid("user+tag@domain.org"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid(""));
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsMissingAtSign() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    void rejectsMissingDomain() {
        assertFalse(EmailValidator.isValid("user@"));
    }

    @Test
    void rejectsMissingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("user@domain"));
    }

    @Test
    void rejectsInvalidCharacters() {
        assertFalse(EmailValidator.isValid("user name@example.com"));
        assertFalse(EmailValidator.isValid("user@exam ple.com"));
    }
}
