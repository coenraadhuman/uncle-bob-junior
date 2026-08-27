import java.net.URI;
import java.net.URISyntaxException;

public class EmailValidator {
    private static final String EMAIL_SCHEME = "mailto";
    
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            URI uri = new URI(EMAIL_SCHEME + ":" + email);
            String localPart = uri.getSchemeSpecificPart();
            return !localPart.isEmpty() && 
                   email.contains("@") && 
                   hasValidDomain(email);
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    private static boolean hasValidDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1 || atIndex == email.length() - 1) {
            return false;
        }
        
        String domain = email.substring(atIndex + 1);
        return domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".");
    }
}
