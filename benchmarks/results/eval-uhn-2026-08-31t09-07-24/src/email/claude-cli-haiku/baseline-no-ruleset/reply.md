Here's a practical Java email validation method:

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

Or, if you prefer a more robust approach using Jakarta Validation (included in modern Java frameworks):

```java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
```

**Assumptions:**
- The first method uses regex for basic format validation (lightweight, no dependencies)
- The second method uses Jakarta Mail's built-in validation (more standards-compliant)
- Both reject null and blank inputs
- Both handle common email formats like `user@example.com`

Choose the first for simplicity, or the second if you're already using Jakarta/Java EE and want stricter RFC 5321 compliance.