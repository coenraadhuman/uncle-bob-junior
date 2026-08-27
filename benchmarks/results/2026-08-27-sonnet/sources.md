# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254; // RFC 5321 max mailbox length

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+"          // local part
      + "@"
      + "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" // domain label
      + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$" // additional labels + TLD
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
````

````java
boolean ok = EmailValidator.isValid("participant_001@example.com"); // true
````

### email · run 1 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
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
        "coenraad.human@postcodeloterij.nl",
        "a@b.co",
        "user+tag@example.com",
        "first.last@sub.example.org"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "missing-at-sign.com",
        "@no-local-part.com",
        "no-domain@",
        "no-tld@example",
        "spaces in@example.com",
        "double@@example.com"
    })
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValidEmail(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsEmailLongerThanMaxLength() {
        String longLocalPart = "a".repeat(250);
        assertFalse(EmailValidator.isValidEmail(longLocalPart + "@example.com"));
    }
}
````

### email · run 2 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322-inspired pattern: local-part@domain with at least one dot in the domain.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+" +          // local part
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" + // first domain label
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$" // subsequent labels (needs >=1)
    );

    private static final int MAX_LENGTH = 254; // RFC 5321 4.5.3.1.3

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void main(String[] args) {
        String[] samples = {
            "user@example.com",
            "user.name+tag@example.co.uk",
            "invalid@",
            "@missinglocal.com",
            "no-at-sign.com",
            "user@localhost", // no dot in domain -> rejected by this pattern
        };
        for (String s : samples) {
            System.out.println(s + " -> " + isValid(s));
        }
    }
}
````

### email · run 2 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    // Local part: letters, digits, and common symbols, dot-separated (no leading/trailing/double dots).
    // Domain: labels of letters/digits/hyphen, dot-separated, final label at least two letters.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    /**
     * Checks whether {@code email} is a syntactically valid email address.
     *
     * <p>This performs format validation only: it does not verify that the
     * domain exists or that the mailbox can receive mail.
     *
     * @param email the candidate address; may be {@code null}
     * @return {@code true} if {@code email} is well-formed, {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

````java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValidEmail("user@example.com"));
    }

    @Test
    void acceptsAddressWithSubdomainAndPlusTag() {
        assertTrue(EmailValidator.isValidEmail("first.last+tag@mail.example.co.uk"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValidEmail(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValidEmail("   "));
    }

    @Test
    void rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("user.example.com"));
    }

    @Test
    void rejectsMissingDomainDot() {
        assertFalse(EmailValidator.isValidEmail("user@example"));
    }

    @Test
    void rejectsDoubleAtSymbol() {
        assertFalse(EmailValidator.isValidEmail("user@@example.com"));
    }

    @Test
    void rejectsAddressExceedingMaxLength() {
        String longLocalPart = "a".repeat(255);
        assertFalse(EmailValidator.isValidEmail(longLocalPart + "@example.com"));
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int amountIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            String[] headers = line.split(",", -1);
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in header.");
                return;
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }
                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", total);

        } catch (IOException e) {
            System.err.println("Error reading file '" + path + "': " + e.getMessage());
        }
    }
}
````

### csv · run 1 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SalesAmountSummer {

    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String CSV_DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        double total = sumAmountColumn(CSV_FILE);
        System.out.printf("Sum of '%s': %.2f%n", AMOUNT_COLUMN, total);
    }

    private static double sumAmountColumn(String csvFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvFile);
            }

            int amountIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            return sumRows(reader, amountIndex);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) throws IOException {
        List<String> headers = Arrays.asList(headerLine.split(CSV_DELIMITER, -1));
        int index = headers.indexOf(columnName);
        if (index == -1) {
            throw new IOException("Column '" + columnName + "' not found in header: " + headerLine);
        }
        return index;
    }

    private static double sumRows(BufferedReader reader, int amountIndex) throws IOException {
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(CSV_DELIMITER, -1);
            total += Double.parseDouble(fields[amountIndex].trim());
        }
        return total;
    }
}
````

### csv · run 2 · baseline

