# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Pragmatic RFC 5322 subset (matches the HTML5 email input rule):
    // local part of allowed atom characters, then a domain of dot-separated
    // labels that start/end with an alphanumeric and may contain hyphens.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
            + "@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 overall limit
    private static final int MAX_LOCAL_PART_LENGTH = 64; // RFC 5321 local-part limit

    private EmailValidator() {
    }

    /**
     * Validates an email address for syntactic correctness.
     *
     * @param email the address to validate; may be null
     * @return true if the address is syntactically valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        String localPart = trimmed.substring(0, atIndex);
        if (localPart.startsWith(".") || localPart.endsWith(".") || localPart.contains("..")) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
````

````java
public class EmailValidatorDemo {
    public static void main(String[] args) {
        System.out.println(EmailValidator.isValidEmail("participant_001@example.com")); // true
        System.out.println(EmailValidator.isValidEmail("first.last@sub.example.co.uk")); // true
        System.out.println(EmailValidator.isValidEmail("no-at-sign.example.com"));       // false
        System.out.println(EmailValidator.isValidEmail("double..dot@example.com"));      // false
        System.out.println(EmailValidator.isValidEmail("trailing.@example.com"));        // false
        System.out.println(EmailValidator.isValidEmail("user@-baddomain.com"));          // false
        System.out.println(EmailValidator.isValidEmail(null));                           // false
    }
}
````

### email · run 1 · uncle-bob-junior

````java
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
````

````java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "participant_001@example.com",
            "first.last@example.co.uk",
            "user+tag@sub.example.org",
            "o'brien@example.ie",
            "a@b.co"
    })
    void acceptsWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "plainaddress",
            "@example.com",
            "user@",
            "user@localhost",
            ".leadingdot@example.com",
            "trailingdot.@example.com",
            "double..dot@example.com",
            "user@-example.com",
            "user@example-.com",
            "user@exa mple.com",
            "user name@example.com"
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @org.junit.jupiter.api.Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @org.junit.jupiter.api.Test
    void enforcesRfcLengthLimits() {
        String local64 = "a".repeat(64);
        assertTrue(EmailValidator.isValid(local64 + "@example.com"));
        assertFalse(EmailValidator.isValid("a".repeat(65) + "@example.com"));

        String overallTooLong = "a@" + ("b".repeat(63) + ".").repeat(4) + "com";
        assertFalse(EmailValidator.isValid(overallTooLong.length() > 254
                ? overallTooLong
                : "a".repeat(250) + "@x.co"));
    }
}
````

### email · run 2 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322 subset: dot-separated atoms in the local part,
    // dot-separated alphanumeric labels (hyphens allowed internally) in the
    // domain, ending in a TLD of at least two letters.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
          + "@"
          + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
          + "[A-Za-z]{2,63}$"
    );

    private static final int MAX_EMAIL_LENGTH = 254;  // RFC 5321 overall limit
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    private EmailValidator() {
    }

    /**
     * Validates the syntax of an email address.
     *
     * @param email the address to validate; may be null
     * @return true if the address is syntactically valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
````

````java
EmailValidator.isValidEmail("participant_001@example.com");   // true
EmailValidator.isValidEmail("first.last@sub.example.co.uk");  // true
EmailValidator.isValidEmail("no-at-sign.example.com");        // false
EmailValidator.isValidEmail("double..dot@example.com");       // false
EmailValidator.isValidEmail("user@-bad-label.example.com");   // false
EmailValidator.isValidEmail(null);                            // false
````

### email · run 2 · uncle-bob-junior

````java
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
````

````java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "participant_001@example.com",
            "first.last@sub.example.co.uk",
            "user+tag@example.org",
            "o'brien@example.ie"
    })
    void acceptsWellFormedAddresses(String email) {
        assertTrue(EmailValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "plainaddress",
            "@example.com",
            "user@",
            "user@localhost",
            ".user@example.com",
            "user.@example.com",
            "user..name@example.com",
            "user@-example.com",
            "user@example..com",
            "user name@example.com"
    })
    void rejectsMalformedAddresses(String email) {
        assertFalse(EmailValidator.isValidEmail(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsOverlongLocalPart() {
        String overlongLocalPart = "a".repeat(65);
        assertFalse(EmailValidator.isValidEmail(overlongLocalPart + "@example.com"));
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
            System.exit(1);
            return;
        }

        if (lines.isEmpty()) {
            System.err.println("File is empty: " + file);
            System.exit(1);
            return;
        }

        String[] header = lines.get(0).split(",", -1);
        int amountIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase("amount")) {
                amountIndex = i;
                break;
            }
        }
        if (amountIndex == -1) {
            System.err.println("No 'amount' column found in header: " + lines.get(0));
            System.exit(1);
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            if (amountIndex >= fields.length) {
                System.err.println("Skipping line " + (lineNo + 1) + ": too few columns");
                continue;
            }
            String value = fields[amountIndex].trim();
            try {
                sum = sum.add(new BigDecimal(value));
            } catch (NumberFormatException e) {
                System.err.println("Skipping line " + (lineNo + 1) + ": invalid amount '" + value + "'");
            }
        }

        System.out.println(sum.toPlainString());
    }
}
````

### csv · run 1 · uncle-bob-junior

````java
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String FIELD_SEPARATOR = ",";

    public static void main(String[] args) {
        List<String> lines = readLines(SALES_FILE);
        if (lines.isEmpty()) {
            System.err.println("Error: " + SALES_FILE + " is empty.");
            System.exit(1);
        }

        int amountIndex = findColumnIndex(lines.get(0), AMOUNT_COLUMN);
        if (amountIndex < 0) {
            System.err.println("Error: no '" + AMOUNT_COLUMN + "' column in header.");
            System.exit(1);
        }

        BigDecimal total = sumColumn(lines.subList(1, lines.size()), amountIndex);
        System.out.println(total);
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(FIELD_SEPARATOR);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static BigDecimal sumColumn(List<String> dataLines, int columnIndex) {
        BigDecimal total = BigDecimal.ZERO;
        for (String line : dataLines) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, columnIndex));
        }
        return total;
    }

    private static BigDecimal parseAmount(String line, int columnIndex) {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        if (columnIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + line);
        }
        return new BigDecimal(fields[columnIndex].trim());
    }
}
````

### csv · run 2 · baseline

````java
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("Error: " + file + " is empty.");
                System.exit(1);
            }

            int amountIndex = findAmountColumn(headerLine.split(",", -1));
            if (amountIndex < 0) {
                System.err.println("Error: no 'amount' column found in header: " + headerLine);
                System.exit(1);
            }

            BigDecimal total = BigDecimal.ZERO;
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.err.println("Warning: skipping line " + lineNumber + " (too few columns)");
                    continue;
                }
                String raw = fields[amountIndex].trim();
                try {
                    total = total.add(new BigDecimal(raw));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: skipping line " + lineNumber
                            + " (invalid amount: '" + raw + "')");
                }
            }

            System.out.println(total.toPlainString());
        } catch (IOException e) {
            System.err.println("Error reading " + file + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static int findAmountColumn(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("amount")) {
                return i;
            }
        }
        return -1;
    }
}
````

````
javac SalesSum.java
java SalesSum          # reads sales.csv in the current directory
java SalesSum path/to/other.csv
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads sales.csv from the working directory and prints the sum of its
 * 'amount' column. Expects a header row; fields must not contain embedded
 * commas or quotes.
 */
