public class EmailValidator {
    /**
     * Validates an email address using InternetAddress.
     * More strict validation that checks RFC standards.
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            javax.mail.internet.InternetAddress address = 
                new javax.mail.internet.InternetAddress(email);
            address.validate();
            return true;
        } catch (javax.mail.internet.AddressException e) {
            return false;
        }
    }
}
