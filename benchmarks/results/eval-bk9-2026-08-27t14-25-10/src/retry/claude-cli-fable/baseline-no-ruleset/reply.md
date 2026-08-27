Here's a reusable retry helper. Assumptions I've made: Java 8+ compatibility, a fixed (non-exponential) delay as requested, support for both value-returning operations and void ones, retry on any exception by default with an optional predicate to control which exceptions are retryable, and interruption is respected (the helper stops and rethrows if the thread is interrupted during the wait).

```java
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Runs an operation and retries it when it throws, up to a maximum number
 * of attempts, waiting a fixed delay between attempts.
 */
public final class Retry {

    private final int maxAttempts;
    private final long delayMillis;
    private final Predicate<Exception> retryOn;

    private Retry(int maxAttempts, long delayMillis, Predicate<Exception> retryOn) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
        this.retryOn = Objects.requireNonNull(retryOn, "retryOn");
    }

    /** Creates a helper that retries on any exception. */
    public static Retry of(int maxAttempts, long delayMillis) {
        return new Retry(maxAttempts, delayMillis, e -> true);
    }

    /** Creates a helper that only retries exceptions matching the given predicate. */
    public static Retry of(int maxAttempts, long delayMillis, Predicate<Exception> retryOn) {
        return new Retry(maxAttempts, delayMillis, retryOn);
    }

    /**
     * Runs the operation, retrying on failure. Returns the operation's result
     * on the first successful attempt.
     *
     * @throws RetryExhaustedException if all attempts fail; the last exception
     *         is the cause and earlier ones are attached as suppressed
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public <T> T call(Callable<T> operation) throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(operation, "operation");
        Exception[] failures = new Exception[maxAttempts];

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                failures[attempt - 1] = e;
                if (attempt == maxAttempts || !retryOn.test(e)) {
                    throw exhausted(attempt, failures);
                }
                if (delayMillis > 0) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw new AssertionError("unreachable");
    }

    /** Runs a void operation, retrying on failure. */
    public void run(ThrowingRunnable operation) throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(operation, "operation");
        call(() -> {
            operation.run();
            return null;
        });
    }

    private RetryExhaustedException exhausted(int attemptsMade, Exception[] failures) {
        Exception last = failures[attemptsMade - 1];
        RetryExhaustedException ex = new RetryExhaustedException(
                "Operation failed after " + attemptsMade + " attempt(s)", last);
        for (int i = 0; i < attemptsMade - 1; i++) {
            ex.addSuppressed(failures[i]);
        }
        return ex;
    }

    /** A void operation that may throw. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Thrown when all retry attempts have failed. */
    public static class RetryExhaustedException extends Exception {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```

Example usage:

```java
public class Example {
    public static void main(String[] args) throws Exception {
        Retry retry = Retry.of(3, 500); // up to 3 attempts, 500 ms between them

        // Value-returning operation
        String response = retry.call(() -> fetchFromApi("participant_001"));
        System.out.println(response);

        // Void operation, retrying only on IllegalStateException
        Retry selective = Retry.of(5, 1000, e -> e instanceof IllegalStateException);
        selective.run(() -> sendNotification("participant_001"));
    }

    static String fetchFromApi(String id) { return "ok:" + id; }
    static void sendNotification(String id) { }
}
```

A few design notes:

- **Failure reporting**: when all attempts fail, you get a single `RetryExhaustedException` whose cause is the last failure, with earlier failures attached as suppressed exceptions, so nothing is lost when debugging.
- **Interruption**: `InterruptedException` from the operation or the sleep is propagated immediately rather than retried, which keeps the helper well behaved in thread pools and during shutdown.
- **No retry on non-matching exceptions**: if you supply a predicate and an exception doesn't match, the helper fails fast instead of burning the remaining attempts.
- The delay is fixed as requested; if you later want exponential backoff or jitter, the sleep calculation is the only place that needs to change.