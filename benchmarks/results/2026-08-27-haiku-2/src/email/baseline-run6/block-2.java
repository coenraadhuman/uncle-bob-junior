public static void main(String[] args) {
    System.out.println(EmailValidator.isValidEmail("user@example.com"));           // true
    System.out.println(EmailValidator.isValidEmail("john.doe+tag@company.co.uk")); // true
    System.out.println(EmailValidator.isValidEmail("invalid.email@"));              // false
    System.out.println(EmailValidator.isValidEmail("no-at-sign.com"));              // false
    System.out.println(EmailValidator.isValidEmail(null));                          // false
}
