# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_LENGTH = 254;

    // Practical, widely-used pattern covering standard email formats
    // (not full RFC 5322, which is impractical to enforce with regex alone).
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
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

/**
 * Validates email address syntax.
 *
 * ubj: uses a practical regex rather than full RFC 5322 grammar;
 * revisit if internationalised (IDN) addresses need support.
 */
public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "participant@example.com",
        "first.last@example.co.uk",
        "user+tag@sub.example.org",
        "user_name@example-domain.com"
    })
    void acceptsValidAddresses(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "missing-at-sign.com",
        "@example.com",
        "user@",
        "user@example",
        "user@@example.com",
        "user name@example.com"
    })
    void rejectsInvalidAddresses(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";

        assertFalse(EmailValidator.isValid(tooLong));
    }
}
````

### email · run 2 · baseline

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    // Practical RFC 5322-inspired pattern: local-part@domain
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+" +
        "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
        "@" +
        "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?" +
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)+$"
    );

    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_EMAIL_LENGTH = 254;

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

### email · run 2 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
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

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValid("participant001@example.com"));
    }

    @Test
    void acceptsAddressWithSubdomainAndPlusTag() {
        assertTrue(EmailValidator.isValid("first.last+tag@mail.example.co.uk"));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsMissingAtSign() {
        assertFalse(EmailValidator.isValid("participant001example.com"));
    }

    @Test
    void rejectsMissingDomain() {
        assertFalse(EmailValidator.isValid("participant001@"));
    }

    @Test
    void rejectsMissingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("participant001@example"));
    }

    @Test
    void rejectsMultipleAtSigns() {
        assertFalse(EmailValidator.isValid("participant001@@example.com"));
    }

    @Test
    void rejectsAddressLongerThanMaxLength() {
        String localPart = "a".repeat(250);
        assertFalse(EmailValidator.isValid(localPart + "@example.com"));
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int amountIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            List<String> header = Arrays.asList(line.split(",", -1));
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i).trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("Column 'amount' not found in header.");
                return;
            }

            String row;
            int lineNumber = 1;
            while ((row = reader.readLine()) != null) {
                lineNumber++;
                if (row.isBlank()) {
                    continue;
                }
                String[] fields = row.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }
                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }

            System.out.printf("Sum of amount: %.2f%n", total);

        } catch (IOException e) {
            System.err.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
````

### csv · run 1 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SalesTotal {

    private static final String DEFAULT_CSV_PATH = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        Path csvPath = Paths.get(args.length > 0 ? args[0] : DEFAULT_CSV_PATH);
        double total = sumColumn(csvPath, AMOUNT_COLUMN);
        System.out.printf("Total %s: %.2f%n", AMOUNT_COLUMN, total);
    }

    private static double sumColumn(Path csvPath, String columnName) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty: " + csvPath);
            }
            int columnIndex = findColumnIndex(headerLine, columnName);
            return sumRows(reader, columnIndex);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }

    private static double sumRows(BufferedReader reader, int columnIndex) throws IOException {
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(DELIMITER);
            total += Double.parseDouble(fields[columnIndex].trim());
        }
        return total;
    }
}
````

