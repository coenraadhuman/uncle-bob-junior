import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

public class EmailValidator {
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
