import java.util.regex.Pattern;

public class EmailValidator {
    // RFC 5322 simplified pattern for practical email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // Alternative: stricter validation with length limits
    public static boolean isValidEmailStrict(String email) {
        if (email == null || email.isBlank() || email.length() > 254) {
            return false;
        }
        
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1 || atIndex == email.length() - 1) {
            return false;
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        
        if (localPart.length() > 64 || !isValidLocalPart(localPart) || !isValidDomain(domain)) {
            return false;
        }
        
        return true;
    }

    private static boolean isValidLocalPart(String localPart) {
        return localPart.matches("^[A-Za-z0-9+_.-]+$") && 
               !localPart.startsWith(".") && 
               !localPart.endsWith(".") &&
               !localPart.contains("..");
    }

    private static boolean isValidDomain(String domain) {
        return domain.matches("^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") &&
               !domain.startsWith(".") &&
               !domain.endsWith(".") &&
               !domain.contains("..");
    }

    public static void main(String[] args) {
        // Test cases
        System.out.println(isValidEmail("user@example.com"));           // true
        System.out.println(isValidEmail("john.doe+tag@company.co.uk")); // true
        System.out.println(isValidEmail("invalid.email@"));             // false
        System.out.println(isValidEmail("@example.com"));               // false
        System.out.println(isValidEmail("no-at-sign.com"));             // false
    }
}
