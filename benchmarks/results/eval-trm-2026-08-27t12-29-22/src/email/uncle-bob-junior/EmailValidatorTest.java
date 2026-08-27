public class EmailValidatorTest {
    
    @Test
    public void validEmailsShouldPass() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
    }
    
    @Test
    public void invalidEmailsShouldFail() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@nodomain.com"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }
}
