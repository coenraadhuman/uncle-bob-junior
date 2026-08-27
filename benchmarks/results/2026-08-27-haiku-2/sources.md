# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
public class EmailValidator {
    
    /**
     * Validates email address using a straightforward regex pattern.
     * Covers most common email formats without being overly strict.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
````

````java
public class EmailValidator {
    
    /**
     * Validates email address with better edge case handling.
     * Allows common special characters and multiple dots.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim();
        
        // Basic structure check
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domain = parts[1];
        
        // Validate local part (before @)
        if (localPart.isEmpty() || localPart.length() > 64) {
            return false;
        }
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }
        if (localPart.contains("..")) {
            return false;
        }
        
        String localRegex = "^[A-Za-z0-9+_.-]+$";
        if (!localPart.matches(localRegex)) {
            return false;
        }
        
        // Validate domain part (after @)
        if (domain.isEmpty() || domain.length() > 255) {
            return false;
        }
        if (domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        if (domain.contains("..")) {
            return false;
        }
        
        String domainRegex = "^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return domain.matches(domainRegex);
    }
}
````

````java
public static void main(String[] args) {
    System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
    System.out.println(EmailValidator.isValidEmail("john.doe@company.co.uk")); // true
    System.out.println(EmailValidator.isValidEmail("invalid.email@"));         // false
    System.out.println(EmailValidator.isValidEmail("@example.com"));           // false
    System.out.println(EmailValidator.isValidEmail("user@.com"));              // false
}
````

### email · run 1 · uncle-bob-junior

````java
public class EmailValidator {
    // Simplified pattern for common email formats
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
````

````java
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            new InternetAddress(email, true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
````

````java
public class EmailValidatorTest {
    public static void main(String[] args) {
        System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
        System.out.println(EmailValidator.isValidEmail("invalid.email@"));         // false
        System.out.println(EmailValidator.isValidEmail(null));                     // false
        System.out.println(EmailValidator.isValidEmail(""));                       // false
    }
}
````

### email · run 2 · baseline

````java
public class EmailValidator {
    
    /**
     * Validates an email address using a regex pattern covering common formats.
     * Returns true if valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
````

````java
// Valid
isValidEmail("user@example.com");           // true
isValidEmail("john.doe+tag@company.co.uk"); // true
isValidEmail("test_123@domain.org");        // true

// Invalid
isValidEmail("invalid.email@");             // false
isValidEmail("@example.com");               // false
isValidEmail("user@domain");                // false
isValidEmail(null);                         // false
isValidEmail("");                           // false
````

````java
import java.util.regex.Pattern;

public static boolean isValidEmailStrict(String email) {
    if (email == null || email.isBlank()) {
        return false;
    }
    
    String strictRegex = "^[A-Za-z0-9][A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]*[A-Za-z0-9]@" +
                         "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9]" +
                         "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$";
    return Pattern.matches(strictRegex, email);
}
````

### email · run 2 · uncle-bob-junior

````java
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
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+tag@domain.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_email@sub.domain.org"));
    }
    
    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("invalid"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user name@example.com"));
    }
    
    @Test
    void edgeCases() {
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertTrue(EmailValidator.isValidEmail("a@b.co"));
    }
}
````

````java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            new InternetAddress(email);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
````

### email · run 3 · baseline

````java
// Simple and practical email validation
public class EmailValidator {
    
    // RFC 5322 simplified regex pattern
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    
    /**
     * Validates email address using regex pattern.
     * @param email the email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
````

````java
public class EmailValidator {
    
    /**
     * Validates email address with comprehensive checks.
     * @param email the email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        email = email.trim();
        
        // Basic length check
        if (email.length() > 254) {
            return false;
        }
        
        // Must contain exactly one @
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || email.lastIndexOf('@') != atIndex) {
            return false;
        }
        
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);
        
        // Validate local part (before @)
        if (localPart.length() > 64 || !isValidLocalPart(localPart)) {
            return false;
        }
        
        // Validate domain part (after @)
        return isValidDomain(domainPart);
    }
    
    private static boolean isValidLocalPart(String local) {
        if (local.isEmpty() || local.startsWith(".") || local.endsWith(".")) {
            return false;
        }
        
        // Check for consecutive dots
        if (local.contains("..")) {
            return false;
        }
        
        // Allow alphanumeric, dots, hyphens, underscores, plus signs
        return local.matches("^[A-Za-z0-9._+%-]+$");
    }
    
    private static boolean isValidDomain(String domain) {
        if (domain.isEmpty() || domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        
        // Must have at least one dot
        if (!domain.contains(".")) {
            return false;
        }
        
        // Check for consecutive dots
        if (domain.contains("..")) {
            return false;
        }
        
        // TLD must be at least 2 characters
        String[] parts = domain.split("\\.");
        if (parts[parts.length - 1].length() < 2) {
            return false;
        }
        
        // Each label must be valid
        for (String part : parts) {
            if (!isValidDomainLabel(part)) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean isValidDomainLabel(String label) {
        if (label.isEmpty() || label.length() > 63) {
            return false;
        }
        
        // Can't start or end with hyphen
        if (label.startsWith("-") || label.endsWith("-")) {
            return false;
        }
        
        return label.matches("^[A-Za-z0-9-]+$");
    }
}
````

````java
public class Main {
    public static void main(String[] args) {
        System.out.println(EmailValidator.isValidEmail("user@example.com"));        // true
        System.out.println(EmailValidator.isValidEmail("invalid.email@"));         // false
        System.out.println(EmailValidator.isValidEmail("test+tag@domain.co.uk")); // true
        System.out.println(EmailValidator.isValidEmail(""));                        // false
        System.out.println(EmailValidator.isValidEmail(null));                      // false
    }
}
````

### email · run 3 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern COMPILED_PATTERN = 
        Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return COMPILED_PATTERN.matcher(email).matches();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@example.co.uk"));
        assertTrue(EmailValidator.isValidEmail("user+tag@example.com"));
        assertTrue(EmailValidator.isValidEmail("user_name@example.com"));
    }
    
    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail("notanemail"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }
}
````

### email · run 4 · baseline

````java
public class EmailValidator {
    /**
     * Validates email address format using regex pattern.
     * Pattern allows: letters, numbers, dots, hyphens, underscores in local part,
     * and standard domain naming conventions.
     */
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
EmailValidator.isValidEmail("user@example.com");      // true
EmailValidator.isValidEmail("invalid.email@");        // false
EmailValidator.isValidEmail("no-at-sign.com");        // false
EmailValidator.isValidEmail("user+tag@domain.co.uk"); // false (+ not allowed)
````

````java
import org.apache.commons.mail.EmailValidator;

public class EmailValidationUtil {
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
````

### email · run 4 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MAX_EMAIL_LENGTH = 254;

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

````java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    private static final int MAX_EMAIL_LENGTH = 254;

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+tag@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_123@test-domain.org"));
    }

    @Test
    void invalidEmails() {
        assertFalse(EmailValidator.isValidEmail("invalid.email"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
    }

    @Test
    void nullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void tooLong() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValidEmail(longEmail));
    }
}
````

### email · run 5 · baseline

````java
public class EmailValidator {
    
    // Simple regex pattern for basic email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates an email address using regex pattern matching.
     * Checks for basic format: local-part@domain.extension
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
    
    /**
     * More strict validation with additional checks
     */
    public static boolean isValidEmailStrict(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim();
        
        // Check basic pattern
        if (!email.matches(EMAIL_PATTERN)) {
            return false;
        }
        
        // Check length constraints
        if (email.length() > 254) {
            return false;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        // Local part shouldn't exceed 64 characters
        if (localPart.length() > 64) {
            return false;
        }
        
        // No consecutive dots allowed
        if (email.contains("..")) {
            return false;
        }
        
        // Shouldn't start or end with dot
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }
        
        // Domain should have at least one dot
        if (!domain.contains(".")) {
            return false;
        }
        
        return true;
    }
}
````

### email · run 5 · uncle-bob-junior

````java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("123@test.io"));
    }
    
    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
    }
}
````

### email · run 6 · baseline

````java
public class EmailValidator {
    // Simple regex pattern for basic email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates an email address using a regex pattern.
     * Allows: alphanumerics, dots, hyphens, underscores, plus signs
     * Rejects: spaces, invalid characters, missing @, missing domain
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
````

````java
public static void main(String[] args) {
    System.out.println(EmailValidator.isValidEmail("user@example.com"));           // true
    System.out.println(EmailValidator.isValidEmail("john.doe+tag@company.co.uk")); // true
    System.out.println(EmailValidator.isValidEmail("invalid.email@"));              // false
    System.out.println(EmailValidator.isValidEmail("no-at-sign.com"));              // false
    System.out.println(EmailValidator.isValidEmail(null));                          // false
}
````

````java
import jakarta.validation.constraints.Email;

public class User {
    @Email(message = "Invalid email address")
    private String email;
}
````

### email · run 6 · uncle-bob-junior

````java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final int MAX_EMAIL_LENGTH = 254;

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void validEmailsPasses() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+tag@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_123@test-domain.org"));
    }

    @Test
    void invalidEmailsFail() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("plaintext"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
    }

    @Test
    void excessivelyLongEmailFails() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValidEmail(longEmail));
    }
}
````

````java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
````

### email · run 7 · baseline

````java
public class EmailValidator {
    // Regex pattern for basic email validation
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates an email address using regex pattern matching.
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
public class EmailValidator {
    /**
     * Validates an email address using InternetAddress.
     * More strict validation that checks RFC standards.
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            javax.mail.internet.InternetAddress address = 
                new javax.mail.internet.InternetAddress(email);
            address.validate();
            return true;
        } catch (javax.mail.internet.AddressException e) {
            return false;
        }
    }
}
````

### email · run 7 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern COMPILED_PATTERN = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        return COMPILED_PATTERN.matcher(email).matches();
    }
}
````

````java
class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test+tag@domain.org"));
    }

    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("plain-text"));
        assertFalse(EmailValidator.isValidEmail("@domain.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user @domain.com"));
    }
}
````

### email · run 8 · baseline

````java
public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
}
````

````java
public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    // Basic length check
    if (email.length() > 254) {
        return false;
    }
    
    // Regex pattern allowing:
    // - Alphanumeric, dots, hyphens, underscores, plus signs before @
    // - Domain with dots and hyphens
    // - TLD with 2+ characters
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    if (!email.matches(emailRegex)) {
        return false;
    }
    
    // Additional checks
    if (email.startsWith(".") || email.startsWith("@")) {
        return false;
    }
    if (email.contains("..")) {
        return false;
    }
    
    String[] parts = email.split("@");
    if (parts[0].length() > 64) {  // Local part max length
        return false;
    }
    
    return true;
}
````

### email · run 8 · uncle-bob-junior

````java
public class EmailValidator {
  private static final String EMAIL_PATTERN =
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  public static boolean isValid(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return email.matches(EMAIL_PATTERN);
  }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
  @Test
  void validEmailAddresses() {
    assertTrue(EmailValidator.isValid("user@example.com"));
    assertTrue(EmailValidator.isValid("john.doe+tag@company.co.uk"));
    assertTrue(EmailValidator.isValid("test_123@sub.domain.org"));
  }

  @Test
  void invalidEmailAddresses() {
    assertFalse(EmailValidator.isValid("plaintext"));
    assertFalse(EmailValidator.isValid("@example.com"));
    assertFalse(EmailValidator.isValid("user@"));
    assertFalse(EmailValidator.isValid("user@.com"));
    assertFalse(EmailValidator.isValid("user name@example.com"));
  }

  @Test
  void nullAndEmptyInputs() {
    assertFalse(EmailValidator.isValid(null));
    assertFalse(EmailValidator.isValid(""));
    assertFalse(EmailValidator.isValid("   "));
  }
}
````

### email · run 9 · baseline

````java
// Simple regex-based validation (good for most use cases)
public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    return email.matches(emailRegex);
}
````

````java
public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
        return false;
    }
    
    email = email.trim();
    
    // Check basic structure
    if (!email.contains("@")) {
        return false;
    }
    
    String[] parts = email.split("@");
    if (parts.length != 2) {
        return false;
    }
    
    String localPart = parts[0];
    String domain = parts[1];
    
    // Validate local part (before @)
    if (localPart.isEmpty() || localPart.length() > 64) {
        return false;
    }
    if (localPart.startsWith(".") || localPart.endsWith(".")) {
        return false;
    }
    if (localPart.contains("..")) {
        return false;
    }
    
    // Validate domain part (after @)
    if (domain.isEmpty() || domain.length() > 255) {
        return false;
    }
    if (!domain.contains(".")) {
        return false;
    }
    if (domain.startsWith(".") || domain.endsWith(".") || domain.contains("..")) {
        return false;
    }
    
    String domainRegex = "^[A-Za-z0-9.-]+$";
    if (!domain.matches(domainRegex)) {
        return false;
    }
    
    String localRegex = "^[A-Za-z0-9+_.-]+$";
    return localPart.matches(localRegex);
}
````

````java
// Using Apache Commons Validator
import org.apache.commons.validator.routines.EmailValidator;

public static boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
}
````

### email · run 9 · uncle-bob-junior

````java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    
    @Test
    void validEmailsAreAccepted() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe+tag@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("test_123@test-domain.org"));
    }
    
    @Test
    void invalidEmailsAreRejected() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("invalid"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user @example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }
}
````

````xml
<dependency>
    <groupId>jakarta.mail</groupId>
    <artifactId>jakarta.mail-api</artifactId>
    <version>2.1.3</version>
</dependency>
````

### email · run 10 · baseline

````java
import java.util.regex.Pattern;

public class EmailValidator {
    // RFC 5322 simplified pattern for email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    /**
     * Validates if the given string is a valid email address.
     * 
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(email.trim()).matches();
    }
    
    // Example usage
    public static void main(String[] args) {
        String[] testEmails = {
            "user@example.com",
            "john.doe+tag@company.co.uk",
            "invalid.email@",
            "@nodomain.com",
            "no-at-sign.com",
            "user@domain",
            "user name@example.com"
        };
        
        for (String email : testEmails) {
            System.out.println(email + " -> " + isValidEmail(email));
        }
    }
}
````

````java
import org.apache.commons.validator.routines.EmailValidator;

public static boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
}
````

### email · run 10 · uncle-bob-junior

````java
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
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
        assertTrue(EmailValidator.isValidEmail("john.doe@company.co.uk"));
        assertTrue(EmailValidator.isValidEmail("alice+tag@domain.org"));
        assertTrue(EmailValidator.isValidEmail("test_123@sub-domain.info"));
    }

    @Test
    void rejectsInvalidEmails() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail("   "));
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@nodomain.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain"));
        assertFalse(EmailValidator.isValidEmail("user name@domain.com"));
    }

    @Test
    void handlesEdgeCases() {
        assertFalse(EmailValidator.isValidEmail("user@@domain.com"));
        assertFalse(EmailValidator.isValidEmail("user@domain..com"));
        assertTrue(EmailValidator.isValidEmail("a@b.co"));
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;
        boolean isFirstLine = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (isFirstLine) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    // Sum the amount column
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Invalid number in amount column: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
        }

        System.out.println("Total amount: " + total);
    }
}
````

````java
import com.opencsv.CSVReader;
import java.io.*;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;
        boolean isFirstLine = true;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    for (int i = 0; i < line.length; i++) {
                        if (line[i].equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    if (amountColumnIndex >= 0 && amountColumnIndex < line.length) {
                        try {
                            total += Double.parseDouble(line[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Invalid number: " + line[amountColumnIndex]);
                        }
                    }
                }
            }
        }

        System.out.println("Total amount: " + total);
    }
}
````

````bash
javac SalesSum.java
java SalesSum
````

### csv · run 1 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SalesCalculator {
    static class CsvRow {
        String[] headers;
        String[] values;

        CsvRow(String[] headers, String[] values) {
            this.headers = headers;
            this.values = values;
        }

        String getValue(String columnName) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(columnName)) {
                    return i < values.length ? values[i].trim() : "";
                }
            }
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
    }

    static CsvRow parseRow(String[] headers, String line) {
        String[] values = line.split(",");
        return new CsvRow(headers, values);
    }

    static double sumAmountColumn(String filePath) throws IOException {
        double sum = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("File is empty");
            }

            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                CsvRow row = parseRow(headers, line);
                String amountStr = row.getValue("amount");
                sum += Double.parseDouble(amountStr);
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        try {
            double total = sumAmountColumn("sales.csv");
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number in amount column: " + e.getMessage());
            System.exit(1);
        }
    }
}
````

````java
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesCalculatorTest {
    static void createTestCsv(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    static void test(String testName, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.001) {
            System.out.println("✓ " + testName);
        } else {
            System.out.println("✗ " + testName + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) throws IOException {
        // Test basic sum
        createTestCsv("test1.csv", "id,product,amount\n1,Widget,100.00\n2,Gadget,250.50\n");
        double result1 = SalesCalculator.sumAmountColumn("test1.csv");
        test("Basic sum", 350.50, result1);

        // Test single row
        createTestCsv("test2.csv", "id,product,amount\n1,Item,500\n");
        double result2 = SalesCalculator.sumAmountColumn("test2.csv");
        test("Single row", 500, result2);

        // Test with spaces around values
        createTestCsv("test3.csv", "id,product,amount\n1,A, 10.5 \n2,B, 20.5 \n");
        double result3 = SalesCalculator.sumAmountColumn("test3.csv");
        test("Values with spaces", 31.0, result3);

        // Cleanup
        Files.deleteIfExists(Paths.get("test1.csv"));
        Files.deleteIfExists(Paths.get("test2.csv"));
        Files.deleteIfExists(Paths.get("test3.csv"));
    }
}
````

### csv · run 2 · baseline

````java
import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        double total = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            boolean isHeader = true;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isHeader) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else {
                    // Sum the amount column
                    if (amountIndex >= 0 && amountIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse value: " + values[amountIndex]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        System.out.println("Sum of amount column: " + total);
    }
}
````

````bash
javac SalesAnalyzer.java
java SalesAnalyzer
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

public class SalesSum {
    public static void main(String[] args) {
        try {
            BigDecimal sum = sumAmountColumn("sales.csv");
            System.out.println("Total amount: " + sum);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static BigDecimal sumAmountColumn(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (header == null) {
                return BigDecimal.ZERO;
            }

            int amountIndex = findColumnIndex(header, "amount");
            if (amountIndex == -1) {
                throw new IllegalArgumentException("Column 'amount' not found in CSV");
            }

            BigDecimal sum = BigDecimal.ZERO;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (amountIndex < fields.length) {
                    try {
                        sum = sum.add(new BigDecimal(fields[amountIndex].trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount: " + fields[amountIndex]);
                    }
                }
            }
            return sum;
        }
    }

    private static int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
````

### csv · run 3 · baseline

````java
import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty");
                return;
            }
            
            // Parse header to find 'amount' column index
            String[] headers = line.split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            
            if (amountIndex == -1) {
                System.out.println("'amount' column not found");
                return;
            }
            
            // Read data rows and sum amounts
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        double amount = Double.parseDouble(values[amountIndex].trim());
                        total += amount;
                    } catch (NumberFormatException e) {
                        // Skip rows with invalid amounts
                    }
                }
            }
            
            System.out.println(total);
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 3 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSumCalculator {
    private static final String CSV_FILE = "sales.csv";
    private static final String DELIMITER = ",";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        double totalAmount = calculateSalesSum();
        System.out.println("Total amount: " + totalAmount);
    }

    private static double calculateSalesSum() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("Error: CSV file is empty");
                return 0;
            }

            int amountColumnIndex = findColumnIndex(headerLine);
            if (amountColumnIndex == -1) {
                System.err.println("Error: 'amount' column not found");
                return 0;
            }

            return sumAmountColumn(reader, amountColumnIndex);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 0;
        }
    }

    private static int findColumnIndex(String headerLine) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        return -1;
    }

    private static double sumAmountColumn(BufferedReader reader, int columnIndex) throws IOException {
        double sum = 0;
        String line;
        int lineNumber = 2;

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(DELIMITER);
            if (values.length > columnIndex) {
                try {
                    double amount = Double.parseDouble(values[columnIndex].trim());
                    sum += amount;
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid number at line " + lineNumber + ": " + values[columnIndex]);
                }
            }
            lineNumber++;
        }

        return sum;
    }
}
````

````bash
javac SalesSumCalculator.java
java SalesSumCalculator
````

### csv · run 4 · baseline

````java
import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double sum = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (lineNumber == 0) {
                    // Find the index of the 'amount' column in the header
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                } else {
                    // Parse and sum the amount values
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value: " + values[amountColumnIndex]);
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        System.out.println("Sum of amount column: " + sum);
    }
}
````

### csv · run 4 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = sumAmountColumn(filePath);
        System.out.println("Total amount: " + totalAmount);
    }
    
    private static double sumAmountColumn(String filePath) {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountColumnIndex < 0) {
                System.err.println("Column '" + AMOUNT_COLUMN + "' not found");
                return 0.0;
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                total += parseAmount(line, amountColumnIndex);
            }
            
            return total;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 0.0;
        }
    }
    
    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0.0;
        }
        
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse amount: " + values[columnIndex]);
            return 0.0;
        }
    }
}
````

````bash
javac SalesAnalyzer.java
java SalesAnalyzer
````

### csv · run 5 · baseline

````java
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        if (lines.isEmpty()) {
            System.out.println("CSV file is empty");
            return;
        }
        
        // Parse header to find 'amount' column index
        String[] headers = lines.get(0).split(",");
        int amountIndex = -1;
        
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("amount")) {
                amountIndex = i;
                break;
            }
        }
        
        if (amountIndex == -1) {
            System.out.println("'amount' column not found");
            return;
        }
        
        // Sum values from amount column
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            if (values.length > amountIndex) {
                try {
                    totalAmount += Double.parseDouble(values[amountIndex].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid amount in row " + (i + 1) + ": " + values[amountIndex]);
                }
            }
        }
        
        System.out.println("Total Amount: " + totalAmount);
    }
}
````

### csv · run 5 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SalesCalculator {
    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        try {
            double total = calculateSalesSum();
            System.out.println("Total sales: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double calculateSalesSum() throws IOException {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountColumnIndex == -1) {
                throw new IOException("Column '" + AMOUNT_COLUMN + "' not found");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                sum += parseAmount(line, amountColumnIndex);
            }
        }
        
        return sum;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0.0;
        }
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
````

````bash
javac SalesCalculator.java
java SalesCalculator
````

````java
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SalesCalculator {
    public static void main(String[] args) {
        try (FileReader reader = new FileReader("sales.csv");
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            double sum = 0.0;
            for (CSVRecord record : csvParser) {
                sum += Double.parseDouble(record.get("amount"));
            }
            System.out.println("Total sales: " + sum);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
````

### csv · run 6 · baseline

````java
import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0.0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            int amountColumnIndex = -1;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    // Parse header to find the 'amount' column index
                    String[] headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountColumnIndex].trim());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse value: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Total Amount: " + totalAmount);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 6 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    private static final String FILENAME = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        try {
            double total = calculateTotalAmount();
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double calculateTotalAmount() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            int amountColumnIndex = findColumnIndex(headerLine);
            if (amountColumnIndex == -1) {
                throw new IOException("Column '" + AMOUNT_COLUMN + "' not found in CSV");
            }

            return sumAmountColumn(reader, amountColumnIndex);
        }
    }

    private static int findColumnIndex(String headerLine) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        return -1;
    }

    private static double sumAmountColumn(BufferedReader reader, int columnIndex) throws IOException {
        double total = 0.0;
        String line;

        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            if (columnIndex < fields.length) {
                try {
                    total += Double.parseDouble(fields[columnIndex].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Skipping non-numeric value: " + fields[columnIndex]);
                }
            }
        }

        return total;
    }
}
````

````bash
javac SalesCalculator.java
java SalesCalculator
````

### csv · run 7 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean headerProcessed = false;
            
            while ((line = reader.readLine()) != null) {
                if (!headerProcessed) {
                    // Parse header to find amount column
                    String[] headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found in CSV");
                        return;
                    }
                    headerProcessed = true;
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountColumnIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountColumnIndex].trim());
                            total += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse value: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + total);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
````

### csv · run 7 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSumCalculator {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = calculateSum(filePath);
        System.out.println("Total amount: " + total);
    }

    private static double calculateSum(String filePath) {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("CSV file is empty");
                return 0.0;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                System.err.println("'amount' column not found");
                return 0.0;
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (amountColumnIndex < fields.length) {
                    try {
                        sum += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount value: " + fields[amountColumnIndex]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        return sum;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
````

### csv · run 8 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            // Read header line
            if ((line = br.readLine()) != null) {
                headers = line.split(",");
                // Find the index of 'amount' column
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].trim().equalsIgnoreCase("amount")) {
                        amountIndex = i;
                        break;
                    }
                }
                
                if (amountIndex == -1) {
                    System.err.println("'amount' column not found");
                    return;
                }
            }
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        double amount = Double.parseDouble(values[amountIndex].trim());
                        totalAmount += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Total amount: " + totalAmount);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 8 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = calculateSalesSum(filePath);
        System.out.println("Total Sales Amount: " + totalAmount);
    }

    private static double calculateSalesSum(String filePath) {
        double sum = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("File is empty");
                return 0.0;
            }

            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                System.err.println("Column 'amount' not found");
                return 0.0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                sum += extractAmount(line, amountColumnIndex);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return sum;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static double extractAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0.0;
        }

        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount value: " + values[columnIndex]);
            return 0.0;
        }
    }
}
````

````bash
javac SalesAnalyzer.java
java SalesAnalyzer
````

### csv · run 9 · baseline

````java
import java.io.*;
import java.util.*;

public class SalesSumCalculator {
    public static void main(String[] args) {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                // First line: find the index of the 'amount' column
                if (headers == null) {
                    headers = values;
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                } else {
                    // Data row: add to sum
                    try {
                        total += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Skipping invalid amount value: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Total amount: " + total);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: sales.csv file not found");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 9 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty");
                return;
            }
            
            String[] headers = line.split(",");
            int amountColumnIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountColumnIndex = i;
                    break;
                }
            }
            
            if (amountColumnIndex == -1) {
                System.out.println("'amount' column not found");
                return;
            }
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountColumnIndex) {
                    try {
                        total += Double.parseDouble(values[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format: " + values[amountColumnIndex]);
                    }
                }
            }
            
            System.out.printf("Sum of 'amount' column: %.2f%n", total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 10 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalysis {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                if (headers == null) {
                    // Parse header row to find 'amount' column
                    headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.out.println("'amount' column not found");
                        return;
                    }
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountIndex].trim());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping invalid amount: " + values[amountIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + totalAmount);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 10 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class SalesAnalyzer {
    public static void main(String[] args) {
        try {
            double totalAmount = sumSalesAmount("sales.csv");
            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static double sumSalesAmount(String filePath) throws IOException {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            String[] headers = headerLine.split(",");
            int amountIndex = Arrays.asList(headers).indexOf("amount");
            
            if (amountIndex == -1) {
                throw new IOException("'amount' column not found in CSV");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    total += Double.parseDouble(values[amountIndex].trim());
                }
            }
        }
        
        return total;
    }
}
````

## retry

### retry · run 1 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw lastException;
    }

    public void execute(Runnable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw lastException;
    }
}
````

````java
// For operations that return a value
RetryHelper retry = new RetryHelper(3, 1000);

String result = retry.execute(() -> {
    // Some operation that might fail
    return fetchDataFromApi();
});

// For operations without return value
retry.execute(() -> {
    // Some operation that might fail
    saveToDatabase();
});
````

### retry · run 1 · uncle-bob-junior

````java
public class Retry {
    private final int maxAttempts;
    private final long delayMillis;

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    public Retry(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw lastException;
    }
}
````

````java
class RetryTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 50;

    @Test
    void successesImmediately() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }

    @Test
    void retriesAndEventuallySucceeds() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("Temporary failure");
            }
            return "success";
        });

        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsAfterExhaustingAttempts() {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);

        IOException thrown = assertThrows(IOException.class, () ->
            retry.execute(() -> {
                throw new IOException("Persistent failure");
            })
        );

        assertEquals("Persistent failure", thrown.getMessage());
    }

    @Test
    void waitsBeforeRetrying() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, 100);
        long start = System.currentTimeMillis();

        assertThrows(IOException.class, () ->
            retry.execute(() -> {
                throw new IOException("Fail");
            })
        );

        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 200, "Should wait at least 2 * 100ms between 3 attempts");
    }

    @Test
    void workdsWithOperationsThatReturnNull() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);
        String result = retry.execute(() -> null);
        assertNull(result);
    }
}
````

### retry · run 2 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    /**
     * Executes an operation with retry logic.
     * 
     * @param operation the operation to execute
     * @param <T> the return type of the operation
     * @return the result of the operation
     * @throws Exception the last exception thrown if all attempts fail
     */
    public <T> T execute(CheckedSupplier<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }

        throw lastException;
    }

    /**
     * Executes an operation with retry logic (void operation).
     * 
     * @param operation the operation to execute
     * @throws Exception the last exception thrown if all attempts fail
     */
    public void execute(CheckedRunnable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }

        throw lastException;
    }

    private void sleep() {
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", e);
            }
        }
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    // Convenience static methods
    public static <T> T retryOperation(int maxAttempts, long delayMillis, CheckedSupplier<T> operation) throws Exception {
        return new RetryHelper(maxAttempts, delayMillis).execute(operation);
    }

    public static void retryOperation(int maxAttempts, long delayMillis, CheckedRunnable operation) throws Exception {
        new RetryHelper(maxAttempts, delayMillis).execute(operation);
    }
}
````

````java
// Example 1: Operation with return value
RetryHelper retryHelper = new RetryHelper(3, 1000); // 3 attempts, 1 second delay
String result = retryHelper.execute(() -> {
    // Your operation here
    return fetchDataFromAPI();
});

// Example 2: Void operation
retryHelper.execute(() -> {
    // Your operation here
    saveToDatabase();
});

// Example 3: Using static convenience methods
String data = RetryHelper.retryOperation(5, 500, () -> {
    return callRemoteService();
});

// Example 4: With lambda exception handling
RetryHelper.retryOperation(3, 2000, () -> {
    URL url = new URL("http://api.example.com");
    // This will retry up to 3 times with 2-second delays
    return url.openConnection();
});
````

### retry · run 2 · uncle-bob-junior

````java
@FunctionalInterface
public interface Operation<T> {
    T execute() throws Exception;
}

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryHelper() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }
    
    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
}
````

````java
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class RetryHelperTest {
    
    @Test
    public void testSucceedsOnFirstAttempt() throws Exception {
        RetryHelper helper = new RetryHelper(3, 100);
        int result = helper.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    public void testSucceedsAfterRetry() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper helper = new RetryHelper(3, 100);
        
        int result = helper.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 2) {
                throw new IOException("Temporary failure");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(2, attempts.get());
    }
    
    @Test
    public void testThrowsAfterMaxAttempts() throws Exception {
        RetryHelper helper = new RetryHelper(3, 50);
        
        assertThrows(IOException.class, () -> {
            helper.execute(() -> {
                throw new IOException("Permanent failure");
            });
        });
    }
    
    @Test
    public void testVoidOperation() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        RetryHelper helper = new RetryHelper(2, 50);
        
        helper.execute(() -> {
            counter.incrementAndGet();
            return null;
        });
        
        assertEquals(1, counter.get());
    }
    
    @Test
    public void testRejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
    }
    
    @Test
    public void testRejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
````

````java
RetryHelper retrier = new RetryHelper(5, 2000); // 5 attempts, 2-second delay

String response = retrier.execute(() -> {
    return fetchFromUnreliableService();
});

// Or with the default constructor:
new RetryHelper().execute(() -> {
    databaseOperation();
    return null;
});
````

### retry · run 3 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
````

````java
// Basic usage with return value
RetryHelper retryHelper = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

String result = retryHelper.execute(() -> {
    // Your operation here
    return apiCall();
});

// With void operations
retryHelper.execute(() -> {
    database.write(data);
    return null;
});

// With specific exception handling
try {
    retryHelper.execute(() -> {
        return risky Operation();
    });
} catch (IOException | TimeoutException e) {
    // Handle final failure
}
````

### retry · run 3 · uncle-bob-junior

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }

    public void executeVoid(VoidOperation operation) throws Exception {
        execute(() -> {
            operation.execute();
            return null;
        });
    }

    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }
}
````

````java
// Operation returning a value
RetryHelper retry = new RetryHelper(3, 1000);
String result = retry.execute(() -> fetchDataFromAPI());

// Void operation
retry.executeVoid(() -> saveToDatabase());

// With lambda capturing local state
int retries = 5;
int delayMs = 500;
User user = retry.execute(() -> lookupUser(userId));
````

````java
public class RetryHelperTest {
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }

    @Test
    void retriesAndSucceedsAfterFailure() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) throw new IOException("fail");
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsAfterMaxAttemptsExhausted() throws Exception {
        RetryHelper retry = new RetryHelper(2, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        
        assertThrows(IOException.class, () -> 
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new IOException("always fail");
            })
        );
        
        assertEquals(2, attempts.get());
    }

    @Test
    void voidOperationWorks() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger callCount = new AtomicInteger(0);
        
        retry.executeVoid(() -> callCount.incrementAndGet());
        
        assertEquals(1, callCount.get());
    }

    @Test
    void respectsDelayBetweenAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(3, 200);
        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        
        try {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("fail");
            });
        } catch (RuntimeException e) {
            // expected
        }
        
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 400, "Should have delayed ~400ms for 2 retries");
    }
}
````

### retry · run 4 · baseline

````java
import java.util.concurrent.Callable;

/**
 * Utility class for retrying operations with exponential or fixed backoff.
 */
public class RetryHelper {

    /**
     * Executes an operation with retry logic.
     *
     * @param operation the operation to execute
     * @param maxAttempts maximum number of attempts (minimum 1)
     * @param delayMs delay in milliseconds between retries
     * @param <T> return type of the operation
     * @return the result of the operation
     * @throws Exception the last exception if all attempts fail
     */
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delayMs) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }

    /**
     * Executes a void operation with retry logic.
     *
     * @param operation the operation to execute
     * @param maxAttempts maximum number of attempts (minimum 1)
     * @param delayMs delay in milliseconds between retries
     * @throws Exception the last exception if all attempts fail
     */
    public static void retry(Runnable operation, int maxAttempts, long delayMs) throws Exception {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMs);
    }
}
````

````java
public class RetryHelperExample {

    // Example 1: Retrying a method that returns a value
    public static void exampleWithReturnValue() throws Exception {
        String result = RetryHelper.retry(
            () -> callUnstableAPI(),
            3,           // max 3 attempts
            1000         // 1 second delay between retries
        );
        System.out.println("Success: " + result);
    }

    // Example 2: Retrying a void operation
    public static void exampleVoidOperation() throws Exception {
        RetryHelper.retry(
            () -> sendMessage("Hello"),
            5,           // max 5 attempts
            500          // 500ms delay
        );
        System.out.println("Message sent successfully");
    }

    // Example 3: With lambda that throws checked exception
    public static void exampleWithIOOperation() throws Exception {
        byte[] data = RetryHelper.retry(
            () -> downloadFile("https://example.com/file.txt"),
            3,
            2000
        );
    }

    private static String callUnstableAPI() throws Exception {
        if (Math.random() > 0.7) {
            return "Success!";
        }
        throw new Exception("API temporarily unavailable");
    }

    private static void sendMessage(String msg) throws Exception {
        if (Math.random() > 0.6) {
            return;
        }
        throw new Exception("Network error");
    }

    private static byte[] downloadFile(String url) throws Exception {
        throw new Exception("Connection timeout");
    }
}
````

````java
/**
 * Advanced retry helper with builder pattern.
 */
public class RetryBuilder<T> {
    private final Callable<T> operation;
    private int maxAttempts = 3;
    private long delayMs = 1000;
    private boolean exponentialBackoff = false;
    private Class<? extends Exception> retryOn = Exception.class;

    public RetryBuilder(Callable<T> operation) {
        this.operation = operation;
    }

    public RetryBuilder<T> maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RetryBuilder<T> delay(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    public RetryBuilder<T> exponentialBackoff() {
        this.exponentialBackoff = true;
        return this;
    }

    public RetryBuilder<T> retryOn(Class<? extends Exception> exceptionType) {
        this.retryOn = exceptionType;
        return this;
    }

    public T execute() throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (!retryOn.isInstance(e)) {
                    throw e; // Don't retry on this exception type
                }
                lastException = e;
                if (attempt < maxAttempts) {
                    long wait = exponentialBackoff ? delayMs * (long) Math.pow(2, attempt - 1) : delayMs;
                    Thread.sleep(wait);
                }
            }
        }
        throw lastException;
    }
}
````

````java
String result = new RetryBuilder<>(() -> callAPI())
    .maxAttempts(5)
    .delay(500)
    .exponentialBackoff()
    .retryOn(IOException.class)
    .execute();
````

### retry · run 4 · uncle-bob-junior

````java
@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}

public class Retry {
    private Retry() {}
    
    public static <T> T execute(
        Callable<T> operation,
        int maxAttempts,
        long delayMillis
    ) throws Exception {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    public static void execute(
        ThrowingRunnable operation,
        int maxAttempts,
        long delayMillis
    ) throws Exception {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryTest {
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        int result = Retry.execute(() -> 42, 3, 10);
        assertEquals(42, result);
    }
    
    @Test
    void retriesAndSucceedsEventually() throws Exception {
        int[] calls = {0};
        
        int result = Retry.execute(
            () -> {
                calls[0]++;
                if (calls[0] < 3) throw new RuntimeException("fail");
                return 99;
            },
            5,
            10
        );
        
        assertEquals(99, result);
        assertEquals(3, calls[0]);
    }
    
    @Test
    void throwsLastExceptionAfterMaxAttempts() {
        assertThrows(RuntimeException.class, () ->
            Retry.execute(
                () -> { throw new RuntimeException("always fails"); },
                3,
                10
            )
        );
    }
    
    @Test
    void executesRunnableWithRetry() throws Exception {
        int[] calls = {0};
        
        Retry.execute(
            () -> {
                calls[0]++;
                if (calls[0] == 1) throw new IOException("fail");
            },
            3,
            10
        );
        
        assertEquals(2, calls[0]);
    }
}
````

````java
// Retry a function that returns a value
String response = Retry.execute(
    () -> httpClient.get("https://api.example.com/data"),
    3,
    1000
);

// Retry a void operation
Retry.execute(
    () -> database.save(record),
    5,
    2000
);
````

### retry · run 5 · baseline

````java
public class RetryHelper {
    
    /**
     * Retries an operation up to maxAttempts times with a fixed delay between attempts.
     * 
     * @param operation the operation to retry
     * @param maxAttempts maximum number of attempts (including the first)
     * @param delayMillis delay in milliseconds between attempts
     * @throws Exception if all attempts fail
     */
    public static void retry(VoidOperation operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.execute();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Retries an operation that returns a value up to maxAttempts times with a fixed delay between attempts.
     * 
     * @param operation the operation to retry
     * @param maxAttempts maximum number of attempts (including the first)
     * @param delayMillis delay in milliseconds between attempts
     * @return the result of the operation
     * @throws Exception if all attempts fail
     */
    public static <T> T retry(Operation<T> operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
````

````java
// Void operation - retries up to 3 times with 1 second delay
RetryHelper.retry(
    () -> someMethod(),
    3,
    1000
);

// Operation returning a value
String result = RetryHelper.retry(
    () -> fetchData(),
    5,
    2000
);

// With lambdas that throw checked exceptions
RetryHelper.retry(
    () -> {
        URL url = new URL("http://example.com");
        url.openConnection().getInputStream();
    },
    3,
    500
);
````

### retry · run 5 · uncle-bob-junior

````java
public final class Retry {
    private final int maxAttempts;
    private final long delayMs;
    
    public Retry(int maxAttempts, long delayMs) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public void execute(Operation operation) throws Exception {
        executeWithRetry(() -> {
            operation.execute();
            return null;
        });
    }
    
    public <T> T execute(OperationWithResult<T> operation) throws Exception {
        return executeWithRetry(operation::execute);
    }
    
    private <T> T executeWithRetry(RetryOperation<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts - 1) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }
    
    @FunctionalInterface
    private interface RetryOperation<T> {
        T execute() throws Exception;
    }
}

@FunctionalInterface
public interface Operation {
    void execute() throws Exception;
}

@FunctionalInterface
public interface OperationWithResult<T> {
    T execute() throws Exception;
}
````

````java
Retry retry = new Retry(3, 500); // 3 attempts, 500ms delay

// Void operation
retry.execute(() -> makeNetworkCall());

// Operation with return value
String result = retry.execute(() -> fetchDataFromApi());
````

````java
public class RetryTest {
    @Test
    public void successOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 100);
        boolean[] executed = {false};
        
        retry.execute(() -> executed[0] = true);
        
        assertTrue(executed[0]);
    }
    
    @Test
    public void successAfterRetry() throws Exception {
        Retry retry = new Retry(3, 50);
        int[] attempts = {0};
        
        retry.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 2) throw new RuntimeException("Fail");
        });
        
