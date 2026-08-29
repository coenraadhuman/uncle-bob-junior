I'll provide a practical email validation method using regex, which covers most real-world cases:

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

For stricter RFC 5322 compliance, use `javax.mail.internet.InternetAddress`:

```java
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

public class EmailValidator {
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
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
- The regex approach suits most applications; it rejects emails without a domain and TLD
- The `InternetAddress` approach is more thorough but requires javax.mail dependency
- Both methods reject null and empty strings

Choose the first for simplicity, the second for stricter validation.