### csv · run 2 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double total = 0.0;
        int amountIndex = -1;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");

                if (lineNumber == 1) {
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.err.println("Could not find an 'amount' column in the header.");
                        return;
                    }
                    continue;
                }

                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                if (rawValue.isEmpty()) {
                    continue;
                }

                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + csvFile + ": " + e.getMessage());
            return;
        }

        System.out.printf("Sum of 'amount': %.2f%n", total);
    }
}
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAmountSum {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        String csvPath = args.length > 0 ? args[0] : "sales.csv";
        double total = sumAmountColumn(csvPath);
        System.out.printf("Total amount: %.2f%n", total);
    }

    private static double sumAmountColumn(String csvPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvPath);
            }

            int amountColumnIndex = findAmountColumnIndex(headerLine);
            double total = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                total += parseAmount(line, amountColumnIndex);
            }
            return total;
        }
    }

    private static int findAmountColumnIndex(String headerLine) throws IOException {
        String[] headers = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IOException("Column '" + AMOUNT_COLUMN_NAME + "' not found in CSV header");
    }

    private static double parseAmount(String line, int amountColumnIndex) throws IOException {
        String[] fields = line.split(CSV_DELIMITER);
        if (amountColumnIndex >= fields.length) {
            throw new IOException("Row is missing the amount field: " + line);
        }
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
````

## retry

### retry · run 1 · baseline

````java
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Reusable helper that retries an operation a fixed number of times,
 * waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on exception up to maxAttempts times,
     * waiting delayBetweenAttempts between each attempt.
     *
     * @param operation            the operation to run
     * @param maxAttempts          maximum number of attempts (must be >= 1)
     * @param delayBetweenAttempts fixed delay to wait between attempts
     * @param <T>                  the result type of the operation
     * @return the result of the first successful attempt
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation,
                                      int maxAttempts,
                                      Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delayBetweenAttempts.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", interrupted);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Convenience overload for operations that don't return a value.
     */
    public static void runWithRetry(RunnableWithException operation,
                                     int maxAttempts,
                                     Duration delayBetweenAttempts) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayBetweenAttempts);
    }

    /**
     * Like Runnable, but allowed to throw a checked exception.
     */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /**
     * Thrown when an operation still fails after all retry attempts are exhausted.
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
        3,
        Duration.ofSeconds(2)
);

RetryHelper.runWithRetry(
        () -> writeToFile(data),
        5,
        Duration.ofMillis(500)
);
````

### retry · run 1 · uncle-bob-junior

````java
// RetryableOperation.java
package com.plg.retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T call() throws Exception;
}
````

````java
// RetryConfig.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class RetryConfig {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public RetryConfig(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getDelayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
````

````java
// RetryExhaustedException.java
package com.plg.retry;

public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempts", lastFailure);
    }
}
````

````java
// RetryInterruptedException.java
package com.plg.retry;

public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry delay was interrupted", cause);
    }
}
````

````java
// Retryer.java
package com.plg.retry;

public final class Retryer {

    public <T> T run(RetryableOperation<T> operation, RetryConfig config) {
        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= config.getMaxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            delayBeforeNextAttempt(attempt, config);
        }

        throw new RetryExhaustedException(config.getMaxAttempts(), lastFailure);
    }

    private void delayBeforeNextAttempt(int attempt, RetryConfig config) {
        boolean hasMoreAttempts = attempt < config.getMaxAttempts();
        if (!hasMoreAttempts) {
            return;
        }
        sleep(config.getDelayBetweenAttempts());
    }

    private void sleep(java.time.Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interruptedException);
        }
    }
}
````

````java
// RetryerTest.java
package com.plg.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ZERO;
    private static final Duration SHORT_DELAY = Duration.ofMillis(20);

    @Test
    void returnsResultWhenOperationSucceedsFirstTry() {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(3, NO_DELAY);

        String result = retryer.run(() -> "ok", config);

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilOperationSucceeds() {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(5, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger(0);

        String result = retryer.run(() -> {
            int callNumber = callCount.incrementAndGet();
            if (callNumber < 3) {
                throw new RuntimeException("transient failure " + callNumber);
            }
            return "recovered";
        }, config);

        assertEquals("recovered", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger(0);
        RuntimeException persistentFailure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.run(() -> {
                    callCount.incrementAndGet();
                    throw persistentFailure;
                }, config));

        assertEquals(3, callCount.get());
        assertEquals(persistentFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterLast() {
        Retryer retryer = new Retryer();
        int maxAttempts = 3;
        RetryConfig config = new RetryConfig(maxAttempts, SHORT_DELAY);

        long startNanos = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                retryer.run(() -> {
                    throw new RuntimeException("always fails");
                }, config));
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        long expectedMinimumMillis = SHORT_DELAY.toMillis() * (maxAttempts - 1);
        assertTrue(elapsedMillis >= expectedMinimumMillis,
                "expected at least " + expectedMinimumMillis + "ms, took " + elapsedMillis + "ms");
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(0, NO_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(3, Duration.ofMillis(-1)));
    }

    @Test
    void wrapsInterruptionDuringDelay() throws InterruptedException {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(2, Duration.ofSeconds(10));
        AtomicInteger caughtCorrectException = new AtomicInteger(0);

        Thread worker = new Thread(() -> {
            try {
                retryer.run(() -> {
                    throw new RuntimeException("fails so the delay kicks in");
                }, config);
            } catch (RetryInterruptedException expected) {
                caughtCorrectException.set(1);
            }
        });

        worker.start();
        Thread.sleep(50);
        worker.interrupt();
        worker.join();

        assertEquals(1, caughtCorrectException.get());
    }
}
````

### retry · run 2 · baseline

````java
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Reusable retry helper: runs an operation and retries it on exception,
 * up to a fixed number of attempts, waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on any exception up to maxAttempts times,
     * waiting delayBetweenAttempts between each attempt.
     *
     * @param operation            the operation to run
     * @param maxAttempts          total number of attempts (must be >= 1)
     * @param delayBetweenAttempts fixed delay to wait between attempts
     * @param <T>                  the result type of the operation
     * @return the result of the operation, if it eventually succeeds
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delayBetweenAttempts.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", interrupted);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastException);
    }

    /**
     * Convenience overload for operations that do not return a value.
     */
    public static void runWithRetry(Runnable operation, int maxAttempts, Duration delayBetweenAttempts) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayBetweenAttempts);
    }

    /**
     * Thrown when an operation still fails after all retry attempts are exhausted.
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
        3,
        Duration.ofSeconds(2)
);

RetryHelper.runWithRetry(
        () -> writeToFile(data),
        5,
        Duration.ofMillis(500)
);
````

### retry · run 2 · uncle-bob-junior

````java
package com.plg.retry;

/**
 * A unit of work that may fail and can be retried.
 *
 * @param <T> the type produced on success
 */
