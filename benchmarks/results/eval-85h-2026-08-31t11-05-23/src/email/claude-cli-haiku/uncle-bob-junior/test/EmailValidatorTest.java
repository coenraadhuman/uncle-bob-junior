public class EmailValidatorTest {
  @Test
  void validEmailsPass() {
    assertTrue(EmailValidator.isValid("user@example.com"));
    assertTrue(EmailValidator.isValid("coenraad.human@postcodeloterij.nl"));
    assertTrue(EmailValidator.isValid("john+tag@domain.co.uk"));
    assertTrue(EmailValidator.isValid("a@b.co"));
  }

  @Test
  void invalidEmailsFail() {
    assertFalse(EmailValidator.isValid(null));
    assertFalse(EmailValidator.isValid(""));
    assertFalse(EmailValidator.isValid("   "));
    assertFalse(EmailValidator.isValid("notanemail"));
    assertFalse(EmailValidator.isValid("user@"));
    assertFalse(EmailValidator.isValid("@example.com"));
    assertFalse(EmailValidator.isValid("user @example.com"));
    assertFalse(EmailValidator.isValid("user@example"));
  }

  @Test
  void edgeCases() {
    assertFalse(EmailValidator.isValid("user..name@example.com"));
    assertTrue(EmailValidator.isValid("user_name@example.com"));
    assertTrue(EmailValidator.isValid("user-name@example-domain.com"));
  }
}