````java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of("sales.csv");
        List<String> lines = Files.readAllLines(csvPath);

        if (lines.isEmpty()) {
            System.out.println("sales.csv is empty; sum = 0");
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
            System.out.println("No 'amount' column found in header.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        int skipped = 0;

        for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            if (amountIndex >= fields.length) {
                skipped++;
                continue;
            }
            String rawValue = fields[amountIndex].trim();
            if (rawValue.isEmpty()) {
                continue;
            }
            try {
                total = total.add(new BigDecimal(rawValue));
            } catch (NumberFormatException e) {
                System.err.println("Skipping unparseable value on line " + (lineNo + 1) + ": " + rawValue);
                skipped++;
            }
        }

        System.out.println("Sum of 'amount': " + total);
        if (skipped > 0) {
            System.out.println("(" + skipped + " row(s) skipped due to missing/invalid values)");
        }
    }
}
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesTotal {

    private static final String CSV_FILE_PATH = "sales.csv";
    private static final String COLUMN_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        double totalAmount = sumAmountColumn(CSV_FILE_PATH);
        System.out.printf("Total amount: %.2f%n", totalAmount);
    }

    private static double sumAmountColumn(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }
            int amountColumnIndex = findAmountColumnIndex(headerLine);

            double total = 0.0;
            String row;
            while ((row = reader.readLine()) != null) {
                if (row.isBlank()) {
                    continue;
                }
                total += parseAmount(row, amountColumnIndex);
            }
            return total;
        }
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columnNames = headerLine.split(COLUMN_DELIMITER);
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalStateException("CSV header has no '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String row, int amountColumnIndex) {
        String[] fields = row.split(COLUMN_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
````

## retry

### retry · run 1 · baseline

````java
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Generic retry helper that re-runs an operation on failure, up to a
 * maximum number of attempts, waiting a fixed delay between attempts.
 */
public final class Retry {

    private Retry() {
    }

    /**
     * Functional interface for an operation that may throw a checked exception.
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws Exception;
    }

    /**
     * Thrown when all retry attempts have been exhausted.
     * The last failure is available via {@link #getCause()}.
     */
    public static final class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Runs {@code operation}, retrying on any exception up to {@code maxAttempts} times,
     * waiting {@code delay} between attempts. No exception is thrown from within
     * the wait itself unless the thread is interrupted, in which case retrying stops.
     *
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param operation   the operation to run
     * @return the operation's result, if any attempt succeeds
     * @throws RetryException if every attempt fails or the thread is interrupted while waiting
     */
    public static <T> T run(int maxAttempts, Duration delay, RetryableOperation<T> operation) {
        return run(maxAttempts, delay, operation, attempt -> { });
    }

    /**
     * Same as {@link #run(int, Duration, RetryableOperation)} but invokes
     * {@code onFailure} after each failed attempt (e.g. for logging), before waiting.
     */
    public static <T> T run(int maxAttempts,
                             Duration delay,
                             RetryableOperation<T> operation,
                             Consumer<AttemptFailure> onFailure) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, was " + maxAttempts);
        }

        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastFailure = e;
                onFailure.accept(new AttemptFailure(attempt, maxAttempts, e));

                boolean isLastAttempt = attempt == maxAttempts;
                if (!isLastAttempt && delay.toMillis() > 0) {
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RetryException(
                                "Retry interrupted while waiting between attempts", ie);
                    }
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempts", lastFailure);
    }

    /**
     * Details of a single failed attempt, passed to the onFailure callback.
     */
    public static final class AttemptFailure {
        private final int attemptNumber;
        private final int maxAttempts;
        private final Exception cause;

        public AttemptFailure(int attemptNumber, int maxAttempts, Exception cause) {
            this.attemptNumber = attemptNumber;
            this.maxAttempts = maxAttempts;
            this.cause = cause;
        }

        public int getAttemptNumber() {
            return attemptNumber;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public Exception getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "Attempt " + attemptNumber + "/" + maxAttempts + " failed: " + cause;
        }
    }
}
````

````java
String result = Retry.run(
        3,
        Duration.ofSeconds(2),
        () -> callFlakyService(),
        failure -> System.out.println(failure + " - retrying...")
);
````

### retry · run 1 · uncle-bob-junior

````java
// RetryableOperation.java
@FunctionalInterface
public interface RetryableOperation<T> {
    T run() throws Exception;
}
````

````java
// RetryExhaustedException.java
public final class RetryExhaustedException extends Exception {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
````

````java
// RetryInterruptedException.java
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry was interrupted while waiting between attempts", cause);
    }
}
````

