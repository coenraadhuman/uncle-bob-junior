public static boolean isValidEmailStrict(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    String emailRegex = 
        "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    return email.matches(emailRegex);
}
