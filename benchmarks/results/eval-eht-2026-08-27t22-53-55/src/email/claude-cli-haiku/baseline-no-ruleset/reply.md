```java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
```

Or if you need stricter validation, use a more robust regex:

```java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9][a-zA-Z0-9._%-]*@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
```

Or for production use, consider Apache Commons Validator:

```java
import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidationUtil {
    private static final EmailValidator validator = EmailValidator.getInstance();
    
    public static boolean isValidEmail(String email) {
        return validator.isValid(email);
    }
}
```

**Assumptions:** The first two solutions use regex for practical email validation (not full RFC 5321 compliance). They reject null/empty values and validate basic structure. The third option uses a battle-tested library that handles edge cases better. Choose the first for simplicity, the second for stricter rules, or the third for production reliability.