Here's a dependency-free email validator using a pragmatic regex. Assumptions: standard Java (no Jakarta Mail dependency), validation to a practical subset of RFC 5322 that covers real-world addresses, and null-safe behaviour returning false rather than throwing.

```java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical subset of RFC 5322: dot-atom local part, domain labels
    // separated by dots, TLD of at least two letters. Rejects edge cases
    // like quoted local parts and IP-literal domains, which are rarely
    // legitimate in participant-facing systems.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
                    + "[A-Za-z]{2,}$"
    );

    // RFC 5321 limits: 64 chars for the local part, 254 for the whole address
    private static final int MAX_TOTAL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > MAX_TOTAL_LENGTH) {
            return false;
        }
        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex < 1 || atIndex > MAX_LOCAL_PART_LENGTH) {
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
EmailValidator.isValidEmail("invalid@@example.com"); // false
EmailValidator.isValidEmail("no-at-sign.example.com"); // false
EmailValidator.isValidEmail(null); // false
```

A few notes on the design choices:

- The regex deliberately rejects exotic but technically valid forms (quoted strings like `"a b"@example.com`, IP-literal domains like `user@[192.168.1.1]`) because they are almost never legitimate in sign-up or contact forms.
- Length checks are applied before the regex to enforce RFC 5321 limits and avoid pathological regex input.
- Internationalised addresses (non-ASCII, e.g. Cyrillic or accented characters) are rejected. If you need to accept those, the domain should be converted with `java.net.IDN.toASCII` first, and the local part rules relaxed.
- Format validation only confirms the address is well formed, not that it exists or can receive mail. For anything participant-facing, pair this with a confirmation email (double opt-in), which is also the safer pattern from a GDPR consent standpoint.