        assertEquals(2, attempts[0]);
    }
    
    @Test
    public void maxAttemptsExceeded() throws Exception {
        Retry retry = new Retry(3, 50);
        
        assertThrows(RuntimeException.class, () -> 
            retry.execute(() -> { throw new RuntimeException("Always fail"); })
        );
    }
    
    @Test
    public void waitsDelayBetweenAttempts() throws Exception {
        Retry retry = new Retry(3, 100);
        long[] times = new long[3];
        int[] attempts = {0};
        
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> {
                times[attempts[0]] = System.currentTimeMillis();
                attempts[0]++;
                throw new RuntimeException("Fail");
            })
        );
        
        assertTrue(times[1] - times[0] >= 100);
        assertTrue(times[2] - times[1] >= 100);
    }
    
    @Test
    public void returnsValueAfterRetry() throws Exception {
        Retry retry = new Retry(3, 50);
        int[] attempts = {0};
        
        String result = retry.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 2) throw new RuntimeException("Fail");
            return "Success";
        });
        
        assertEquals("Success", result);
    }
    
    @Test
    public void throwsOnInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, 100));
    }
}
````

### retry · run 6 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.perform();
            } catch (Exception e) {
                lastException = e;

                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw new RetryException(
            String.format("Operation failed after %d attempts", maxAttempts),
            lastException
        );
    }

    @FunctionalInterface
    public interface Operation<T> {
        T perform() throws Exception;
    }

    public static class RetryException extends Exception {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
// Example 1: Void operation
RetryHelper retryHelper = new RetryHelper(3, 1000);
try {
    retryHelper.execute(() -> {
        // Some operation that might fail
        connectToDatabase();
        return null;
    });
} catch (RetryHelper.RetryException e) {
    System.err.println(e.getMessage());
    e.getCause().printStackTrace();
}

// Example 2: Operation with return value
RetryHelper retryHelper = new RetryHelper(5, 500);
try {
    String result = retryHelper.execute(() -> fetchDataFromApi());
    System.out.println("Result: " + result);
} catch (Exception e) {
    System.err.println("Failed to fetch data");
}

// Example 3: Lambda with logic
RetryHelper retryHelper = new RetryHelper(3, 2000);
try {
    int value = retryHelper.execute(() -> {
        int data = riskyCalculation();
        if (data < 0) throw new IllegalStateException("Invalid result");
        return data;
    });
} catch (Exception e) {
    e.printStackTrace();
}
````

### retry · run 6 · uncle-bob-junior

````java
public class Retry {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public Retry(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public static Retry withDefaults() {
        return new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }
    
    public <T> T execute(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }
        
        throw lastException;
    }
    
    public void execute(CheckedRunnable operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }
        
        throw lastException;
    }
    
    private void sleep() throws InterruptedException {
        Thread.sleep(delayMillis);
    }
    
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}
````

````java
class RetryTest {
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 0);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    void succeedsAfterRetries() throws Exception {
        Retry retry = new Retry(3, 0);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Not yet");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void throwsAfterMaxAttempts() {
        Retry retry = new Retry(3, 0);
        
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> {
                throw new RuntimeException("Always fails");
            })
        );
    }
    
    @Test
    void executesRunnableWithRetry() throws Exception {
        Retry retry = new Retry(2, 0);
        AtomicInteger counter = new AtomicInteger(0);
        
        retry.execute(() -> counter.incrementAndGet());
        
        assertEquals(1, counter.get());
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, 1000));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(3, -1));
    }
}
````

````java
// With defaults: 3 attempts, 1 second delay
Retry retry = Retry.withDefaults();

// Operation returning a value
String data = retry.execute(() -> fetchFromAPI());

// Void operation
retry.execute(() -> writeToDatabase());

// Custom attempts and delay
new Retry(5, 2000).execute(() -> callUnreliableService());
````

### retry · run 7 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return operation.execute();
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMillis);
            }
        }
    }

    public void execute(VoidOperation operation) throws Exception {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                operation.execute();
                return;
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMillis);
            }
        }
    }

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }
}
````

````java
// For operations that return a value
RetryHelper retry = new RetryHelper(3, 1000);

String result = retry.execute(() -> {
    // Your operation here
    return fetchDataFromAPI();
});

// For operations with no return value
retry.execute(() -> {
    // Your operation here
    saveDataToDB();
});
````

### retry · run 7 · uncle-bob-junior

````java
import java.util.concurrent.Callable;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        
        int result = retry.execute(() -> 42);
        
        assertEquals(42, result);
    }
    
    @Test
    void successAfterRetry() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void throwsExceptionAfterMaxAttemptsExhausted() {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        String failureMessage = "Persistent failure";
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException(failureMessage);
            });
        });
        
        assertEquals(failureMessage, exception.getMessage());
        assertEquals(3, attempts.get());
    }
    
    @Test
    void waitsDelayBetweenAttempts() {
        RetryHelper retry = new RetryHelper(3, 100);
        long startTime = System.currentTimeMillis();
        
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Fail");
            });
        });
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= 200, "Expected ≥200ms for 2 delays");
    }
    
    @Test
    void supportsMultipleReturnTypes() throws Exception {
        RetryHelper retry = new RetryHelper(2, 50);
        
        String stringResult = retry.execute(() -> "hello");
        assertEquals("hello", stringResult);
        
        Integer intResult = retry.execute(() -> 123);
        assertEquals(123, intResult);
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(0, 100);
        });
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(3, -1);
        });
    }
}
````

````java
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1s delay

try {
    String result = retry.execute(() -> unreliableApiCall());
    System.out.println("Success: " + result);
} catch (Exception e) {
    System.err.println("Failed after all retries: " + e.getMessage());
}
````

### retry · run 8 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be >= 0");
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts && delayMillis > 0) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    public void executeVoid(VoidCallable operation) throws Exception {
        execute(() -> {
            operation.call();
            return null;
        });
    }
    
    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}
````

````java
// With return value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay
String result = retry.execute(() -> {
    return apiCall();
});

// Void operation
retry.executeVoid(() -> {
    database.connect();
});

// Lambda with network call
retry.execute(() -> {
    return httpClient.get("https://example.com");
});
````

### retry · run 8 · uncle-bob-junior

````java
import java.util.concurrent.Callable;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }
}
````

````java
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RetryHelperTest {
    private RetryHelper retryHelper;

    @Before
    public void setUp() {
        retryHelper = new RetryHelper(3, 10);
    }

    @Test
    public void execute_returns_result_on_first_attempt() throws Exception {
        int result = retryHelper.execute(() -> 42);
        assertEquals(42, result);
    }

    @Test
    public void execute_retries_until_success() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        int result = retryHelper.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("fail");
            }
            return 42;
        });
        assertEquals(3, attempts.get());
        assertEquals(42, result);
    }

    @Test(expected = RuntimeException.class)
    public void execute_throws_exception_after_max_attempts() throws Exception {
        retryHelper.execute(() -> {
            throw new RuntimeException("permanent failure");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejects_zero_attempts() {
        new RetryHelper(0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejects_negative_delay() {
        new RetryHelper(3, -1);
    }
}
````

````java
RetryHelper retryHelper = new RetryHelper(3, 1000);

// String result
String data = retryHelper.execute(() -> fetchDataFromAPI());

// Void operation
retryHelper.execute(() -> {
    sendMessage();
    return null;
});
````

### retry · run 9 · baseline

````java
import java.util.concurrent.Callable;

public class RetryHelper {
    
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delayMs) 
            throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    public static void retryVoid(Runnable operation, int maxAttempts, long delayMs) 
            throws Exception {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMs);
    }
}
````

````java
// With return value
String result = RetryHelper.retry(
    () -> someApiCall(),
    3,      // max attempts
    1000    // 1 second delay
);

// Void operation
RetryHelper.retryVoid(
    () -> writeToDatabase(),
    3,
    1000
);
````

````java
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;
    private final Predicate<Exception> retryOn;
    
    private RetryHelper(int maxAttempts, long delayMs, Predicate<Exception> retryOn) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
        this.retryOn = retryOn;
    }
    
    public static RetryBuilder builder() {
        return new RetryBuilder();
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (!retryOn.test(e)) {
                    throw e;
                }
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    public static class RetryBuilder {
        private int maxAttempts = 3;
        private long delayMs = 1000;
        private Predicate<Exception> retryOn = e -> true;
        
        public RetryBuilder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }
        
        public RetryBuilder delayMs(long delayMs) {
            this.delayMs = delayMs;
            return this;
        }
        
        public RetryBuilder retryOn(Predicate<Exception> retryOn) {
            this.retryOn = retryOn;
            return this;
        }
        
        public RetryHelper build() {
            return new RetryHelper(maxAttempts, delayMs, retryOn);
        }
    }
}
````

````java
RetryHelper helper = RetryHelper.builder()
    .maxAttempts(5)
    .delayMs(2000)
    .retryOn(e -> e instanceof IOException)  // only retry on IOException
    .build();

String result = helper.execute(() -> someApiCall());
````

### retry · run 9 · uncle-bob-junior

````java
import java.util.concurrent.Callable;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }
    
    public void execute(RunnableWithException operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }
    
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 10;
    
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    void retriesUntilSuccess() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attempts = {0};
        Integer result = retry.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 3) throw new IOException("Fail");
            return 99;
        });
        assertEquals(3, attempts[0]);
        assertEquals(99, result);
    }
    
    @Test
    void throwsAfterExhaustingRetries() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attempts = {0};
        assertThrows(IOException.class, () ->
            retry.execute(() -> {
                attempts[0]++;
                throw new IOException("Always fails");
            })
        );
        assertEquals(MAX_ATTEMPTS, attempts[0]);
    }
    
    @Test
    void voidOperationSucceeds() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] calls = {0};
        retry.execute(() -> calls[0]++);
        assertEquals(1, calls[0]);
    }
    
    @Test
    void voidOperationRetries() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attempts = {0};
        assertThrows(IOException.class, () ->
            retry.execute(() -> {
                attempts[0]++;
                throw new IOException("Always fails");
            })
        );
        assertEquals(MAX_ATTEMPTS, attempts[0]);
    }
    
    @Test
    void rejectsZeroAttempts() {
        assertThrows(IllegalArgumentException.class, () ->
            new RetryHelper(0, DELAY_MILLIS)
        );
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () ->
            new RetryHelper(MAX_ATTEMPTS, -1)
        );
    }
}
````

````java
// Retry a callable operation
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1s delay

String data = retry.execute(() -> 
    fetchDataFromApi("https://api.example.com/data")
);

// Retry a void operation
retry.execute(() -> 
    database.executeUpdate("UPDATE users SET active = true")
);

// Exception propagates after max attempts exhausted
try {
    retry.execute(() -> riskyOperation());
} catch (IOException e) {
    logger.error("Failed after retries", e);
}
````

### retry · run 10 · baseline

````java
import java.util.concurrent.Callable;

/**
 * Retry helper that executes an operation with exponential backoff.
 * Assumes RuntimeException or checked exceptions wrapped in RuntimeException.
 */
public class Retry {
    private final int maxAttempts;
    private final long delayMs;

    public Retry(int maxAttempts, long delayMs) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }

        throw lastException;
    }

    public void executeVoid(VoidCallable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.call();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }

        throw lastException;
    }

    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}
````

````java
public class Example {
    public static void main(String[] args) throws Exception {
        Retry retry = new Retry(3, 1000); // 3 attempts, 1 second delay

        // With return value
        String result = retry.execute(() -> {
            return fetchDataFromApi();
        });

        // Void operation
        retry.executeVoid(() -> {
            saveDataToDatabase();
        });
    }

    private static String fetchDataFromApi() {
        return "data";
    }

    private static void saveDataToDatabase() {
    }
}
````

### retry · run 10 · uncle-bob-junior

````java
import java.util.concurrent.Callable;

public class Retry {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public Retry(int maxAttempts, long delayMs) {
        validateParameters(maxAttempts, delayMs);
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    public void executeVoid(VoidOperation operation) throws Exception {
        execute(() -> {
            operation.run();
            return null;
        });
    }
    
    public static <T> T withRetry(Callable<T> operation) throws Exception {
        return new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS).execute(operation);
    }
    
    public static void withRetry(VoidOperation operation) throws Exception {
        new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS).executeVoid(operation);
    }
    
    private void validateParameters(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
    }
    
    @FunctionalInterface
    public interface VoidOperation {
        void run() throws Exception;
    }
}
````

````java
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class RetryTest {
    
    @Test
    public void successOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 10);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    public void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Retry retry = new Retry(3, 10);
        
        int result = retry.execute(() -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new RuntimeException("Attempt " + current + " failed");
            }
            return current;
        });
        
        assertEquals(3, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throwsAfterMaxAttempts() throws Exception {
        Retry retry = new Retry(2, 10);
        
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> { throw new RuntimeException("Always fails"); })
        );
    }
    
    @Test
    public void voidOperationRetries() throws Exception {
        AtomicInteger calls = new AtomicInteger(0);
        Retry retry = new Retry(2, 10);
        
        retry.executeVoid(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new RuntimeException("First call fails");
            }
        });
        
        assertEquals(2, calls.get());
    }
    
    @Test
    public void staticHelperWithRetry() throws Exception {
        int result = Retry.withRetry(() -> 99);
        assertEquals(99, result);
    }
    
    @Test
    public void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () ->
            new Retry(0, 100)
        );
    }
    
    @Test
    public void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () ->
            new Retry(3, -1)
        );
    }
}
````

## ratelimit

### ratelimit · run 1 · baseline

````java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clientRequests = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
        
        // Check if limit exceeded
        if (timestamps.size() >= maxRequests) {
            return false;
        }
        
        // Add current request
        timestamps.addLast(now);
        return true;
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.get(clientId);
        
        if (timestamps == null) {
            return maxRequests;
        }
        
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
                timestamps.removeFirst();
            }
            return Math.max(0, maxRequests - timestamps.size());
        }
    }
}
````

````java
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpHandler {
    private static final RateLimiter rateLimiter = new RateLimiter(
        10,                    // 10 requests
        60_000                 // per 60 seconds (1 minute)
    );
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", exchange -> {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                exchange.getResponseHeaders().set("X-RateLimit-Remaining", "0");
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, -1); // Too Many Requests
                exchange.close();
                return;
            }
            
            int remaining = rateLimiter.getRemainingRequests(clientIp);
            exchange.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
            exchange.getResponseHeaders().set("X-RateLimit-Limit", "10");
            
            String response = "Hello, " + clientIp + "!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
        
        server.start();
        System.out.println("Server started on port 8080");
    }
}
````

````java
public class TokenBucketRateLimiter {
    private final double tokensPerSecond;
    private final double maxTokens;
    private final ConcurrentHashMap<String, ClientBucket> buckets;
    
    private static class ClientBucket {
        double tokens;
        long lastRefillTime;
        
        ClientBucket(double maxTokens) {
            this.tokens = maxTokens;
            this.lastRefillTime = System.nanoTime();
        }
    }
    
    public TokenBucketRateLimiter(double tokensPerSecond, double maxTokens) {
        this.tokensPerSecond = tokensPerSecond;
        this.maxTokens = maxTokens;
        this.buckets = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId, int tokensRequested) {
        ClientBucket bucket = buckets.computeIfAbsent(clientId, 
            k -> new ClientBucket(maxTokens));
        
        synchronized (bucket) {
            long now = System.nanoTime();
            double elapsedSeconds = (now - bucket.lastRefillTime) / 1_000_000_000.0;
            bucket.tokens = Math.min(maxTokens, bucket.tokens + elapsedSeconds * tokensPerSecond);
            bucket.lastRefillTime = now;
            
            if (bucket.tokens >= tokensRequested) {
                bucket.tokens -= tokensRequested;
                return true;
            }
            return false;
        }
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private final ConcurrentHashMap<String, RequestWindow> windows = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        RequestWindow window = windows.compute(clientId, (key, existing) -> {
            if (existing == null || now - existing.startTime >= WINDOW_MILLIS) {
                return new RequestWindow(now, 1);
            }
            existing.increment();
            return existing;
        });
        
        boolean allowed = window.count <= MAX_REQUESTS;
        if (allowed) {
            cleanupOldWindows(now);
        }
        return allowed;
    }

    private void cleanupOldWindows(long now) {
        windows.entrySet().removeIf(entry ->
            now - entry.getValue().startTime >= WINDOW_MILLIS
        );
    }

    private static class RequestWindow {
        final long startTime;
        int count;

        RequestWindow(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }

        void increment() {
            count++;
        }
    }
}
````

````java
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter = new RateLimiter();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Maximum 10 requests per minute.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingServletFilter implements Filter {
    private final RateLimiter rateLimiter = new RateLimiter();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        String clientIp = getClientIp(httpReq);

        if (!rateLimiter.allowRequest(clientIp)) {
            httpRes.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpRes.setContentType("application/json");
            httpRes.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private final RateLimiter limiter = new RateLimiter();

    @Test
    public void allowsRequestsUnderLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.allowRequest("client1"));
        }
    }

    @Test
    public void blocksRequestsOverLimit() {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest("client1");
        }
        assertFalse(limiter.allowRequest("client1"));
    }

    @Test
    public void isolatesDifferentClients() {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest("client1");
        }
        assertTrue(limiter.allowRequest("client2"));
    }

    @Test
    public void resetsAfterTimeWindow() throws InterruptedException {
        limiter.allowRequest("client1");
        Thread.sleep(61000); // Wait just over 1 minute
        assertTrue(limiter.allowRequest("client1"));
    }
}
````

### ratelimit · run 2 · baseline

