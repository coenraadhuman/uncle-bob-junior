/**
 * Validates email addresses against a pragmatic subset of RFC 5321/5322:
 * unquoted local part, dot-separated alphanumeric domain labels, and the
 * standard length limits. Quoted local parts and IP-literal domains are
 * deliberately rejected as they never appear in participant sign-ups.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LABEL_LENGTH = 63;
    private static final int MIN_DOMAIN_LABEL_COUNT = 2;
    private static final char AT_SIGN = '@';
    private static final String LOCAL_PART_SPECIAL_CHARACTERS = "!#$%&'*+-/=?^_`{|}~.";

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atSignIndex = email.lastIndexOf(AT_SIGN);
        if (atSignIndex < 1 || atSignIndex == email.length() - 1) {
            return false;
        }

        String localPart = email.substring(0, atSignIndex);
        String domain = email.substring(atSignIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    private static boolean isValidLocalPart(String localPart) {
        if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return false;
        }
        if (hasEdgeOrConsecutiveDot(localPart)) {
            return false;
        }
        return localPart.chars().allMatch(EmailValidator::isAllowedLocalPartCharacter);
    }

    private static boolean isValidDomain(String domain) {
        if (hasEdgeOrConsecutiveDot(domain)) {
            return false;
        }
        String[] labels = domain.split("\\.", -1);
        if (labels.length < MIN_DOMAIN_LABEL_COUNT) {
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
        if (label.startsWith("-") || label.endsWith("-")) {
            return false;
        }
        return label.chars().allMatch(EmailValidator::isAllowedDomainCharacter);
    }

    private static boolean hasEdgeOrConsecutiveDot(String value) {
        return value.startsWith(".") || value.endsWith(".") || value.contains("..");
    }

    private static boolean isAllowedLocalPartCharacter(int character) {
        return isAsciiLetterOrDigit(character)
                || LOCAL_PART_SPECIAL_CHARACTERS.indexOf(character) >= 0;
    }

    private static boolean isAllowedDomainCharacter(int character) {
        return isAsciiLetterOrDigit(character) || character == '-';
    }

    private static boolean isAsciiLetterOrDigit(int character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z')
                || (character >= '0' && character <= '9');
    }
}