public final class SalesAmountSummer {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String FIELD_SEPARATOR = ",";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        if (lines.isEmpty()) {
            System.err.println("sales.csv is empty");
            System.exit(1);
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        BigDecimal total = sumAmounts(lines.subList(1, lines.size()), amountColumnIndex);
        System.out.println(total);
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(FIELD_SEPARATOR);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException("No '" + AMOUNT_COLUMN + "' column in header: " + headerLine);
    }

    private static BigDecimal sumAmounts(List<String> dataLines, int amountColumnIndex) {
        BigDecimal total = BigDecimal.ZERO;
        for (String line : dataLines) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, amountColumnIndex));
        }
        return total;
    }

    private static BigDecimal parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        if (amountColumnIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + line);
        }
        return new BigDecimal(fields[amountColumnIndex].trim());
    }
}
````

## retry

### retry · run 1 · baseline

````java
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Runs an operation and retries it when it throws, up to a maximum number
 * of attempts, waiting a fixed delay between attempts.
 *
 * Instances are immutable and safe to share between threads.
 */
public final class Retry {

    private final int maxAttempts;
    private final Duration delay;
    private final Predicate<Exception> retryOn;

    private Retry(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative, was " + delay);
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        this.retryOn = Objects.requireNonNull(retryOn, "retryOn");
    }

    /** Retries on any exception. */
    public static Retry of(int maxAttempts, Duration delay) {
        return new Retry(maxAttempts, delay, e -> true);
    }

    /** Retries only when the thrown exception matches the given predicate. */
    public static Retry of(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        return new Retry(maxAttempts, delay, retryOn);
    }

    /**
     * Executes the operation, retrying on failure.
     *
     * @return the operation's result from the first successful attempt
     * @throws RetryExhaustedException if all attempts fail or the exception is
     *         not retryable; the last failure is its cause, earlier failures
     *         are attached as suppressed exceptions
     * @throws InterruptedException if the thread is interrupted while waiting
     *         between attempts
     */
    public <T> T execute(Callable<T> operation) throws InterruptedException {
        Objects.requireNonNull(operation, "operation");
        Exception[] failures = new Exception[maxAttempts];

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                failures[attempt - 1] = e;
                boolean lastAttempt = attempt == maxAttempts;
                if (lastAttempt || !retryOn.test(e)) {
                    throw exhausted(attempt, e, failures);
                }
                Thread.sleep(delay.toMillis());
            }
        }
        throw new AssertionError("unreachable");
    }

    /** Convenience overload for operations that return no result. */
    public void execute(ThrowingRunnable operation) throws InterruptedException {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private RetryExhaustedException exhausted(int attempts, Exception last, Exception[] failures) {
        RetryExhaustedException ex = new RetryExhaustedException(
                "Operation failed after " + attempts + " attempt(s)", last);
        for (int i = 0; i < attempts - 1; i++) {
            ex.addSuppressed(failures[i]);
        }
        return ex;
    }

    /** Like Runnable, but allowed to throw checked exceptions. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Thrown when every attempt has failed or the failure was not retryable. */
    public static final class RetryExhaustedException extends RuntimeException {
        RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
import java.time.Duration;

public class Example {
    public static void main(String[] args) throws InterruptedException {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        // Operation with a result
        String response = retry.execute(() -> fetchFromApi("participant_001"));
        System.out.println(response);

        // Fire-and-forget operation
        retry.execute(() -> sendNotification("participant_001"));

        // Only retry transient failures
        Retry selective = Retry.of(5, Duration.ofMillis(500),
                e -> e instanceof java.io.IOException);
        selective.execute(() -> uploadReport());
    }

    static String fetchFromApi(String id) { /* ... */ return "ok"; }
    static void sendNotification(String id) { /* ... */ }
    static void uploadReport() throws java.io.IOException { /* ... */ }
}
````

### retry · run 1 · uncle-bob-junior

````java
package retry;

/** An operation that produces a value and may fail with any exception. */
@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}
````

````java
package retry;

/** An operation with no result that may fail with any exception. */
@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Exception;
}
````

````java
package retry;

/**
 * Thrown when an operation still fails after the configured number of attempts.
 * The last failure is the cause; earlier failures are attached as suppressed exceptions.
 */
public final class RetryExhaustedException extends Exception {

