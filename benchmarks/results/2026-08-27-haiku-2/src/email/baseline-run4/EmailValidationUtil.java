import org.apache.commons.mail.EmailValidator;

public class EmailValidationUtil {
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
