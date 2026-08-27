import java.util.regex.Pattern;

/**
 * Validates email addresses against a practical subset of RFC 5321/5322,
 * suitable for user-input boundaries such as registration forms.
 *
 * Deliberately rejected: quoted local parts ("john doe"@x.com),
 * IP-literal domains (user@[192.168.0.1]), and dotless domains (user@localhost).
 */
public final class EmailValidator {

    /** RFC 5321: a path is limited to 256 octets including angle brackets. */
    private static final int MAX_TOTAL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 253;

    /** Dot-separated atoms of RFC 5322 atext; no leading, trailing, or double dots. */
    private static final Pattern LOCAL_PART = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*");

    /** Two or more labels; each label is 1-63 chars, alphanumeric with inner hyphens. */
    private static final Pattern DOMAIN = Pattern.compile(
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+");

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.length() > MAX_TOTAL_LENGTH) {
            return false;
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0) {
            return false;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    private static boolean isValidLocalPart(String localPart) {
        return localPart.length() >= 1
                && localPart.length() <= MAX_LOCAL_PART_LENGTH
                && LOCAL_PART.matcher(localPart).matches();
    }

    private static boolean isValidDomain(String domain) {
        return domain.length() >= 1
                && domain.length() <= MAX_DOMAIN_LENGTH
                && DOMAIN.matcher(domain).matches();
    }
}