    RetryExhaustedException(int attempts, Exception lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
````

````java
package retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Retries an operation up to a maximum number of attempts, waiting a fixed
 * delay between attempts. Instances are immutable and safe to share.
 *
 * <p>Preconditions: {@code maxAttempts >= 1}, {@code delay} is non-negative.
 */
public final class RetryPolicy {

    /** Seam for waiting, so tests never sleep for real. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        return new RetryPolicy(maxAttempts, delayBetweenAttempts,
                duration -> Thread.sleep(duration.toMillis()));
    }

    RetryPolicy(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * Runs the operation, retrying on any exception until it succeeds or
     * the attempt limit is reached.
     *
     * @throws RetryExhaustedException if every attempt failed
     * @throws InterruptedException    if interrupted while waiting between attempts
     */
    public <T> T execute(CheckedSupplier<T> operation)
            throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(operation, "operation");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (attempt > 1) {
                waitBeforeNextAttempt();
            }
            try {
                return operation.get();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw interrupted;
            } catch (Exception failure) {
                if (lastFailure != null) {
                    failure.addSuppressed(lastFailure);
                }
                lastFailure = failure;
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    /** Runs a result-less operation with the same retry behaviour as {@link #execute}. */
    public void executeVoid(CheckedRunnable operation)
            throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private void waitBeforeNextAttempt() throws InterruptedException {
        if (!delayBetweenAttempts.isZero()) {
            sleeper.sleep(delayBetweenAttempts);
        }
    }
}
````

````java
package retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryPolicyTest {

    private static final Duration TEST_DELAY = Duration.ofMillis(100);

    private final List<Duration> recordedSleeps = new ArrayList<>();
    private final RetryPolicy threeAttempts =
            new RetryPolicy(3, TEST_DELAY, recordedSleeps::add);

    @Test
    void returnsResultOnFirstSuccessWithoutWaiting() throws Exception {
        String result = threeAttempts.execute(() -> "ok");

        assertEquals("ok", result);
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndWaitsBetweenAttempts() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        String result = threeAttempts.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("attempt " + calls.get() + " failed");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
        assertEquals(List.of(TEST_DELAY, TEST_DELAY), recordedSleeps);
    }

    @Test
    void throwsRetryExhaustedWithLastFailureAsCauseAndEarlierOnesSuppressed() {
        AtomicInteger calls = new AtomicInteger();

        RetryExhaustedException exhausted = assertThrows(RetryExhaustedException.class,
                () -> threeAttempts.execute(() -> {
                    throw new IllegalStateException("failure " + calls.incrementAndGet());
                }));

        assertEquals(3, calls.get());
        assertEquals("failure 3", exhausted.getCause().getMessage());
        assertEquals(1, exhausted.getCause().getSuppressed().length);
    }

    @Test
    void interruptionWhileWaitingAbortsAndRestoresInterruptFlag() {
        RetryPolicy interruptedPolicy = new RetryPolicy(3, TEST_DELAY, duration -> {
            throw new InterruptedException();
        });

        assertThrows(InterruptedException.class,
                () -> interruptedPolicy.execute(() -> {
                    throw new IllegalStateException("first attempt fails");
                }));
    }

    @Test
    void zeroDelayNeverInvokesSleeper() {
        RetryPolicy noDelay = new RetryPolicy(2, Duration.ZERO, recordedSleeps::add);

        assertThrows(RetryExhaustedException.class,
                () -> noDelay.execute(() -> {
                    throw new IllegalStateException("always fails");
                }));
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, TEST_DELAY));
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(1, Duration.ofMillis(-1)));
    }
}
````

### retry · run 2 · baseline

````java
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Runs an operation and retries it on failure, waiting a fixed delay
 * between attempts.
 */
public final class Retry {

    /** Thrown when all attempts have failed; the last failure is the cause. */
    public static class RetryException extends RuntimeException {
        private final int attempts;

        public RetryException(String message, int attempts, Throwable cause) {
            super(message, cause);
            this.attempts = attempts;
        }

        public int getAttempts() {
            return attempts;
        }
    }

    private final int maxAttempts;
    private final Duration delay;
    private final Predicate<Exception> retryOn;

    private Retry(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        this.retryOn = Objects.requireNonNull(retryOn, "retryOn");
    }

    /** Retries on any exception. */
    public static Retry of(int maxAttempts, Duration delay) {
        return new Retry(maxAttempts, delay, e -> true);
    }

    /** Retries only when the given predicate accepts the thrown exception. */
    public static Retry of(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        return new Retry(maxAttempts, delay, retryOn);
    }

    /**
     * Runs the operation, retrying up to maxAttempts times.
     *
     * @return the operation's result on the first successful attempt
     * @throws RetryException if every attempt failed, with the last exception as cause
     */
    public <T> T call(Callable<T> operation) {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;
                if (!retryOn.test(e) || attempt == maxAttempts) {
                    break;
                }
                sleep(attempt);
            }
        }
        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempt(s)", maxAttempts, lastFailure);
    }

    /** Convenience overload for operations that return nothing. */
    public void run(ThrowingRunnable operation) {
        Objects.requireNonNull(operation, "operation");
        call(() -> {
            operation.run();
            return null;
        });
    }

    private void sleep(int attempt) {
        if (delay.isZero()) {
            return;
        }
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RetryException(
                    "Interrupted while waiting to retry (after attempt " + attempt + ")",
                    attempt, ie);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
````

````java
import java.time.Duration;

public class Example {
    public static void main(String[] args) {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        // Operation with a result:
        String payload = retry.call(() -> fetchFromApi("participant_001"));
        System.out.println(payload);

        // Void operation, retrying only on a specific exception type:
        Retry ioRetry = Retry.of(5, Duration.ofMillis(500),
                e -> e instanceof java.io.IOException);
        ioRetry.run(() -> uploadReport("draw-results.csv"));
    }

    static String fetchFromApi(String id) throws Exception { /* ... */ return "ok"; }
    static void uploadReport(String name) throws Exception { /* ... */ }
}
````

### retry · run 2 · uncle-bob-junior

````java
import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation and retries it when it throws, waiting a fixed delay
 * between attempts.
 *
 * <p>Instances are immutable and safe to share between threads. Any
 * {@link Exception} triggers a retry; {@link Error}s propagate immediately.
 * If all attempts fail, the last exception is thrown with the earlier
 * failures attached as suppressed exceptions.
 */
public final class Retry {

    /** Operation that may fail and can therefore be retried. */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws Exception;
    }

    /** Seam for waiting, so tests can observe delays without sleeping. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public Retry(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts,
                duration -> Thread.sleep(duration.toMillis()));
    }

    Retry(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException(
                    "delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * Runs {@code operation}, retrying on any {@link Exception} up to the
     * configured number of attempts.
     *
     * @return the first successful result
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws Exception the last failure, with earlier failures suppressed
     */
    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception failure) {
                lastFailure = collectFailure(lastFailure, failure);
            }
            if (attempt < maxAttempts) {
                waitBeforeNextAttempt();
            }
        }
        throw lastFailure;
    }

    /** Convenience overload for operations that return nothing. */
    public void executeVoid(RetryableRunnable operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    /** Void counterpart of {@link RetryableOperation}. */
    @FunctionalInterface
    public interface RetryableRunnable {
        void run() throws Exception;
    }

    private static Exception collectFailure(Exception previous, Exception current) {
        if (previous != null) {
            current.addSuppressed(previous);
        }
        return current;
    }

    private void waitBeforeNextAttempt() throws InterruptedException {
        try {
            sleeper.sleep(delayBetweenAttempts);
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw interruption;
        }
    }
}
````

