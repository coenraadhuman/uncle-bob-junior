import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
  @Test
  void validEmailAddresses() {
    assertTrue(EmailValidator.isValid("user@example.com"));
    assertTrue(EmailValidator.isValid("john.doe+tag@company.co.uk"));
    assertTrue(EmailValidator.isValid("test_123@sub.domain.org"));
  }

  @Test
  void invalidEmailAddresses() {
    assertFalse(EmailValidator.isValid("plaintext"));
    assertFalse(EmailValidator.isValid("@example.com"));
    assertFalse(EmailValidator.isValid("user@"));
    assertFalse(EmailValidator.isValid("user@.com"));
    assertFalse(EmailValidator.isValid("user name@example.com"));
  }

  @Test
  void nullAndEmptyInputs() {
    assertFalse(EmailValidator.isValid(null));
    assertFalse(EmailValidator.isValid(""));
    assertFalse(EmailValidator.isValid("   "));
  }
}
