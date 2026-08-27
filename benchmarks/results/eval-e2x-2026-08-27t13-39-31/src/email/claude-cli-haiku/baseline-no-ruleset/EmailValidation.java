import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidation {
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
