Here's a Java method for email validation using regex:

```java
public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
```

**Usage:**
```java
EmailValidator.validateEmail("coenraad.human@postcodeloterij.nl"); // true
EmailValidator.validateEmail("invalid.email");                     // false
EmailValidator.validateEmail("user+tag@example.co.uk");            // true
```

**Assumptions:**
- Basic RFC 5322 compliance for most practical cases
- Allows alphanumeric characters, dots, hyphens, and plus signs in the local part
- Requires a domain with at least one dot and 2+ letter TLD
- No length validation (RFC limits to 254 total, 64 local part)

**For stricter validation**, use Jakarta Mail (add dependency: `jakarta.mail:jakarta.mail-api`):

```java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean validateEmail(String email) {
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
```

The regex approach is lightweight; the Jakarta Mail approach is more RFC-compliant but requires a dependency.