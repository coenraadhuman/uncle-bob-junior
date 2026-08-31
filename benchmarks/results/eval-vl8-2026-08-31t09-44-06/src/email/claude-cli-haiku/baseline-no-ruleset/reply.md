```java
public class EmailValidator {
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
```

**Alternative: More comprehensive validation using Jakarta Mail**

```java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            new InternetAddress(email);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
```

**Assumptions:**
- For the regex approach: validates common email formats (alphanumeric, dots, hyphens, underscores in local part; domain with at least one dot and 2+ letter TLD)
- For the Jakarta Mail approach: provides stricter RFC 5322 compliance but requires the `jakarta.mail:jakarta.mail-api` dependency
- Both handle null/empty strings gracefully
- Neither performs DNS verification or checks if the mailbox actually exists (that requires external services)

The regex method is simpler and dependency-free; use Jakarta Mail if you need stricter compliance.