public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    email = email.trim();
    
    if (email.length() > 254) {
        return false;
    }
    
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    if (!email.matches(emailRegex)) {
        return false;
    }
    
    String[] parts = email.split("@");
    if (parts[0].length() > 64 || parts[0].startsWith(".") || parts[0].endsWith(".")) {
        return false;
    }
    
    return true;
}