@FunctionalInterface
public interface RetryableTask<T> {

    T execute() throws Exception;
}
````

````java
package com.plg.retry;

/**
 * Thrown when a {@link RetryableTask} still fails after the configured
 * number of attempts. The last failure is available via {@link #getCause()}.
 */
public class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempt(s)", lastFailure);
    }
}
````

````java
package com.plg.retry;

import java.time.Duration;

/**
 * Runs a {@link RetryableTask}, retrying on failure up to a fixed number of
 * attempts with a fixed delay between them.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    /**
     * Executes {@code task}, retrying on any {@link Exception} until it
     * succeeds or {@code maxAttempts} is reached.
     *
     * @throws RetryExhaustedException if every attempt failed
     */
    public <T> T run(RetryableTask<T> task) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return task.execute();
            } catch (Exception failure) {
                lastFailure = failure;
                if (isLastAttempt(attempt)) {
                    break;
                }
                waitBeforeNextAttempt();
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private boolean isLastAttempt(int attempt) {
        return attempt == maxAttempts;
    }

    private void waitBeforeNextAttempt() {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry wait was interrupted", interrupted);
        }
    }
}
````

````java
package com.plg.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultWhenFirstAttemptSucceeds() {
        Retryer retryer = new Retryer(3, NO_DELAY);

        String result = retryer.run(() -> "ok");

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilTaskEventuallySucceeds() {
        Retryer retryer = new Retryer(3, NO_DELAY);
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retryer.run(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer(2, NO_DELAY);
        AtomicInteger attempts = new AtomicInteger(0);

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
            retryer.run(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("always fails");
            }));

        assertEquals(2, attempts.get());
        assertEquals("always fails", thrown.getCause().getMessage());
    }

    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, NO_DELAY));
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
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler and rejects requests once a client exceeds
 * maxRequests within windowMillis, using a per-client sliding window
 * of request timestamps.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;

    // Per-client request timestamp log (sliding window).
    private final Map<String, ClientWindow> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleaner");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;

        // Periodically drop clients that have been idle for longer than the
        // window, so memory doesn't grow unbounded with one-off visitors.
        cleaner.scheduleAtFixedRate(this::evictIdleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        long now = System.currentTimeMillis();

        ClientWindow window = clients.computeIfAbsent(clientId, id -> new ClientWindow());

        if (window.tryAcquire(now, windowMillis, maxRequests)) {
            delegate.handle(exchange);
        } else {
            long retryAfterSeconds = (windowMillis / 1000) + 1;
            rejectWithTooManyRequests(exchange, retryAfterSeconds);
        }
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }

    private void evictIdleClients() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> e.getValue().isIdle(now, windowMillis));
    }

    private static String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private static void rejectWithTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Thread-safe sliding-window request log for a single client. */
    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ConcurrentLinkedDeque<>();

        synchronized boolean tryAcquire(long now, long windowMillis, int maxRequests) {
            evictOlderThan(now - windowMillis);
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }

        synchronized boolean isIdle(long now, long windowMillis) {
            evictOlderThan(now - windowMillis);
            return timestamps.isEmpty();
        }

        private void evictOlderThan(long cutoff) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }
        }
    }

    // Example wiring with the JDK's built-in HttpServer.
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler applicationHandler = exchange -> {
            byte[] response = "OK\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        };

        // 5 requests per minute per client IP.
        RateLimitingHandler limited = RateLimitingHandler.perMinute(applicationHandler, 5);

        server.createContext("/", limited);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sliding-window request counter, one window per client key.
 */
public final class RateLimiter implements AutoCloseable {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimestampsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService staleClientCleaner = Executors.newSingleThreadScheduledExecutor(this::newDaemonThread);

    public RateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, Clock.systemUTC());
    }

    public RateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
        long cleanupIntervalSeconds = Math.max(1, windowDuration.toSeconds());
        staleClientCleaner.scheduleAtFixedRate(
                this::removeClientsWithNoRecentRequests, cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS);
    }

    public boolean isRequestAllowed(String clientKey) {
        Deque<Instant> timestamps = requestTimestampsByClient.computeIfAbsent(clientKey, key -> new ConcurrentLinkedDeque<>());
        synchronized (timestamps) {
            evictExpiredTimestamps(timestamps);
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(clock.instant());
            return true;
        }
    }

    public Duration windowDuration() {
        return windowDuration;
    }

    @Override
    public void close() {
        staleClientCleaner.shutdownNow();
    }

    private void evictExpiredTimestamps(Deque<Instant> timestamps) {
        Instant windowStart = clock.instant().minus(windowDuration);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }
    }

    private void removeClientsWithNoRecentRequests() {
        requestTimestampsByClient.forEach((clientKey, timestamps) -> {
            synchronized (timestamps) {
                evictExpiredTimestamps(timestamps);
                if (timestamps.isEmpty()) {
                    requestTimestampsByClient.remove(clientKey, timestamps);
                }
            }
        });
    }

    private Thread newDaemonThread(Runnable task) {
        Thread thread = new Thread(task, "rate-limiter-cleanup");
        thread.setDaemon(true);
        return thread;
    }
}
````

