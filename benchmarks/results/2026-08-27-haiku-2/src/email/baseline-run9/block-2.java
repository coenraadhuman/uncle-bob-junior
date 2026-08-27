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
