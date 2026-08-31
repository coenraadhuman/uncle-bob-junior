import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmailAddresses() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+filter@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_email@domain.org"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void rejectsNoAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("userexample.com"));
    }

    @Test
    void rejectsMultipleAtSymbols() {
        assertFalse(EmailValidator.isValidEmail("user@exam@ple.com"));
    }

    @Test
    void rejectsNoLocalPart() {
        assertFalse(EmailValidator.isValidEmail("@example.com"));
    }

    @Test
    void rejectsNoDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
    }

    @Test
    void rejectsNoTopLevelDomain() {
        assertFalse(EmailValidator.isValidEmail("user@domain"));
    }

    @Test
    void rejectsConsecutiveDots() {
        assertFalse(EmailValidator.isValidEmail("user..name@example.com"));
    }

    @Test
    void rejectsLeadingOrTrailingDots() {
        assertFalse(EmailValidator.isValidEmail(".user@example.com"));
        assertFalse(EmailValidator.isValidEmail("user.@example.com"));
    }

    @Test
    void rejectsExcessiveLength() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValidEmail(longEmail));
    }

    @Test
    void acceptsLeadingAndTrailingWhitespace() {
        assertTrue(EmailValidator.isValidEmail("  user@example.com  "));
    }
}