````java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decorates an HttpHandler with per-client rate limiting.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429;

    private final HttpHandler delegateHandler;
    private final RateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegateHandler, RateLimiter rateLimiter) {
        this.delegateHandler = delegateHandler;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = resolveClientKey(exchange);
        if (!rateLimiter.isRequestAllowed(clientKey)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegateHandler.handle(exchange);
    }

    private String resolveClientKey(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] responseBody = "429 Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(rateLimiter.windowDuration().toSeconds()));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS_CODE, responseBody.length);
        try (OutputStream responseStream = exchange.getResponseBody()) {
            responseStream.write(responseBody);
        }
    }
}
````

````java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Example wiring: 5 requests per 60 seconds per client IP.
 */
public final class ExampleServer {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/", new RateLimitingHttpHandler(ExampleServer::handleRequest, rateLimiter));
        server.start();
    }

    private static void handleRequest(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] responseBody = "OK\n".getBytes();
        exchange.sendResponseHeaders(200, responseBody.length);
        try (var responseStream = exchange.getResponseBody()) {
            responseStream.write(responseBody);
        }
    }
}
````

````java
package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW_DURATION = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToTheLimit() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(rateLimiter.isRequestAllowed("client-a"));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.isRequestAllowed("client-a");
        }

        assertFalse(rateLimiter.isRequestAllowed("client-a"));
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, clock);

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.isRequestAllowed("client-a");
        }
        assertFalse(rateLimiter.isRequestAllowed("client-a"));

        clock.advanceBy(WINDOW_DURATION.plusSeconds(1));

        assertTrue(rateLimiter.isRequestAllowed("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.isRequestAllowed("client-a");
        }

        assertTrue(rateLimiter.isRequestAllowed("client-b"));
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        MutableClock(Instant startInstant) {
            this.currentInstant = startInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
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
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Not suitable for multi-instance deployments; state is process-local.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, 60_000L);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        ClientWindow window = clients.computeIfAbsent(clientKey, k -> new ClientWindow());

        long retryAfterSeconds = window.tryConsume(windowMillis, maxRequestsPerWindow);
        if (retryAfterSeconds >= 0) {
            sendTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        maybeCleanup();
        delegate.handle(exchange);
    }

    private static String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private static void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    // Occasionally sweep clients with no recent activity so the map doesn't grow unbounded.
    private volatile long lastCleanup = 0L;
    private void maybeCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < windowMillis) {
            return;
        }
        lastCleanup = now;
        for (Iterator<ConcurrentHashMap.Entry<String, ClientWindow>> it = clients.entrySet().iterator(); it.hasNext(); ) {
            ConcurrentHashMap.Entry<String, ClientWindow> entry = it.next();
            if (entry.getValue().isStale(now, windowMillis)) {
                it.remove();
            }
        }
    }

    /** Tracks request timestamps for one client within the sliding window. */
    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * @return -1 if the request is allowed, otherwise seconds until the client may retry.
         */
        long tryConsume(long windowMillis, int maxRequests) {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                evictOlderThan(now - windowMillis);

                if (timestamps.size() >= maxRequests) {
                    long oldest = timestamps.peekFirst();
                    long retryAfterMillis = windowMillis - (now - oldest);
                    return Math.max(1, (retryAfterMillis + 999) / 1000);
                }

                timestamps.addLast(now);
                return -1;
            } finally {
                lock.unlock();
            }
        }

        boolean isStale(long now, long windowMillis) {
            lock.lock();
            try {
                evictOlderThan(now - windowMillis);
                return timestamps.isEmpty();
            } finally {
                lock.unlock();
            }
        }

        private void evictOlderThan(long cutoff) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpHandler myHandler = exchange -> {
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        };

        server.createContext("/api", RateLimitingHandler.perMinute(myHandler, 5));
        server.start();
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToLimitWithinWindow() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, fixedClock(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsExceedingLimitWithinWindow() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, fixedClock(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void resetsCountAfterWindowElapses() {
        AtomicLong now = new AtomicLong(0);
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, now::get);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }
        assertFalse(limiter.tryAcquire("client-a"));

        now.set(WINDOW.toMillis() + 1);

        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, fixedClock(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertTrue(limiter.tryAcquire("client-b"));
    }

    @Test
    void evictStaleWindowsRemovesClientsInactiveForTwoWindows() {
        AtomicLong now = new AtomicLong(0);
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, now::get);
        limiter.tryAcquire("client-a");

        now.set(WINDOW.toMillis() * 2 + 1);
        limiter.evictStaleWindows();

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    private static LongSupplier fixedClock(long value) {
        return () -> value;
    }
}
````