````java
// Retry.java
import java.time.Duration;

public final class Retry {

    private Retry() {
    }

    public static <T> T execute(RetryableOperation<T> operation, int maxAttempts, Duration delayBetweenAttempts)
            throws RetryExhaustedException {
        requireValidMaxAttempts(maxAttempts);

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastFailure = e;
                if (attempt < maxAttempts) {
                    waitBeforeNextAttempt(delayBetweenAttempts);
                }
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private static void requireValidMaxAttempts(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, got " + maxAttempts);
        }
    }

    private static void waitBeforeNextAttempt(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(e);
        }
    }
}
````

````java
// RetryTest.java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryTest {

    @Test
    void returnsResultWithoutRetryingOnFirstSuccess() throws Exception {
        String result = Retry.execute(() -> "ok", 3, Duration.ZERO);

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilOperationSucceeds() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = Retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        }, 5, Duration.ZERO);

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttemptsAllFail() {
        AtomicInteger attempts = new AtomicInteger();

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                Retry.execute(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("always fails");
                }, 3, Duration.ZERO));

        assertEquals(3, attempts.get());
        assertNotNull(thrown.getCause());
    }

    @Test
    void rejectsMaxAttemptsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () ->
                Retry.execute(() -> "ok", 0, Duration.ZERO));
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterLastOne() {
        Duration delay = Duration.ofMillis(50);
        long start = System.nanoTime();

        assertThrows(RetryExhaustedException.class, () ->
                Retry.execute(() -> {
                    throw new RuntimeException("fail");
                }, 3, delay));

        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        assertTrue(elapsedMillis >= delay.toMillis() * 2);
    }
}
````

### retry · run 2 · baseline

````java
import java.util.concurrent.Callable;

/**
 * Generic retry helper: runs an operation, retrying on exception up to a
 * maximum number of attempts, with a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delayMillis fixed delay between attempts, in milliseconds
     * @param <T>         the result type
     * @return the operation's result on the first successful attempt
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;

                boolean lastAttempt = attempt == maxAttempts;
                if (lastAttempt) {
                    break;
                }

                if (delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RetryExhaustedException(
                                "Retry interrupted after " + attempt + " attempt(s)", ie);
                    }
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastFailure);
    }

    /**
     * Convenience overload for operations with no return value.
     */
    public static void runWithRetry(Runnable operation, int maxAttempts, long delayMillis) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMillis);
    }

    /**
     * Thrown when an operation still fails after all retry attempts are exhausted.
     * The original last exception is available via {@link #getCause()}.
     */
    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
String result = RetryHelper.runWithRetry(
        () -> callFlakyService(),
        3,      // max attempts
        500L    // 500ms delay between attempts
);
````

### retry · run 2 · uncle-bob-junior

````java
// File: RetryableOperation.java
package com.example.retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T call() throws Exception;
}
````

````java
// File: RetryExhaustedException.java
package com.example.retry;

public final class RetryExhaustedException extends Exception {

    public RetryExhaustedException(int attemptsMade, Exception lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
    }
}
````

````java
// File: Retryer.java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;

public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
    }

    public <T> T execute(RetryableOperation<T> operation) throws RetryExhaustedException {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    sleepBetweenAttempts();
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void sleepBetweenAttempts() {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry delay was interrupted", interrupted);
        }
    }
}
````

````java
// File: RetryerTest.java
package com.example.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryerTest {

    @Test
    void returnsResultOnFirstSuccess() throws Exception {
        Retryer retryer = new Retryer(3, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() throws Exception {
        Retryer retryer = new Retryer(3, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsRetryExhaustedExceptionWhenAllAttemptsFail() {
        Retryer retryer = new Retryer(3, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();
        RuntimeException persistentFailure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    calls.incrementAndGet();
                    throw persistentFailure;
                }));

        assertEquals(3, calls.get());
        assertSame(persistentFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() {
        Duration delay = Duration.ofMillis(50);
        Retryer retryer = new Retryer(3, delay);

        long start = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    throw new RuntimeException("fails every time");
                }));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        assertTrue(elapsedMillis >= delay.toMillis() * 2,
                "expected at least two delays between three attempts");
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, Duration.ofMillis(10)));
    }
}
````

## ratelimit

### ratelimit · run 1 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler and rejects requests once a client exceeds
 * maxRequests within a rolling windowMillis period.
 */
