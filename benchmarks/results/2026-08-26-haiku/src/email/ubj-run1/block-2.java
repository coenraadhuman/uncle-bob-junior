EmailValidator.isValidEmail("user@example.com");        // true
EmailValidator.isValidEmail("invalid.email");            // false
EmailValidator.isValidEmail("user+tag@example.co.uk");  // true
EmailValidator.isValidEmail("");                         // false
EmailValidator.isValidEmail(null);                       // false
