# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical pattern: dot-separated atoms in the local part (no leading/trailing/double dots),
    // dot-separated domain labels that don't start or end with a hyphen, alphabetic TLD of 2+ chars.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+"
            + "[A-Za-z]{2,63}$");

    private static final int MAX_TOTAL_LENGTH = 254; // RFC 5321 practical limit
    private static final int MAX_LOCAL_LENGTH = 64;  // RFC 5321 local-part limit

    private EmailValidator() {
    }

    /**
     * Validates an email address for typical application use (e.g. registration forms).
     *
     * @param email the address to validate; may be null
     * @return true if the address is structurally valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_TOTAL_LENGTH) {
            return false;
        }

        int atIndex = trimmed.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
````

````java
public static void main(String[] args) {
    System.out.println(EmailValidator.isValidEmail("participant_001@example.com"));  // true
    System.out.println(EmailValidator.isValidEmail("first.last+tag@example.co.uk")); // true
    System.out.println(EmailValidator.isValidEmail("no-at-sign.example.com"));       // false
    System.out.println(EmailValidator.isValidEmail(".leading.dot@example.com"));     // false
    System.out.println(EmailValidator.isValidEmail("user@-badlabel-.com"));          // false
    System.out.println(EmailValidator.isValidEmail(null));                           // false
}
````

### email · run 1 · uncle-bob-junior

````java
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
````

````java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void acceptsWellFormedAddresses() {
        assertTrue(EmailValidator.isValidEmail("participant_001@example.com"));
        assertTrue(EmailValidator.isValidEmail("first.last@sub.example.co.uk"));
        assertTrue(EmailValidator.isValidEmail("user+tag@example.org"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
    }

    @Test
    void rejectsMissingOrMisplacedAtSign() {
        assertFalse(EmailValidator.isValidEmail("no-at-sign.example.com"));
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail("user@"));
    }

    @Test
    void rejectsBadLocalParts() {
        assertFalse(EmailValidator.isValidEmail(".starts.with.dot@example.com"));
        assertFalse(EmailValidator.isValidEmail("ends.with.dot.@example.com"));
        assertFalse(EmailValidator.isValidEmail("double..dot@example.com"));
        assertFalse(EmailValidator.isValidEmail("a".repeat(65) + "@example.com"));
    }

    @Test
    void rejectsBadDomains() {
        assertFalse(EmailValidator.isValidEmail("user@localhost"));
        assertFalse(EmailValidator.isValidEmail("user@example..com"));
        assertFalse(EmailValidator.isValidEmail("user@-example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example-.com"));
        assertFalse(EmailValidator.isValidEmail("user@exa mple.com"));
    }

    @Test
    void rejectsOverlongTotalLength() {
        String overlong = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(130) + ".com";
        assertFalse(EmailValidator.isValidEmail(overlong));
    }
}
````

### email · run 2 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    // Practical RFC 5322 subset: dot-atom local part, domain labels of
    // letters/digits/hyphens (no leading or trailing hyphen), TLD of 2+ letters.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

    private EmailValidator() {
    }

    /**
     * Validates an email address syntactically.
     *
     * @param email the address to check, may be null
     * @return true if the address is structurally valid
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.length() > MAX_EMAIL_LENGTH) {
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
EmailValidator.isValid("participant_001@example.com");   // true
EmailValidator.isValid("first.last@sub.example.co.uk");  // true
EmailValidator.isValid("no-at-sign.example.com");        // false
EmailValidator.isValid("trailing.dot.@example.com");     // false
EmailValidator.isValid(null);                            // false
````

### email · run 2 · uncle-bob-junior

````java
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
````

````java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    @Test
    void acceptsTypicalAddresses() {
        assertTrue(EmailValidator.isValidEmail("participant_001@example.com"));
        assertTrue(EmailValidator.isValidEmail("first.last+tag@sub.example.co.uk"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertFalse(EmailValidator.isValidEmail(null));
        assertFalse(EmailValidator.isValidEmail(""));
    }

    @Test
    void rejectsMissingOrDuplicateSeparator() {
        assertFalse(EmailValidator.isValidEmail("no-separator.example.com"));
        assertFalse(EmailValidator.isValidEmail("two@@example.com"));
        assertFalse(EmailValidator.isValidEmail("a@b@example.com"));
    }

    @Test
    void rejectsBadLocalPart() {
        assertFalse(EmailValidator.isValidEmail("@example.com"));
        assertFalse(EmailValidator.isValidEmail(".leading@example.com"));
        assertFalse(EmailValidator.isValidEmail("double..dot@example.com"));
        assertFalse(EmailValidator.isValidEmail("a".repeat(65) + "@example.com"));
    }

    @Test
    void rejectsBadDomain() {
        assertFalse(EmailValidator.isValidEmail("user@"));
        assertFalse(EmailValidator.isValidEmail("user@localhost"));
        assertFalse(EmailValidator.isValidEmail("user@-bad.example.com"));
        assertFalse(EmailValidator.isValidEmail("user@example..com"));
        assertFalse(EmailValidator.isValidEmail("user@example.com."));
    }

    @Test
    void enforcesTotalLengthLimit() {
        String tooLong = "a".repeat(64) + "@" + "b".repeat(60) + "." + "c".repeat(60)
                + "." + "d".repeat(60) + ".example.com";
        assertFalse(EmailValidator.isValidEmail(tooLong));
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
import java.util.ArrayList;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty()) {
                System.err.println("Error: sales.csv is empty.");
                System.exit(1);
            }

            List<String> header = parseCsvLine(lines.get(0));
            int amountIndex = -1;
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i).trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            if (amountIndex < 0) {
                System.err.println("Error: no 'amount' column found in header: " + header);
                System.exit(1);
            }

            BigDecimal sum = BigDecimal.ZERO;
            for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
                String line = lines.get(lineNo);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseCsvLine(line);
                if (fields.size() <= amountIndex) {
                    System.err.println("Warning: skipping line " + (lineNo + 1) + " (too few fields)");
                    continue;
                }
                String raw = fields.get(amountIndex).trim();
                if (raw.isEmpty()) {
                    continue;
                }
                try {
                    sum = sum.add(new BigDecimal(raw));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: skipping line " + (lineNo + 1)
                            + " (invalid amount: '" + raw + "')");
                }
            }

            System.out.println(sum.toPlainString());
        } catch (IOException e) {
            System.err.println("Error reading " + file + ": " + e.getMessage());
            System.exit(1);
        }
    }

    // Minimal CSV parser: handles commas inside double-quoted fields and "" escapes.
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
````

````
javac SalesSum.java
java SalesSum
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
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        List<String> lines = readLines(SALES_FILE);
        if (lines.isEmpty()) {
            System.out.println(BigDecimal.ZERO);
            return;
        }

        int amountIndex = findColumnIndex(lines.get(0), AMOUNT_COLUMN);
        BigDecimal total = sumColumn(lines.subList(1, lines.size()), amountIndex);
        System.out.println(total);
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Missing column: " + columnName);
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
        String[] fields = line.split(DELIMITER, -1);
        if (columnIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + line);
        }
        return new BigDecimal(fields[columnIndex].trim());
    }
}
````

### csv · run 2 · baseline

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
            System.exit(1);
            return;
        }

        if (lines.isEmpty()) {
            System.err.println("File is empty: " + file);
            System.exit(1);
        }

        String[] header = lines.get(0).split(",", -1);
        int amountIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase("amount")) {
                amountIndex = i;
                break;
            }
        }
        if (amountIndex < 0) {
            System.err.println("No 'amount' column found in header: " + lines.get(0));
            System.exit(1);
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            if (fields.length <= amountIndex) {
                System.err.println("Skipping line " + (lineNo + 1) + ": too few fields");
                continue;
            }
            String raw = fields[amountIndex].trim();
            if (raw.isEmpty()) {
                continue;
            }
            try {
                sum = sum.add(new BigDecimal(raw));
            } catch (NumberFormatException e) {
                System.err.println("Skipping line " + (lineNo + 1) + ": invalid amount '" + raw + "'");
            }
        }

        System.out.println(sum.toPlainString());
    }
}
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(SALES_FILE, StandardCharsets.UTF_8);
            System.out.println(sumAmountColumn(lines));
        } catch (IOException e) {
            System.err.println("Could not read " + SALES_FILE + ": " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    static BigDecimal sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("sales.csv is empty: no header row found");
        }
        int amountIndex = findAmountColumnIndex(lines.get(0));

        BigDecimal total = BigDecimal.ZERO;
        for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, amountIndex, lineNumber));
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(DELIMITER, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "No '" + AMOUNT_COLUMN + "' column in header: " + headerLine);
    }

    private static BigDecimal parseAmount(String line, int amountIndex, int lineNumber) {
        String[] fields = line.split(DELIMITER, -1);
        if (amountIndex >= fields.length) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + " has no value in the '" + AMOUNT_COLUMN + "' column");
        }
        try {
            return new BigDecimal(fields[amountIndex].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + " has a non-numeric amount: " + fields[amountIndex]);
        }
    }

    private SalesAmountSummer() {
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountColumnAcrossRows() {
        List<String> lines = List.of(
                "id,amount,region",
                "1,10.50,NL",
                "2,4.25,UK");
        assertEquals(new BigDecimal("14.75"), SalesAmountSummer.sumAmountColumn(lines));
    }

    @Test
    void headerOnlyFileSumsToZero() {
        assertEquals(BigDecimal.ZERO, SalesAmountSummer.sumAmountColumn(List.of("id,amount")));
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of("amount", "3", "", "4");
        assertEquals(new BigDecimal("7"), SalesAmountSummer.sumAmountColumn(lines));
    }

    @Test
    void rejectsEmptyFile() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(List.of()));
    }

    @Test
    void rejectsMissingAmountColumn() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(List.of("id,total", "1,5")));
    }

    @Test
    void rejectsNonNumericAmountWithLineNumber() {
        List<String> lines = List.of("amount", "abc");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(lines));
        assertEquals("Line 2 has a non-numeric amount: abc", e.getMessage());
    }
}
````

## retry

### retry · run 1 · baseline

````java
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation and retries it on failure, waiting a fixed delay
 * between attempts. Thread-safe and stateless; one instance can be shared.
 */