````java
Retry retry = new Retry(3, Duration.ofMillis(500));

String response = retry.execute(() -> httpClient.fetch("https://example.org"));
retry.executeVoid(() -> repository.save(record));
````

````java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryTest {

    private static final Duration NO_DELAY = Duration.ZERO;
    private static final Duration TEST_DELAY = Duration.ofMillis(100);

    private final List<Duration> recordedSleeps = new ArrayList<>();
    private final Retry.Sleeper recordingSleeper = recordedSleeps::add;

    @Test
    void returnsResultOnFirstSuccessWithoutWaiting() throws Exception {
        Retry retry = new Retry(3, TEST_DELAY, recordingSleeper);

        String result = retry.execute(() -> "ok");

        assertEquals("ok", result);
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndWaitsBetweenAttempts() throws Exception {
        Retry retry = new Retry(3, TEST_DELAY, recordingSleeper);
        AtomicInteger attempts = new AtomicInteger();

        String result = retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
        assertEquals(List.of(TEST_DELAY, TEST_DELAY), recordedSleeps);
    }

    @Test
    void throwsLastFailureWithEarlierFailuresSuppressed() {
        Retry retry = new Retry(2, NO_DELAY, recordingSleeper);
        IllegalStateException first = new IllegalStateException("first");
        IllegalStateException second = new IllegalStateException("second");
        AtomicInteger attempts = new AtomicInteger();

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> retry.execute(() -> {
                    throw attempts.incrementAndGet() == 1 ? first : second;
                }));

        assertSame(second, thrown);
        assertEquals(List.of(first), List.of(thrown.getSuppressed()));
    }

    @Test
    void singleAttemptFailureIsThrownWithoutWaiting() {
        Retry retry = new Retry(1, TEST_DELAY, recordingSleeper);

        assertThrows(IllegalStateException.class, () -> retry.execute(() -> {
            throw new IllegalStateException("boom");
        }));
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void interruptionDuringWaitStopsRetryingAndRestoresFlag() {
        Retry retry = new Retry(3, TEST_DELAY, duration -> {
            throw new InterruptedException("interrupted");
        });

        assertThrows(InterruptedException.class, () -> retry.execute(() -> {
            throw new IllegalStateException("fails once");
        }));
        assertTrue(Thread.interrupted(), "interrupt flag must be restored");
    }

    @Test
    void executeVoidRetriesLikeExecute() throws Exception {
        Retry retry = new Retry(2, NO_DELAY, recordingSleeper);
        AtomicInteger attempts = new AtomicInteger();

        retry.executeVoid(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new IllegalStateException("not yet");
            }
        });

        assertEquals(2, attempts.get());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, NO_DELAY));
        assertThrows(IllegalArgumentException.class,
                () -> new Retry(1, Duration.ofMillis(-1)));
    }
}
````

## ratelimit

### ratelimit · run 1 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter keyed by client identifier (IP address).
 * Thread-safe: HttpServer may dispatch concurrent requests.
 */
final class RateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    /**
     * Returns 0 if the request is allowed, otherwise the number of seconds
     * the client should wait before retrying.
     */
    long tryAcquire(String clientKey) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps =
                requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            long cutoff = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                long oldest = timestamps.peekFirst();
                long retryAfterMillis = (oldest + windowMillis) - now;
                return Math.max(1, (retryAfterMillis + 999) / 1000);
            }
            timestamps.addLast(now);
            return 0;
        }
    }

    /** Drop entries for clients with no requests in the current window. */
    void evictStale() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                Long newest = timestamps.peekLast();
                return newest == null || newest <= cutoff;
            }
        });
    }
}

/** Wraps any HttpHandler and enforces the per-client rate limit. */
final class RateLimitingHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter limiter;

    RateLimitingHandler(HttpHandler delegate, RateLimiter limiter) {
        this.delegate = delegate;
        this.limiter = limiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long retryAfterSeconds = limiter.tryAcquire(clientKey);

        if (retryAfterSeconds > 0) {
            byte[] body = "Too many requests. Please try again later.\n"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After",
                    Long.toString(retryAfterSeconds));
            exchange.getResponseHeaders().set("Content-Type",
                    "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private static String clientKey(HttpExchange exchange) {
        // If behind a trusted reverse proxy, prefer X-Forwarded-For instead.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        // 5 requests per client per 60 seconds
        RateLimiter limiter = new RateLimiter(5, 60_000);

        HttpHandler businessHandler = exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitingHandler(businessHandler, limiter));
        server.start();

        // Periodically evict idle clients so the map doesn't grow unbounded.
        Thread evictor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                limiter.evictStale();
            }
        }, "rate-limiter-evictor");
        evictor.setDaemon(true);
        evictor.start();

        System.out.println("Server running on http://localhost:8080");
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
package ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter: each client may make at most
 * {@code maxRequestsPerWindow} requests within any rolling {@code window}.
 *
 * Thread-safe. State is in-memory, so limits apply per server instance.
 * Time is injected by the caller, which keeps this class a pure function
 * of its inputs and makes it testable without sleeping.
 */
public final class SlidingWindowRateLimiter {

    /** Outcome of one acquire attempt; {@code retryAfterSeconds} is 0 when allowed. */
    public record Decision(boolean allowed, long retryAfterSeconds) {}

