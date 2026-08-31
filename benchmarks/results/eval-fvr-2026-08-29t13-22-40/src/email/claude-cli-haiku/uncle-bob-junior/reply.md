```java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertThat(EmailValidator.isValid("user@example.com")).isTrue();
        assertThat(EmailValidator.isValid("john.doe@company.co.uk")).isTrue();
        assertThat(EmailValidator.isValid("test+tag@domain.org")).isTrue();
        assertThat(EmailValidator.isValid("a@b.co")).isTrue();
    }

    @Test
    void rejectsInvalidEmails() {
        assertThat(EmailValidator.isValid(null)).isFalse();
        assertThat(EmailValidator.isValid("")).isFalse();
        assertThat(EmailValidator.isValid("   ")).isFalse();
        assertThat(EmailValidator.isValid("plainaddress")).isFalse();
        assertThat(EmailValidator.isValid("user@domain")).isFalse();
        assertThat(EmailValidator.isValid("@example.com")).isFalse();
        assertThat(EmailValidator.isValid("user@.com")).isFalse();
        assertThat(EmailValidator.isValid("user..name@example.com")).isFalse();
    }
}
```

**Assumptions:** Validates common email formats using a practical regex pattern (not RFC 5322 compliant, which is extremely complex). Accepts alphanumeric, dots, dashes, underscores, and plus signs; requires a domain with a TLD of 2+ letters. Rejects null and blank inputs upfront.

For production use with regulated data (GDPR-relevant at PLG), consider using a well-maintained library like `commons-validator` or sending a confirmation email, as regex validation doesn't verify the address actually exists.