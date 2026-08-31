Here's a self-contained Java email validator. Assumptions: you want practical validation (the kind used for sign-up forms), not full RFC 5322 support, since the complete grammar allows exotic forms like quoted local parts that most systems reject anyway. The method checks structure, length limits from RFC 5321, and requires a dot in the domain, so `user@localhost` is rejected.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    private EmailValidator() {
    }

    // Practical pattern: dot-atom local part, domain labels separated by dots,
    // TLD of at least two letters. Intentionally stricter than full RFC 5322.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
            + "[A-Za-z]{2,63}$");

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 practical limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    /**
     * Validates an email address for use in forms and data intake.
     *
     * @param email the address to validate, may be null
     * @return true if the address is structurally valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
```

Example usage:

```java
EmailValidator.isValidEmail("participant_001@example.com"); // true
EmailValidator.isValidEmail("first.last@sub.example.co.uk"); // true
EmailValidator.isValidEmail("no-at-sign.example.com");       // false
EmailValidator.isValidEmail("user@localhost");               // false (no TLD)
EmailValidator.isValidEmail(null);                           // false
```

Two notes. First, this rejects internationalised addresses (non-ASCII, such as `jörg@münchen.de`); if you need those, validate with `java.net.IDN` conversion on the domain or a library like Apache Commons Validator. Second, structural validation only proves the format is plausible. If deliverability matters, for instance for participant communications, the reliable check is a confirmation email, and that flow would fall under your usual compliance review for participant contact.