    private static final long MILLIS_PER_SECOND = 1000;

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Map<String, Deque<Instant>> requestTimesByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window) {
        if (maxRequestsPerWindow < 1) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be >= 1, was " + maxRequestsPerWindow);
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
    }

    /** Records the request if the client is within its limit, and says whether it was. */
    public Decision tryAcquire(String clientId, Instant now) {
        // compute() makes evict-decide-append atomic per client; the one-element
        // array carries the lambda's decision out to the caller.
        Decision[] decision = new Decision[1];
        requestTimesByClient.compute(clientId, (id, times) -> {
            Deque<Instant> recent = times == null ? new ArrayDeque<>() : times;
            evictOutsideWindow(recent, now);
            decision[0] = decide(recent, now);
            if (decision[0].allowed()) {
                recent.addLast(now);
            }
            return recent.isEmpty() ? null : recent; // drop idle clients so the map cannot grow unbounded
        });
        return decision[0];
    }

    private Decision decide(Deque<Instant> recentRequests, Instant now) {
        if (recentRequests.size() < maxRequestsPerWindow) {
            return new Decision(true, 0);
        }
        Instant oldestLeavesWindowAt = recentRequests.peekFirst().plus(window);
        return new Decision(false, ceilSeconds(Duration.between(now, oldestLeavesWindowAt)));
    }

    private void evictOutsideWindow(Deque<Instant> recentRequests, Instant now) {
        Instant windowStart = now.minus(window);
        while (!recentRequests.isEmpty() && !recentRequests.peekFirst().isAfter(windowStart)) {
            recentRequests.removeFirst();
        }
    }

    /** Rounds up so a client that waits exactly Retry-After seconds is admitted. */
    private static long ceilSeconds(Duration duration) {
        return Math.max(1, (duration.toMillis() + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND);
    }
}
````

````java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

/**
 * Decorates any {@link HttpHandler} with per-client rate limiting.
 *
 * Clients are identified by remote IP address. A client over its limit
 * receives 429 with a Retry-After header, and the delegate is never invoked.
 */
public final class RateLimitingHandler implements HttpHandler {

    static final int HTTP_TOO_MANY_REQUESTS = 429;
    static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final byte[] REJECTION_BODY =
            "Too many requests, please retry later.\n".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter limiter;
    private final Clock clock;

    public RateLimitingHandler(HttpHandler delegate, SlidingWindowRateLimiter limiter, Clock clock) {
        this.delegate = delegate;
        this.limiter = limiter;
        this.clock = clock;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SlidingWindowRateLimiter.Decision decision =
                limiter.tryAcquire(clientIdFor(exchange), clock.instant());
        if (!decision.allowed()) {
            reject(exchange, decision.retryAfterSeconds());
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: remote IP only — behind a reverse proxy all clients share the proxy's IP;
    // switch to X-Forwarded-For from the trusted proxy hop when one is introduced.
    private static String clientIdFor(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void reject(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, Long.toString(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, REJECTION_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(REJECTION_BODY);
        }
    }
}
````

````java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/** Example wiring: any existing handler gains rate limiting by being wrapped. */
public final class Server {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int HTTP_OK = 200;

    public static void main(String[] args) throws IOException {
        HttpHandler greeting = exchange -> respondOk(exchange, "Hello, participant_001!\n");
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitingHandler(greeting, limiter, Clock.systemUTC()));
        server.start();
        System.out.println("Listening on http://localhost:" + PORT);
    }

    private static void respondOk(HttpExchange exchange, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(HTTP_OK, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
````

````java
package ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final String CLIENT = "203.0.113.7";

    private final SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(LIMIT, WINDOW);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(CLIENT, START.plusSeconds(i)).allowed());
        }
    }

    @Test
    void rejectsTheRequestOverTheLimitAndSaysWhenToRetry() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT, START);
        }
        SlidingWindowRateLimiter.Decision decision =
                limiter.tryAcquire(CLIENT, START.plusSeconds(10));

        assertFalse(decision.allowed());
        assertEquals(50, decision.retryAfterSeconds()); // oldest request leaves the window at +60s
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT, START);
        }
        assertTrue(limiter.tryAcquire(CLIENT, START.plus(WINDOW)).allowed());
    }

    @Test
    void windowSlidesInsteadOfResetting() {
        limiter.tryAcquire(CLIENT, START);
        limiter.tryAcquire(CLIENT, START.plusSeconds(30));
        limiter.tryAcquire(CLIENT, START.plusSeconds(30));

        assertFalse(limiter.tryAcquire(CLIENT, START.plusSeconds(59)).allowed());
        // at +60s only the first request has expired, freeing exactly one slot
        assertTrue(limiter.tryAcquire(CLIENT, START.plusSeconds(60)).allowed());
        assertFalse(limiter.tryAcquire(CLIENT, START.plusSeconds(60)).allowed());
    }

    @Test
    void tracksEachClientIndependently() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT, START);
        }
        assertTrue(limiter.tryAcquire("198.51.100.9", START).allowed());
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowRateLimiter(0, WINDOW));
    }
}
````