````java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    private final int maxRequests;
    private final Map<String, ConcurrentLinkedQueue<Long>> clientRequests;
    private final ConcurrentHashMap<String, Object> locks;

    public RateLimiter(int maxRequests) {
        this.maxRequests = maxRequests;
        this.clientRequests = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        
        // Get or create lock for this client
        Object lock = locks.computeIfAbsent(clientId, k -> new Object());
        
        synchronized (lock) {
            ConcurrentLinkedQueue<Long> requests = clientRequests
                .computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
            
            // Remove requests outside the window
            while (!requests.isEmpty() && requests.peek() < now - WINDOW_SIZE_MS) {
                requests.poll();
            }
            
            // Check if limit exceeded
            if (requests.size() < maxRequests) {
                requests.offer(now);
                return true;
            }
            return false;
        }
    }

    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Object lock = locks.computeIfAbsent(clientId, k -> new Object());
        
        synchronized (lock) {
            ConcurrentLinkedQueue<Long> requests = clientRequests
                .getOrDefault(clientId, new ConcurrentLinkedQueue<>());
            
            while (!requests.isEmpty() && requests.peek() < now - WINDOW_SIZE_MS) {
                requests.poll();
            }
            
            return Math.max(0, maxRequests - requests.size());
        }
    }
}
````

````java
import javax.servlet.*;
import javax.servlet.http.*;

