public class EmailValidatorTest {
    @Test
    void acceptsValidEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }

    @Test
    void acceptsEmailWithSubdomain() {
        assertTrue(EmailValidator.isValid("user@mail.example.co.uk"));
    }

    @Test
    void acceptsEmailWithPlus() {
        assertTrue(EmailValidator.isValid("user+tag@example.com"));
    }

    @Test
    void rejectsEmailWithoutAtSymbol() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    void rejectsEmailWithoutDomain() {
        assertFalse(EmailValidator.isValid("user@"));
    }

    @Test
    void rejectsEmailWithoutTopLevelDomain() {
        assertFalse(EmailValidator.isValid("user@example"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlankString() {
        assertFalse(EmailValidator.isValid("   "));
    }
}
