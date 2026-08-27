Here's a practical Java email validation method:

```java
public class EmailValidator {
    
    // Simple regex pattern for basic email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates email address using regex pattern matching.
     * 
     * @param email the email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
    
    /**
     * More thorough validation including length checks and format rules.
     */
    public static boolean isValidEmailStrict(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        email = email.trim();
        
        // RFC 5321: max 254 characters total
        if (email.length() > 254) {
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String local = parts[0];
        String domain = parts[1];
        
        // Local part: max 64 characters
        if (local.isEmpty() || local.length() > 64) {
            return false;
        }
        
        // Local part cannot start or end with dot
        if (local.startsWith(".") || local.endsWith(".")) {
            return false;
        }
        
        // No consecutive dots
        if (local.contains("..")) {
            return false;
        }
        
        // Domain validation
        if (domain.isEmpty() || domain.length() > 255) {
            return false;
        }
        
        if (domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        
        if (domain.contains("..")) {
            return false;
        }
        
        // Domain must have at least one dot and valid characters
        return domain.matches("^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
```

**Usage:**

```java
EmailValidator.isValidEmail("user@example.com");        // true
EmailValidator.isValidEmail("invalid.email@");          // false
EmailValidator.isValidEmail("test@domain.co.uk");       // true

EmailValidator.isValidEmailStrict("user@example.com");  // true
EmailValidator.isValidEmailStrict("user..name@domain"); // false
```

**Assumptions made:**
- Basic validation suitable for most use cases; strict version follows RFC 5321 length/format rules
- Null/blank strings are invalid
- No support for quoted strings or special characters in local part (follows common practice)
- For production use with complex requirements, consider Apache Commons Email (`EmailValidator.getInstance().isValid()`)