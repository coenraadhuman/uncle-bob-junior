import org.apache.commons.validator.routines.EmailValidator;

public static boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
}
