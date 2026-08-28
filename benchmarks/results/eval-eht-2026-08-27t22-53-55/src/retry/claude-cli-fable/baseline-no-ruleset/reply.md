Here's a reusable retry helper. Assumptions: fixed delay between attempts, retries on any exception by default with an optional predicate to limit which exceptions are retryable, and the last exception is rethrown once attempts are exhausted. Interruption during the delay aborts the retry loop and restores the interrupt flag.

```java
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Runs an operation and retries it on failure, up to a maximum number of
 * attempts, waiting a fixed delay between attempts.
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
        this.retryOn = Objects.requireNonNull(retryOn, "retryOn");
    }

    /** Retries on any exception. */
    public static Retry of(int maxAttempts, Duration delay) {
        return new Retry(maxAttempts, delay, e -> true);
    }

    /** Retries only on exceptions matching the given predicate. */
    public static Retry of(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        return new Retry(maxAttempts, delay, retryOn);
    }

    /**
     * Executes the operation, retrying on failure. Returns the first
     * successful result, or rethrows the last exception once all attempts
     * are exhausted or the exception is not retryable.
     *
     * @throws RetryException      wrapping the last failure
     * @throws InterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(Callable<T> operation) throws RetryException, InterruptedException {
        Objects.requireNonNull(operation, "operation");
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                last = e;
                boolean lastAttempt = attempt == maxAttempts;
                if (lastAttempt || !retryOn.test(e)) {
                    throw new RetryException(attempt, e);
                }
                if (!delay.isZero()) {
                    Thread.sleep(delay.toMillis());
                }
            }
        }
        // Unreachable: the loop either returns or throws.
        throw new RetryException(maxAttempts, last);
    }

    /** Convenience overload for operations without a return value. */
    public void execute(Runnable operation) throws RetryException, InterruptedException {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    /** Thrown when the operation still fails after all attempts. */
    public static final class RetryException extends Exception {
        private final int attempts;

        public RetryException(int attempts, Throwable cause) {
            super("Operation failed after " + attempts + " attempt(s)", cause);
            this.attempts = attempts;
        }

        public int getAttempts() {
            return attempts;
        }
    }
}
```

Example usage:

```java
import java.time.Duration;

public class Example {
    public static void main(String[] args) throws Exception {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        String result = retry.execute(() -> fetchDrawResult("participant_001"));
        System.out.println(result);

        // Retry only on transient network errors:
        Retry networkRetry = Retry.of(5, Duration.ofMillis(500),
                e -> e instanceof java.io.IOException);
        networkRetry.execute(() -> uploadReport("participant_001"));
    }

    static String fetchDrawResult(String participantId) {
        // placeholder for a flaky operation
        return "ok";
    }

    static void uploadReport(String participantId) {
        // placeholder
    }
}
```

Design notes:

- `Callable<T>` is used so the operation can return a value and throw checked exceptions; a `Runnable` overload covers void operations.
- The final failure is wrapped in a `RetryException` carrying the attempt count, with the original exception as the cause, so callers can distinguish "exhausted retries" from other errors.
- `InterruptedException` is never retried; it propagates immediately with the interrupt flag restored, so the helper behaves well in thread pools and during shutdown.
- If you need exponential backoff or jitter later, the fixed `delay` field is the single place to change.