import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidation {
    private static final EmailValidator VALIDATOR = EmailValidator.getInstance();
    
    public static boolean isValid(String email) {
        return VALIDATOR.isValid(email);
    }
}