public final class RateLimitedHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitedHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        // periodically evict clients idle longer than the window, so memory
        // doesn't grow unbounded with one-off visitors
        cleaner.scheduleAtFixedRate(this::evictStale, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitedHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitedHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientKey(exchange);
        ClientState state = clients.computeIfAbsent(clientId, k -> new ClientState());

        if (!state.tryAcquire(maxRequests, windowMillis)) {
            long retryAfterSeconds = (windowMillis + 999) / 1000;
            exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
            byte[] body = "429 Too Many Requests\n".getBytes();
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void evictStale() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> e.getValue().isStale(now, windowMillis));
    }

    public void shutdown() {
        cleaner.shutdown();
    }

    /** Fixed-window counter per client, reset once the window elapses. */
    private static final class ClientState {
        private final ReentrantLock lock = new ReentrantLock();
        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        boolean tryAcquire(int maxRequests, long windowMillis) {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                if (now - windowStart >= windowMillis) {
                    windowStart = now;
                    count = 0;
                }
                if (count >= maxRequests) {
                    return false;
                }
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }

        boolean isStale(long now, long windowMillis) {
            lock.lock();
            try {
                return now - windowStart >= windowMillis;
            } finally {
                lock.unlock();
            }
        }
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
package ratelimit;

/**
 * Decides whether a client identified by {@code clientId} may proceed with a request.
 * Implementations must be thread-safe.
 */
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
````

````java
package ratelimit;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * Allows at most {@code maxRequestsPerWindow} requests per client within each
 * fixed {@code windowDuration}. A background task evicts clients that have been
 * idle for a couple of windows, so memory does not grow unbounded.
 */
public final class FixedWindowRateLimiter implements RateLimiter, Closeable {

    private static final int EVICTION_GRACE_WINDOWS = 2;

    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final LongSupplier clock;
    private final ConcurrentHashMap<String, Window> windowsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, System::currentTimeMillis, true);
    }

    // Package-private: lets tests inject a fake clock and skip the real cleanup thread.
    FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, LongSupplier clock, boolean scheduleCleanup) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowDuration.toMillis();
        this.clock = clock;
        this.cleanupExecutor = scheduleCleanup ? startCleanupTask() : null;
    }

    @Override
    public boolean tryAcquire(String clientId) {
        long now = clock.getAsLong();
        Window window = windowsByClient.compute(clientId, (id, existing) -> currentOrNewWindow(existing, now));
        return window.count.incrementAndGet() <= maxRequestsPerWindow;
    }

    @Override
    public void close() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
        }
    }

    private Window currentOrNewWindow(Window existing, long now) {
        boolean expired = existing == null || now - existing.startMillis >= windowMillis;
        return expired ? new Window(now) : existing;
    }

    private ScheduledExecutorService startCleanupTask() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(this::evictStaleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
        return executor;
    }

    private void evictStaleClients() {
        long staleThreshold = windowMillis * EVICTION_GRACE_WINDOWS;
        long now = clock.getAsLong();
        windowsByClient.entrySet().removeIf(entry -> now - entry.getValue().startMillis >= staleThreshold);
    }

    private static final class Window {
        final long startMillis;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
````

````java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Wraps a delegate {@link HttpHandler} and rejects requests from a client once it
 * exceeds the configured {@link RateLimiter}, responding with 429 and a Retry-After header.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final int NO_RESPONSE_BODY = -1;

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter, long retryAfterSeconds) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
````

````java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

/** Example wiring: at most 5 requests per minute per client IP on "/api". */
public final class Server {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW);
        HttpHandlerAdapter apiHandler = new HttpHandlerAdapter();

        server.createContext("/api", new RateLimitingHandler(apiHandler, rateLimiter, WINDOW.toSeconds()));
        server.start();
    }

    private static final class HttpHandlerAdapter implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            }
        }
    }
}
````