public final class Retry {

    private final int maxAttempts;
    private final Duration delay;

    /**
     * @param maxAttempts total number of attempts, including the first (must be >= 1)
     * @param delay       fixed wait between attempts (must not be negative)
     */
    public Retry(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    /**
     * Executes the operation, retrying on any exception until it succeeds
     * or the maximum number of attempts is reached.
     *
     * @return the operation's result
     * @throws RetryExhaustedException if all attempts fail; the last failure
     *                                 is the cause and earlier failures are suppressed
     * @throws InterruptedException    if the thread is interrupted while waiting
     */
    public <T> T execute(Callable<T> operation) throws InterruptedException {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (lastFailure != null) {
                    e.addSuppressed(lastFailure);
                }
                lastFailure = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delay.toMillis());
                }
            }
        }
        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempts", lastFailure);
    }

    /** Convenience overload for operations without a return value. */
    public void execute(ThrowingRunnable operation) throws InterruptedException {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Thrown when all retry attempts have failed. */
    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
public class Example {
    public static void main(String[] args) throws InterruptedException {
        Retry retry = new Retry(3, java.time.Duration.ofSeconds(2));

        String result = retry.execute(() -> fetchFromFlakyService("participant_001"));
        System.out.println(result);

        retry.execute(() -> sendNotification("participant_001"));
    }

    static String fetchFromFlakyService(String id) throws Exception {
        // stand-in for a network call that may fail transiently
        return "data for " + id;
    }

    static void sendNotification(String id) throws Exception {
        // stand-in for a side-effecting call
    }
}
````

### retry · run 1 · uncle-bob-junior

````java
package com.example.retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying on any exception up to a maximum number of
 * attempts, with a fixed delay between attempts.
 *
 * <p>Usage:
 * <pre>{@code
 * Retry retry = Retry.of(3, Duration.ofMillis(200));
 * String body = retry.execute(() -> httpClient.fetch(url));
 * }</pre>
 */
public final class Retry {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private Retry(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static Retry of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        if (delayBetweenAttempts == null || delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must be zero or positive");
        }
        return new Retry(maxAttempts, delayBetweenAttempts);
    }

    /** Runs the operation and returns its result, retrying on any exception. */
    public <T> T execute(Callable<T> operation) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt < maxAttempts) {
                waitBeforeNextAttempt(lastFailure);
            }
        }
        throw new RetryException("Operation failed after " + maxAttempts + " attempt(s)", lastFailure);
    }

    /** Runs an operation that returns no result, retrying on any exception. */
    public void execute(ThrowingRunnable operation) {
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private void waitBeforeNextAttempt(Exception lastFailure) {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
            RetryException aborted =
                    new RetryException("Retry aborted: interrupted while waiting between attempts", interrupt);
            aborted.addSuppressed(lastFailure);
            throw aborted;
        }
    }

    /** A no-result operation that may throw a checked exception. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
````

````java
package com.example.retry;

/** Thrown when an operation still fails after all retry attempts, or retrying is interrupted. */
public class RetryException extends RuntimeException {

    RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
````

````java
package com.example.retry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultWhenFirstAttemptSucceeds() {
        AtomicInteger calls = new AtomicInteger();

        String result = Retry.of(3, NO_DELAY).execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        AtomicInteger calls = new AtomicInteger();

        String result = Retry.of(3, NO_DELAY).execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IOException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsRetryExceptionWithLastFailureWhenAllAttemptsFail() {
        AtomicInteger calls = new AtomicInteger();
        IOException lastFailure = new IOException("still down");

        RetryException thrown = assertThrows(RetryException.class, () ->
                Retry.of(2, NO_DELAY).execute(() -> {
                    calls.incrementAndGet();
                    throw lastFailure;
                }));

        assertSame(lastFailure, thrown.getCause());
        assertEquals(2, calls.get());
    }

    @Test
    void runsVoidOperationsWithRetries() {
        AtomicInteger calls = new AtomicInteger();

        Retry.of(2, NO_DELAY).execute(() -> {
            if (calls.incrementAndGet() < 2) {
                throw new IllegalStateException("first attempt fails");
            }
        });

        assertEquals(2, calls.get());
    }

    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> Retry.of(0, NO_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> Retry.of(3, Duration.ofMillis(-1)));
    }

    @Test
    void restoresInterruptFlagWhenWaitIsInterrupted() throws Exception {
        Thread worker = new Thread(() ->
                assertThrows(RetryException.class, () ->
                        Retry.of(2, Duration.ofSeconds(10)).execute(() -> {
                            throw new IOException("forces a wait");
                        })));

        worker.start();
        Thread.sleep(100);
        worker.interrupt();
        worker.join(Duration.ofSeconds(5).toMillis());

        assertEquals(Thread.State.TERMINATED, worker.getState());
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
 * Runs an operation, retrying up to a maximum number of attempts when it
 * throws, with a fixed delay between attempts.
 */
public final class Retry {

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
        this.retryOn = Objects.requireNonNull(retryOn);
    }

    /** Retries on any exception. */
    public static Retry of(int maxAttempts, Duration delay) {
        return new Retry(maxAttempts, delay, e -> true);
    }

    /** Retries only when the given predicate matches the thrown exception. */
    public static Retry of(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        return new Retry(maxAttempts, delay, retryOn);
    }

    /**
     * Executes the operation, retrying on failure.
     *
     * @return the operation's result on the first successful attempt
     * @throws RetryExhaustedException if all attempts fail; the last exception
     *         is the cause, earlier ones are attached as suppressed
     * @throws InterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(Callable<T> operation) throws InterruptedException {
        Objects.requireNonNull(operation);
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (last != null) {
                    e.addSuppressed(last);
                }
                last = e;
                if (attempt == maxAttempts || !retryOn.test(e)) {
                    break;
                }
                Thread.sleep(delay.toMillis());
            }
        }
        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", last);
    }

    /** Convenience overload for operations that return nothing. */
    public void execute(RunnableWithException operation) throws InterruptedException {
        execute(() -> {
            operation.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /** Thrown when all retry attempts have been exhausted. */
    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
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

        // Operation that returns a value
        String result = retry.execute(() -> fetchFromApi("participant_001"));
        System.out.println(result);

        // Void operation, retrying only on a specific exception type
        Retry ioRetry = Retry.of(5, Duration.ofMillis(500), e -> e instanceof java.io.IOException);
        ioRetry.execute(() -> sendNotification("participant_001"));
    }

    static String fetchFromApi(String id) throws Exception { /* ... */ return "ok"; }
    static void sendNotification(String id) throws Exception { /* ... */ }
}
````

### retry · run 2 · uncle-bob-junior

````java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation and retries it with a fixed delay when it throws.
 * Immutable and thread-safe: build one policy and reuse it.
 *
 * <pre>
 *   RetryPolicy policy = RetryPolicy.of(3, Duration.ofMillis(200));
 *   String body = policy.execute(() -> httpClient.fetch(url));
 * </pre>
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException(
                    "delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    /** Runs {@code operation}, retrying on failure; returns its result or rethrows the last failure. */
    public <T> T execute(Callable<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException interruption) {
                // An interrupted operation must not be retried; preserve the interrupt contract.
                Thread.currentThread().interrupt();
                throw withSuppressed(interruption, lastFailure);
            } catch (Exception failure) {
                lastFailure = withSuppressed(failure, lastFailure);
                if (attempt < maxAttempts) {
                    sleepBeforeNextAttempt(lastFailure);
                }
            }
        }
        throw lastFailure;
    }

    /** Overload for operations without a return value. */
    public void execute(ThrowingRunnable operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private void sleepBeforeNextAttempt(Exception lastFailure) throws InterruptedException {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw withSuppressed(interruption, lastFailure);
        }
    }

    /** Keeps every earlier attempt's failure visible on the exception that finally escapes. */
    private static <E extends Exception> E withSuppressed(E current, Exception previous) {
        if (previous != null) {
            current.addSuppressed(previous);
        }
        return current;
    }

    /** Like {@link Runnable} but allowed to throw checked exceptions. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
````

````java
package com.example.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryPolicyTest {

    private static final int THREE_ATTEMPTS = 3;
    private static final Duration NO_DELAY = Duration.ZERO;

    private final RetryPolicy policy = RetryPolicy.of(THREE_ATTEMPTS, NO_DELAY);

    @Test
    void returnsResultOnFirstSuccessWithoutRetrying() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = policy.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, attempts.get());
    }

    @Test
    void retriesUntilTheOperationSucceeds() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = policy.execute(() -> {
            if (attempts.incrementAndGet() < THREE_ATTEMPTS) {
                throw new IOException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(THREE_ATTEMPTS, attempts.get());
    }

    @Test
    void throwsLastFailureAfterExhaustingAttempts() {
        AtomicInteger attempts = new AtomicInteger();
        IOException lastFailure = new IOException("attempt 3");

        IOException thrown = assertThrows(IOException.class, () -> policy.execute(() -> {
            if (attempts.incrementAndGet() == THREE_ATTEMPTS) {
                throw lastFailure;
            }
            throw new IOException("attempt " + attempts.get());
        }));

        assertSame(lastFailure, thrown);
        assertEquals(THREE_ATTEMPTS, attempts.get());
        assertEquals(1, thrown.getSuppressed().length);
    }

    @Test
    void runnableOverloadRetriesLikeCallable() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        policy.execute((RetryPolicy.ThrowingRunnable) () -> {
            if (attempts.incrementAndGet() < 2) {
                throw new IllegalStateException("not ready");
            }
        });

        assertEquals(2, attempts.get());
    }

    @Test
    void doesNotRetryAnInterruptedOperation() {
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(InterruptedException.class, () -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new InterruptedException("cancelled");
        }));

        assertEquals(1, attempts.get());
        assertEquals(true, Thread.interrupted()); // also clears the flag so other tests are unaffected
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, NO_DELAY));
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(1, Duration.ofMillis(-1)));
        assertThrows(NullPointerException.class, () -> RetryPolicy.of(1, null));
        assertThrows(NullPointerException.class, () -> policy.execute((java.util.concurrent.Callable<String>) null));
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Sliding-window rate limiter keyed by client identifier.
 * Thread-safe; suitable for a multi-threaded HttpServer.
 */
final class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final long windowNanos;
    private final ConcurrentMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    SlidingWindowRateLimiter(int maxRequests, long window, TimeUnit unit) {
        this.maxRequests = maxRequests;
        this.windowNanos = unit.toNanos(window);
    }

    /**
     * Records an attempt for the given client and reports whether it is allowed.
     *
     * @return 0 if allowed, otherwise the number of seconds until the client
     *         may retry (suitable for a Retry-After header).
     */
    long tryAcquire(String clientId) {
        long now = System.nanoTime();
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Evict entries that have fallen out of the window.
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                timestamps.pollFirst();
            }
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return 0;
            }
            long oldest = timestamps.peekFirst();
            long retryAfterNanos = windowNanos - (now - oldest);
            return Math.max(1, TimeUnit.NANOSECONDS.toSeconds(retryAfterNanos) + 1);
        }
    }

    /** Removes clients with no requests inside the current window. Call periodically. */
    void evictIdleClients() {
        long now = System.nanoTime();
        requestLog.forEach((clientId, timestamps) -> {
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    // Remove only if still the same (empty) deque, to avoid
                    // discarding a concurrent insertion.
                    requestLog.remove(clientId, timestamps);
                }
            }
        });
    }
}