public class RateLimitedHttpHandler extends HttpServlet {
    private static final RateLimiter rateLimiter = new RateLimiter(5); // 5 requests/min

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 5 requests per minute.");
            return;
        }

        // Add rate limit info headers
        int remaining = rateLimiter.getRemainingRequests(clientIp);
        response.setHeader("X-RateLimit-Limit", "5");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", 
            String.valueOf(System.currentTimeMillis() + 60_000));

        // Your actual handler logic here
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Request processed successfully");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````xml
<servlet>
    <servlet-name>rateLimitedHandler</servlet-name>
    <servlet-class>RateLimitedHttpHandler</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>rateLimitedHandler</servlet-name>
    <url-pattern>/api/*</url-pattern>
</servlet-mapping>
````

### ratelimit · run 2 · uncle-bob-junior

````java
public class TokenBucketRateLimiter {
    private static final double TOKENS_PER_SECOND = 1.0 / 6.0;  // 10/min = ~0.167/sec
    private static final double CAPACITY = 10.0;

    private double tokens;
    private long lastRefillTimeMs;

    public TokenBucketRateLimiter() {
        this.tokens = CAPACITY;
        this.lastRefillTimeMs = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedMs = now - lastRefillTimeMs;
        double tokensToAdd = (elapsedMs / 1000.0) * TOKENS_PER_SECOND;
        tokens = Math.min(CAPACITY, tokens + tokensToAdd);
        lastRefillTimeMs = now;
    }
}
````

````java
public class RateLimitingHandler {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private final ConcurrentHashMap<String, TokenBucketRateLimiter> limiters;

    public RateLimitingHandler() {
        this.limiters = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String clientId) {
        TokenBucketRateLimiter limiter = limiters.computeIfAbsent(
            clientId,
            key -> new TokenBucketRateLimiter()
        );
        return limiter.allowRequest();
    }

    public void handleHttpRequest(HttpExchange exchange) throws IOException {
        String clientId = exchange.getRemoteAddress().getAddress().getHostAddress();
        if (!isAllowed(clientId)) {
            exchange.sendResponseHeaders(429, -1);  // Too Many Requests
            return;
        }
        processRequest(exchange);
    }

    private void processRequest(HttpExchange exchange) throws IOException {
        String response = "OK";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
````

````java
public class RateLimiterTest {
    private TokenBucketRateLimiter limiter;

    @Before
    public void setUp() {
        limiter = new TokenBucketRateLimiter();
    }

    @Test
    public void allowsUpToCapacity() {
        for (int i = 0; i < 10; i++) {
            assertTrue("Request " + i + " should be allowed", limiter.allowRequest());
        }
    }

    @Test
    public void rejectsExcessiveRequests() {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest();
        }
        assertFalse("11th request should be rejected", limiter.allowRequest());
    }

    @Test
    public void refillsTokensOverTime() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest();
        }
        assertFalse("Immediate refill should not occur", limiter.allowRequest());
        
        Thread.sleep(6100);  // Wait ~6 seconds for 1 token
        assertTrue("Should allow after refill period", limiter.allowRequest());
    }

    @Test
    public void handlerTracksClientsIndependently() {
        RateLimitingHandler handler = new RateLimitingHandler();
        assertTrue("Client A allowed", handler.isAllowed("192.168.1.1"));
        assertTrue("Client B allowed", handler.isAllowed("192.168.1.2"));
    }
}
````

### ratelimit · run 3 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> clientTimestamps = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientTimestamps.computeIfAbsent(clientId, k -> new LinkedList<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        clientTimestamps.values().removeIf(timestamps -> {
            synchronized (timestamps) {
                return timestamps.isEmpty() || 
                       (timestamps.stream().allMatch(t -> t < now - windowMillis));
            }
        });
    }
}
````

````java
public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHandler() {
        // 5 requests per minute (60,000 ms)
        this.rateLimiter = new RateLimiter(5, 60_000);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().close();
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write("{\"status\":\"ok\"}".getBytes());
        exchange.close();
    }
}
````

````java
public class TokenBucketRateLimiter {
    private final int capacity;
    private final double refillRate; // tokens per millisecond
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacity, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillRate = (double) capacity / refillIntervalMillis;
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket());
        return bucket.tryConsume(1);
    }

    private class TokenBucket {
        private double tokens;
        private long lastRefillTime = System.currentTimeMillis();

        synchronized boolean tryConsume(int needed) {
            refill();
            if (tokens >= needed) {
                tokens -= needed;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            tokens = Math.min(capacity, tokens + (timePassed * refillRate));
            lastRefillTime = now;
        }
    }
}
````

````java
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter = new RateLimiter(5, 60_000);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", new RateLimitedHandler());
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Server running on port 8080");
    }
}
````

### ratelimit · run 3 · uncle-bob-junior

````java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    private final int maxRequests;
    private final Map<String, Queue<Long>> requestsByClient = new ConcurrentHashMap<>();
    
    public RateLimiter(int maxRequests) {
        this.maxRequests = maxRequests;
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = requestsByClient.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
        
        // Remove requests older than the window
        while (!timestamps.isEmpty() && now - timestamps.peek() >= WINDOW_MILLIS) {
            timestamps.poll();
        }
        
        if (timestamps.size() < maxRequests) {
            timestamps.offer(now);
            return true;
        }
        
        return false;
    }
}
````

````java
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class RateLimitedServlet extends HttpServlet {
    private static final int REQUESTS_PER_MINUTE = 10;
    private final RateLimiter rateLimiter = new RateLimiter(REQUESTS_PER_MINUTE);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("text/plain");
            response.getWriter().println("Rate limit exceeded: max " + REQUESTS_PER_MINUTE + " requests per minute");
            return;
        }
        
        // Your actual request handling here
        response.setContentType("text/plain");
        response.getWriter().println("Request successful");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @Before
    public void setUp() {
        rateLimiter = new RateLimiter(3); // 3 requests per window for testing
    }
    
    @Test
    public void allowsRequestsWithinLimit() {
        String clientId = "client-1";
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    public void deniesRequestsExceedingLimit() {
        String clientId = "client-1";
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
        
        assertFalse(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    public void isolatesRateLimitsPerClient() {
        assertTrue(rateLimiter.allowRequest("client-1"));
        assertTrue(rateLimiter.allowRequest("client-2"));
        assertTrue(rateLimiter.allowRequest("client-1"));
        
        assertTrue(rateLimiter.allowRequest("client-2"));
        assertTrue(rateLimiter.allowRequest("client-2"));
        
        assertFalse(rateLimiter.allowRequest("client-1")); // client-1 at limit
        assertFalse(rateLimiter.allowRequest("client-2")); // client-2 at limit
    }
    
    @Test
    public void handlesMultipleClientsConcurrently() throws InterruptedException {
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final String clientId = "client-" + i;
            threads[i] = new Thread(() -> {
                assertTrue(rateLimiter.allowRequest(clientId));
                assertTrue(rateLimiter.allowRequest(clientId));
                assertTrue(rateLimiter.allowRequest(clientId));
                assertFalse(rateLimiter.allowRequest(clientId));
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
````

### ratelimit · run 4 · baseline

````java
import java.util.concurrent.*;
import java.util.*;

public class RateLimiter {
    private static class ClientRateInfo {
        private final Queue<Long> requestTimes = new ConcurrentLinkedQueue<>();
        private final int maxRequests;
        private final long windowMillis;

        ClientRateInfo(int maxRequests, long windowMillis) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
        }

        boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            // Remove timestamps outside the window
            requestTimes.removeIf(time -> now - time > windowMillis);
            
            if (requestTimes.size() < maxRequests) {
                requestTimes.offer(now);
                return true;
            }
            return false;
        }
    }

    private final ConcurrentHashMap<String, ClientRateInfo> clientLimits;
    private final int maxRequests;
    private final long windowMillis;
    private final ScheduledExecutorService cleaner;

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clientLimits = new ConcurrentHashMap<>();
        
        // Clean up inactive clients every 5 minutes
        this.cleaner = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        cleaner.scheduleAtFixedRate(this::cleanupInactiveClients, 
            5, 5, TimeUnit.MINUTES);
    }

    public boolean allowRequest(String clientId) {
        return clientLimits
            .computeIfAbsent(clientId, k -> new ClientRateInfo(maxRequests, windowMillis))
            .allowRequest();
    }

    private void cleanupInactiveClients() {
        long now = System.currentTimeMillis();
        clientLimits.entrySet().removeIf(entry -> {
            ClientRateInfo info = entry.getValue();
            info.requestTimes.removeIf(time -> now - time > windowMillis);
            return info.requestTimes.isEmpty();
        });
    }

    public void shutdown() {
        cleaner.shutdown();
        try {
            if (!cleaner.awaitTermination(10, TimeUnit.SECONDS)) {
                cleaner.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleaner.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
````

````java
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class RateLimitedHttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler() {
        // 10 requests per minute (60,000 milliseconds)
        this.rateLimiter = new RateLimiter(10, TimeUnit.MINUTES.toMillis(1));
    }

    public HttpHandler createHandler(HttpHandler delegate) {
        return exchange -> {
            String clientIp = getClientIp(exchange);
            
            if (!rateLimiter.allowRequest(clientIp)) {
                sendRateLimitResponse(exchange);
                return;
            }
            
            delegate.handle(exchange);
        };
    }

    private String getClientIp(HttpExchange exchange) {
        String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getInetAddress().getHostAddress();
    }

    private void sendRateLimitResponse(HttpExchange exchange) throws IOException {
        String response = "Rate limit exceeded. Maximum 10 requests per minute.";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    public static void main(String[] args) throws IOException {
        RateLimitedHttpHandler rateLimitHandler = new RateLimitedHttpHandler();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Wrap your actual handler with rate limiting
        HttpHandler yourHandler = exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 7);
            exchange.getResponseBody().write("Success".getBytes());
            exchange.close();
        };
        
        server.createContext("/api", rateLimitHandler.createHandler(yourHandler));
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running on http://localhost:8080");
    }
}
````

### ratelimit · run 4 · uncle-bob-junior

````java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final long MINUTE_IN_MILLIS = 60_000;

    private final Map<String, Deque<Long>> requestTimestamps;
    private final int maxRequests;
    private final long windowMillis;

    public RateLimiter() {
        this(REQUESTS_PER_MINUTE, MINUTE_IN_MILLIS);
    }

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.requestTimestamps = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientId,
            k -> new ArrayDeque<>()
        );

        removeExpiredTimestamps(timestamps, now);

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }

        return false;
    }

    private void removeExpiredTimestamps(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler() {
        this.rateLimiter = new RateLimiter();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }

        handleRequest(exchange);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String response = "Success";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        String response = "Too many requests. Max 5 per minute.";
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {

    @Test
    public void allowsRequestsUnderLimit() {
        RateLimiter limiter = new RateLimiter(3, 1000);

        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
    }

    @Test
    public void deniesRequestsOverLimit() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));
    }

    @Test
    public void isolatesClientsFromEachOther() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));

        assertTrue(limiter.allowRequest("client2"));
        assertTrue(limiter.allowRequest("client2"));
    }

    @Test
    public void allowsRequestsAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, 100);

        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));

        Thread.sleep(150);
        assertTrue(limiter.allowRequest("client1"));
    }
}
````

### ratelimit · run 5 · baseline

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter {
    private static class TokenBucket {
        private final long maxTokens;
        private final long refillRate; // tokens per second
        private AtomicLong tokens;
        private volatile long lastRefillTime;

        TokenBucket(long maxTokens, long refillRate) {
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean allowRequest() {
            refillTokens();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refillTokens() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            long tokensToAdd = (timePassed * refillRate) / 1000;
            
            if (tokensToAdd > 0) {
                tokens.set(Math.min(maxTokens, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long maxTokens;
    private final long refillRate;

    public TokenBucketRateLimiter(long requestsPerMinute) {
        this.maxTokens = requestsPerMinute;
        this.refillRate = requestsPerMinute / 60; // convert to per-second
    }

    public boolean isAllowed(String clientId) {
        return buckets.computeIfAbsent(clientId, k -> 
            new TokenBucket(maxTokens, refillRate)
        ).allowRequest();
    }
}
````

````java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private TokenBucketRateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) {
        // 10 requests per minute per IP
        rateLimiter = new TokenBucketRateLimiter(10);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIp(httpRequest);
        
        if (!rateLimiter.isAllowed(clientIp)) {
            httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
            return;
        }
        
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }

    @Override
    public void destroy() {}
}
````

````java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitedHttpHandler() {
        this.rateLimiter = new TokenBucketRateLimiter(10);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.isAllowed(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            String response = "Rate limit exceeded. Max 10 requests per minute.";
            exchange.sendResponseHeaders(429, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }
        
        // Process the request normally
        String responseBody = "Request processed successfully";
        exchange.sendResponseHeaders(200, responseBody.getBytes().length);
        exchange.getResponseBody().write(responseBody.getBytes());
        exchange.close();
    }
}
````

### ratelimit · run 5 · uncle-bob-junior

````java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_DURATION_MILLIS = 60_000;
    
    private final int maxRequests;
    private final long windowDurationMillis;
    private final Map<String, ClientRequestHistory> clientHistory;
    
    public RateLimiter(int maxRequests, long windowDurationMillis) {
        this.maxRequests = maxRequests;
        this.windowDurationMillis = windowDurationMillis;
        this.clientHistory = new ConcurrentHashMap<>();
    }
    
    public RateLimiter() {
        this(REQUESTS_PER_MINUTE, WINDOW_DURATION_MILLIS);
    }
    
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        ClientRequestHistory history = clientHistory.computeIfAbsent(
            clientId,
            k -> new ClientRequestHistory()
        );
        return history.recordRequest(now);
    }
    
    private class ClientRequestHistory {
        private final Deque<Long> requestTimestamps = new ArrayDeque<>();
        
        synchronized boolean recordRequest(long now) {
            removeExpiredRequests(now);
            
            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.addLast(now);
                return true;
            }
            return false;
        }
        
        private void removeExpiredRequests(long now) {
            while (!requestTimestamps.isEmpty()) {
                long oldest = requestTimestamps.peekFirst();
                if (now - oldest >= windowDurationMillis) {
                    requestTimestamps.removeFirst();
                } else {
                    break;
                }
            }
        }
    }
}
````

````java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_DURATION_MILLIS = 60_000;
    
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) throws ServletException {
        rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW_DURATION_MILLIS);
    }
    
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = extractClientId(httpRequest);
        
        if (rateLimiter.isAllowed(clientId)) {
            chain.doFilter(request, response);
        } else {
            sendRateLimitExceeded(httpResponse);
        }
    }
    
    private String extractClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private void sendRateLimitExceeded(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Rate limit exceeded. Max " 
            + MAX_REQUESTS_PER_MINUTE + " requests per minute.\"}");
    }
    
    @Override
    public void destroy() {
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private static final int MAX_REQUESTS = 3;
    private static final long WINDOW_MILLIS = 60_000;
    
    @Test
    void allowsRequestsWithinLimit() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS);
        
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.isAllowed("client-1"));
        }
    }
    
    @Test
    void rejectsRequestsExceedingLimit() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS);
        String clientId = "client-1";
        
        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.isAllowed(clientId);
        }
        
        assertFalse(limiter.isAllowed(clientId));
    }
    
    @Test
    void isolatesLimitPerClient() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS);
        
        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.isAllowed("client-1");
        }
        
        assertTrue(limiter.isAllowed("client-2"));
    }
    
    @Test
    void allowsNewRequestsAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, 100);
        
        assertTrue(limiter.isAllowed("client-1"));
        assertFalse(limiter.isAllowed("client-1"));
        
        Thread.sleep(110);
        assertTrue(limiter.isAllowed("client-1"));
    }
}
````

### ratelimit · run 6 · baseline

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {
    private static class ClientState {
        AtomicLong requestCount = new AtomicLong(0);
        long windowStartTime = System.currentTimeMillis();
        
        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long windowAge = now - windowStartTime;
            
            if (windowAge >= 60_000) {
                requestCount.set(1);
                windowStartTime = now;
                return true;
            }
            
            long count = requestCount.incrementAndGet();
            return count <= maxRequestsPerMinute;
        }
    }
    
    private final int maxRequestsPerMinute;
    private final ConcurrentHashMap<String, ClientState> clients = new ConcurrentHashMap<>();
    
    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }
    
    public boolean allowRequest(String clientId) {
        return clients
            .computeIfAbsent(clientId, k -> new ClientState())
            .allowRequest();
    }
}
````

````java
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        rateLimiter = new RateLimiter(10); // 10 requests per minute
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = getClientIp(httpRequest);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    @Override
    public void destroy() {
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    
    public RateLimitedHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter(10); // 10 requests per minute
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().add("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().write("Rate limit exceeded".getBytes());
            exchange.close();
            return;
        }
        
        delegate.handle(exchange);
    }
}
````

````java
// For servlet filter, register in web.xml or with annotation:
@WebFilter("/*")
public class RateLimitingFilter implements Filter { ... }

// For HttpServer:
server.createContext("/api", 
    new RateLimitedHandler(new YourApiHandler()));
````

### ratelimit · run 6 · uncle-bob-junior

````java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(10, 60);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientIp(request);
        if (!rateLimiter.allowRequest(clientId)) {
            response.setStatus(429);
            response.getWriter().write("{\"error\":\"Too many requests. Max 10 requests per minute.\"}");
            response.setContentType("application/json");
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

class RateLimiter {
    private static class ClientBucket {
        long lastResetTime;
        int requestCount;

        ClientBucket() {
            this.lastResetTime = System.currentTimeMillis();
            this.requestCount = 0;
        }
    }

    private final int maxRequests;
    private final int windowSeconds;
    private final ConcurrentHashMap<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    RateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds * 1000;
    }

    boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientBucket bucket = buckets.compute(clientId, (id, existing) -> {
            if (existing == null) {
                return new ClientBucket();
            }
            if (now - existing.lastResetTime > windowSeconds) {
                existing.lastResetTime = now;
                existing.requestCount = 0;
            }
            return existing;
        });

        if (bucket.requestCount < maxRequests) {
            bucket.requestCount++;
            return true;
        }
        return false;
    }
}
````

````java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor);
    }
}
````

````java
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class TokenBucket {
        long lastRefillTime;
        double tokens;

        TokenBucket() {
            this.lastRefillTime = System.currentTimeMillis();
            this.tokens = 0;
        }
    }

    private final double tokensPerSecond;
    private final double maxTokens;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerMinute) {
        this.tokensPerSecond = requestsPerMinute / 60.0;
        this.maxTokens = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        TokenBucket bucket = buckets.compute(clientId, (id, existing) -> {
            if (existing == null) {
                return new TokenBucket();
            }
            double elapsedSeconds = (now - existing.lastRefillTime) / 1000.0;
            existing.tokens = Math.min(maxTokens, existing.tokens + elapsedSeconds * tokensPerSecond);
            existing.lastRefillTime = now;
            return existing;
        });

        if (bucket.tokens >= 1.0) {
            bucket.tokens -= 1.0;
            return true;
        }
        return false;
    }
}
````

````java
public class ApiHandler {
    private final RateLimiter rateLimiter = new RateLimiter(10);

    public void handleRequest(String clientIp, HttpRequest request, HttpResponse response) {
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.setBody("{\"error\":\"Too many requests\"}");
            return;
        }
        // Process request normally
    }
}
````

### ratelimit · run 7 · baseline

````java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class TokenBucket {
        private double tokens;
        private long lastRefillTime;
        private final double capacity;
        private final double refillRatePerSecond;

        TokenBucket(double capacity, double requestsPerMinute) {
            this.capacity = capacity;
            this.refillRatePerSecond = requestsPerMinute / 60.0;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(int tokensRequested) {
            refill();
            if (tokens >= tokensRequested) {
                tokens -= tokensRequested;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedMillis = now - lastRefillTime;
            double tokensToAdd = (elapsedMillis / 1000.0) * refillRatePerSecond;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final double requestsPerMinute;
    private final double capacity;

    public RateLimiter(double requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.capacity = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
            key -> new TokenBucket(capacity, requestsPerMinute));
        return bucket.tryConsume(1);
    }

    public void cleanup() {
        // Optionally remove old buckets after a timeout period
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> (now - entry.getValue().lastRefillTime) > 3600000);
    }
}
````

````java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(10); // 10 requests per minute

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429); // Too Many Requests
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Max 10 requests per minute.\"}");
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
````

````java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor);
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter(10);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().write("{\"error\": \"Rate limit exceeded\"}".getBytes());
            exchange.close();
            return;
        }
        
        // Handle normal request
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write("OK".getBytes());
        exchange.close();
    }
}
````

### ratelimit · run 7 · uncle-bob-junior

````java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    
    private final ConcurrentHashMap<String, Deque<Long>> requestTimes = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> times = requestTimes.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside the window
        while (!times.isEmpty() && times.peekFirst() < now - WINDOW_MILLIS) {
            times.pollFirst();
        }
        
        if (times.size() < MAX_REQUESTS) {
            times.addLast(now);
            return true;
        }
        return false;
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> times = requestTimes.getOrDefault(clientId, new ConcurrentLinkedDeque<>());
        
        while (!times.isEmpty() && times.peekFirst() < now - WINDOW_MILLIS) {
            times.pollFirst();
        }
        
        return Math.max(0, MAX_REQUESTS - times.size());
    }
}
````

````java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private final RateLimiter limiter = new RateLimiter();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientIp(httpRequest);
        
        if (!limiter.allowRequest(clientId)) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 5 requests per minute.");
            return;
        }
        
        httpResponse.addHeader("X-RateLimit-Remaining", String.valueOf(limiter.getRemainingRequests(clientId)));
        chain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    @Override
    public void init(FilterConfig config) { }
    
    @Override
    public void destroy() { }
}
````

````java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class RateLimitedController {
    private final RateLimiter limiter = new RateLimiter();
    
    @GetMapping("/api/data")
    public ResponseEntity<?> getData(jakarta.servlet.http.HttpServletRequest request) {
        String clientId = getClientIp(request);
        
        if (!limiter.allowRequest(clientId)) {
            return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body("Rate limit exceeded. Max 5 requests per minute.");
        }
        
        return ResponseEntity
            .ok()
            .header("X-RateLimit-Remaining", String.valueOf(limiter.getRemainingRequests(clientId)))
            .body(Map.of("message", "Success"));
    }
    
    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    
    @Test
    void allowsRequestsWithinLimit() {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest(clientId));
        }
    }
    
    @Test
    void blocksRequestsExceedingLimit() {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            limiter.allowRequest(clientId);
        }
        assertFalse(limiter.allowRequest(clientId));
    }
    
    @Test
    void tracksRemainingRequests() {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        assertEquals(5, limiter.getRemainingRequests(clientId));
        limiter.allowRequest(clientId);
        assertEquals(4, limiter.getRemainingRequests(clientId));
        limiter.allowRequest(clientId);
        assertEquals(3, limiter.getRemainingRequests(clientId));
    }
    
    @Test
    void isolatesClientLimits() {
        RateLimiter limiter = new RateLimiter();
        
        for (int i = 0; i < 5; i++) {
            limiter.allowRequest("client-1");
        }
        
        assertTrue(limiter.allowRequest("client-2"));
    }
    
    @Test
    void resetsAfterWindow() throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        limiter.allowRequest(clientId);
        assertEquals(4, limiter.getRemainingRequests(clientId));
        
        Thread.sleep(61_000); // Wait for window to expire
        
        assertEquals(5, limiter.getRemainingRequests(clientId));
    }
}
````

### ratelimit · run 8 · baseline

````java
import java.util.concurrent.*;
import java.util.*;

public class RateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    
    private static class ClientQuota {
        private final Queue<Long> requestTimestamps = new LinkedList<>();
        private final Object lock = new Object();
        
        boolean allowRequest() {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                
                // Remove timestamps older than the window
                requestTimestamps.removeIf(ts -> now - ts > WINDOW_SIZE_MS);
                
                // Check if limit exceeded
                if (requestTimestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                    return false;
                }
                
                requestTimestamps.add(now);
                return true;
            }
        }
    }
    
    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientIdentifier) {
        ClientQuota quota = quotas.computeIfAbsent(
            clientIdentifier, 
            k -> new ClientQuota()
        );
        return quota.allowRequest();
    }
    
    public void resetClient(String clientIdentifier) {
        quotas.remove(clientIdentifier);
    }
}
````

````java
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            // Return 429 Too Many Requests
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "Rate limit exceeded. Max 10 requests per minute.".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        // Handle the actual request
        String responseBody = "Hello, World!";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, responseBody.length());
        exchange.getResponseBody().write(responseBody.getBytes());
        exchange.close();
    }
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on port 8080");
    }
}
````

### ratelimit · run 8 · uncle-bob-junior

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientRateLimiter {
  private static final int REQUESTS_PER_MINUTE = 5;
  private static final long MINUTE_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

  private final ConcurrentHashMap<String, ClientRequestBucket> buckets;
  private final ReentrantReadWriteLock lock;
  private long lastCleanup;

  public ClientRateLimiter() {
    this.buckets = new ConcurrentHashMap<>();
    this.lock = new ReentrantReadWriteLock();
    this.lastCleanup = System.currentTimeMillis();
  }

  public boolean allowRequest(String clientId) {
    lock.readLock().lock();
    try {
      ClientRequestBucket bucket = buckets.get(clientId);
      long now = System.currentTimeMillis();

      if (bucket == null || bucket.hasExpired(now)) {
        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
          buckets.put(clientId, new ClientRequestBucket(now));
          maybeCleanup(now);
          return true;
        } finally {
          lock.readLock().lock();
          lock.writeLock().unlock();
        }
      }

      boolean allowed = bucket.tryConsumeRequest(now);
      if (allowed) {
        maybeCleanup(now);
      }
      return allowed;
    } finally {
      lock.readLock().unlock();
    }
  }

  private void maybeCleanup(long now) {
    if (now - lastCleanup > CLEANUP_INTERVAL_MILLIS) {
      buckets.entrySet().removeIf(e -> e.getValue().hasExpired(now));
      lastCleanup = now;
    }
  }

  private static class ClientRequestBucket {
    private final long windowStart;
    private int requestCount;

    ClientRequestBucket(long windowStart) {
      this.windowStart = windowStart;
      this.requestCount = 1;
    }

    boolean hasExpired(long now) {
      return now - windowStart > MINUTE_MILLIS;
    }

    boolean tryConsumeRequest(long now) {
      if (now - windowStart > MINUTE_MILLIS) {
        windowStart = now;
        requestCount = 1;
        return true;
      }
      if (requestCount < REQUESTS_PER_MINUTE) {
        requestCount++;
        return true;
      }
      return false;
    }
  }
}
````

````java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
  private final ClientRateLimiter rateLimiter;

  public RateLimitingFilter() {
    this.rateLimiter = new ClientRateLimiter();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String clientIp = getClientIp(request);

    if (!rateLimiter.allowRequest(clientIp)) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setStatus(429);
      httpResponse.setContentType("application/json");
      httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum 5 requests per minute.\"}");
      return;
    }

    chain.doFilter(request, response);
  }

  private String getClientIp(ServletRequest request) {
    String forwardedFor = request.getServletContext().getAttribute("X-Forwarded-For") != null
        ? (String) request.getServletContext().getAttribute("X-Forwarded-For")
        : null;

    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      return forwardedFor.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientRateLimiterTest {
  @Test
  void allowsFirstFiveRequests() {
    ClientRateLimiter limiter = new ClientRateLimiter();
    String clientId = "192.168.1.1";

    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allowRequest(clientId), "Request " + (i + 1) + " should be allowed");
    }
  }

  @Test
  void deniesRequestsAfterLimit() {
    ClientRateLimiter limiter = new ClientRateLimiter();
    String clientId = "192.168.1.1";

    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }

    assertFalse(limiter.allowRequest(clientId), "Sixth request should be denied");
    assertFalse(limiter.allowRequest(clientId), "Seventh request should be denied");
  }

  @Test
  void isolatesClientsIndependently() {
    ClientRateLimiter limiter = new ClientRateLimiter();

    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client-a");
    }

    assertTrue(limiter.allowRequest("client-b"), "Other client should not be affected");
  }

  @Test
  void resetsAfterMinute() throws InterruptedException {
    ClientRateLimiter limiter = new ClientRateLimiter();
    String clientId = "192.168.1.1";

    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }
    assertFalse(limiter.allowRequest(clientId));

    Thread.sleep(61_000);

    assertTrue(limiter.allowRequest(clientId), "Should allow request after window expires");
  }
}
````

### ratelimit · run 9 · baseline

````java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    private static final long CLEANUP_INTERVAL_MILLIS = 5 * 60_000; // 5 minutes
    
    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        // Periodic cleanup to prevent memory leaks
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MILLIS) {
            cleanup(now);
            lastCleanupTime = now;
        }
        
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new ArrayDeque<>());
        
        synchronized (timestamps) {
            // Remove timestamps outside the window
            while (!timestamps.isEmpty() && timestamps.getFirst() <= now - WINDOW_MILLIS) {
                timestamps.removeFirst();
            }
            
            // Check if limit exceeded
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            
            // Add current request timestamp
            timestamps.addLast(now);
            return true;
        }
    }
    
    private void cleanup(long now) {
        requestTimestamps.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                return timestamps.stream().allMatch(ts -> ts <= now - WINDOW_MILLIS);
            }
        });
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitingHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    
    public RateLimitingHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequests(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getInetSocketAddress().getAddress().getHostAddress();
    }
    
    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        String response = "Too Many Requests";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

// Example usage:
public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Wrap your handler with rate limiting
        server.createContext("/api", new RateLimitingHandler(exchange -> {
            String response = "Success";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }));
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }
}
````

### ratelimit · run 9 · uncle-bob-junior

````java
public class RateLimiter {
  private static final int MAX_REQUESTS = 10;
  private static final long WINDOW_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 5 * 60_000;

  private final Map<String, Queue<Long>> requestsByClient;
  private final int maxRequests;
  private final long windowSize;
  private final ReentrantReadWriteLock lock;
  private volatile long lastCleanup;

  public RateLimiter(int maxRequests, long windowMillis) {
    this.requestsByClient = new ConcurrentHashMap<>();
    this.maxRequests = maxRequests;
    this.windowSize = windowMillis;
    this.lock = new ReentrantReadWriteLock();
    this.lastCleanup = System.currentTimeMillis();
  }

  public RateLimiter() {
    this(MAX_REQUESTS, WINDOW_MILLIS);
  }

  public boolean allowRequest(String clientId) {
    long now = System.currentTimeMillis();
    
    lock.readLock().lock();
    try {
      Queue<Long> timestamps = requestsByClient.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
      
      while (!timestamps.isEmpty() && timestamps.peek() < now - windowSize) {
        timestamps.poll();
      }
      
      if (timestamps.size() < maxRequests) {
        timestamps.offer(now);
        return true;
      }
      return false;
    } finally {
      lock.readLock().unlock();
    }
  }

  private void cleanupOldClients(long now) {
    if (now - lastCleanup < CLEANUP_INTERVAL_MILLIS) {
      return;
    }
    
    lock.writeLock().lock();
    try {
      lastCleanup = now;
      requestsByClient.forEach((clientId, timestamps) -> {
        while (!timestamps.isEmpty() && timestamps.peek() < now - windowSize) {
          timestamps.poll();
        }
      });
      
      requestsByClient.entrySet().removeIf(e -> e.getValue().isEmpty());
    } finally {
      lock.writeLock().unlock();
    }
  }
}
````

````java
@Component
public class RateLimitingFilter implements Filter {
  private static final String RATE_LIMIT_EXCEEDED_MSG = "Rate limit exceeded: max 10 requests per minute";
  private static final int TOO_MANY_REQUESTS_STATUS = 429;

  private final RateLimiter rateLimiter;

  @Autowired
  public RateLimitingFilter(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String clientId = extractClientId(httpRequest);

    if (!rateLimiter.allowRequest(clientId)) {
      sendRateLimitResponse((HttpServletResponse) response);
      return;
    }

    chain.doFilter(request, response);
  }

  private String extractClientId(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
    response.setStatus(TOO_MANY_REQUESTS_STATUS);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + RATE_LIMIT_EXCEEDED_MSG + "\"}");
  }

  @Override
  public void init(FilterConfig config) {}

  @Override
  public void destroy() {}
}
````

````java
@Configuration
public class RateLimitingConfig {
  @Bean
  public RateLimiter rateLimiter() {
    return new RateLimiter(10, 60_000);
  }
}
````

````java
public class RateLimiterTest {
  private RateLimiter rateLimiter;

  @Before
  public void setUp() {
    rateLimiter = new RateLimiter(3, 1000);
  }

  @Test
  public void allowsRequestsUnderLimit() {
    for (int i = 0; i < 3; i++) {
      assertTrue(rateLimiter.allowRequest("client1"));
    }
  }

  @Test
  public void rejectsRequestsOverLimit() {
    for (int i = 0; i < 3; i++) {
      rateLimiter.allowRequest("client1");
    }
    assertFalse(rateLimiter.allowRequest("client1"));
  }

  @Test
  public void allowsAfterWindowExpires() throws InterruptedException {
    for (int i = 0; i < 3; i++) {
      rateLimiter.allowRequest("client1");
    }
    Thread.sleep(1100);
    assertTrue(rateLimiter.allowRequest("client1"));
  }

  @Test
  public void isolatesClientsIndependently() {
    for (int i = 0; i < 3; i++) {
      rateLimiter.allowRequest("client1");
    }
    assertTrue(rateLimiter.allowRequest("client2"));
  }

  @Test
  public void handlesHighConcurrency() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    AtomicInteger allowed = new AtomicInteger();
    
    for (int i = 0; i < 50; i++) {
      executor.submit(() -> {
        if (rateLimiter.allowRequest("concurrent")) {
          allowed.incrementAndGet();
        }
      });
    }
    
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
    
    assertEquals(3, allowed.get());
  }
}
````

### ratelimit · run 10 · baseline

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {
    private static class ClientQuota {
        AtomicLong tokens;
        long lastRefillTime;

        ClientQuota(long initialTokens) {
            this.tokens = new AtomicLong(initialTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }
    }

    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final long maxTokens;
    private final long refillIntervalMs;
    private final long tokensPerInterval;

    public RateLimiter(long requestsPerMinute) {
        this.maxTokens = requestsPerMinute;
        this.refillIntervalMs = 60_000; // 1 minute
        this.tokensPerInterval = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientQuota quota = quotas.computeIfAbsent(clientId, k -> new ClientQuota(maxTokens));

        synchronized (quota) {
            long timePassed = now - quota.lastRefillTime;
            if (timePassed >= refillIntervalMs) {
                quota.tokens.set(maxTokens);
                quota.lastRefillTime = now;
            }

            if (quota.tokens.get() > 0) {
                quota.tokens.decrementAndGet();
                return true;
            }
        }
        return false;
    }

    public void cleanupExpiredQuotas(long expirationTimeMs) {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> 
            now - entry.getValue().lastRefillTime > expirationTimeMs
        );
    }
}
````

````java
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(long requestsPerMinute) {
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        String clientId = getClientIp(request);

        if (!rateLimiter.allowRequest(clientId)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("X-RateLimit-Limit", "10");
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
````

````java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(10))
                .addPathPatterns("/**");
    }
}
````

````java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/api/test")
    public String test() {
        return "Request successful";
    }
}
````

### ratelimit · run 10 · uncle-bob-junior

````java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
  private static final long REFILL_INTERVAL_MS = 60_000; // 1 minute
  private static final int MAX_TOKENS = 5;
  
  private long lastRefillTime;
  private int availableTokens;
  
  public RateLimiter() {
    this.lastRefillTime = System.currentTimeMillis();
    this.availableTokens = MAX_TOKENS;
  }
  
  public synchronized boolean tryConsume() {
    refillTokens();
    if (availableTokens > 0) {
      availableTokens--;
      return true;
    }
    return false;
  }
  
  private void refillTokens() {
    long now = System.currentTimeMillis();
    long timeSinceRefill = now - lastRefillTime;
    
    if (timeSinceRefill >= REFILL_INTERVAL_MS) {
      availableTokens = MAX_TOKENS;
      lastRefillTime = now;
    }
  }
}
````

````java
import java.util.concurrent.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RateLimitingFilter implements Filter {
  private static final ConcurrentHashMap<String, RateLimiter> LIMITERS = 
    new ConcurrentHashMap<>();
  private static final long CLEANUP_INTERVAL_MS = 300_000; // 5 minutes
  private static final long LIMITER_EXPIRY_MS = 600_000; // 10 minutes
  private long lastCleanupTime = System.currentTimeMillis();
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, 
      FilterChain chain) throws ServletException, java.io.IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    String clientIp = getClientIp(httpRequest);
    RateLimiter limiter = LIMITERS.computeIfAbsent(clientIp, k -> new RateLimiter());
    
    if (limiter.tryConsume()) {
      chain.doFilter(request, response);
    } else {
      httpResponse.setStatus(429); // Too Many Requests
      httpResponse.getWriter().write("Rate limit exceeded");
    }
    
    cleanupExpiredLimiters();
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
  
  private void cleanupExpiredLimiters() {
    long now = System.currentTimeMillis();
    if (now - lastCleanupTime < CLEANUP_INTERVAL_MS) {
      return;
    }
    lastCleanupTime = now;
    
    LIMITERS.values().removeIf(limiter -> 
      now - limiter.getLastAccessTime() > LIMITER_EXPIRY_MS);
  }
  
  @Override
  public void init(FilterConfig config) {}
  
  @Override
  public void destroy() {}
}
````

````java
public class RateLimiter {
  private static final long REFILL_INTERVAL_MS = 60_000;
  private static final int MAX_TOKENS = 5;
  
  private long lastRefillTime;
  private int availableTokens;
  private long lastAccessTime;
  
  public RateLimiter() {
    this.lastRefillTime = System.currentTimeMillis();
    this.lastAccessTime = this.lastRefillTime;
    this.availableTokens = MAX_TOKENS;
  }
  
  public synchronized boolean tryConsume() {
    lastAccessTime = System.currentTimeMillis();
    refillTokens();
    if (availableTokens > 0) {
      availableTokens--;
      return true;
    }
    return false;
  }
  
  public long getLastAccessTime() {
    return lastAccessTime;
  }
  
  private void refillTokens() {
    long now = System.currentTimeMillis();
    long timeSinceRefill = now - lastRefillTime;
    
    if (timeSinceRefill >= REFILL_INTERVAL_MS) {
      availableTokens = MAX_TOKENS;
      lastRefillTime = now;
    }
  }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
  
  @Test
  public void allowsFirstFiveRequests() {
    RateLimiter limiter = new RateLimiter();
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.tryConsume(), "Request " + i + " should be allowed");
    }
  }
  
  @Test
  public void rejectsSixthRequest() {
    RateLimiter limiter = new RateLimiter();
    for (int i = 0; i < 5; i++) {
      limiter.tryConsume();
    }
    assertFalse(limiter.tryConsume(), "Sixth request should be rejected");
  }
  
  @Test
  public void refillsAfterWindow() throws InterruptedException {
    RateLimiter limiter = new RateLimiter();
    for (int i = 0; i < 5; i++) {
      limiter.tryConsume();
    }
    assertFalse(limiter.tryConsume());
    
    Thread.sleep(60_100);
    assertTrue(limiter.tryConsume(), "Should allow request after window refills");
  }
}
````

````java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RateLimitingInterceptor());
  }
}

public class RateLimitingInterceptor implements HandlerInterceptor {
  private static final ConcurrentHashMap<String, RateLimiter> LIMITERS = 
    new ConcurrentHashMap<>();
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
      Object handler) throws Exception {
    String clientIp = getClientIp(request);
    RateLimiter limiter = LIMITERS.computeIfAbsent(clientIp, k -> new RateLimiter());
    
    if (!limiter.tryConsume()) {
      response.setStatus(429);
      response.getWriter().write("Rate limit exceeded");
      return false;
    }
    return true;
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return (forwarded != null && !forwarded.isEmpty()) 
      ? forwarded.split(",")[0].trim() 
      : request.getRemoteAddr();
  }
}
````

## order

### order · run 1 · baseline

````java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final double price;
    private final int quantity;

    public LineItem(String description, double price, int quantity) {
        this.description = Objects.requireNonNull(description);
        this.price = price;
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return price * quantity;
    }
}

class Order {
    private final List<LineItem> lineItems;

    public Order() {
        this.lineItems = new ArrayList<>();
    }

    public void addLineItem(LineItem item) {
        if (item.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        lineItems.add(item);
    }

    public String generateReceipt() {
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Order has no line items");
        }

        double subtotal = calculateSubtotal();
        double discount = subtotal > 100 ? subtotal * 0.10 : 0;
        double discountedSubtotal = subtotal - discount;
        double vat = discountedSubtotal * 0.21;
        double total = discountedSubtotal + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-25s %8.2f x %2d = %8.2f EUR\n",
                    item.getDescription(), item.getPrice(), item.getQuantity(), item.getTotal()));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:                       %8.2f EUR\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):               -%8.2f EUR\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:        %8.2f EUR\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):                      %8.2f EUR\n", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("Total:                          %8.2f EUR\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }

    private double calculateSubtotal() {
        return lineItems.stream()
                .mapToDouble(LineItem::getTotal)
                .sum();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Apples", 2.50, 10));
        order.addLineItem(new LineItem("Bread", 1.80, 5));
        order.addLineItem(new LineItem("Milk", 3.20, 3));
        order.addLineItem(new LineItem("Cheese", 8.50, 2));

        System.out.println(order.generateReceipt());
    }
}
````

### order · run 1 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private final String description;
    private final BigDecimal pricePerUnit;
    private final int quantity;

    public LineItem(String description, BigDecimal pricePerUnit, int quantity) {
        validate(description, pricePerUnit, quantity);
        this.description = description;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    private void validate(String description, BigDecimal pricePerUnit, int quantity) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    private final List<LineItem> lineItems = new ArrayList<>();

    public void addLineItem(LineItem item) {
        lineItems.add(item);
    }

    public String generateReceipt() {
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Cannot generate receipt for empty order");
        }

        BigDecimal preTaxTotal = calculatePreTaxTotal();
        BigDecimal discount = calculateDiscount(preTaxTotal);
        BigDecimal discountedTotal = preTaxTotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedTotal);
        BigDecimal total = discountedTotal.add(vat);

        return formatReceipt(preTaxTotal, discount, vat, total);
    }

    private BigDecimal calculatePreTaxTotal() {
        return lineItems.stream()
            .map(LineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal preTaxTotal) {
        if (preTaxTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return preTaxTotal.multiply(DISCOUNT_RATE);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private String formatReceipt(BigDecimal preTaxTotal, BigDecimal discount, 
                                 BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        appendLineItems(receipt);
        receipt.append(String.format("Pre-tax total:  €%.2f\n", preTaxTotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%): -€%.2f\n", discount));
        }

        receipt.append(String.format("VAT (21%%):     €%.2f\n", vat));
        receipt.append("-------------------\n");
        receipt.append(String.format("Total:          €%.2f\n", total));

        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : lineItems) {
            receipt.append(String.format("%s x%d: €%.2f\n", 
                item.getDescription(), 
                item.getQuantity(), 
                item.getSubtotal()));
        }
    }
}
````

````java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    public void lineItemRejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("", new BigDecimal("10"), 1));
    }

    @Test
    public void lineItemRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", new BigDecimal("-5"), 1));
    }

    @Test
    public void lineItemRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", new BigDecimal("10"), 0));
    }

    @Test
    public void lineItemComputesSubtotal() {
        LineItem item = new LineItem("Widget", new BigDecimal("25.00"), 4);
        assertEquals(new BigDecimal("100.00"), item.getSubtotal());
    }

    @Test
    public void orderWithoutDiscountIncludesVatOnly() {
        Order order = new Order();
        order.addLineItem(new LineItem("Item A", new BigDecimal("30.00"), 1));
        order.addLineItem(new LineItem("Item B", new BigDecimal("40.00"), 1));

        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Pre-tax total:  €70.00"));
        assertTrue(receipt.contains("VAT (21%):     €14.70"));
        assertTrue(receipt.contains("Total:          €84.70"));
        assertFalse(receipt.contains("Discount"));
    }

    @Test
    public void orderExceedingThresholdAppliesDiscount() {
        Order order = new Order();
        order.addLineItem(new LineItem("Item A", new BigDecimal("60.00"), 1));
        order.addLineItem(new LineItem("Item B", new BigDecimal("50.00"), 1));

        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Pre-tax total:  €110.00"));
        assertTrue(receipt.contains("Discount (10%): -€11.00"));
        assertTrue(receipt.contains("VAT (21%):     €20.79"));
        assertTrue(receipt.contains("Total:          €119.79"));
    }

    @Test
    public void emptyOrderThrows() {
        Order order = new Order();
        assertThrows(IllegalStateException.class, order::generateReceipt);
    }
}
````

### order · run 2 · baseline

````java
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    
    static class LineItem {
        String product;
        int quantity;
        double unitPrice;
        
        public LineItem(String product, int quantity, double unitPrice) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public double getTotal() {
            return quantity * unitPrice;
        }
    }
    
    static class Order {
        private List<LineItem> items;
        private static final double VAT_RATE = 0.21;
        private static final double DISCOUNT_RATE = 0.10;
        private static final double DISCOUNT_THRESHOLD = 100.0;
        
        public Order(List<LineItem> items) {
            this.items = items;
        }
        
        // Validate all line items
        public void validate() {
            for (LineItem item : items) {
                if (item.quantity <= 0 || item.unitPrice < 0) {
                    throw new IllegalArgumentException(
                        "Invalid item: " + item.product + " (qty: " + item.quantity + 
                        ", price: €" + item.unitPrice + ")");
                }
            }
        }
        
        // Calculate subtotal before VAT
        private double calculateSubtotal() {
            double sum = 0;
            for (LineItem item : items) {
                sum += item.getTotal();
            }
            return sum;
        }
        
        // Generate receipt with totals
        public String generateReceipt() {
            validate();
            
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== RECEIPT ===\n");
            
            // Itemize products
            for (LineItem item : items) {
                double lineTotal = item.getTotal();
                receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n", 
                    item.product, item.quantity, item.unitPrice, lineTotal));
            }
            
            // Calculate amounts
            double subtotal = calculateSubtotal();
            double discount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
            double subtotalAfterDiscount = subtotal - discount;
            double vat = subtotalAfterDiscount * VAT_RATE;
            double total = subtotalAfterDiscount + vat;
            
            receipt.append("---\n");
            receipt.append(String.format("Subtotal:    €%.2f\n", subtotal));
            
            if (discount > 0) {
                receipt.append(String.format("Discount:    -€%.2f\n", discount));
            }
            
            receipt.append(String.format("Subtotal:    €%.2f\n", subtotalAfterDiscount));
            receipt.append(String.format("VAT (21%%):   €%.2f\n", vat));
            receipt.append(String.format("TOTAL:       €%.2f\n", total));
            
            return receipt.toString();
        }
    }
    
    // Example usage
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Laptop", 1, 75.00));
        items.add(new LineItem("Mouse", 2, 15.00));
        items.add(new LineItem("Keyboard", 1, 25.00));
        
        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
````

### order · run 2 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LineItem {
    private final String name;
    private final int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String name, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    String format() {
        return String.format("%s x%d @ €%.2f = €%.2f", 
            name, quantity, unitPrice, lineTotal());
    }
}

public class OrderProcessor {
    private static final int VAT_RATE = 21;
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("10");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static String processOrder(List<LineItem> items) {
        validateItems(items);
        
        BigDecimal subtotal = computeSubtotal(items);
        BigDecimal discountAmount = computeDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        BigDecimal vat = computeVat(afterDiscount);
        BigDecimal total = afterDiscount.add(vat);
        
        return buildReceipt(items, subtotal, discountAmount, afterDiscount, vat, total);
    }

    private static void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    private static BigDecimal computeSubtotal(List<LineItem> items) {
        return items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, ROUNDING);
    }

    private static BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .divide(new BigDecimal("100"), 2, ROUNDING);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal computeVat(BigDecimal amount) {
        return amount.multiply(new BigDecimal(VAT_RATE))
            .divide(new BigDecimal("100"), 2, ROUNDING);
    }

    private static String buildReceipt(List<LineItem> items, BigDecimal subtotal,
            BigDecimal discount, BigDecimal afterDiscount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== ORDER RECEIPT =====\n");
        
        for (LineItem item : items) {
            receipt.append(item.format()).append("\n");
        }
        
        receipt.append("\nSubtotal:    €").append(subtotal).append("\n");
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append("Discount:   -€").append(discount).append("\n");
            receipt.append("After disc:   €").append(afterDiscount).append("\n");
        }
        
        receipt.append("VAT (21%):    €").append(vat).append("\n");
        receipt.append("TOTAL:        €").append(total).append("\n");
        
        return receipt.toString();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorTest {
    @Test
    void processesOrderWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Milk", 2, new BigDecimal("3.50")),
            new LineItem("Bread", 1, new BigDecimal("2.00"))
        );
        
        String receipt = OrderProcessor.processOrder(items);
        
        assertTrue(receipt.contains("Milk x2 @ €3.50 = €7.00"));
        assertTrue(receipt.contains("Bread x1 @ €2.00 = €2.00"));
        assertTrue(receipt.contains("Subtotal:    €9.00"));
        assertTrue(receipt.contains("VAT (21%):    €1.89"));
        assertTrue(receipt.contains("TOTAL:        €10.89"));
        assertFalse(receipt.contains("Discount"));
    }

    @Test
    void appliesDiscountWhenSubtotalExceeds100() {
        List<LineItem> items = List.of(
            new LineItem("Laptop", 1, new BigDecimal("150.00"))
        );
        
        String receipt = OrderProcessor.processOrder(items);
        
        assertTrue(receipt.contains("Subtotal:    €150.00"));
        assertTrue(receipt.contains("Discount:   -€15.00"));
        assertTrue(receipt.contains("After disc:   €135.00"));
        assertTrue(receipt.contains("VAT (21%):    €28.35"));
        assertTrue(receipt.contains("TOTAL:        €163.35"));
    }

    @Test
    void throwsOnInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 0, new BigDecimal("10.00")));
    }

    @Test
    void throwsOnInvalidPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 1, BigDecimal.ZERO));
    }

    @Test
    void throwsOnEmptyOrder() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderProcessor.processOrder(List.of()));
    }
}
````

### order · run 3 · baseline

````java
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class LineItem {
    private String description;
    private int quantity;
    private double unitPrice;

    public LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public boolean isValid() {
        return quantity > 0 && unitPrice > 0;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return quantity * unitPrice;
    }
}

class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public Order(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public boolean validateLineItems() {
        if (lineItems == null || lineItems.isEmpty()) {
            return false;
        }
        return lineItems.stream().allMatch(LineItem::isValid);
    }

    public double calculatePreVatTotal() {
        return lineItems.stream()
            .mapToDouble(LineItem::getLineTotal)
            .sum();
    }

    public double calculateDiscount(double preVatTotal) {
        return preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0.0;
    }

    public double calculateSubtotal(double preVatTotal, double discount) {
        return preVatTotal - discount;
    }

    public double calculateVat(double subtotal) {
        return subtotal * VAT_RATE;
    }

    public double calculateTotal(double preVatTotal) {
        double discount = calculateDiscount(preVatTotal);
        double subtotal = calculateSubtotal(preVatTotal, discount);
        double vat = calculateVat(subtotal);
        return subtotal + vat;
    }

    public String generateReceipt() {
        if (!validateLineItems()) {
            return "ERROR: Invalid order - check line items";
        }

        double preVatTotal = calculatePreVatTotal();
        double discount = calculateDiscount(preVatTotal);
        double subtotal = calculateSubtotal(preVatTotal, discount);
        double vat = calculateVat(subtotal);
        double total = subtotal + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("=".repeat(50)).append("\n");
        receipt.append("RECEIPT\n");
        receipt.append("=".repeat(50)).append("\n");
        receipt.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n\n");

        receipt.append(String.format("%-30s %10s %10s\n", "Item", "Qty", "Total"));
        receipt.append("-".repeat(50)).append("\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("%-30s %10d €%9.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getLineTotal()));
        }

        receipt.append("-".repeat(50)).append("\n");
        receipt.append(String.format("%-40s €%9.2f\n", "Pre-VAT Total:", preVatTotal));

        if (discount > 0) {
            receipt.append(String.format("%-40s €%9.2f\n", "Discount (10%):", -discount));
        }

        receipt.append(String.format("%-40s €%9.2f\n", "Subtotal:", subtotal));
        receipt.append(String.format("%-40s €%9.2f\n", "VAT (21%):", vat));
        receipt.append("=".repeat(50)).append("\n");
        receipt.append(String.format("%-40s €%9.2f\n", "TOTAL:", total));
        receipt.append("=".repeat(50)).append("\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, 3.50));
        items.add(new LineItem("Sandwich", 1, 8.75));
        items.add(new LineItem("Pastry", 3, 4.25));
        items.add(new LineItem("Juice", 1, 3.00));

        Order order = new Order(items);
        System.out.println(order.generateReceipt());

        System.out.println("\n--- Order Summary ---");
        double preVatTotal = order.calculatePreVatTotal();
        double discount = order.calculateDiscount(preVatTotal);
        double total = order.calculateTotal(preVatTotal);
        System.out.printf("Pre-VAT Total: €%.2f\n", preVatTotal);
        System.out.printf("Discount Applied: €%.2f\n", discount);
        System.out.printf("Final Total (with VAT): €%.2f\n", total);
    }
}
````

### order · run 3 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description);
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
    }

    String description() {
        return description;
    }

    int quantity() {
        return quantity;
    }

    BigDecimal unitPrice() {
        return unitPrice;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    private final List<LineItem> items;

    Order(List<LineItem> items) {
        this.items = new ArrayList<>(items);
        validateItems();
    }

    private void validateItems() {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            validateLineItem(item);
        }
    }

    private void validateLineItem(LineItem item) {
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + item.description());
        }
        if (item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive: " + item.description());
        }
        if (item.description().isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
    }

    BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal discountAmount() {
        BigDecimal subtotal = subtotal();
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    BigDecimal discountedSubtotal() {
        return subtotal().subtract(discountAmount());
    }

    BigDecimal vat() {
        return discountedSubtotal().multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal total() {
        return discountedSubtotal().add(vat()).setScale(2, RoundingMode.HALF_UP);
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.description(),
                item.quantity(),
                item.unitPrice(),
                item.lineTotal()));
        }

        sb.append("\n");
        sb.append(String.format("Subtotal: €%.2f\n", subtotal()));
        
        if (discountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
        }
        
        sb.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal()));
        sb.append(String.format("VAT (21%%): €%.2f\n", vat()));
        sb.append(String.format("Total: €%.2f\n", total()));
        
        return sb.toString();
    }
}

class OrderTest {
    static void testValidation() {
        try {
            new Order(new ArrayList<>());
            throw new AssertionError("Should reject empty order");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("at least one item");
        }

        try {
            List<LineItem> items = new ArrayList<>();
            items.add(new LineItem("Widget", 0, new BigDecimal("10.00")));
            new Order(items);
            throw new AssertionError("Should reject zero quantity");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Quantity must be positive");
        }

        try {
            List<LineItem> items = new ArrayList<>();
            items.add(new LineItem("Widget", 1, new BigDecimal("-10.00")));
            new Order(items);
            throw new AssertionError("Should reject negative price");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Unit price must be positive");
        }
    }

    static void testCalculationsNoDiscount() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, new BigDecimal("3.50")));
        items.add(new LineItem("Cake", 1, new BigDecimal("5.00")));
        
        Order order = new Order(items);
        
        assert order.subtotal().equals(new BigDecimal("12.00"));
        assert order.discountAmount().equals(BigDecimal.ZERO);
        assert order.vat().compareTo(new BigDecimal("2.52")) == 0;
        assert order.total().compareTo(new BigDecimal("14.52")) == 0;
    }

    static void testCalculationsWithDiscount() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Expensive Widget", 1, new BigDecimal("150.00")));
        
        Order order = new Order(items);
        
        assert order.subtotal().compareTo(new BigDecimal("150.00")) == 0;
        assert order.discountAmount().compareTo(new BigDecimal("15.00")) == 0;
        assert order.discountedSubtotal().compareTo(new BigDecimal("135.00")) == 0;
        assert order.vat().compareTo(new BigDecimal("28.35")) == 0;
        assert order.total().compareTo(new BigDecimal("163.35")) == 0;
    }

    static void runAll() {
        testValidation();
        testCalculationsNoDiscount();
        testCalculationsWithDiscount();
        System.out.println("All tests passed!");
    }
}

class Main {
    public static void main(String[] args) {
        OrderTest.runAll();
        
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, new BigDecimal("3.50")));
        items.add(new LineItem("Croissant", 3, new BigDecimal("2.75")));
        items.add(new LineItem("Sandwich", 1, new BigDecimal("8.50")));
        
        Order order = new Order(items);
        System.out.println(order.receipt());
    }
}
````

### order · run 4 · baseline

````java
public class OrderProcessor {
    
    static class OrderItem {
        String productName;
        int quantity;
        double unitPrice;
        
        OrderItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        double getLineTotal() {
            return quantity * unitPrice;
        }
    }
    
    static class Order {
        private final java.util.List<OrderItem> items = new java.util.ArrayList<>();
        private static final double VAT_RATE = 0.21;
        private static final double DISCOUNT_RATE = 0.10;
        private static final double DISCOUNT_THRESHOLD = 100.0;
        
        void addItem(OrderItem item) {
            validateItem(item);
            items.add(item);
        }
        
        private void validateItem(OrderItem item) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.quantity);
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative: " + item.unitPrice);
            }
            if (item.productName == null || item.productName.trim().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
        }
        
        double getSubtotal() {
            return items.stream().mapToDouble(OrderItem::getLineTotal).sum();
        }
        
        double getDiscount() {
            double subtotal = getSubtotal();
            return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        }
        
        double getDiscountedSubtotal() {
            return getSubtotal() - getDiscount();
        }
        
        double getVAT() {
            return getDiscountedSubtotal() * VAT_RATE;
        }
        
        double getTotal() {
            return getDiscountedSubtotal() + getVAT();
        }
        
        String getReceipt() {
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== ORDER RECEIPT ===\n");
            receipt.append(String.format("%-30s %10s %10s %12s\n", "Product", "Qty", "Unit Price", "Line Total"));
            receipt.append("-".repeat(62)).append("\n");
            
            for (OrderItem item : items) {
                receipt.append(String.format("%-30s %10d €%9.2f €%11.2f\n", 
                    item.productName, 
                    item.quantity, 
                    item.unitPrice, 
                    item.getLineTotal()));
            }
            
            receipt.append("-".repeat(62)).append("\n");
            receipt.append(String.format("%-52s €%11.2f\n", "Subtotal:", getSubtotal()));
            
            double discount = getDiscount();
            if (discount > 0) {
                receipt.append(String.format("%-52s €%11.2f\n", "Discount (10%):", -discount));
            }
            
            receipt.append(String.format("%-52s €%11.2f\n", "VAT (21%):", getVAT()));
            receipt.append("=".repeat(62)).append("\n");
            receipt.append(String.format("%-52s €%11.2f\n", "TOTAL:", getTotal()));
            
            return receipt.toString();
        }
    }
    
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem(new OrderItem("Laptop", 1, 65.00));
        order.addItem(new OrderItem("Mouse", 2, 15.00));
        order.addItem(new OrderItem("Keyboard", 1, 45.00));
        
        System.out.println(order.getReceipt());
        System.out.println("Final Total: €" + String.format("%.2f", order.getTotal()));
    }
}
````

````
Subtotal: €125.00
Discount (10%): €-12.50
VAT (21%): €23.54
TOTAL: €136.04
````

### order · run 4 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

final class LineItem {
    final String description;
    final int quantity;
    final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

final class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    private final List<LineItem> items;

    Order(List<LineItem> items) {
        if (items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
        this.items = new ArrayList<>(items);
    }

    BigDecimal preVatTotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    boolean isDiscountEligible() {
        return preVatTotal().compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    BigDecimal applyDiscount(BigDecimal amount) {
        if (!isDiscountEligible()) return amount;
        return amount.multiply(BigDecimal.ONE.subtract(DISCOUNT_RATE));
    }

    BigDecimal discountedSubtotal() {
        return applyDiscount(preVatTotal());
    }

    BigDecimal vat() {
        return discountedSubtotal().multiply(VAT_RATE)
            .setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal total() {
        return discountedSubtotal().add(vat())
            .setScale(2, RoundingMode.HALF_UP);
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.description, item.quantity, item.unitPrice, item.subtotal()));
        }
        
        sb.append("\nSubtotal: €").append(String.format("%.2f", preVatTotal())).append("\n");
        
        if (isDiscountEligible()) {
            BigDecimal discountAmount = preVatTotal()
                .multiply(DISCOUNT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            sb.append("Discount (10%): -€").append(String.format("%.2f", discountAmount)).append("\n");
        }
        
        sb.append("Subtotal after discount: €").append(String.format("%.2f", discountedSubtotal())).append("\n");
        sb.append("VAT (21%): €").append(String.format("%.2f", vat())).append("\n");
        sb.append("TOTAL: €").append(String.format("%.2f", total())).append("\n");
        
        return sb.toString();
    }
}

final class OrderProcessor {
    static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Widget", 10, new BigDecimal("5.50")),
            new LineItem("Gadget", 3, new BigDecimal("20.00")),
            new LineItem("Doohickey", 5, new BigDecimal("8.75"))
        );

        Order order = new Order(items);
        System.out.println(order.receipt());
    }
}
````

### order · run 5 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LineItem {
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    public LineItem(String productName, int quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    private List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = items;
    }

    public void validate() throws IllegalArgumentException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                    "Quantity must be positive for: " + item.getProductName());
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                    "Price cannot be negative for: " + item.getProductName());
            }
        }
    }

    public String generateReceipt() {
        validate();

        BigDecimal preVatTotal = items.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = preVatTotal.compareTo(DISCOUNT_THRESHOLD) > 0
            ? preVatTotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal subtotalAfterDiscount = preVatTotal.subtract(discount)
            .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal vat = subtotalAfterDiscount.multiply(VAT_RATE)
            .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal total = subtotalAfterDiscount.add(vat)
            .setScale(SCALE, RoundingMode.HALF_UP);

        StringBuilder receipt = new StringBuilder();
        receipt.append("════════════════ RECEIPT ════════════════\n");
        receipt.append("Items:\n");

        for (LineItem item : items) {
            BigDecimal lineTotal = item.getLineTotal().setScale(SCALE, RoundingMode.HALF_UP);
            receipt.append(String.format("  %-30s %3d × €%7.2f = €%8.2f\n",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal));
        }

        receipt.append("\n");
        receipt.append(String.format("Subtotal:                        €%8.2f\n", preVatTotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%% over €100):      -€%8.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:         €%8.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%):                       €%8.2f\n", vat));
        receipt.append("────────────────────────────────────────\n");
        receipt.append(String.format("TOTAL:                           €%8.2f\n", total));
        receipt.append("═════════════════════════════════════════\n");

        return receipt.toString();
    }
}

public class Main {
    public static void main(String[] args) {
        // Example: Order with 2 items totaling €110 (qualifies for discount)
        List<LineItem> items = List.of(
            new LineItem("Laptop", 1, new BigDecimal("80.00")),
            new LineItem("Mouse", 2, new BigDecimal("15.00"))
        );

        Order order = new Order(items);
        System.out.println(order.generateReceipt());

        // Example: Order under threshold (no discount)
        System.out.println("\n");
        List<LineItem> smallOrder = List.of(
            new LineItem("Pencil", 5, new BigDecimal("2.50")),
            new LineItem("Notebook", 2, new BigDecimal("12.00"))
        );

        Order order2 = new Order(smallOrder);
        System.out.println(order2.generateReceipt());
    }
}
````

### order · run 5 · uncle-bob-junior

````java
public class LineItem {
    private final String description;
    private final int quantityInCents;
    private final int pricePerUnitInCents;

    public LineItem(String description, int quantityInCents, int pricePerUnitInCents) {
        if (quantityInCents <= 0 || pricePerUnitInCents <= 0) {
            throw new IllegalArgumentException("Quantity and price must be positive");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description = description;
        this.quantityInCents = quantityInCents;
        this.pricePerUnitInCents = pricePerUnitInCents;
    }

    public int subtotalInCents() {
        return quantityInCents * pricePerUnitInCents;
    }

    public String description() {
        return description;
    }
}

public class Order {
    private static final int VAT_PERCENTAGE = 21;
    private static final int DISCOUNT_PERCENTAGE = 10;
    private static final int DISCOUNT_THRESHOLD_IN_CENTS = 10000; // €100.00
    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.items = new ArrayList<>(items);
    }

    public int subtotalInCents() {
        return items.stream().mapToInt(LineItem::subtotalInCents).sum();
    }

    public int discountInCents() {
        int subtotal = subtotalInCents();
        if (subtotal > DISCOUNT_THRESHOLD_IN_CENTS) {
            return subtotal * DISCOUNT_PERCENTAGE / 100;
        }
        return 0;
    }

    public int subtotalAfterDiscountInCents() {
        return subtotalInCents() - discountInCents();
    }

    public int vatInCents() {
        return subtotalAfterDiscountInCents() * VAT_PERCENTAGE / 100;
    }

    public int totalInCents() {
        return subtotalAfterDiscountInCents() + vatInCents();
    }

    public String receipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            receipt.append(formatLineItem(item)).append("\n");
        }
        
        receipt.append("--------------\n");
        receipt.append(formatAmount("Subtotal", subtotalInCents())).append("\n");
        
        if (discountInCents() > 0) {
            receipt.append(formatAmount("Discount (10%)", -discountInCents())).append("\n");
        }
        
        receipt.append(formatAmount("VAT (21%)", vatInCents())).append("\n");
        receipt.append("--------------\n");
        receipt.append(formatAmount("TOTAL", totalInCents())).append("\n");
        
        return receipt.toString();
    }

    private String formatLineItem(LineItem item) {
        return String.format("%s: €%.2f", item.description(), item.subtotalInCents() / 100.0);
    }

    private String formatAmount(String label, int amountInCents) {
        return String.format("%-20s €%.2f", label, amountInCents / 100.0);
    }
}
````

````java
public class OrderTest {
    @Test
    void validateOrderMustHaveItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void validateLineItemQuantityMustBePositive() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", 0, 1000));
    }

    @Test
    void validateLineItemPriceMustBePositive() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", 100, 0));
    }

    @Test
    void computeTotalWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Pen", 100, 500)  // €5.00
        );
        Order order = new Order(items);
        
        assertEquals(500, order.subtotalInCents());
        assertEquals(0, order.discountInCents());
        assertEquals(105, order.vatInCents());
        assertEquals(605, order.totalInCents());
    }

    @Test
    void applyDiscountWhenSubtotalExceeds100Euros() {
        List<LineItem> items = List.of(
            new LineItem("Box", 1, 12000)  // €120.00
        );
        Order order = new Order(items);
        
        assertEquals(12000, order.subtotalInCents());
        assertEquals(1200, order.discountInCents());  // 10% of 12000
        assertEquals(10800, order.subtotalAfterDiscountInCents());
        assertEquals(2268, order.vatInCents());  // 21% of 10800
        assertEquals(13068, order.totalInCents());
    }

    @Test
    void multipleItems() {
        List<LineItem> items = List.of(
            new LineItem("Notebook", 2, 3500),  // €70.00
            new LineItem("Pen", 10, 150)        // €15.00
        );
        Order order = new Order(items);
        
        assertEquals(8500, order.subtotalInCents());
        assertEquals(850, order.discountInCents());
        assertEquals(1604, order.vatInCents());
        assertEquals(10254, order.totalInCents());
    }

    @Test
    void receiptFormatted() {
        List<LineItem> items = List.of(
            new LineItem("Coffee", 1, 350)  // €3.50
        );
        Order order = new Order(items);
        String receipt = order.receipt();
        
        assertThat(receipt)
            .contains("=== RECEIPT ===")
            .contains("Coffee: €3.50")
            .contains("Subtotal")
            .contains("VAT (21%)")
            .contains("TOTAL");
    }
}
````

````java
List<LineItem> items = List.of(
    new LineItem("Laptop", 1, 100000),  // €1000.00
    new LineItem("Mouse", 2, 2500)      // €25.00 × 2
);
Order order = new Order(items);
System.out.println(order.receipt());
````

````
=== RECEIPT ===
Laptop: €1000.00
Mouse: €50.00
--------------
Subtotal              €1050.00
Discount (10%)        €-105.00
VAT (21%)             €198.45
--------------
TOTAL                 €1143.45
````

### order · run 6 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

class LineItem {
    private final String description;
    private final BigDecimal price;
    private final int quantity;

    public LineItem(String description, BigDecimal price, int quantity) {
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    
    private final List<LineItem> items = new ArrayList<>();

    public void addItem(LineItem item) {
        items.add(item);
    }

    public void validate() throws IllegalArgumentException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive: " + item.getDescription());
            }
            if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Item price must be positive: " + item.getDescription());
            }
        }
    }

    public String generateReceipt() {
        validate();

        BigDecimal subtotal = items.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
            ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = subtotalAfterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotalAfterDiscount.add(vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        
        for (LineItem item : items) {
            BigDecimal lineTotal = item.getLineTotal();
            receipt.append(String.format("%s @ €%.2f x %d = €%.2f\n",
                item.getDescription(), item.getPrice(), item.getQuantity(), lineTotal));
        }
        
        receipt.append("------------------------------\n");
        receipt.append(String.format("Subtotal:              €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%):        -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Before VAT:            €%.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%):             €%.2f\n", vat));
        receipt.append("------------------------------\n");
        receipt.append(String.format("TOTAL:                 €%.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        // Example 1: Order with discount (total > 100 before VAT)
        Order order1 = new Order();
        order1.addItem(new LineItem("Laptop", new BigDecimal("750.00"), 1));
        order1.addItem(new LineItem("Mouse", new BigDecimal("25.00"), 2));
        
        System.out.println("=== Order 1 (with discount) ===");
        System.out.println(order1.generateReceipt());

        // Example 2: Order without discount (total < 100 before VAT)
        Order order2 = new Order();
        order2.addItem(new LineItem("Coffee", new BigDecimal("5.50"), 3));
        order2.addItem(new LineItem("Pastry", new BigDecimal("3.25"), 2));
        
        System.out.println("=== Order 2 (no discount) ===");
        System.out.println(order2.generateReceipt());

        // Example 3: Validation error handling
        try {
            Order order3 = new Order();
            order3.addItem(new LineItem("Product", new BigDecimal("-10.00"), 1));
            order3.generateReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}
````

### order · run 6 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        validateLineItem();
    }
    
    private void validateLineItem() {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice.signum() <= 0) throw new IllegalArgumentException("Unit price must be positive");
    }
    
    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    String description() { return description; }
    int quantity() { return quantity; }
    BigDecimal unitPrice() { return unitPrice; }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        this.items = Objects.requireNonNull(items, "Items list cannot be null");
        if (items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
    }
    
    BigDecimal preVatTotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    BigDecimal discountAmount() {
        BigDecimal total = preVatTotal();
        return total.compareTo(DISCOUNT_THRESHOLD) > 0
            ? total.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }
    
    BigDecimal totalAfterDiscount() {
        return preVatTotal().subtract(discountAmount());
    }
    
    BigDecimal vatAmount() {
        return totalAfterDiscount().multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    BigDecimal grandTotal() {
        return totalAfterDiscount().add(vatAmount());
    }
    
    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        items.forEach(item -> 
            sb.append(String.format("  %s x%d @ €%.2f = €%.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.subtotal()))
        );
        sb.append(String.format("Subtotal:        €%.2f\n", preVatTotal()));
        if (discountAmount().signum() > 0) {
            sb.append(String.format("Discount (10%%):  -€%.2f\n", discountAmount()));
            sb.append(String.format("After discount:  €%.2f\n", totalAfterDiscount()));
        }
        sb.append(String.format("VAT (21%%):       €%.2f\n", vatAmount()));
        sb.append(String.format("TOTAL:           €%.2f\n", grandTotal()));
        return sb.toString();
    }
}
````

````java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Widget", 0, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Widget", 1, BigDecimal.ZERO));
    }
    
    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(List.of()));
    }
    
    @Test
    void computesTotalWithoutDiscount() {
        Order order = new Order(List.of(
            new LineItem("Widget", 2, new BigDecimal("25.00"))
        ));
        // 50.00 + (50.00 * 0.21) = 60.50
        assertEquals(new BigDecimal("60.50"), order.grandTotal());
    }
    
    @Test
    void appliesDiscountWhenThresholdExceeded() {
        Order order = new Order(List.of(
            new LineItem("Widget", 5, new BigDecimal("25.00"))
        ));
        // Pre-VAT: 125 → discount: 12.50 → after: 112.50 → VAT: 23.63 → total: 136.13
        assertEquals(new BigDecimal("12.50"), order.discountAmount());
        assertEquals(new BigDecimal("136.13"), order.grandTotal());
    }
    
    @Test
    void noDiscountWhenBelowThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", 2, new BigDecimal("40.00"))
        ));
        assertEquals(BigDecimal.ZERO, order.discountAmount());
    }
    
    @Test
    void receiptContainsAllComponents() {
        Order order = new Order(List.of(
            new LineItem("Widget", 1, new BigDecimal("150.00"))
        ));
        String receipt = order.receipt();
        assertTrue(receipt.contains("Widget"));
        assertTrue(receipt.contains("Subtotal"));
        assertTrue(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT"));
        assertTrue(receipt.contains("TOTAL"));
    }
}
````

### order · run 7 · baseline

````java
import java.util.ArrayList;
import java.util.List;

class LineItem {
    private String product;
    private double unitPrice;
    private int quantity;

    public LineItem(String product, double unitPrice, int quantity) {
        this.product = product;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return unitPrice * quantity;
    }
}

class Order {
    private List<LineItem> lineItems = new ArrayList<>();
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public void addLineItem(LineItem item) {
        lineItems.add(item);
    }

    public void validate() throws IllegalArgumentException {
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        for (LineItem item : lineItems) {
            if (item.getProduct() == null || item.getProduct().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be null or empty");
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative: " + item.getProduct());
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.getProduct());
            }
        }
    }

    public double calculateSubtotal() {
        return lineItems.stream().mapToDouble(LineItem::getLineTotal).sum();
    }

    public double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }

    public double calculateTotal() {
        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double afterDiscount = subtotal - discount;
        double vat = afterDiscount * VAT_RATE;
        return afterDiscount + vat;
    }

    public String generateReceipt() {
        validate();

        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double afterDiscount = subtotal - discount;
        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        receipt.append("Items:\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("  %-20s x%d @ €%.2f = €%.2f\n",
                    item.getProduct(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
        }

        receipt.append(String.format("\nSubtotal:          €%.2f\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%):   -€%.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount: €%.2f\n", afterDiscount));
        receipt.append(String.format("VAT (21%):         €%.2f\n", vat));
        receipt.append(String.format("TOTAL:             €%.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        // Example usage
        Order order = new Order();
        order.addLineItem(new LineItem("Widget A", 25.50, 2));
        order.addLineItem(new LineItem("Widget B", 30.00, 2));
        order.addLineItem(new LineItem("Widget C", 15.75, 1));

        System.out.println(order.generateReceipt());
    }
}
````

### order · run 7 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
  private final String description;
  private final int quantity;
  private final BigDecimal unitPrice;

  LineItem(String description, int quantity, BigDecimal unitPrice) {
    Objects.requireNonNull(description, "Description cannot be null");
    Objects.requireNonNull(unitPrice, "Unit price cannot be null");
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Unit price cannot be negative");
    }
    this.description = description;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  @Override
  public String toString() {
    return String.format("%-30s %3d × €%8.2f = €%8.2f",
        description, quantity, unitPrice, lineTotal());
  }
}

class Order {
  private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
  private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
  private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

  private final List<LineItem> items;

  Order(List<LineItem> items) {
    Objects.requireNonNull(items, "Items cannot be null");
    if (items.isEmpty()) {
      throw new IllegalArgumentException("Order must contain at least one item");
    }
    this.items = new ArrayList<>(items);
  }

  private BigDecimal subtotal() {
    return items.stream()
        .map(LineItem::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal discountAmount(BigDecimal preVatTotal) {
    if (preVatTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
      return preVatTotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal vatAmount(BigDecimal taxableAmount) {
    return taxableAmount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  String generateReceipt() {
    BigDecimal preVatTotal = subtotal();
    BigDecimal discount = discountAmount(preVatTotal);
    BigDecimal afterDiscount = preVatTotal.subtract(discount);
    BigDecimal vat = vatAmount(afterDiscount);
    BigDecimal total = afterDiscount.add(vat);

    StringBuilder receipt = new StringBuilder();
    receipt.append("═══════════════════════════════════════════════════\n");
    receipt.append("RECEIPT\n");
    receipt.append("═══════════════════════════════════════════════════\n\n");

    items.forEach(item -> receipt.append(item).append("\n"));

    receipt.append("───────────────────────────────────────────────────\n");
    receipt.append(String.format("Subtotal                           €%8.2f\n", preVatTotal));

    if (discount.compareTo(BigDecimal.ZERO) > 0) {
      receipt.append(String.format("Discount (10%%)                     €%8.2f\n", discount));
      receipt.append(String.format("After Discount                     €%8.2f\n", afterDiscount));
    }

    receipt.append(String.format("VAT (21%%)                          €%8.2f\n", vat));
    receipt.append("═══════════════════════════════════════════════════\n");
    receipt.append(String.format("Total                              €%8.2f\n", total));
    receipt.append("═══════════════════════════════════════════════════\n");

    return receipt.toString();
  }
}

// Test
public class OrderProcessing {
  public static void main(String[] args) {
    // Example 1: order under 100 euros (no discount)
    List<LineItem> items1 = List.of(
        new LineItem("Widget A", 2, new BigDecimal("25.00")),
        new LineItem("Widget B", 1, new BigDecimal("45.00"))
    );
    Order order1 = new Order(items1);
    System.out.println(order1.generateReceipt());

    // Example 2: order over 100 euros (10% discount applied)
    List<LineItem> items2 = List.of(
        new LineItem("Premium Widget", 5, new BigDecimal("25.50")),
        new LineItem("Service Fee", 1, new BigDecimal("10.00"))
    );
    Order order2 = new Order(items2);
    System.out.println(order2.generateReceipt());

    // Example 3: validation test
    try {
      new LineItem("Invalid", -1, new BigDecimal("10"));
    } catch (IllegalArgumentException e) {
      System.out.println("Validation error caught: " + e.getMessage());
    }
  }
}
````

### order · run 8 · baseline

````java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderProcessor {
    
    static class LineItem {
        private final String description;
        private final double price;
        private final int quantity;
        
        public LineItem(String description, double price, int quantity) {
            this.description = Objects.requireNonNull(description, "Description cannot be null");
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getDescription() {
            return description;
        }
        
        public double getPrice() {
            return price;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public double getTotal() {
            return price * quantity;
        }
    }
    
    static class Order {
        private final List<LineItem> lineItems;
        private static final double VAT_RATE = 0.21;
        private static final double DISCOUNT_RATE = 0.10;
        private static final double DISCOUNT_THRESHOLD = 100.0;
        
        public Order(List<LineItem> lineItems) {
            this.lineItems = new ArrayList<>(Objects.requireNonNull(lineItems, "Line items cannot be null"));
        }
        
        public void validate() throws IllegalArgumentException {
            if (lineItems.isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one line item");
            }
            
            for (LineItem item : lineItems) {
                if (item.getPrice() < 0) {
                    throw new IllegalArgumentException("Price cannot be negative: " + item.getDescription());
                }
                if (item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive: " + item.getDescription());
                }
            }
        }
        
        public double getSubtotal() {
            return lineItems.stream().mapToDouble(LineItem::getTotal).sum();
        }
        
        public double getDiscount() {
            double subtotal = getSubtotal();
            return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        }
        
        public double getSubtotalAfterDiscount() {
            return getSubtotal() - getDiscount();
        }
        
        public double getVat() {
            return getSubtotalAfterDiscount() * VAT_RATE;
        }
        
        public double getTotal() {
            return getSubtotalAfterDiscount() + getVat();
        }
        
        public String generateReceipt() {
            validate();
            StringBuilder receipt = new StringBuilder();
            receipt.append("╔════════════════════════════════════╗\n");
            receipt.append("║           ORDER RECEIPT            ║\n");
            receipt.append("╠════════════════════════════════════╣\n");
            
            for (LineItem item : lineItems) {
                receipt.append(String.format("║ %-25s %2d x €%6.2f║\n", 
                    truncate(item.getDescription(), 25), 
                    item.getQuantity(), 
                    item.getPrice()));
            }
            
            receipt.append("╠════════════════════════════════════╣\n");
            receipt.append(String.format("║ Subtotal:              €%10.2f ║\n", getSubtotal()));
            
            if (getDiscount() > 0) {
                receipt.append(String.format("║ Discount (10%):       -€%10.2f ║\n", getDiscount()));
            }
            
            receipt.append(String.format("║ VAT (21%):             €%10.2f ║\n", getVat()));
            receipt.append("╠════════════════════════════════════╣\n");
            receipt.append(String.format("║ TOTAL:                 €%10.2f ║\n", getTotal()));
            receipt.append("╚════════════════════════════════════╝\n");
            
            return receipt.toString();
        }
        
        private String truncate(String str, int length) {
            return str.length() > length ? str.substring(0, length - 2) + ".." : str;
        }
    }
    
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 4.50, 2));
        items.add(new LineItem("Sandwich", 8.75, 3));
        items.add(new LineItem("Cookie", 2.25, 5));
        
        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
````

### order · run 8 · uncle-bob-junior

````java
class LineItem {
    private final String name;
    private final double price;
    private final int quantity;
    
    LineItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        validate();
    }
    
    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    double subtotal() {
        return price * quantity;
    }
    
    String name() { return name; }
    double price() { return price; }
    int quantity() { return quantity; }
}

class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        validateItems(items);
        this.items = new ArrayList<>(items);
    }
    
    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Line items cannot be null");
            }
        }
    }
    
    private double subtotal() {
        return items.stream().mapToDouble(LineItem::subtotal).sum();
    }
    
    private double discountAmount() {
        if (subtotal() > DISCOUNT_THRESHOLD) {
            return subtotal() * DISCOUNT_RATE;
        }
        return 0.0;
    }
    
    private double subtotalAfterDiscount() {
        return subtotal() - discountAmount();
    }
    
    private double vatAmount() {
        return subtotalAfterDiscount() * VAT_RATE;
    }
    
    double total() {
        return subtotalAfterDiscount() + vatAmount();
    }
    
    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.name(), item.quantity(), item.price(), item.subtotal()));
        }
        
        sb.append("-".repeat(30)).append("\n");
        sb.append(String.format("Subtotal:              €%.2f\n", subtotal()));
        
        if (discountAmount() > 0) {
            sb.append(String.format("Discount (10%%):        -€%.2f\n", discountAmount()));
        }
        
        sb.append(String.format("Subtotal (discount):   €%.2f\n", subtotalAfterDiscount()));
        sb.append(String.format("VAT (21%%):             €%.2f\n", vatAmount()));
        sb.append("-".repeat(30)).append("\n");
        sb.append(String.format("Total:                 €%.2f\n", total()));
        
        return sb.toString();
    }
}

// Example usage and test
class OrderProcessor {
    public static void main(String[] args) {
        // Order under 100 euros (no discount)
        List<LineItem> items1 = List.of(
            new LineItem("Coffee", 2.50, 2),
            new LineItem("Croissant", 3.00, 3)
        );
        Order order1 = new Order(items1);
        System.out.println(order1.receipt());
        
        // Order over 100 euros (applies 10% discount)
        List<LineItem> items2 = List.of(
            new LineItem("Monitor", 250.00, 1),
            new LineItem("Keyboard", 75.00, 2),
            new LineItem("Mouse", 25.00, 1)
        );
        Order order2 = new Order(items2);
        System.out.println(order2.receipt());
    }
}
````

### order · run 9 · baseline

````java
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {

    static class LineItem {
        String productName;
        int quantity;
        double unitPrice;

        LineItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        double getSubtotal() {
            return quantity * unitPrice;
        }
    }

    static class Order {
        List<LineItem> items;

        Order() {
            this.items = new ArrayList<>();
        }

        void addItem(String productName, int quantity, double unitPrice) {
            items.add(new LineItem(productName, quantity, unitPrice));
        }

        String processAndGenerateReceipt() {
            validateLineItems();
            return generateReceipt();
        }

        void validateLineItems() {
            for (LineItem item : items) {
                if (item.quantity <= 0) {
                    throw new IllegalArgumentException(
                        "Invalid quantity for " + item.productName + ": " + item.quantity);
                }
                if (item.unitPrice < 0) {
                    throw new IllegalArgumentException(
                        "Invalid price for " + item.productName + ": " + item.unitPrice);
                }
            }
        }

        double calculateSubtotal() {
            return items.stream().mapToDouble(LineItem::getSubtotal).sum();
        }

        double calculateTotal() {
            double subtotal = calculateSubtotal();
            double discount = subtotal > 100 ? subtotal * 0.10 : 0;
            double discountedSubtotal = subtotal - discount;
            double vat = discountedSubtotal * 0.21;
            return discountedSubtotal + vat;
        }

        String generateReceipt() {
            double subtotal = calculateSubtotal();
            double discount = subtotal > 100 ? subtotal * 0.10 : 0;
            double discountedSubtotal = subtotal - discount;
            double vat = discountedSubtotal * 0.21;
            double total = discountedSubtotal + vat;

            StringBuilder receipt = new StringBuilder();
            receipt.append("========== RECEIPT ==========\n");
            for (LineItem item : items) {
                receipt.append(String.format("%-20s %d x €%.2f = €%.2f\n",
                    item.productName, item.quantity, item.unitPrice, item.getSubtotal()));
            }
            receipt.append("-----------------------------\n");
            receipt.append(String.format("Subtotal:               €%.2f\n", subtotal));
            if (discount > 0) {
                receipt.append(String.format("Discount (10%%):        -€%.2f\n", discount));
            }
            receipt.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal));
            receipt.append(String.format("VAT (21%%):              €%.2f\n", vat));
            receipt.append("-----------------------------\n");
            receipt.append(String.format("TOTAL:                  €%.2f\n", total));
            receipt.append("=============================\n");
            return receipt.toString();
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.addItem("Coffee", 2, 4.50);
        order.addItem("Croissant", 1, 3.25);
        order.addItem("Sandwich", 3, 8.00);

        try {
            String receipt = order.processAndGenerateReceipt();
            System.out.println(receipt);
            System.out.printf("Final Total: €%.2f%n", order.calculateTotal());
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}
````

### order · run 9 · uncle-bob-junior

````java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LineItem {
    private final String description;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        this.description = Objects.requireNonNull(description, "Description required");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price required");
        this.quantity = quantity;
        validate();
    }

    private void validate() {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got " + quantity);
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int SCALE = 2;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items required"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        receipt.append(itemLines());
        receipt.append(String.format("Subtotal:      %s\n", formatCurrency(subtotal())));
        if (hasDiscount()) {
            receipt.append(String.format("Discount:      -%s\n", formatCurrency(discountAmount())));
            receipt.append(String.format("After Discount: %s\n", formatCurrency(subtotalAfterDiscount())));
        }
        receipt.append(String.format("VAT (21%%):      %s\n", formatCurrency(vatAmount())));
        receipt.append(String.format("Total:         %s\n", formatCurrency(total())));
        receipt.append("==============================");
        return receipt.toString();
    }

    private String itemLines() {
        StringBuilder items = new StringBuilder();
        for (LineItem item : this.items) {
            items.append(String.format("  %s x%d @ %s = %s\n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatCurrency(item.getUnitPrice()),
                    formatCurrency(item.lineTotal())));
        }
        return items.toString();
    }

    private BigDecimal subtotal() {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private boolean hasDiscount() {
        return subtotal().compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    private BigDecimal discountAmount() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }
        return subtotal().multiply(DISCOUNT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal subtotalAfterDiscount() {
        return subtotal().subtract(discountAmount())
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal vatAmount() {
        return subtotalAfterDiscount().multiply(VAT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal total() {
        return subtotalAfterDiscount().add(vatAmount())
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private String formatCurrency(BigDecimal amount) {
        return "€" + String.format("%.2f", amount);
    }
}
````

````java
import java.math.BigDecimal;
import java.util.List;

public class OrderTest {
    public static void main(String[] args) {
        testSmallOrder();
        testLargeOrderWithDiscount();
        testInvalidLineItem();
    }

    private static void testSmallOrder() {
        List<LineItem> items = List.of(
                new LineItem("Coffee", new BigDecimal("2.50"), 2),
                new LineItem("Pastry", new BigDecimal("3.00"), 1)
        );
        Order order = new Order(items);

        assert order.total().equals(new BigDecimal("7.83")) : "Small order total incorrect";
        System.out.println("✓ Small order (no discount):\n" + order.generateReceipt() + "\n");
    }

    private static void testLargeOrderWithDiscount() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("50.00"), 2),
                new LineItem("Gadget", new BigDecimal("30.00"), 1)
        );
        Order order = new Order(items);

        BigDecimal subtotal = new BigDecimal("130.00");
        BigDecimal discounted = subtotal.subtract(subtotal.multiply(new BigDecimal("0.10")));
        BigDecimal expected = discounted.multiply(new BigDecimal("1.21"));

        assert order.total().equals(expected) : "Large order total incorrect";
        System.out.println("✓ Large order (10% discount applied):\n" + order.generateReceipt() + "\n");
    }

    private static void testInvalidLineItem() {
        try {
            new LineItem("Bad Item", new BigDecimal("10.00"), 0);
            assert false : "Should reject zero quantity";
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly rejected zero quantity: " + e.getMessage());
        }
    }
}
````

### order · run 10 · baseline

````java
public class LineItem {
    private String productName;
    private int quantity;
    private double unitPrice;

    public LineItem(String productName, int quantity, double unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotal() {
        return quantity * unitPrice;
    }
}
````

````java
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private List<LineItem> items;

    public OrderProcessor(List<LineItem> items) {
        this.items = items;
    }

    public void validateLineItems() {
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                    "Invalid quantity for " + item.getProductName() + ": must be positive"
                );
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException(
                    "Invalid price for " + item.getProductName() + ": cannot be negative"
                );
            }
        }
    }

    public String generateReceipt() {
        validateLineItems();

        double subtotal = items.stream()
            .mapToDouble(LineItem::getTotal)
            .sum();

        double discount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        double afterDiscount = subtotal - discount;
        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");

        for (LineItem item : items) {
            receipt.append(String.format("%-25s %3d x €%7.2f = €%8.2f\n",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotal()
            ));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:                    €%8.2f\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):             -€%8.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:     €%8.2f\n", afterDiscount));
        receipt.append(String.format("VAT (21%%):                  €%8.2f\n", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("TOTAL:                       €%8.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}
````

````java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Example 1: Order exceeding 100 euros (gets 10% discount)
        List<LineItem> items1 = new ArrayList<>();
        items1.add(new LineItem("Laptop", 1, 80.00));
        items1.add(new LineItem("Mouse", 2, 15.00));
        items1.add(new LineItem("USB Cable", 3, 5.00));

        OrderProcessor processor1 = new OrderProcessor(items1);
        System.out.println("=== Order 1 (with discount) ===");
        System.out.println(processor1.generateReceipt());

        // Example 2: Order below 100 euros (no discount)
        List<LineItem> items2 = new ArrayList<>();
        items2.add(new LineItem("Book", 2, 20.00));
        items2.add(new LineItem("Pen", 5, 2.00));

        OrderProcessor processor2 = new OrderProcessor(items2);
        System.out.println("\n=== Order 2 (no discount) ===");
        System.out.println(processor2.generateReceipt());

        // Example 3: Invalid order (demonstrates validation)
        try {
            List<LineItem> items3 = new ArrayList<>();
            items3.add(new LineItem("Invalid Item", -1, 50.00));
            OrderProcessor processor3 = new OrderProcessor(items3);
            processor3.generateReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("\n=== Order 3 (validation error) ===");
            System.out.println("Error: " + e.getMessage());
        }
    }
}
````

### order · run 10 · uncle-bob-junior

````java
public class LineItem {
    private final String productName;
    private final double pricePerUnit;
    private final int quantity;

    public LineItem(String productName, double pricePerUnit, int quantity) {
        validateInputs(productName, pricePerUnit, quantity);
        this.productName = productName;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    private void validateInputs(String productName, double pricePerUnit, int quantity) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (pricePerUnit < 0) {
            throw new IllegalArgumentException("Price per unit cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public double getLineTotal() {
        return pricePerUnit * quantity;
    }

    public String getProductName() {
        return productName;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }
}
````

````java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private final List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        this.lineItems = new ArrayList<>(lineItems);
    }

    public double getSubtotalBeforeDiscount() {
        return lineItems.stream()
                .mapToDouble(LineItem::getLineTotal)
                .sum();
    }

    public boolean isEligibleForDiscount() {
        return getSubtotalBeforeDiscount() > DISCOUNT_THRESHOLD;
    }

    public double getDiscountAmount() {
        return isEligibleForDiscount() ? getSubtotalBeforeDiscount() * DISCOUNT_RATE : 0.0;
    }

    public double getSubtotalAfterDiscount() {
        return getSubtotalBeforeDiscount() - getDiscountAmount();
    }

    public double getVatAmount() {
        return getSubtotalAfterDiscount() * VAT_RATE;
    }

    public double getTotalWithVat() {
        return getSubtotalAfterDiscount() + getVatAmount();
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        appendLineItems(receipt);
        appendTotals(receipt);
        receipt.append("====================\n");
        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : lineItems) {
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPricePerUnit(),
                    item.getLineTotal()));
        }
    }

    private void appendTotals(StringBuilder receipt) {
        receipt.append(String.format("Subtotal:     €%.2f\n", getSubtotalBeforeDiscount()));

        if (isEligibleForDiscount()) {
            receipt.append(String.format("Discount 10%: -€%.2f\n", getDiscountAmount()));
        }

        receipt.append(String.format("After disc.:  €%.2f\n", getSubtotalAfterDiscount()));
        receipt.append(String.format("VAT 21%%:      €%.2f\n", getVatAmount()));
        receipt.append(String.format("TOTAL:        €%.2f\n", getTotalWithVat()));
    }
}
````

````java
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OrderTest {

    @Test
    public void orderWithoutDiscountQualification() {
        List<LineItem> items = List.of(
                new LineItem("Apple", 1.0, 10),
                new LineItem("Banana", 2.0, 5)
        );
        Order order = new Order(items);

        assertEquals(20.0, order.getSubtotalBeforeDiscount());
        assertFalse(order.isEligibleForDiscount());
        assertEquals(0.0, order.getDiscountAmount());
        assertEquals(20.0, order.getSubtotalAfterDiscount());
        assertEquals(4.2, order.getVatAmount(), 0.01);
        assertEquals(24.2, order.getTotalWithVat(), 0.01);
    }

    @Test
    public void orderExceedingDiscountThresholdApplies10Percent() {
        List<LineItem> items = List.of(
                new LineItem("Laptop", 60.0, 2),
                new LineItem("Mouse", 20.0, 1)
        );
        Order order = new Order(items);

        assertEquals(140.0, order.getSubtotalBeforeDiscount());
        assertTrue(order.isEligibleForDiscount());
        assertEquals(14.0, order.getDiscountAmount(), 0.01);
        assertEquals(126.0, order.getSubtotalAfterDiscount(), 0.01);
        assertEquals(26.46, order.getVatAmount(), 0.01);
        assertEquals(152.46, order.getTotalWithVat(), 0.01);
    }

    @Test
    public void receiptIncludesLineItemsAndTotals() {
        List<LineItem> items = List.of(
                new LineItem("Book", 50.0, 3)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();

        assertTrue(receipt.contains("Book x3 @ €50.00 = €150.00"));
        assertTrue(receipt.contains("Discount 10%: -€15.00"));
        assertTrue(receipt.contains("TOTAL:        €165.90"));
    }

    @Test
    public void lineItemValidatesNonEmptyName() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LineItem("", 10.0, 1));
    }

    @Test
    public void lineItemValidatesNonNegativePrice() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LineItem("Product", -5.0, 1));
    }

    @Test
    public void lineItemValidatesPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LineItem("Product", 10.0, 0));
    }

    @Test
    public void orderRequiresAtLeastOneLineItem() {
        assertThrows(IllegalArgumentException.class, 
                () -> new Order(List.of()));
    }
}
````