````java
package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToTheLimitWithinAWindow() {
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(new AtomicLong(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsBeyondTheLimitWithinAWindow() {
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(new AtomicLong(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void resetsTheLimitOnceTheWindowElapses() {
        AtomicLong fakeNow = new AtomicLong(0);
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(fakeNow);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }
        assertFalse(limiter.tryAcquire("client-a"));

        fakeNow.set(WINDOW.toMillis() + 1);

        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(new AtomicLong(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertTrue(limiter.tryAcquire("client-b"));
    }

    private FixedWindowRateLimiter newLimiterWithFakeClock(AtomicLong fakeNow) {
        return new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, fakeNow::get, false);
    }
}
````

### ratelimit · run 2 · baseline

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps another HttpHandler and enforces a per-client fixed-window
 * request limit (default: 5 requests per 60-second window).
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        Window window = windows.computeIfAbsent(clientKey, k -> new Window(now));

        if (!window.tryAcquire(now, windowMillis, maxRequestsPerWindow)) {
            long retryAfterSeconds = Math.max(1, (window.windowStart.get() + windowMillis - now) / 1000);
            sendTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "429 Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Periodically call this (e.g. from a scheduled task) to evict stale entries. */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().windowStart.get() > windowMillis * 2);
    }

    private static final class Window {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger windowStart;

        Window(long now) {
            this.windowStart = new AtomicInteger((int) (now / 1000)); // seconds, avoids overflow concerns for our use
        }

        /** Not actually used; kept simple below with a synchronized fallback for correctness. */
        boolean tryAcquire(long now, long windowMillis, int max) {
            synchronized (this) {
                long start = startMillis;
                if (start == 0L || now - start >= windowMillis) {
                    startMillis = now;
                    count.set(0);
                }
                return count.incrementAndGet() <= max;
            }
        }

        volatile long startMillis;
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler myHandler = exchange -> {
            byte[] body = "Hello".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        };

        server.createContext("/api", new RateLimitingHandler(myHandler, 5, 60_000L));
        server.start();
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        Window window = windows.computeIfAbsent(clientKey, k -> new Window());
        long retryAfterSeconds = window.tryAcquire(now, windowMillis, maxRequestsPerWindow);

        if (retryAfterSeconds >= 0) {
            sendTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "429 Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Call periodically (e.g. every few minutes) to evict stale per-client entries. */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().lastSeen() > windowMillis * 2);
    }

    /** Fixed-window request counter for a single client. */
    private final class Window {
        private long windowStart = 0L;
        private int count = 0;

        synchronized long lastSeen() {
            return windowStart;
        }

        /** Returns -1 if the request is allowed, otherwise seconds until retry. */
        synchronized long tryAcquire(long now, long windowMillis, int max) {
            if (windowStart == 0L || now - windowStart >= windowMillis) {
                windowStart = now;
                count = 0;
            }
            count++;
            if (count <= max) {
                return -1;
            }
            return Math.max(1, (windowStart + windowMillis - now) / 1000);
        }
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fixed-window rate limiter. Thread-safe; one window per client id.
 *
 * @implNote Uses {@link ConcurrentHashMap#compute} so the check-and-increment
 * for a given client is atomic without a separate lock.
 */
public final class RateLimiter {

    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final Clock clock;
    private final ConcurrentHashMap<String, ClientWindow> clientWindows = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = window.toMillis();
        this.clock = clock;
    }

    /** @return true if the client is under its limit for the current window. */
    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        AtomicBoolean allowed = new AtomicBoolean();

        clientWindows.compute(clientId, (id, existing) -> {
            ClientWindow current = isExpired(existing, now) ? new ClientWindow(now, 0) : existing;
            allowed.set(current.count() < maxRequestsPerWindow);
            return allowed.get() ? current.incremented() : current;
        });

        return allowed.get();
    }

    /** Removes windows that expired more than {@code maxAge} ago, to bound memory use. */
    public void evictStaleClients(Duration maxAge) {
        long cutoff = clock.millis() - windowMillis - maxAge.toMillis();
        clientWindows.values().removeIf(window -> window.windowStartMillis() < cutoff);
    }

    private boolean isExpired(ClientWindow window, long now) {
        return window == null || now - window.windowStartMillis() >= windowMillis;
    }

    private record ClientWindow(long windowStartMillis, int count) {
        ClientWindow incremented() {
            return new ClientWindow(windowStartMillis, count + 1);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Wraps a delegate handler, rejecting a client's requests once it exceeds the configured rate. */
public final class RateLimitingHandler implements HttpHandler {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration STALE_CLIENT_TTL = Duration.ofMinutes(10);
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, new RateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW, Clock.systemUTC()));
    }

    RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        startEvictionSchedule();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);

        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(WINDOW.toSeconds()));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }

    private void startEvictionSchedule() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-eviction");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                () -> rateLimiter.evictStaleClients(STALE_CLIENT_TTL),
                STALE_CLIENT_TTL.toMinutes(), STALE_CLIENT_TTL.toMinutes(), TimeUnit.MINUTES);
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToTheLimit() {
        RateLimiter limiter = newLimiterAt(Instant.EPOCH);

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("client-a"), "request " + i + " should be allowed");
        }
    }

    @Test
    void blocksRequestsBeyondTheLimitWithinTheSameWindow() {
        RateLimiter limiter = newLimiterAt(Instant.EPOCH);
        exhaustLimit(limiter, "client-a");

        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void resetsTheLimitOnceTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        RateLimiter limiter = new RateLimiter(LIMIT, WINDOW, clock);
        exhaustLimit(limiter, "client-a");

        clock.advance(WINDOW.plusSeconds(1));

        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter limiter = newLimiterAt(Instant.EPOCH);
        exhaustLimit(limiter, "client-a");

        assertTrue(limiter.tryAcquire("client-b"));
    }

    private RateLimiter newLimiterAt(Instant now) {
        return new RateLimiter(LIMIT, WINDOW, new MutableClock(now));
    }

    private void exhaustLimit(RateLimiter limiter, String clientId) {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(clientId);
        }
    }

    /** Test double: a {@link Clock} whose current instant can be advanced manually. */
    private static final class MutableClock extends Clock {
        private final AtomicLong epochMillis;

        MutableClock(Instant start) {
            this.epochMillis = new AtomicLong(start.toEpochMilli());
        }

        void advance(Duration duration) {
            epochMillis.addAndGet(duration.toMillis());
        }

        @Override
        public long millis() {
            return epochMillis.get();
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }
    }
}
````

