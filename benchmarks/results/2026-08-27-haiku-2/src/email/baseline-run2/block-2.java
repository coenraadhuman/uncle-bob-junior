// Valid
isValidEmail("user@example.com");           // true
isValidEmail("john.doe+tag@company.co.uk"); // true
isValidEmail("test_123@domain.org");        // true

// Invalid
isValidEmail("invalid.email@");             // false
isValidEmail("@example.com");               // false
isValidEmail("user@domain");                // false
isValidEmail(null);                         // false
isValidEmail("");                           // false
