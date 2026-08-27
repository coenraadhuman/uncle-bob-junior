public static void main(String[] args) {
    System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
    System.out.println(EmailValidator.isValidEmail("john.doe@company.co.uk")); // true
    System.out.println(EmailValidator.isValidEmail("invalid.email@"));         // false
    System.out.println(EmailValidator.isValidEmail("@example.com"));           // false
    System.out.println(EmailValidator.isValidEmail("user@.com"));              // false
}