## order

### order · run 1 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {
        public LineItem {
            Objects.requireNonNull(description, "description must not be null");
            if (description.isBlank()) {
                throw new IllegalArgumentException("description must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive: " + quantity);
            }
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
            if (unitPrice.signum() < 0) {
                throw new IllegalArgumentException("unitPrice must not be negative: " + unitPrice);
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record OrderResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            String receipt) {
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one line item");
        }

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, vat, total, discountApplies);

        return new OrderResult(subtotal, discount, vat, total, receipt);
    }

    private String buildReceipt(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(
                    "%-20s %3d x %8.2f = %10.2f%n",
                    item.description(),
                    item.quantity(),
                    item.unitPrice(),
                    item.lineTotal().setScale(2, RoundingMode.HALF_UP)));
        }
        sb.append("-------\n");
        sb.append(String.format("Subtotal (excl. VAT): EUR %10.2f%n", subtotal));
        if (discountApplies) {
            sb.append(String.format("Discount (10%%):       EUR %10.2f%n", discount.negate()));
        }
        sb.append(String.format("VAT (21%%):            EUR %10.2f%n", vat));
        sb.append(String.format("Total (incl. VAT):    EUR %10.2f%n", total));
        return sb.toString();
    }
}
````

````java
List<OrderProcessor.LineItem> items = List.of(
        new OrderProcessor.LineItem("Widget", 3, new BigDecimal("25.00")),
        new OrderProcessor.LineItem("Gadget", 1, new BigDecimal("40.00"))
);

OrderProcessor.OrderResult result = new OrderProcessor().processOrder(items);
System.out.print(result.receipt());
````

### order · run 1 · uncle-bob-junior

````java
// LineItem.java
package com.plg.orders;

import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive: " + unitPrice);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// OrderTotals.java
package com.plg.orders;

import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotalBeforeDiscount, BigDecimal discount, BigDecimal vat, BigDecimal total) {

    public BigDecimal subtotalAfterDiscount() {
        return subtotalBeforeDiscount.subtract(discount);
    }
}
````

