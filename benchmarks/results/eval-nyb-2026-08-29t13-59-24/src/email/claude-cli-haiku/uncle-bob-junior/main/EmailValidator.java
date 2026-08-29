import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern COMPILED_PATTERN = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return COMPILED_PATTERN.matcher(email).matches();
    }
}
