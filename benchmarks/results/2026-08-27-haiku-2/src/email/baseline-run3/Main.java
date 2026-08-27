public class Main {
    public static void main(String[] args) {
        System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
        System.out.println(EmailValidator.isValidEmail("invalid.email@"));         // false
        System.out.println(EmailValidator.isValidEmail("test+tag@domain.co.uk")); // true
        System.out.println(EmailValidator.isValidEmail(""));                        // false
        System.out.println(EmailValidator.isValidEmail(null));                      // false
    }
}
