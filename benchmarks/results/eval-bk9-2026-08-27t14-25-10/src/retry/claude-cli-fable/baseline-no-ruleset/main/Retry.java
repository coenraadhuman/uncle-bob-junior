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
