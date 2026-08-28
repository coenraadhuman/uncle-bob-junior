```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
```

**Assumptions:**
- Simple regex-based validation covering most common email formats
- Accepts alphanumeric characters, plus signs, underscores, hyphens, and dots in the local part
- Requires at least one dot in the domain with a 2+ character TLD
- Rejects null and blank strings

**Alternative for stricter validation** (if you need RFC 5321 compliance), use Jakarta Mail:
```java
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

The first approach is lightweight; the second is more compliant with email standards but requires the Jakarta Mail dependency.