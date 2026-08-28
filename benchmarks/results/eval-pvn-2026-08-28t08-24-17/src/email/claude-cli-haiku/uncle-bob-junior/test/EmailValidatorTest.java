import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }

    @Test
    void acceptsEmailWithSubdomain() {
        assertTrue(EmailValidator.isValidEmail("user@mail.example.com"));
    }

    @Test
    void acceptsEmailWithPlus() {
        assertTrue(EmailValidator.isValidEmail("user+tag@example.com"));
    }

    @Test
    void acceptsEmailWithDot() {
        assertTrue(EmailValidator.isValidEmail("first.last@example.com"));
    }

    @Test
    void rejectsNullEmail() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsBlankEmail() {
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
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
    void rejectsEmailWithoutTld() {
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }

    @Test
    void rejectsEmailWithSpaces() {
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
        assertFalse(EmailValidator.isValidEmail("user@ example.com"));
    }

    @Test
    void rejectsEmailWithMultipleAtSigns() {
        assertFalse(EmailValidator.isValidEmail("user@mail@example.com"));
    }
}
