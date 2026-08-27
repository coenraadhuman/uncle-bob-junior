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