/**
 * Decorator that applies rate limiting before delegating to the real handler.
 */
final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter limiter;
    private final boolean trustForwardedHeader;

    RateLimitingHandler(HttpHandler delegate, SlidingWindowRateLimiter limiter, boolean trustForwardedHeader) {
        this.delegate = delegate;
        this.limiter = limiter;
        this.trustForwardedHeader = trustForwardedHeader;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        long retryAfterSeconds = limiter.tryAcquire(clientId);

        if (retryAfterSeconds > 0) {
            byte[] body = "Too many requests. Please try again later.\n"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfterSeconds));
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private String resolveClientId(HttpExchange exchange) {
        if (trustForwardedHeader) {
            String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // First entry is the originating client.
                return forwarded.split(",")[0].trim();
            }
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}

public class RateLimitedServer {

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(5, 1, TimeUnit.MINUTES);

        HttpHandler businessHandler = exchange -> {
            byte[] body = "Hello from the handler!\n".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Set trustForwardedHeader to true only when running behind a trusted proxy.
        server.createContext("/api", new RateLimitingHandler(businessHandler, limiter, false));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        // Housekeeping: drop idle clients so the map does not grow unbounded.
        var cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-cleanup");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(limiter::evictIdleClients, 1, 1, TimeUnit.MINUTES);

        System.out.println("Server listening on http://localhost:8080/api");
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter: each client may make at most
 * maxRequestsPerWindow requests within any rolling window.
 * Thread-safe; the Clock is injectable so tests control time.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimesByClient =
            new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /** Returns true and records the request if the client is under its limit. */
    public boolean tryAcquire(String clientId) {
        while (true) {
            Deque<Instant> times =
                    requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
            synchronized (times) {
                // ubj: re-check mapping because removeIdleClients may have evicted
                // this deque between computeIfAbsent and taking the lock.
                if (requestTimesByClient.get(clientId) != times) {
                    continue;
                }
                evictExpired(times);
                if (times.size() >= maxRequestsPerWindow) {
                    return false;
                }
                times.addLast(clock.instant());
                return true;
            }
        }
    }

    /** Seconds until the client's oldest request leaves the window; 0 if not limited. */
    public long secondsUntilNextSlot(String clientId) {
        Deque<Instant> times = requestTimesByClient.get(clientId);
        if (times == null) {
            return 0;
        }
        synchronized (times) {
            evictExpired(times);
            if (times.size() < maxRequestsPerWindow) {
                return 0;
            }
            Instant oldestExpiry = times.peekFirst().plus(window);
            return Math.max(1, Duration.between(clock.instant(), oldestExpiry).getSeconds());
        }
    }

    /** Drops clients with no requests left in the window; call periodically to bound memory. */
    public void removeIdleClients() {
        requestTimesByClient.forEach((clientId, times) -> {
            synchronized (times) {
                evictExpired(times);
                if (times.isEmpty()) {
                    requestTimesByClient.remove(clientId, times);
                }
            }
        });
    }

    private void evictExpired(Deque<Instant> times) {
        Instant windowStart = clock.instant().minus(window);
        while (!times.isEmpty() && times.peekFirst().isBefore(windowStart)) {
            times.removeFirst();
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

/** Wraps any HttpHandler and rejects over-limit clients with 429. */
public final class RateLimitingHandler implements HttpHandler {

    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String REJECTION_BODY = "Too many requests. Please retry later.\n";

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
            rejectAsRateLimited(exchange, clientId);
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: identifies clients by socket IP; behind a trusted reverse proxy,
    // replace with the proxy-set forwarding header instead.
    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectAsRateLimited(HttpExchange exchange, String clientId) throws IOException {
        byte[] body = REJECTION_BODY.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After",
                String.valueOf(rateLimiter.secondsUntilNextSlot(clientId)));
        exchange.sendResponseHeaders(STATUS_TOO_MANY_REQUESTS, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Example wiring: a plain handler protected by the rate limiter. */
public final class RateLimitedServer {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, RATE_WINDOW, Clock.systemUTC());

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitingHandler(RateLimitedServer::handleHello, rateLimiter));
        startIdleClientSweeper(rateLimiter);
        server.start();
    }

    private static void handleHello(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static void startIdleClientSweeper(SlidingWindowRateLimiter rateLimiter) {
        ScheduledExecutorService sweeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-sweeper");
            thread.setDaemon(true);
            return thread;
        });
        sweeper.scheduleAtFixedRate(rateLimiter::removeIdleClients,
                RATE_WINDOW.toSeconds(), RATE_WINDOW.toSeconds(), TimeUnit.SECONDS);
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Clock whose time the test advances explicitly. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) { now = now.plus(duration); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private final MutableClock clock = new MutableClock();
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("participant_001"));
        }
    }