## order

### order · run 1 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        BigDecimal lineTotal() {
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

    public static OrderResult process(List<LineItem> lineItems) {
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
        BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, vat, total, discountApplies);

        return new OrderResult(subtotal, discount, vat, total, receipt);
    }

    private static String buildReceipt(
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
                    Locale.UK,
                    "%-20s %3d x EUR %8.2f = EUR %8.2f%n",
                    item.description(),
                    item.quantity(),
                    item.unitPrice(),
                    item.lineTotal().setScale(2, RoundingMode.HALF_UP)));
        }
        sb.append("-------\n");
        sb.append(String.format(Locale.UK, "Subtotal (excl. VAT): EUR %8.2f%n", subtotal));
        if (discountApplies) {
            sb.append(String.format(Locale.UK, "Discount (10%%):       EUR %8.2f%n", discount));
        }
        sb.append(String.format(Locale.UK, "VAT (21%%):            EUR %8.2f%n", vat));
        sb.append(String.format(Locale.UK, "Total (incl. VAT):    EUR %8.2f%n", total));
        return sb.toString();
    }
}
````

### order · run 1 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new InvalidOrderException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new InvalidOrderException("Unit price must be greater than zero: " + description);
        }
        if (quantity <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero: " + description);
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
````

````java
import java.math.BigDecimal;

public record Receipt(BigDecimal subtotal, BigDecimal discount, BigDecimal vatAmount, BigDecimal total) {

    public String toReceiptString() {
        return """
                Subtotal:  %s
                Discount:  %s
                VAT (21%%): %s
                Total:     %s
                """.formatted(
                Money.format(subtotal),
                Money.format(discount),
                Money.format(vatAmount),
                Money.format(total));
    }
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

final class Money {

    private static final int SCALE = 2;

    private Money() {
    }

    static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    static String format(BigDecimal amount) {
        return String.format(Locale.US, "€%.2f", amount);
    }
}
````

````java
import java.math.BigDecimal;
import java.util.List;

public class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    public Receipt processOrder(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item");
        }

        BigDecimal subtotal = calculateSubtotal(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vatAmount);

        return new Receipt(
                Money.round(subtotal),
                Money.round(discount),
                Money.round(vatAmount),
                Money.round(total));
    }

    private BigDecimal calculateSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean isEligibleForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return isEligibleForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }
}
````

