public static void main(String[] args) {
    System.out.println(EmailValidator.isValidEmail("participant_001@example.com"));  // true
    System.out.println(EmailValidator.isValidEmail("first.last+tag@example.co.uk")); // true
    System.out.println(EmailValidator.isValidEmail("no-at-sign.example.com"));       // false
    System.out.println(EmailValidator.isValidEmail(".leading.dot@example.com"));     // false
    System.out.println(EmailValidator.isValidEmail("user@-badlabel-.com"));          // false
    System.out.println(EmailValidator.isValidEmail(null));                           // false
}
