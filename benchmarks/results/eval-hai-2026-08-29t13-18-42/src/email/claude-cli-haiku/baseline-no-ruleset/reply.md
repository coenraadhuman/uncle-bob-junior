```java
public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
}
```

**Assumptions:**
- Uses regex pattern for typical email formats (local@domain.extension)
- Accepts alphanumeric characters plus `+`, `_`, `.`, and `-` in the local part
- Requires at least one dot in the domain with 2+ letter TLD
- Returns `false` for null or empty strings
- This covers ~99% of real-world email addresses; full RFC 5322 compliance requires significantly more complex logic

For stricter validation, you could use Apache Commons Validator (`EmailValidator.getInstance().isValid(email)`) or javax.mail's `InternetAddress` class, but this regex-based approach is lightweight and suitable for most applications.