````java
package ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimitingHandlerTest {

    private static final int LIMIT = 2;
    private static final int HTTP_OK = 200;
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private final HttpHandler okDelegate = exchange -> exchange.sendResponseHeaders(HTTP_OK, -1);
    private final RateLimitingHandler handler = new RateLimitingHandler(
            okDelegate, new SlidingWindowRateLimiter(LIMIT, Duration.ofMinutes(1)), FIXED_CLOCK);

    @Test
    void delegatesWhileClientIsUnderTheLimit() throws IOException {
        RecordingExchange exchange = new RecordingExchange("203.0.113.7");
        handler.handle(exchange);
        assertEquals(HTTP_OK, exchange.getResponseCode());
    }

    @Test
    void answers429WithRetryAfterOnceOverTheLimit() throws IOException {
        for (int i = 0; i < LIMIT; i++) {
            handler.handle(new RecordingExchange("203.0.113.7"));
        }
        RecordingExchange exchange = new RecordingExchange("203.0.113.7");
        handler.handle(exchange);

        assertEquals(RateLimitingHandler.HTTP_TOO_MANY_REQUESTS, exchange.getResponseCode());
        assertEquals("60", exchange.getResponseHeaders()
                .getFirst(RateLimitingHandler.RETRY_AFTER_HEADER));
    }

    @Test
    void doesNotPenaliseOtherClients() throws IOException {
        for (int i = 0; i < LIMIT; i++) {
            handler.handle(new RecordingExchange("203.0.113.7"));
        }
        RecordingExchange otherClient = new RecordingExchange("198.51.100.9");
        handler.handle(otherClient);
        assertEquals(HTTP_OK, otherClient.getResponseCode());
    }

    /** Minimal in-memory HttpExchange so tests need no sockets or mocking framework. */
    private static final class RecordingExchange extends HttpExchange {
        private static final int CLIENT_PORT = 40000;

        private final InetSocketAddress remoteAddress;
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        RecordingExchange(String clientIp) {
            this.remoteAddress = new InetSocketAddress(clientIp, CLIENT_PORT);
        }

        @Override public InetSocketAddress getRemoteAddress() { return remoteAddress; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public int getResponseCode() { return responseCode; }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public void sendResponseHeaders(int code, long length) { this.responseCode = code; }

        @Override public Headers getRequestHeaders() { return new Headers(); }
        @Override public URI getRequestURI() { return URI.create("/"); }
        @Override public String getRequestMethod() { return "GET"; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return new ByteArrayInputStream(new byte[0]); }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream in, OutputStream out) {}
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}
````

### ratelimit · run 2 · baseline

````java
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Sliding-window rate limiter, keyed per client. Thread-safe.
 */
public final class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rate-limiter-cleanup");
                t.setDaemon(true);
                return t;
            });

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.windowMillis = window.toMillis();
        // Evict idle clients so the map cannot grow without bound.
        cleaner.scheduleAtFixedRate(this::evictIdleClients,
                windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * @return 0 if the request is allowed, otherwise the number of seconds
     *         the client should wait before retrying.
     */
    public long tryAcquire(String clientKey) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps =
                requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            long cutoff = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.pollFirst();
            }
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return 0;
            }
            long oldest = timestamps.peekFirst();
            long retryAfterMillis = (oldest + windowMillis) - now;
            return Math.max(1, (retryAfterMillis + 999) / 1000);
        }
    }

    private void evictIdleClients() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                Long newest = timestamps.peekLast();
                return newest == null || newest <= cutoff;
            }
        });
    }
}
````

````java
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Wraps any HttpHandler and rejects clients that exceed the rate limit.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final RateLimiter limiter;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter limiter) {
        this.delegate = delegate;
        this.limiter = limiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long retryAfterSeconds = limiter.tryAcquire(clientKey);

        if (retryAfterSeconds > 0) {
            byte[] body = "Too many requests. Please try again later.\n"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After",
                    Long.toString(retryAfterSeconds));
            exchange.getResponseHeaders().set("Content-Type",
                    "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        // Behind a trusted reverse proxy, prefer the first entry of
        // X-Forwarded-For instead. Do NOT trust that header from the
        // open internet, as clients can forge it.
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }
}
````

````java
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.sun.net.httpserver.HttpServer;

public final class Main {

    public static void main(String[] args) throws Exception {
        // 5 requests per client per rolling 60-second window.
        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitingHandler(exchange -> {
            byte[] body = "Hello\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }, limiter));
        server.start();
        System.out.println("Listening on http://localhost:8080");
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sliding-window rate limiter: each client may make at most
 * {@code maxRequests} requests within any {@code window}-long period.
 *
 * <p>Thread-safe. State is in-memory, so limits apply per process,
 * not across a cluster.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentMap<String, Deque<Long>> requestTimesByClient =
            new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /**
     * Records a request for {@code clientId} if the client is under its limit.
     *
     * @return true if the request is allowed, false if the client must wait
     */
    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        Deque<Long> requestTimes =
                requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (requestTimes) {
            evictExpired(requestTimes, now);
            if (requestTimes.size() >= maxRequestsPerWindow) {
                return false;
            }
            requestTimes.addLast(now);
            return true;
        }
    }

    /** Seconds until {@code clientId} may retry; zero if it may retry now. */
    public long secondsUntilRetry(String clientId) {
        Deque<Long> requestTimes = requestTimesByClient.get(clientId);
        if (requestTimes == null) {
            return 0;
        }
        synchronized (requestTimes) {
            evictExpired(requestTimes, clock.millis());
            if (requestTimes.size() < maxRequestsPerWindow) {
                return 0;
            }
            long oldestExpiresAt = requestTimes.peekFirst() + window.toMillis();
            long millisUntilRetry = Math.max(0, oldestExpiresAt - clock.millis());
            return (millisUntilRetry + 999) / 1000; // round up to whole seconds
        }
    }

    private void evictExpired(Deque<Long> requestTimes, long now) {
        long windowStart = now - window.toMillis();
        while (!requestTimes.isEmpty() && requestTimes.peekFirst() <= windowStart) {
            requestTimes.removeFirst();
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decorator that rejects requests over the per-client limit with 429,
 * delegating allowed requests to the wrapped handler.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter rateLimiter;

    public RateLimitingHandler(HttpHandler delegate, SlidingWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectTooManyRequests(exchange, clientId);
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: keys on the socket address; behind a proxy or load balancer,
    // switch to a trusted X-Forwarded-For header or all clients share one key.
    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectTooManyRequests(HttpExchange exchange, String clientId) throws IOException {
        byte[] body = "Too many requests. Please retry later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders()
                .set("Retry-After", Long.toString(rateLimiter.secondsUntilRetry(clientId)));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/** Wires the rate limiter around an example handler. */
public final class RateLimitedServer {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_WINDOW, WINDOW, Clock.systemUTC());
        server.createContext("/", new RateLimitingHandler(RateLimitedServer::sayHello, rateLimiter));
        server.start();
    }

    private static void sayHello(HttpExchange exchange) throws IOException {
        byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
````

````java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Test clock that only moves when told to, so the window is deterministic. */
    private static final class ManualClock extends Clock {
        private Instant now = Instant.parse("2026-08-27T12:00:00Z");

        void advance(Duration duration) { now = now.plus(duration); }

        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    private final ManualClock clock = new ManualClock();
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsOverTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void limitsClientsIndependently() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        assertTrue(limiter.tryAcquire("client-b"));
    }

    @Test
    void allowsAgainOnceTheWindowSlidesPast() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void reportsSecondsUntilRetryWhenBlocked() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        clock.advance(Duration.ofSeconds(20));
        assertEquals(40, limiter.secondsUntilRetry("client-a"));
    }

    @Test
    void reportsZeroRetryDelayForUnknownClient() {
        assertEquals(0, limiter.secondsUntilRetry("client-x"));
    }
}
````

## order

### order · run 1 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Processes an order: validates line items, applies a 10% discount when the
 * pre-VAT subtotal exceeds 100.00 EUR, adds 21% VAT on the discounted amount,
 * and produces a receipt string.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /** A single order line: product name, quantity, and unit price excluding VAT. */
    public record LineItem(String name, int quantity, BigDecimal unitPrice) { }

    /** Computed totals plus the formatted receipt. */
    public record Receipt(BigDecimal subtotal, BigDecimal discount, BigDecimal vat,
                          BigDecimal total, String text) { }

    private OrderProcessor() { }

    public static Receipt processOrder(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(subtotal, discount, vat, total,
                buildReceiptText(items, subtotal, discount, vat, total));
    }

    private static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new IllegalArgumentException("Line item " + (i + 1) + " is null");
            }
            if (item.name() == null || item.name().isBlank()) {
                throw new IllegalArgumentException("Line item " + (i + 1) + " has no name");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.name() + "' has non-positive quantity: " + item.quantity());
            }
            if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.name() + "' has a missing or negative unit price");
            }
        }
    }

    private static String buildReceiptText(List<LineItem> items, BigDecimal subtotal,
                                           BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("--------------------------------------------\n");
        for (LineItem item : items) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(SCALE, ROUNDING);
            sb.append(String.format("%-20s %3d x %8s EUR %10s%n",
                    item.name(), item.quantity(),
                    item.unitPrice().setScale(SCALE, ROUNDING), lineTotal));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-33s EUR %10s%n", "Subtotal (excl. VAT)", subtotal));
        if (discount.signum() > 0) {
            sb.append(String.format("%-33s EUR %10s%n", "Discount (10%)", discount.negate()));
        }
        sb.append(String.format("%-33s EUR %10s%n", "VAT (21%)", vat));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-33s EUR %10s%n", "TOTAL", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 2, new BigDecimal("34.95")));

        Receipt receipt = processOrder(order);
        System.out.print(receipt.text());
    }
}
````

