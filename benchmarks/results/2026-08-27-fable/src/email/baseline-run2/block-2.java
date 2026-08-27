EmailValidator.isValidEmail("participant_001@example.com");   // true
EmailValidator.isValidEmail("first.last@sub.example.co.uk");  // true
EmailValidator.isValidEmail("no-at-sign.example.com");        // false
EmailValidator.isValidEmail("double..dot@example.com");       // false
EmailValidator.isValidEmail("user@-bad-label.example.com");   // false
EmailValidator.isValidEmail(null);                            // false
