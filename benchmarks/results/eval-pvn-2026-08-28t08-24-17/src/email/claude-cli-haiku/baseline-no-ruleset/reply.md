Here's a practical Java email validation method:

```java
public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
```

**Assumptions:**
- Uses regex pattern matching for simplicity and performance
- Accepts standard email format: `local-part@domain.tld`
- Allows alphanumerics, dots, hyphens, and underscores in the local part
- Requires domain with at least one dot and 2+ character TLD
- Does not validate against full RFC 5321/5322 (which is extremely complex) but covers 99% of real-world cases

**Usage example:**
```java
EmailValidator.isValidEmail("coenraad.human@postcodeloterij.nl");  // true
EmailValidator.isValidEmail("invalid.email@");                     // false
EmailValidator.isValidEmail("user@domain.c");                      // false
```

For stronger validation (checking if the email actually exists), you'd need to verify the domain's MX records or send a confirmation email—regex alone cannot do that.