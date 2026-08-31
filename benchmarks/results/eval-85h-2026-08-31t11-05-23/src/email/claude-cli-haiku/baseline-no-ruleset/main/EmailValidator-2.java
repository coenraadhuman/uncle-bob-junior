import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidator {
    private static final EmailValidator validator = EmailValidator.getInstance();
    
    public static boolean isValidEmail(String email) {
        return validator.isValid(email);
    }
}
