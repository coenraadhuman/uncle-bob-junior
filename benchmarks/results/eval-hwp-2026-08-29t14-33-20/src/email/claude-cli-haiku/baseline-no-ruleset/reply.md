Here's a Java method that validates email addresses:

```java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
```

**Usage example:**
```java
EmailValidator.isValidEmail("user@example.com");      // true
EmailValidator.isValidEmail("invalid.email@");        // false
EmailValidator.isValidEmail("test+tag@domain.co.uk"); // true
```

**Assumptions:**
- Email should not be null or blank
- Basic structure: `localpart@domain.extension`
- Local part allows letters, numbers, `.`, `+`, `_`, and `-`
- Domain must have at least one dot
- TLD must be at least 2 characters
- This regex covers most common cases but not all edge cases per RFC 5322

**For production systems**, consider using a library like Apache Commons Validator or Spring's `@Email` annotation, which handle more complex RFC-compliant validation and normalization.