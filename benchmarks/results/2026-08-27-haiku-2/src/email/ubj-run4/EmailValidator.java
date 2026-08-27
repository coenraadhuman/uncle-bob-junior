import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MAX_EMAIL_LENGTH = 254;

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