### order · run 1 · uncle-bob-junior

````java
import java.math.BigDecimal;

/** One validated order line. Prices are in euros, VAT-exclusive. */
public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item needs a description");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be zero or positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
import java.math.BigDecimal;

/** All amounts in euros, rounded to cents. Discount and VAT are already applied. */
public record OrderTotals(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal netAmount,
        BigDecimal vat,
        BigDecimal grandTotal) {

    public boolean hasDiscount() {
        return discount.signum() > 0;
    }
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENTS = 2;

    private OrderCalculator() {
    }

    /** Discount applies to the pre-VAT subtotal; VAT is charged on the discounted net. */
    public static OrderTotals totalsFor(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line item");
        }
        BigDecimal subtotal = roundToCents(sumOf(items));
        BigDecimal discount = roundToCents(discountOn(subtotal));
        BigDecimal netAmount = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(netAmount.multiply(VAT_RATE));
        return new OrderTotals(subtotal, discount, netAmount, vat, netAmount.add(vat));
    }

    private static BigDecimal sumOf(List<LineItem> items) {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountOn(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }
}
````

````java
import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private ReceiptFormatter() {
    }

    public static String receiptFor(List<LineItem> items) {
        OrderTotals totals = OrderCalculator.totalsFor(items);
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(lineFor(item)));
        receipt.append(totalsBlockFor(totals));
        return receipt.toString();
    }

    private static String lineFor(LineItem item) {
        return String.format(Locale.ROOT, "%-20s %3d x %9.2f = %10.2f%n",
                item.description(), item.quantity(), item.unitPrice(), item.lineTotal());
    }

    private static String totalsBlockFor(OrderTotals totals) {
        StringBuilder block = new StringBuilder();
        block.append(amountRow("Subtotal (excl. VAT)", totals.subtotal()));
        if (totals.hasDiscount()) {
            block.append(amountRow("Discount (10%)", totals.discount().negate()));
            block.append(amountRow("Net (excl. VAT)", totals.netAmount()));
        }
        block.append(amountRow("VAT (21%)", totals.vat()));
        block.append(amountRow("TOTAL (EUR)", totals.grandTotal()));
        return block.toString();
    }

    private static String amountRow(String label, java.math.BigDecimal amount) {
        return String.format(Locale.ROOT, "%-25s %12.2f%n", label, amount);
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCalculatorTest {

    private static LineItem item(String description, String unitPrice, int quantity) {
        return new LineItem(description, new BigDecimal(unitPrice), quantity);
    }

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        OrderTotals totals = OrderCalculator.totalsFor(List.of(item("Ticket", "25.00", 3)));

        assertEquals(new BigDecimal("75.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("15.75"), totals.vat());
        assertEquals(new BigDecimal("90.75"), totals.grandTotal());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        OrderTotals totals = OrderCalculator.totalsFor(List.of(item("Bundle", "60.00", 2)));

        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("108.00"), totals.netAmount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.grandTotal());
    }

    @Test
    void exactlyOneHundredEurosGetsNoDiscount() {
        OrderTotals totals = OrderCalculator.totalsFor(List.of(item("Ticket", "100.00", 1)));

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.grandTotal());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderCalculator.totalsFor(List.of()));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> item(" ", "10.00", 1));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", "-1.00", 1));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", "10.00", 0));
    }

    @Test
    void receiptShowsDiscountRowOnlyWhenGranted() {
        String discounted = ReceiptFormatter.receiptFor(List.of(item("Bundle", "60.00", 2)));
        String plain = ReceiptFormatter.receiptFor(List.of(item("Ticket", "25.00", 3)));

        assertTrue(discounted.contains("Discount (10%)"));
        assertTrue(discounted.contains("130.68"));
        assertFalse(plain.contains("Discount"));
        assertTrue(plain.contains("90.75"));
    }
}
````

