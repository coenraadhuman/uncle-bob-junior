```java
public class EmailValidator {
    
    /**
     * Validates an email address using a regex pattern.
     * Handles most common email formats while avoiding overly complex RFC 5322 compliance.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(pattern);
    }
    
    /**
     * More permissive validation allowing additional special characters.
     */
    public static boolean isValidEmailLenient(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String pattern = "^[A-Za-z0-9+_'.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(pattern);
    }
}
```

**Assumptions:**
- Standard email format validation is sufficient (not strict RFC 5322 compliance)
- Rejects null/blank strings
- Requires at least one character before `@`, a domain with at least one dot, and a TLD of 2+ letters
- Allows common special characters in the local part: `+`, `_`, `.`, `-`

If you need stricter validation (e.g., checking DNS records or catching all edge cases in the RFC), you'd use a library like Apache Commons Validator or implement a more complex pattern. For most real-world applications, the above methods work well and are maintainable.