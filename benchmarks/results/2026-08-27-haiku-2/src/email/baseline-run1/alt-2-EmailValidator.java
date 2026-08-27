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