    @Test
    void rejectsRequestsOverTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        assertFalse(limiter.tryAcquire("participant_001"));
    }

    @Test
    void allowsAgainOnceOldRequestsLeaveTheWindow() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire("participant_001"));
    }

    @Test
    void limitsClientsIndependently() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        assertTrue(limiter.tryAcquire("participant_002"));
    }

    @Test
    void reportsSecondsUntilNextSlotWhenLimited() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        assertEquals(WINDOW.getSeconds(), limiter.secondsUntilNextSlot("participant_001"));
    }

    @Test
    void reportsZeroWaitWhenUnderTheLimit() {
        limiter.tryAcquire("participant_001");
        assertEquals(0, limiter.secondsUntilNextSlot("participant_001"));
    }

    @Test
    void idleSweepDoesNotDropActiveClients() {
        limiter.tryAcquire("participant_001");
        limiter.removeIdleClients();
        for (int i = 1; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("participant_001"));
        }
        assertFalse(limiter.tryAcquire("participant_001"));
    }
}
````

### ratelimit · run 2 · baseline

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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps any HttpHandler with per-client rate limiting using a sliding
 * window: a client may make at most {@code maxRequests} requests in any
 * rolling {@code windowMillis} period. Excess requests get HTTP 429.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;

    /** Per-client timestamps of recent requests (millis since epoch). */
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rate-limiter-cleanup");
                t.setDaemon(true);
                return t;
            });

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        // Evict clients that have gone quiet so the map doesn't grow forever.
        cleaner.scheduleAtFixedRate(this::evictStaleClients,
                windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);
        long now = System.currentTimeMillis();

        long retryAfterSeconds = tryAcquire(clientId, now);
        if (retryAfterSeconds > 0) {
            reject(exchange, retryAfterSeconds);
            return;
        }
        delegate.handle(exchange);
    }

    /**
     * Records the request if allowed. Returns 0 if allowed, otherwise the
     * number of seconds until the oldest in-window request expires.
     */
    private long tryAcquire(String clientId, long now) {
        Deque<Long> timestamps =
                requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            long windowStart = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                long oldest = timestamps.peekFirst();
                long waitMillis = oldest + windowMillis - now;
                return Math.max(1, (waitMillis + 999) / 1000);
            }
            timestamps.addLast(now);
            return 0;
        }
    }

    private String clientId(HttpExchange exchange) {
        // If behind a trusted reverse proxy, prefer the X-Forwarded-For
        // header here instead. Do not trust it from direct internet traffic.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void reject(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Too many requests. Please try again later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfterSeconds));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictStaleClients() {
        long windowStart = System.currentTimeMillis() - windowMillis;
        Iterator<Map.Entry<String, Deque<Long>>> it = requestLog.entrySet().iterator();
        while (it.hasNext()) {
            Deque<Long> timestamps = it.next().getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    it.remove();
                }
            }
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Your existing business handler, unchanged.
        com.sun.net.httpserver.HttpHandler apiHandler = exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        // Allow 5 requests per client per rolling 60 seconds.
        server.createContext("/api",
                new RateLimitingHandler(apiHandler, 5, TimeUnit.MINUTES.toMillis(1)));

        server.start();
        System.out.println("Listening on http://localhost:8080/api");
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Allows each client a fixed number of requests within a sliding time window.
 * Thread-safe. The injected Clock keeps time controllable in tests.
 */
