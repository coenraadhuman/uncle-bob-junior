import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    @Test
    void acceptsTypicalAddresses() {
        assertTrue(EmailValidator.isValidEmail("participant_001@example.com"));
        assertTrue(EmailValidator.isValidEmail("first.last+tag@sub.example.co.uk"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
    }

    @Test
    void rejectsMissingOrDuplicateSeparator() {
        assertFalse(EmailValidator.isValidEmail("no-separator.example.com"));
        assertFalse(EmailValidator.isValidEmail("two@@example.com"));
        assertFalse(EmailValidator.isValidEmail("a@b@example.com"));
    }

    @Test
    void rejectsBadLocalPart() {
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail(".leading@example.com"));
        assertFalse(EmailValidator.isValidEmail("double..dot@example.com"));
        assertFalse(EmailValidator.isValidEmail("a".repeat(65) + "@example.com"));
    }

    @Test
    void rejectsBadDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@localhost"));
        assertFalse(EmailValidator.isValidEmail("user@-bad.example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example..com"));
        assertFalse(EmailValidator.isValidEmail("user@example.com."));
    }

    @Test
    void enforcesTotalLengthLimit() {
        String tooLong = "a".repeat(64) + "@" + "b".repeat(60) + "." + "c".repeat(60)
                + "." + "d".repeat(60) + ".example.com";
        assertFalse(EmailValidator.isValidEmail(tooLong));
    }
}
