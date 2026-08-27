EmailValidator.isValidEmail("user@example.com");        // true
EmailValidator.isValidEmail("invalid.email@");          // false
EmailValidator.isValidEmail("test@domain.co.uk");       // true

EmailValidator.isValidEmailStrict("user@example.com");  // true
EmailValidator.isValidEmailStrict("user..name@domain"); // false