final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;

    // ubj: entries are never evicted; swap for an expiring cache if client cardinality grows large.
    private final ConcurrentMap<String, Deque<Instant>> requestTimesByClient = new ConcurrentHashMap<>();

    SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /** Records a request for the client and reports whether it is within the limit. */
    boolean tryAcquire(String clientId) {
        Deque<Instant> requestTimes =
                requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (requestTimes) {
            pruneExpired(requestTimes);
            if (requestTimes.size() >= maxRequestsPerWindow) {
                return false;
            }
            requestTimes.addLast(clock.instant());
            return true;
        }
    }

    /** Seconds until the client's oldest recorded request leaves the window; 0 if not limited. */
    long retryAfterSeconds(String clientId) {
        Deque<Instant> requestTimes = requestTimesByClient.get(clientId);
        if (requestTimes == null) {
            return 0;
        }
        synchronized (requestTimes) {
            pruneExpired(requestTimes);
            if (requestTimes.size() < maxRequestsPerWindow) {
                return 0;
            }
            Instant oldestExpiry = requestTimes.peekFirst().plus(window);
            return Math.max(1, Duration.between(clock.instant(), oldestExpiry).toSeconds());
        }
    }

    private void pruneExpired(Deque<Instant> requestTimes) {
        Instant cutoff = clock.instant().minus(window);
        while (!requestTimes.isEmpty() && requestTimes.peekFirst().isBefore(cutoff)) {
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
import java.time.Clock;
import java.time.Duration;

/** Wraps any HttpHandler and rejects clients that exceed the per-minute request limit. */
public final class RateLimitedHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);
    private static final byte[] REJECTION_BODY =
            "Too many requests. Please retry later.\n".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter rateLimiter;

    public RateLimitedHttpHandler(HttpHandler delegate) {
        this(delegate, new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, RATE_WINDOW, Clock.systemUTC()));
    }

    RateLimitedHttpHandler(HttpHandler delegate, SlidingWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectTooManyRequests(exchange, rateLimiter.retryAfterSeconds(clientId));
            return;
        }
        delegate.handle(exchange);
    }

    private static String clientId(HttpExchange exchange) {
        // Trusts the socket address; behind a reverse proxy, derive this
        // from a validated X-Forwarded-For header from the trusted proxy instead.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void rejectTooManyRequests(HttpExchange exchange, long retryAfterSeconds)
            throws IOException {
        exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, REJECTION_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(REJECTION_BODY);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitedHttpHandler(Server::handleHello));
        server.start();
    }

    private static void handleHello(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] body = "Hello\n".getBytes(StandardCharsets.UTF_8);
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT = "203.0.113.10";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-08-26T12:00:00Z"));
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(CLIENT), "request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsOverTheLimit() {
        exhaustLimit(CLIENT);
        assertFalse(limiter.tryAcquire(CLIENT));
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        exhaustLimit(CLIENT);
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire(CLIENT));
    }

    @Test
    void limitsEachClientIndependently() {
        exhaustLimit(CLIENT);
        assertTrue(limiter.tryAcquire("198.51.100.7"));
    }

    @Test
    void retryAfterIsZeroWhenNotLimited() {
        assertEquals(0, limiter.retryAfterSeconds(CLIENT));
    }

    @Test
    void retryAfterCountsDownToTheOldestRequestExpiring() {
        exhaustLimit(CLIENT);
        clock.advance(Duration.ofSeconds(20));
        assertEquals(40, limiter.retryAfterSeconds(CLIENT));
    }

    private void exhaustLimit(String clientId) {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(clientId));
        }
    }

    /** A Clock whose instant is advanced manually by tests. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }
}
````

````java
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
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
import org.junit.jupiter.api.Test;

class RateLimitedHttpHandlerTest {

    private static final int LIMIT = 2;
    private static final int OK = 200;
    private static final int TOO_MANY_REQUESTS = 429;

    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, Duration.ofMinutes(1), Clock.systemUTC());
    private final RateLimitedHttpHandler handler =
            new RateLimitedHttpHandler(RateLimitedHttpHandlerTest::respondOk, limiter);

    @Test
    void delegatesWhileWithinTheLimit() throws IOException {
        StubHttpExchange exchange = new StubHttpExchange("203.0.113.10");
        handler.handle(exchange);
        assertEquals(OK, exchange.getResponseCode());
    }

    @Test
    void rejectsWithRetryAfterOnceOverTheLimit() throws IOException {
        for (int i = 0; i < LIMIT; i++) {
            handler.handle(new StubHttpExchange("203.0.113.10"));
        }
        StubHttpExchange rejected = new StubHttpExchange("203.0.113.10");
        handler.handle(rejected);

        assertEquals(TOO_MANY_REQUESTS, rejected.getResponseCode());
        assertEquals("60", rejected.getResponseHeaders().getFirst("Retry-After"));
    }

    private static void respondOk(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(OK, -1);
    }

    /** Minimal in-memory HttpExchange: records the response, no real socket. */
    private static final class StubHttpExchange extends HttpExchange {
        private final InetSocketAddress remoteAddress;
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        StubHttpExchange(String clientIp) {
            this.remoteAddress = new InetSocketAddress(clientIp, 54321);
        }

        @Override public void sendResponseHeaders(int code, long length) { responseCode = code; }
        @Override public int getResponseCode() { return responseCode; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public InetSocketAddress getRemoteAddress() { return remoteAddress; }
        @Override public Headers getRequestHeaders() { return new Headers(); }
        @Override public InputStream getRequestBody() { return new ByteArrayInputStream(new byte[0]); }
        @Override public URI getRequestURI() { return URI.create("/"); }
        @Override public String getRequestMethod() { return "GET"; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(8080); }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public HttpPrincipal getPrincipal() { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) { }
        @Override public void setStreams(InputStream in, OutputStream out) { }
        @Override public void close() { }
    }
}
````

