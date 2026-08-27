import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
  @Test
  void acceptsValidEmails() {
    assertTrue(EmailValidator.isValidEmail("user@example.com"));
    assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
    assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
    assertTrue(EmailValidator.isValidEmail("a@b.co"));
  }

  @Test
  void rejectsInvalidEmails() {
    assertFalse(EmailValidator.isValidEmail(null));
    assertFalse(EmailValidator.isValidEmail(""));
    assertFalse(EmailValidator.isValidEmail("   "));
    assertFalse(EmailValidator.isValidEmail("plaintext"));
    assertFalse(EmailValidator.isValidEmail("@example.com"));
    assertFalse(EmailValidator.isValidEmail("user@"));
    assertFalse(EmailValidator.isValidEmail("user@.com"));
    assertFalse(EmailValidator.isValidEmail("user @example.com"));
    assertFalse(EmailValidator.isValidEmail("user@example"));
  }
}
