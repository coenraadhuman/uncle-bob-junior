import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EmailValidatorTest {
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }

    @Test
    void acceptsComplexLocalPart() {
        assertTrue(EmailValidator.isValid("user.name+tag@example.co.uk"));
    }

    @Test
    void rejectsEmailWithoutAtSign() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    void rejectsIncompleteEmail() {
        assertFalse(EmailValidator.isValid("user@"));
        assertFalse(EmailValidator.isValid("@example.com"));
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
    void rejectsInvalidFormat() {
        assertFalse(EmailValidator.isValid("user @example.com"));
        assertFalse(EmailValidator.isValid("user@example"));
    }
}
