public class EmailValidator {
    
    // Simple regex pattern for basic email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
    
    // More comprehensive validation
    public static boolean isValidEmailStrict(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Check basic format
        if (!email.matches(EMAIL_PATTERN)) {
            return false;
        }
        
        // Additional checks
        if (email.length() > 254) {
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts[0].length() > 64) {
            return false;
        }
        
        if (email.startsWith(".") || email.endsWith(".")) {
            return false;
        }
        
        if (email.contains("..")) {
            return false;
        }
        
        return true;
    }
    
    public static void main(String[] args) {
        System.out.println(isValidEmail("user@example.com"));        // true
        System.out.println(isValidEmail("invalid.email@"));          // false
        System.out.println(isValidEmail("user+tag@domain.co.uk"));   // true
        System.out.println(isValidEmail(""));                        // false
    }
}
