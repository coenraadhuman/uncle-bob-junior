I'll provide a practical email validation method. Here are my assumptions:
- You want a balance between simplicity and correctness (regex-based)
- Validation for typical email formats, not strict RFC 5322 compliance
- A single standalone method you can drop into your code

```java
public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
        return false;
    }
    
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
}
```

For a more comprehensive solution using Java's built-in utilities:

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
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
```

The second version pre-compiles the regex pattern for better performance if called multiple times.

**For production code**, consider using a dedicated library like Jakarta Validation (with `@Email` annotation) or Apache Commons Email for more robust validation including DNS checks.