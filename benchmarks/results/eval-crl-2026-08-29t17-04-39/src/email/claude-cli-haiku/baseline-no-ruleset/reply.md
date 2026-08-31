```java
public static boolean isValidEmail(String email) {
    if (email == null || email.isEmpty()) {
        return false;
    }
    
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
}
```

**Assumptions:**
- Basic email format validation is sufficient (local-part @ domain . tld)
- Allows common characters in the local part: letters, numbers, plus, underscore, dot, hyphen
- Requires at least 2 characters for the top-level domain
- Returns `false` for null or empty strings

**For production use**, consider the Apache Commons Validator library which handles RFC compliance more robustly:

```java
import org.apache.commons.validator.routines.EmailValidator;

public static boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
}
```