## order

### order · run 1 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String name, int quantity, BigDecimal unitPrice) {}

    public static class OrderValidationException extends RuntimeException {
        public OrderValidationException(String message) {
            super(message);
        }
    }

    /**
     * Validates the order, computes totals and returns a formatted receipt.
     *
     * @throws OrderValidationException if the order or any line item is invalid
     */
    public static String processOrder(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = netAfterDiscount.add(vat);

        return buildReceipt(items, subtotal, discount, netAfterDiscount, vat, total);
    }

    private static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new OrderValidationException("Line item " + (i + 1) + " is null");
            }
            if (item.name() == null || item.name().isBlank()) {
                throw new OrderValidationException("Line item " + (i + 1) + " has no name");
            }
            if (item.quantity() <= 0) {
                throw new OrderValidationException(
                        "Line item '" + item.name() + "' has a non-positive quantity: " + item.quantity());
            }
            if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
                throw new OrderValidationException(
                        "Line item '" + item.name() + "' has a missing or negative unit price");
            }
        }
    }

    private static String buildReceipt(List<LineItem> items, BigDecimal subtotal, BigDecimal discount,
                                       BigDecimal netAfterDiscount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("--------------------------------------------\n");
        for (LineItem item : items) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            sb.append(String.format("%-20s %3d x %8s  %9s%n",
                    truncate(item.name(), 20), item.quantity(),
                    format(item.unitPrice().setScale(2, RoundingMode.HALF_UP)), format(lineTotal)));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-34s %9s%n", "Subtotal (excl. VAT):", format(subtotal)));
        if (discount.signum() > 0) {
            sb.append(String.format("%-34s %9s%n", "Discount (10%):", "-" + format(discount)));
            sb.append(String.format("%-34s %9s%n", "Net after discount:", format(netAfterDiscount)));
        }
        sb.append(String.format("%-34s %9s%n", "VAT (21%):", format(vat)));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-34s %9s%n", "TOTAL:", format(total)));
        return sb.toString();
    }

    private static String format(BigDecimal amount) {
        return "EUR " + amount.toPlainString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 2, new BigDecimal("34.95")),
                new LineItem("USB-C cable", 1, new BigDecimal("9.99"))
        );
        System.out.println(processOrder(order));
    }
}
````

### order · run 1 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENTS = 2;

    public record LineItem(String name, int quantity, BigDecimal unitPrice) {}

    public String process(List<LineItem> items) {
        validate(items);
        BigDecimal subtotal = subtotalOf(items);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal netTotal = subtotal.subtract(discount);
        BigDecimal vat = vatOn(netTotal);
        BigDecimal grandTotal = netTotal.add(vat);
        return buildReceipt(items, subtotal, discount, vat, grandTotal);
    }

    private void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        items.forEach(this::validateItem);
    }

    private void validateItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Line item must not be null");
        }
        if (item.name() == null || item.name().isBlank()) {
            throw new IllegalArgumentException("Line item name must not be blank");
        }
        if (item.quantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1: " + item.name());
        }
        if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be negative: " + item.name());
        }
    }

    private BigDecimal subtotalOf(List<LineItem> items) {
        return items.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal lineTotal(LineItem item) {
        return item.unitPrice()
                .multiply(BigDecimal.valueOf(item.quantity()))
                .setScale(CENTS, RoundingMode.HALF_UP);
    }

    private BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(CENTS, RoundingMode.HALF_UP);
    }

    private BigDecimal vatOn(BigDecimal netTotal) {
        return netTotal.multiply(VAT_RATE).setScale(CENTS, RoundingMode.HALF_UP);
    }

    private String buildReceipt(List<LineItem> items, BigDecimal subtotal,
                                BigDecimal discount, BigDecimal vat, BigDecimal grandTotal) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(formatLine(item)));
        receipt.append(String.format("Subtotal (excl. VAT): EUR %s%n", subtotal));
        if (discount.signum() > 0) {
            receipt.append(String.format("Discount (10%%): -EUR %s%n", discount));
        }
        receipt.append(String.format("VAT (21%%): EUR %s%n", vat));
        receipt.append(String.format("Total: EUR %s%n", grandTotal));
        return receipt.toString();
    }

    private String formatLine(LineItem item) {
        return String.format("%d x %s @ EUR %s = EUR %s%n",
                item.quantity(), item.name(), item.unitPrice(), lineTotal(item));
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

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    private static OrderProcessor.LineItem item(String name, int quantity, String unitPrice) {
        return new OrderProcessor.LineItem(name, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void computesVatWithoutDiscountAtOrBelowThreshold() {
        String receipt = processor.process(List.of(item("Notebook", 2, "50.00")));

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): EUR 21.00"));
        assertTrue(receipt.contains("Total: EUR 121.00"));
    }

    @Test
    void appliesDiscountAboveThresholdBeforeVat() {
        String receipt = processor.process(List.of(item("Desk", 1, "200.00")));

        assertTrue(receipt.contains("Discount (10%): -EUR 20.00"));
        assertTrue(receipt.contains("VAT (21%): EUR 37.80"));
        assertTrue(receipt.contains("Total: EUR 217.80"));
    }

    @Test
    void listsEachLineItemOnReceipt() {
        String receipt = processor.process(List.of(item("Pen", 3, "1.50")));

        assertTrue(receipt.contains("3 x Pen @ EUR 1.50 = EUR 4.50"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(null));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.process(List.of(item("  ", 1, "10.00"))));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.process(List.of(item("Pen", 0, "10.00"))));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.process(List.of(item("Pen", 1, "-1.00"))));
    }

    @Test
    void boundaryExactlyOneHundredGetsNoDiscount() {
        String receipt = processor.process(List.of(item("Chair", 1, "100.00")));

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Total: EUR 121.00"));
    }
}
````

