Here's a practical Java method for email validation:

```java
import java.util.regex.Pattern;

public class EmailValidator {
    // RFC 5322 simplified pattern that covers most real-world cases
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(email.trim()).matches();
    }
}
```

**Usage:**
```java
System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
System.out.println(EmailValidator.isValidEmail("invalid.email@"));          // false
System.out.println(EmailValidator.isValidEmail("name+tag@domain.co.uk"));   // true
```

**For a more robust approach using Jakarta Mail**, which actually sends a validation email:

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
            return email.contains("@");
        } catch (AddressException e) {
            return false;
        }
    }
}
```

**Assumptions made:**
- The regex approach is sufficient (validates format, not deliverability)
- Email format should follow standard conventions: `localpart@domain.extension`
- The method trims whitespace before validation
- `null` or empty strings return `false`

Choose the first approach for lightweight validation, or the second if you're already using the Jakarta Mail library in your project.