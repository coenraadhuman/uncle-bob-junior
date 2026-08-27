public class EmailValidatorDemo {
    public static void main(String[] args) {
        System.out.println(EmailValidator.isValidEmail("participant_001@example.com")); // true
        System.out.println(EmailValidator.isValidEmail("first.last@sub.example.co.uk")); // true
        System.out.println(EmailValidator.isValidEmail("no-at-sign.example.com"));       // false
        System.out.println(EmailValidator.isValidEmail("double..dot@example.com"));      // false
        System.out.println(EmailValidator.isValidEmail("trailing.@example.com"));        // false
        System.out.println(EmailValidator.isValidEmail("user@-baddomain.com"));          // false
        System.out.println(EmailValidator.isValidEmail(null));                           // false
    }
}
