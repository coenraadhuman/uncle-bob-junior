EmailValidator.isValid("participant_001@example.com");   // true
EmailValidator.isValid("first.last@sub.example.co.uk");  // true
EmailValidator.isValid("no-at-sign.example.com");        // false
EmailValidator.isValid("trailing.dot.@example.com");     // false
EmailValidator.isValid(null);                            // false