````java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("50"), 2); // subtotal = 100
        Receipt receipt = processor.processOrder(List.of(item));

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("60"), 2); // subtotal = 120
        Receipt receipt = processor.processOrder(List.of(item));

        assertEquals(new BigDecimal("12.00"), receipt.discount());
        assertEquals(new BigDecimal("130.68"), receipt.total()); // (120-12)*1.21
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidOrderException.class, () -> processor.processOrder(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(InvalidOrderException.class, () -> processor.processOrder(null));
    }

    @Test
    void rejectsLineItemWithBlankDescription() {
        assertThrows(InvalidOrderException.class,
                () -> new LineItem("  ", new BigDecimal("10"), 1));
    }

    @Test
    void rejectsLineItemWithNonPositivePrice() {
        assertThrows(InvalidOrderException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsLineItemWithNonPositiveQuantity() {
        assertThrows(InvalidOrderException.class,
                () -> new LineItem("Widget", new BigDecimal("10"), 0));
    }

    @Test
    void receiptStringContainsFormattedAmounts() {
        LineItem item = new LineItem("Widget", new BigDecimal("50"), 1); // subtotal = 50
        Receipt receipt = processor.processOrder(List.of(item));

        String receiptText = receipt.toReceiptString();

        assertEquals(true, receiptText.contains("€50.00"));
        assertEquals(true, receiptText.contains("€60.50")); // 50 * 1.21
    }
}
````

### order · run 2 · baseline

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public static final class LineItem {
        private final String description;
        private final int quantity;
        private final BigDecimal unitPrice;

        public LineItem(String description, int quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class OrderValidationException extends RuntimeException {
        public OrderValidationException(String message) {
            super(message);
        }
    }

    public static final class OrderResult {
        public final BigDecimal subtotalExVat;
        public final BigDecimal discount;
        public final BigDecimal vatAmount;
        public final BigDecimal total;
        public final String receipt;

        public OrderResult(BigDecimal subtotalExVat, BigDecimal discount, BigDecimal vatAmount,
                            BigDecimal total, String receipt) {
            this.subtotalExVat = subtotalExVat;
            this.discount = discount;
            this.vatAmount = vatAmount;
            this.total = total;
            this.receipt = receipt;
        }
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        validate(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(SCALE);

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = discountedSubtotal.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vatAmount).setScale(SCALE, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, discountedSubtotal, vatAmount, total);

        return new OrderResult(subtotal, discount, vatAmount, total, receipt);
    }

    private void validate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item == null) {
                throw new OrderValidationException("Line item cannot be null.");
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new OrderValidationException("Line item description cannot be empty.");
            }
            if (item.getQuantity() <= 0) {
                throw new OrderValidationException(
                        "Line item '" + item.getDescription() + "' must have a positive quantity.");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException(
                        "Line item '" + item.getDescription() + "' must have a non-negative unit price.");
            }
        }
    }

    private String buildReceipt(List<LineItem> lineItems, BigDecimal subtotal, BigDecimal discount,
                                 BigDecimal discountedSubtotal, BigDecimal vatAmount, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt - ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .append(System.lineSeparator());
        sb.append("--------------------------------------------------").append(System.lineSeparator());

        for (LineItem item : lineItems) {
            sb.append(String.format("%-30s %3d x %8s = %10s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatAmount(item.getUnitPrice()),
                    formatAmount(item.lineTotal())));
        }

        sb.append("--------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("%-46s %10s%n", "Subtotal (ex. VAT):", formatAmount(subtotal)));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-46s %10s%n", "Discount (10%):", "-" + formatAmount(discount)));
            sb.append(String.format("%-46s %10s%n", "Subtotal after discount:", formatAmount(discountedSubtotal)));
        }

        sb.append(String.format("%-46s %10s%n", "VAT (21%):", formatAmount(vatAmount)));
        sb.append("--------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("%-46s %10s%n", "Total:", formatAmount(total)));

        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return "EUR " + amount.setScale(SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        List<LineItem> items = List.of(
                new LineItem("Lottery subscription - monthly", 2, new BigDecimal("45.00")),
                new LineItem("Charity donation add-on", 1, new BigDecimal("15.00"))
        );

        OrderProcessor processor = new OrderProcessor();
        OrderResult result = processor.processOrder(items);
        System.out.println(result.receipt);
    }
}
````

