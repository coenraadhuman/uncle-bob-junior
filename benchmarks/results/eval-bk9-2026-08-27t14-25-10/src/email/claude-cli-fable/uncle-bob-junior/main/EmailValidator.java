import java.util.regex.Pattern;

/**
 * Validates email addresses for use at input boundaries (e.g. sign-up forms).
 *
 * Accepts the practical subset of RFC 5321/5322: an ASCII local part, an "@",
 * and a dotted domain with at least two labels. Deliberately rejects
 * quoted local parts, IP-literal domains, and internationalised addresses.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LABEL_LENGTH = 63;
    private static final int MINIMUM_DOMAIN_LABELS = 2;

    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$");
    private static final Pattern DOMAIN_LABEL_PATTERN =
            Pattern.compile("^[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?$");

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
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
        if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return false;
        }
        return LOCAL_PART_PATTERN.matcher(localPart).matches();
    }

    private static boolean isValidDomain(String domain) {
        // -1 keeps a trailing dot as an empty label so "user@example." fails
        String[] labels = domain.split("\\.", -1);
        if (labels.length < MINIMUM_DOMAIN_LABELS) {
            return false;
        }
        for (String label : labels) {
            if (!isValidDomainLabel(label)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidDomainLabel(String label) {
        if (label.isEmpty() || label.length() > MAX_DOMAIN_LABEL_LENGTH) {
            return false;
        }
        return DOMAIN_LABEL_PATTERN.matcher(label).matches();
    }
}
