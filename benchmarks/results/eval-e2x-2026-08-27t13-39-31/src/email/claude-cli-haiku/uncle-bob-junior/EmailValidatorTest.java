public class EmailValidatorTest {
    private static final EmailValidator validator = new EmailValidator();

    @Test
    public void acceptsValidEmails() {
        assertTrue(validator.isValidEmail("user@example.com"));
        assertTrue(validator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(validator.isValidEmail("test+tag@domain.org"));
    }

    @Test
    public void rejectsInvalidEmails() {
        assertFalse(validator.isValidEmail(null));
        assertFalse(validator.isValidEmail(""));
        assertFalse(validator.isValidEmail("notanemail"));
        assertFalse(validator.isValidEmail("user@"));
        assertFalse(validator.isValidEmail("@example.com"));
        assertFalse(validator.isValidEmail("user @example.com"));
    }

    @Test
    public void rejectsEmailsWithoutTopLevelDomain() {
        assertFalse(validator.isValidEmail("user@localhost"));
    }
}