### order · run 2 · uncle-bob-junior

````java
import java.math.BigDecimal;

public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
````

````java
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Receipt(
        List<LineItem> lineItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal total) {
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public Receipt process(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = computeSubtotal(lineItems);
        BigDecimal discount = computeDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = computeVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(lineItems, subtotal, discount, vat, total);
    }

    private void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        lineItems.forEach(this::validateLineItem);
    }

    private void validateLineItem(LineItem lineItem) {
        if (lineItem == null) {
            throw new InvalidOrderException("Line item must not be null.");
        }
        if (lineItem.description() == null || lineItem.description().isBlank()) {
            throw new InvalidOrderException("Line item description must not be blank.");
        }
        if (lineItem.quantity() <= 0) {
            throw new InvalidOrderException(
                    "Line item quantity must be positive: " + lineItem.description());
        }
        if (lineItem.unitPrice() == null || lineItem.unitPrice().signum() < 0) {
            throw new InvalidOrderException(
                    "Line item unit price must not be negative: " + lineItem.description());
        }
    }

    private BigDecimal computeSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal computeVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ReceiptFormatter {

    private static final String CURRENCY_SUFFIX = " EUR";
    private static final String SEPARATOR = "-".repeat(32);
    private static final String ITEM_ROW_FORMAT = "%dx %-20s %8s";
    private static final String AMOUNT_ROW_FORMAT = "%-22s %8s";

    public String format(Receipt receipt) {
        StringBuilder receiptText = new StringBuilder();
        appendLineItems(receiptText, receipt.lineItems());
        receiptText.append(SEPARATOR).append(System.lineSeparator());
        appendAmountRow(receiptText, "Subtotal", receipt.subtotal());
        appendDiscountRowIfApplied(receiptText, receipt.discount());
        appendAmountRow(receiptText, "VAT (21%)", receipt.vat());
        appendAmountRow(receiptText, "Total", receipt.total());
        return receiptText.toString();
    }

    private void appendLineItems(StringBuilder receiptText, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receiptText.append(formatLineItem(item)).append(System.lineSeparator());
        }
    }

    private String formatLineItem(LineItem item) {
        return ITEM_ROW_FORMAT.formatted(
                item.quantity(), item.description(), formatMoney(item.lineTotal()));
    }

    private void appendDiscountRowIfApplied(StringBuilder receiptText, BigDecimal discount) {
        if (discount.signum() > 0) {
            appendAmountRow(receiptText, "Discount (10%)", discount.negate());
        }
    }

    private void appendAmountRow(StringBuilder receiptText, String label, BigDecimal amount) {
        receiptText.append(AMOUNT_ROW_FORMAT.formatted(label, formatMoney(amount)))
                .append(System.lineSeparator());
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP) + CURRENCY_SUFFIX;
    }
}
````

````java
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("50.00")));

        Receipt receipt = processor.process(items);

        assertEquals(new BigDecimal("100.00"), receipt.subtotal());
        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 3, new BigDecimal("40.00")));

        Receipt receipt = processor.process(items);

        assertEquals(new BigDecimal("120.00"), receipt.subtotal());
        assertEquals(new BigDecimal("12.00"), receipt.discount());
        assertEquals(new BigDecimal("22.68"), receipt.vat());
        assertEquals(new BigDecimal("130.68"), receipt.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidOrderException.class, () -> processor.process(List.of()));
    }

    @Test
    void rejectsBlankDescription() {
        List<LineItem> items = List.of(new LineItem(" ", 1, new BigDecimal("10.00")));

        assertThrows(InvalidOrderException.class, () -> processor.process(items));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", 0, new BigDecimal("10.00")));

        assertThrows(InvalidOrderException.class, () -> processor.process(items));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("-1.00")));

        assertThrows(InvalidOrderException.class, () -> processor.process(items));
    }
}
````
