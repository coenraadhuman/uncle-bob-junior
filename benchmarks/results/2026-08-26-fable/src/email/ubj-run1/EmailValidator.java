import java.util.regex.Pattern;

/**
 * Validates email address syntax against a practical subset of RFC 5322.
 * Does not verify that the mailbox exists.
 */
public final class EmailValidator {

    private static final int MAX_TOTAL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 253;
    private static final int MAX_DOMAIN_LABEL_LENGTH = 63;

    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$");
    private static final Pattern DOMAIN_LABEL_PATTERN =
            Pattern.compile("^[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?$");

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.length() > MAX_TOTAL_LENGTH) {
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
        if (domain.length() > MAX_DOMAIN_LENGTH || domain.contains("@")) {
            return false;
        }
        String[] labels = domain.split("\\.", -1);
        // ubj: a single-label domain (no dot) is syntactically legal but never a
        // deliverable public address, so it is rejected here on purpose.
        if (labels.length < 2) {
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
