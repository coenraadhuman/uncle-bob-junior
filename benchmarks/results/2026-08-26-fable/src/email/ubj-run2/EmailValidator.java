import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;   // RFC 5321 forward-path limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 253;
    private static final char LOCAL_DOMAIN_SEPARATOR = '@';

    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$");
    private static final Pattern DOMAIN_LABEL_PATTERN =
            Pattern.compile("^[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?$");
    private static final int MIN_DOMAIN_LABELS = 2;

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        int separatorIndex = email.indexOf(LOCAL_DOMAIN_SEPARATOR);
        boolean hasExactlyOneSeparator =
                separatorIndex >= 0 && email.indexOf(LOCAL_DOMAIN_SEPARATOR, separatorIndex + 1) < 0;
        if (!hasExactlyOneSeparator) {
            return false;
        }
        String localPart = email.substring(0, separatorIndex);
        String domain = email.substring(separatorIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    private static boolean isValidLocalPart(String localPart) {
        if (localPart.isEmpty() || localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return false;
        }
        return LOCAL_PART_PATTERN.matcher(localPart).matches();
    }

    private static boolean isValidDomain(String domain) {
        if (domain.isEmpty() || domain.length() > MAX_DOMAIN_LENGTH) {
            return false;
        }
        String[] labels = domain.split("\\.", -1);
        if (labels.length < MIN_DOMAIN_LABELS) {
            return false;
        }
        for (String label : labels) {
            if (!DOMAIN_LABEL_PATTERN.matcher(label).matches()) {
                return false;
            }
        }
        return true;
    }
}
