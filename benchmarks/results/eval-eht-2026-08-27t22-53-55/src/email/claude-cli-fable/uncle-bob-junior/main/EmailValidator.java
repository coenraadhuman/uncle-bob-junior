import java.util.regex.Pattern;

/**
 * Validates email addresses against a pragmatic subset of RFC 5321:
 * a dot-atom local part and a multi-label hostname domain.
 * Quoted local parts and IP-literal domains are deliberately rejected,
 * since they are almost never legitimate participant input.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;      // RFC 5321 path limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;  // RFC 5321 local-part limit
    private static final int MAX_DOMAIN_LENGTH = 253;

    private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*");

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}");

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1 || atIndex == email.length() - 1) {
            return false;
        }
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    private static boolean isValidLocalPart(String localPart) {
        return localPart.length() <= MAX_LOCAL_PART_LENGTH
                && LOCAL_PART_PATTERN.matcher(localPart).matches();
    }

    private static boolean isValidDomain(String domain) {
        return domain.length() <= MAX_DOMAIN_LENGTH
                && DOMAIN_PATTERN.matcher(domain).matches();
    }
}
