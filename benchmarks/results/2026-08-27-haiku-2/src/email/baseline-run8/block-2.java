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
