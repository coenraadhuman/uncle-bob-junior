import java.util.regex.Pattern;

public static boolean isValidEmailStrict(String email) {
    if (email == null || email.isBlank()) {
        return false;
    }
    
    String strictRegex = "^[A-Za-z0-9][A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]*[A-Za-z0-9]@" +
                         "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9]" +
                         "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$";
    return Pattern.matches(strictRegex, email);
}