````java
// OrderCalculator.java
package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderCalculator() {
    }

    public static OrderTotals calculateTotals(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one line item");
        }

        BigDecimal subtotal = round(sumLineTotals(lineItems));
        BigDecimal discount = round(calculateDiscount(subtotal));
        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = round(subtotalAfterDiscount.multiply(VAT_RATE));
        BigDecimal total = subtotalAfterDiscount.add(vat);

        return new OrderTotals(subtotal, discount, vat, total);
    }

    private static BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal calculateDiscount(BigDecimal subtotal) {
        return isEligibleForDiscount(subtotal) ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static boolean isEligibleForDiscount(BigDecimal subtotal) {
        return subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
// ReceiptFormatter.java
package com.plg.orders;

import java.math.BigDecimal;
import java.util.List;

public final class ReceiptFormatter {

    private static final String CURRENCY_SYMBOL = "EUR";

    private ReceiptFormatter() {
    }

    public static String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private static void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-30s %2d x %8s = %10s%n",
                    item.description(), item.quantity(),
                    formatAmount(item.unitPrice()), formatAmount(item.lineTotal())));
        }
    }

    private static void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-".repeat(60)).append(System.lineSeparator());
        receipt.append(String.format("%-47s %10s%n", "Subtotal", formatAmount(totals.subtotalBeforeDiscount())));
        if (totals.discount().signum() > 0) {
            receipt.append(String.format("%-47s -%9s%n", "Discount (10%)", formatAmount(totals.discount())));
        }
        receipt.append(String.format("%-47s %10s%n", "VAT (21%)", formatAmount(totals.vat())));
        receipt.append(String.format("%-47s %10s%n", "Total", formatAmount(totals.total())));
    }

    private static String formatAmount(BigDecimal amount) {
        return CURRENCY_SYMBOL + " " + amount.toPlainString();
    }
}
````

````java
// OrderProcessor.java
package com.plg.orders;

import java.util.List;

public final class OrderProcessor {

    private OrderProcessor() {
    }

    public static String process(List<LineItem> lineItems) {
        OrderTotals totals = OrderCalculator.calculateTotals(lineItems);
        return ReceiptFormatter.format(lineItems, totals);
    }
}
````

````java
// LineItemTest.java
package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void computesLineTotal() {
        LineItem item = new LineItem("Widget", new BigDecimal("9.99"), 3);

        assertEquals(new BigDecimal("29.97"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("  ", BigDecimal.ONE, 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", BigDecimal.ONE, 0));
    }
}
````

````java
// OrderCalculatorTest.java
package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 2));

        OrderTotals totals = OrderCalculator.calculateTotals(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotalBeforeDiscount());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        OrderTotals totals = OrderCalculator.calculateTotals(items);

        assertEquals(new BigDecimal("120.00"), totals.subtotalBeforeDiscount());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void sumsMultipleLineItems() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("10.00"), 3),
                new LineItem("Gadget", new BigDecimal("5.00"), 4));

        OrderTotals totals = OrderCalculator.calculateTotals(items);

        assertEquals(new BigDecimal("50.00"), totals.subtotalBeforeDiscount());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderCalculator.calculateTotals(List.of()));
    }
}
````

