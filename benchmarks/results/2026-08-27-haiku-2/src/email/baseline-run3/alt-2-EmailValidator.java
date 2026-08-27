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
