import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidationExample {
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
