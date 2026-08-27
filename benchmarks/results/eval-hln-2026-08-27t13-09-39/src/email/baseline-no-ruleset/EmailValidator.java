import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    /**
     * Validates an email address against a standard pattern.
     * 
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        return pattern.matcher(email.trim()).matches();
    }
    
    // Test examples
    public static void main(String[] args) {
        System.out.println(isValidEmail("user@example.com"));           // true
        System.out.println(isValidEmail("john.doe+tag@domain.co.uk")); // true
        System.out.println(isValidEmail("invalid.email@"));            // false
        System.out.println(isValidEmail("@nodomain.com"));             // false
        System.out.println(isValidEmail("no-at-sign.com"));            // false
        System.out.println(isValidEmail(null));                        // false
    }
}
