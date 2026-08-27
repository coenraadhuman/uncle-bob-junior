import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void acceptsWellFormedAddresses() {
        assertTrue(EmailValidator.isValidEmail("participant_001@example.com"));
        assertTrue(EmailValidator.isValidEmail("first.last@sub.example.co.uk"));
        assertTrue(EmailValidator.isValidEmail("user+tag@example.org"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
    }

    @Test
    void rejectsMissingOrMisplacedAtSign() {
        assertFalse(EmailValidator.isValidEmail("no-at-sign.example.com"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
    }

    @Test
    void rejectsBadLocalParts() {
        assertFalse(EmailValidator.isValidEmail(".starts.with.dot@example.com"));
        assertFalse(EmailValidator.isValidEmail("ends.with.dot.@example.com"));
        assertFalse(EmailValidator.isValidEmail("double..dot@example.com"));
        assertFalse(EmailValidator.isValidEmail("a".repeat(65) + "@example.com"));
    }

    @Test
    void rejectsBadDomains() {
        assertFalse(EmailValidator.isValidEmail("user@localhost"));
        assertFalse(EmailValidator.isValidEmail("user@example..com"));
        assertFalse(EmailValidator.isValidEmail("user@-example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example-.com"));
        assertFalse(EmailValidator.isValidEmail("user@exa mple.com"));
    }

    @Test
    void rejectsOverlongTotalLength() {
        String overlong = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(130) + ".com";
        assertFalse(EmailValidator.isValidEmail(overlong));
    }
}