### order · run 2 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Receipt(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            String text) {
    }

    /**
     * Validates line items, throwing if any are malformed.
     */
    public static void validate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalArgumentException("Line item description must not be blank.");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a positive quantity.");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a non-negative unit price.");
            }
        }
    }

    /**
     * Validates the order, applies the volume discount and VAT, and builds a receipt.
     */
    public static Receipt process(List<LineItem> lineItems) {
        validate(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        String text = buildReceiptText(lineItems, subtotal, discount, vat, total, discountApplies);

        return new Receipt(subtotal, discount, vat, total, text);
    }

    private static String buildReceiptText(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(Locale.UK, "%-20s %2d x EUR %8.2f = EUR %8.2f%n",
                    item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
        }
        sb.append("-------\n");
        sb.append(String.format(Locale.UK, "Subtotal (excl. VAT): EUR %8.2f%n", subtotal));
        if (discountApplies) {
            sb.append(String.format(Locale.UK, "Discount (10%%):       EUR %8.2f%n", discount.negate()));
        }
        sb.append(String.format(Locale.UK, "VAT (21%%):            EUR %8.2f%n", vat));
        sb.append(String.format(Locale.UK, "Total:                EUR %8.2f%n", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = new ArrayList<>();
        order.add(new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")));
        order.add(new LineItem("Charity add-on", 2, new BigDecimal("15.00")));

        Receipt receipt = process(order);
        System.out.print(receipt.text());
    }
}
````

### order · run 2 · uncle-bob-junior

````java
// OrderItem.java
import java.math.BigDecimal;
import java.util.Objects;

public record OrderItem(String description, int quantity, BigDecimal unitPrice) {

    public OrderItem {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
// OrderValidationException.java
public final class OrderValidationException extends RuntimeException {

    public OrderValidationException(String message) {
        super(message);
    }
}
````

````java
// OrderItemValidator.java
import java.math.BigDecimal;
import java.util.List;

final class OrderItemValidator {

    private OrderItemValidator() {
    }

    static void validate(List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        items.forEach(OrderItemValidator::validateItem);
    }

    private static void validateItem(OrderItem item) {
        if (item.description().isBlank()) {
            throw new OrderValidationException("Line item description must not be blank");
        }
        if (item.quantity() <= 0) {
            throw new OrderValidationException("Line item quantity must be positive: " + item.description());
        }
        if (item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderValidationException("Line item unit price must not be negative: " + item.description());
        }
    }
}
````

````java
// OrderPricing.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderPricing {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int EURO_SCALE = 2;

    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vat;
    private final BigDecimal total;

    private OrderPricing(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.vat = vat;
        this.total = total;
    }

    static OrderPricing of(List<OrderItem> items) {
        BigDecimal subtotal = sumLineTotals(items);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = vatFor(discountedSubtotal);
        BigDecimal total = round(discountedSubtotal.add(vat));
        return new OrderPricing(round(subtotal), round(discount), round(vat), total);
    }

    private static BigDecimal sumLineTotals(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static BigDecimal vatFor(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(EURO_SCALE, RoundingMode.HALF_UP);
    }

    BigDecimal subtotal() {
        return subtotal;
    }

    BigDecimal discount() {
        return discount;
    }

    BigDecimal vat() {
        return vat;
    }

    BigDecimal total() {
        return total;
    }
}
````

````java
// ReceiptFormatter.java
import java.math.BigDecimal;
import java.util.List;

final class ReceiptFormatter {

    private ReceiptFormatter() {
    }

    static String format(List<OrderItem> items, OrderPricing pricing) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt").append(System.lineSeparator());
        receipt.append("-------").append(System.lineSeparator());
        items.forEach(item -> receipt.append(formatLine(item)).append(System.lineSeparator()));
        receipt.append("-------").append(System.lineSeparator());
        receipt.append(formatAmount("Subtotal", pricing.subtotal())).append(System.lineSeparator());
        if (pricing.discount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatAmount("Discount (10%)", pricing.discount().negate())).append(System.lineSeparator());
        }
        receipt.append(formatAmount("VAT (21%)", pricing.vat())).append(System.lineSeparator());
        receipt.append(formatAmount("Total", pricing.total()));
        return receipt.toString();
    }

    private static String formatLine(OrderItem item) {
        return "%2d x %-20s EUR %8.2f".formatted(item.quantity(), item.description(), item.lineTotal());
    }

    private static String formatAmount(String label, BigDecimal amount) {
        return "%-20s EUR %8.2f".formatted(label, amount);
    }
}
````

````java
// OrderProcessor.java
import java.util.List;

public final class OrderProcessor {

    public String processOrder(List<OrderItem> items) {
        OrderItemValidator.validate(items);
        OrderPricing pricing = OrderPricing.of(items);
        return ReceiptFormatter.format(items, pricing);
    }
}
````

````java
// OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsEmptyOrder() {
        assertThrows(OrderValidationException.class, () -> processor.processOrder(List.of()));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 0, new BigDecimal("10.00")));
        assertThrows(OrderValidationException.class, () -> processor.processOrder(items));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 1, new BigDecimal("-1.00")));
        assertThrows(OrderValidationException.class, () -> processor.processOrder(items));
    }

    @Test
    void appliesNoDiscountAtThreshold() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 1, new BigDecimal("100.00")));
        OrderPricing pricing = OrderPricing.of(items);
        assertEquals(new BigDecimal("0.00"), pricing.discount());
        assertEquals(new BigDecimal("121.00"), pricing.total());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 1, new BigDecimal("200.00")));
        OrderPricing pricing = OrderPricing.of(items);
        assertEquals(new BigDecimal("20.00"), pricing.discount());
        assertEquals(new BigDecimal("217.80"), pricing.total());
    }

    @Test
    void receiptContainsTotal() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 2, new BigDecimal("5.00")));
        String receipt = processor.processOrder(items);
        assertTrue(receipt.contains("Total"));
    }
}
````
