public class EmailValidator {
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_LENGTH = 64;
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        if (parts[0].length() > MAX_LOCAL_LENGTH || parts[0].startsWith(".") || parts[0].endsWith(".")) {
            return false;
        }
        
        return email.matches(EMAIL_REGEX);
    }
}