### order · run 2 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/** A single order line: a product name, a quantity, and a unit price in EUR (excl. VAT). */
record LineItem(String name, int quantity, BigDecimal unitPrice) {

    LineItem {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Line item name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Line item '" + name + "': quantity must be positive, was " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Line item '" + name + "': unit price must be non-negative, was " + unitPrice);
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/** Processes an order: validates items, applies discount and VAT, and renders a receipt. */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public String processOrder(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = netAfterDiscount.add(vat);

        return buildReceipt(items, subtotal, discount, netAfterDiscount, vat, total, discountApplies);
    }

    private String buildReceipt(List<LineItem> items,
                                BigDecimal subtotal,
                                BigDecimal discount,
                                BigDecimal net,
                                BigDecimal vat,
                                BigDecimal total,
                                boolean discountApplies) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("--------------------------------------------\n");
        for (LineItem item : items) {
            sb.append(String.format(Locale.ROOT, "%-20s %3d x %8s = %10s%n",
                    item.name(), item.quantity(),
                    money(item.unitPrice().setScale(SCALE, ROUNDING)),
                    money(item.lineTotal().setScale(SCALE, ROUNDING))));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "Subtotal (excl. VAT)", money(subtotal)));
        if (discountApplies) {
            sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "Discount (10%)", "-" + money(discount)));
            sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "Net after discount", money(net)));
        }
        sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "VAT (21%)", money(vat)));
        sb.append("--------------------------------------------\n");
        sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "TOTAL", money(total)));
        return sb.toString();
    }

    private static String money(BigDecimal amount) {
        return "EUR " + amount.toPlainString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> order = List.of(
                new LineItem("Widget", 3, new BigDecimal("25.00")),
                new LineItem("Gadget", 2, new BigDecimal("19.95"))
        );
        System.out.println(processor.processOrder(order));
    }
}
````

````
RECEIPT
--------------------------------------------
Widget                 3 x EUR 25.00 = EUR 75.00
Gadget                 2 x EUR 19.95 = EUR 39.90
--------------------------------------------
Subtotal (excl. VAT)          EUR 114.90
Discount (10%)               -EUR 11.49
Net after discount            EUR 103.41
VAT (21%)                     EUR 21.72
--------------------------------------------
TOTAL                         EUR 125.13
````

### order · run 2 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** A single order line. Immutable; validated once at construction (the trust boundary). */
record LineItem(String description, int quantity, BigDecimal unitPriceEur) {

    LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, was: " + quantity);
        }
        if (unitPriceEur == null || unitPriceEur.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive, was: " + unitPriceEur);
        }
    }

    BigDecimal lineTotal() {
        return unitPriceEur.multiply(BigDecimal.valueOf(quantity));
    }
}

/** Monetary breakdown of a processed order. All amounts in EUR, rounded to cents. */
record OrderTotals(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal grandTotal
) {}

/**
 * Computes order totals and renders a receipt.
 *
 * Pricing rules: unit prices exclude VAT; a 10% discount applies to the
 * pre-VAT subtotal when it exceeds EUR 100.00; 21% VAT is charged on the
 * discounted amount.
 */
final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD_EUR = new BigDecimal("100.00");
    private static final int CENTS_SCALE = 2;

    private OrderProcessor() {
    }

    static String processOrder(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        OrderTotals totals = computeTotals(items);
        return renderReceipt(items, totals);
    }

    static OrderTotals computeTotals(List<LineItem> items) {
        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CENTS_SCALE, RoundingMode.HALF_UP);

        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal grandTotal = discountedSubtotal.add(vat);

        return new OrderTotals(subtotal, discount, vat, grandTotal);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD_EUR) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS_SCALE, RoundingMode.UNNECESSARY);
        }
        return roundToCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS_SCALE, RoundingMode.HALF_UP);
    }

    private static String renderReceipt(List<LineItem> items, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-20s %3d x EUR %8.2f = EUR %9.2f%n",
                    item.description(), item.quantity(), item.unitPriceEur(), item.lineTotal()));
        }
        receipt.append(String.format("%-38s EUR %9.2f%n", "Subtotal (excl. VAT)", totals.subtotal()));
        if (totals.discount().signum() > 0) {
            receipt.append(String.format("%-38s EUR %9.2f%n", "Discount (10%)", totals.discount().negate()));
        }
        receipt.append(String.format("%-38s EUR %9.2f%n", "VAT (21%)", totals.vat()));
        receipt.append(String.format("%-38s EUR %9.2f%n", "TOTAL", totals.grandTotal()));
        return receipt.toString();
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private static LineItem item(String description, int quantity, String unitPrice) {
        return new LineItem(description, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        OrderTotals totals = OrderProcessor.computeTotals(List.of(item("Notebook", 2, "25.00")));

        assertEquals(new BigDecimal("50.00"), totals.subtotal());
        assertEquals(0, totals.discount().signum());
        assertEquals(new BigDecimal("10.50"), totals.vat());
        assertEquals(new BigDecimal("60.50"), totals.grandTotal());
    }

    @Test
    void noDiscountAtExactlyOneHundredEuros() {
        OrderTotals totals = OrderProcessor.computeTotals(List.of(item("Chair", 1, "100.00")));

        assertEquals(0, totals.discount().signum());
        assertEquals(new BigDecimal("121.00"), totals.grandTotal());
    }

    @Test
    void appliesDiscountAboveThresholdAndVatOnDiscountedAmount() {
        OrderTotals totals = OrderProcessor.computeTotals(List.of(item("Desk", 1, "200.00")));

        assertEquals(new BigDecimal("20.00"), totals.discount());
        assertEquals(new BigDecimal("37.80"), totals.vat());   // 21% of 180.00
        assertEquals(new BigDecimal("217.80"), totals.grandTotal());
    }

    @Test
    void receiptListsItemsAndTotals() {
        String receipt = OrderProcessor.processOrder(List.of(item("Desk", 1, "200.00")));

        assertTrue(receipt.contains("Desk"));
        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("217.80"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderProcessor.processOrder(List.of()));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> item(" ", 1, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Pen", 0, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Pen", 1, "-1.00"));
    }
}
````
