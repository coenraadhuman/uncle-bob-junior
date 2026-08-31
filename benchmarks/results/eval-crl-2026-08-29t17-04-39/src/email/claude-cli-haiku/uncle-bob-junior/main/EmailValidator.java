public class EmailValidator {
    private static final int MIN_LOCAL_PART_LENGTH = 1;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        String trimmed = email.trim();
        if (!PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        String localPart = trimmed.substring(0, atIndex);
        
        if (localPart.length() < MIN_LOCAL_PART_LENGTH) {
            return false;
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }

        if (localPart.contains("..")) {
            return false;
        }

        return true;
    }
}
