public class EmailValidatorTest {
    private static final EmailValidator validator = new EmailValidator();

    public static void main(String[] args) {
        testValidEmails();
        testInvalidEmails();
    }

    private static void testValidEmails() {
        assert validator.isValidEmail("user@example.com");
        assert validator.isValidEmail("john.doe@company.co.uk");
        assert validator.isValidEmail("alice+tag@domain.org");
        assert validator.isValidEmail("test_user@test-domain.com");
        System.out.println("✓ Valid email tests passed");
    }

    private static void testInvalidEmails() {
        assert !validator.isValidEmail(null);
        assert !validator.isValidEmail("");
        assert !validator.isValidEmail("plainaddress");
        assert !validator.isValidEmail("@nodomain.com");
        assert !validator.isValidEmail("user@");
        assert !validator.isValidEmail("user@domain");
        assert !validator.isValidEmail("user@.com");
        assert !validator.isValidEmail("user space@domain.com");
        System.out.println("✓ Invalid email tests passed");
    }
}