### order · run 2 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public record LineItem(String description, BigDecimal unitPrice, int quantity) {

        public LineItem {
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING);
        }
    }

    public record Receipt(BigDecimal subtotal,
                          BigDecimal discount,
                          BigDecimal vat,
                          BigDecimal total,
                          String text) {
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public Receipt process(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        BigDecimal discount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING);
        }

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = netAfterDiscount.add(vat).setScale(SCALE, ROUNDING);

        String text = buildReceiptText(items, subtotal, discount, vat, total);
        return new Receipt(subtotal, discount, vat, total, text);
    }

    private void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new InvalidOrderException("Line item " + (i + 1) + " is null.");
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new InvalidOrderException("Line item " + (i + 1) + " has no description.");
            }
            if (item.quantity() <= 0) {
                throw new InvalidOrderException("Line item " + (i + 1) + " has a non-positive quantity.");
            }
            if (item.unitPrice().signum() < 0) {
                throw new InvalidOrderException("Line item " + (i + 1) + " has a negative unit price.");
            }
        }
    }

    private String buildReceiptText(List<LineItem> items,
                                    BigDecimal subtotal,
                                    BigDecimal discount,
                                    BigDecimal vat,
                                    BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT").append(System.lineSeparator());
        sb.append("--------------------------------------------").append(System.lineSeparator());
        for (LineItem item : items) {
            sb.append(String.format("%-20s %3d x EUR %8s = EUR %9s%n",
                    truncate(item.description(), 20),
                    item.quantity(),
                    item.unitPrice().setScale(SCALE, ROUNDING).toPlainString(),
                    item.lineTotal().toPlainString()));
        }
        sb.append("--------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("Subtotal (excl. VAT):        EUR %9s%n", subtotal.toPlainString()));
        if (discount.signum() > 0) {
            sb.append(String.format("Discount (10%%):             -EUR %9s%n", discount.toPlainString()));
        }
        sb.append(String.format("VAT (21%%):                   EUR %9s%n", vat.toPlainString()));
        sb.append(String.format("Total (incl. VAT):           EUR %9s%n", total.toPlainString()));
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> order = List.of(
                new LineItem("Notebook", new BigDecimal("12.50"), 4),
                new LineItem("Desk lamp", new BigDecimal("34.99"), 2)
        );
        Receipt receipt = processor.process(order);
        System.out.print(receipt.text());
    }
}
````

### order · run 2 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** A single order line: a product name, a quantity, and a unit price excluding VAT. */
public record LineItem(String productName, int quantity, BigDecimal unitPriceExVat) {}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Processes an order: validates line items, applies the volume discount,
 * adds VAT, and renders a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD_EX_VAT = new BigDecimal("100.00");
    private static final int CENTS = 2;

    public String processOrder(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = subtotalExVat(items);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);

        return buildReceipt(items, subtotal, discount, vat, total);
    }

    private void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        items.forEach(this::validateItem);
    }

    private void validateItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Line item must not be null");
        }
        if (item.productName() == null || item.productName().isBlank()) {
            throw new IllegalArgumentException("Line item must have a product name");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive for product: " + item.productName());
        }
        if (item.unitPriceExVat() == null
                || item.unitPriceExVat().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Unit price must be zero or more for product: " + item.productName());
        }
    }

    private BigDecimal subtotalExVat(List<LineItem> items) {
        return items.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CENTS, RoundingMode.HALF_UP);
    }

    private BigDecimal lineTotal(LineItem item) {
        return item.unitPriceExVat().multiply(BigDecimal.valueOf(item.quantity()));
    }

    private BigDecimal discountFor(BigDecimal subtotalExVat) {
        if (subtotalExVat.compareTo(DISCOUNT_THRESHOLD_EX_VAT) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS, RoundingMode.HALF_UP);
        }
        return roundToCents(subtotalExVat.multiply(DISCOUNT_RATE));
    }

    private BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }

    private String buildReceipt(List<LineItem> items, BigDecimal subtotal,
                                BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(formatLine(item)));
        receipt.append(formatAmount("Subtotal (ex VAT)", subtotal));
        if (discount.signum() > 0) {
            receipt.append(formatAmount("Discount (10%)", discount.negate()));
        }
        receipt.append(formatAmount("VAT (21%)", vat));
        receipt.append(formatAmount("Total", total));
        return receipt.toString();
    }

    private String formatLine(LineItem item) {
        return String.format("%-20s %3d x EUR %10.2f = EUR %10.2f%n",
                item.productName(), item.quantity(),
                item.unitPriceExVat(), roundToCents(lineTotal(item)));
    }

    private String formatAmount(String label, BigDecimal amount) {
        return String.format("%-30s EUR %10.2f%n", label, amount);
    }
}
````

````java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Notebook", 2, new BigDecimal("10.00"))));

        assertTrue(receipt.contains("Subtotal (ex VAT)"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("24.20")); // 20.00 + 4.20 VAT
    }

    @Test
    void noDiscountAtExactlyOneHundredEuros() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Chair", 1, new BigDecimal("100.00"))));

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("121.00")); // 100.00 + 21.00 VAT
    }

    @Test
    void appliesDiscountAboveThreshold() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Desk", 1, new BigDecimal("200.00"))));

        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("217.80")); // 180.00 + 37.80 VAT
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.processOrder(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.processOrder(null));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(
                List.of(new LineItem("Pen", 0, new BigDecimal("1.00")))));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(
                List.of(new LineItem("Pen", 1, new BigDecimal("-1.00")))));
    }

    @Test
    void rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(
                List.of(new LineItem("  ", 1, new BigDecimal("1.00")))));
    }

    @Test
    void roundsVatToCents() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Sticker", 3, new BigDecimal("0.33"))));

        assertTrue(receipt.contains("1.20")); // 0.99 + 0.21 (0.2079 rounded) VAT
    }
}
````
