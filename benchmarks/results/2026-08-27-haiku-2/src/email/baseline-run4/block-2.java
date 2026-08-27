EmailValidator.isValidEmail("user@example.com");      // true
EmailValidator.isValidEmail("invalid.email@");        // false
EmailValidator.isValidEmail("no-at-sign.com");        // false
EmailValidator.isValidEmail("user+tag@domain.co.uk"); // false (+ not allowed)
