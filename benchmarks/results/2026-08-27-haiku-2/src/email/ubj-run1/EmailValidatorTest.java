public class EmailValidatorTest {
    public static void main(String[] args) {
        System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
        System.out.println(EmailValidator.isValidEmail("invalid.email@"));         // false
        System.out.println(EmailValidator.isValidEmail(null));                     // false
        System.out.println(EmailValidator.isValidEmail(""));                       // false
    }
}
