import javax.mail.internet.InternetAddress;

public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
        return false;
    }
    try {
        new InternetAddress(email).validate();
        return true;
    } catch (Exception e) {
        return false;
    }
}
