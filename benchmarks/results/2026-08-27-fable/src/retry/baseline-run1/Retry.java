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
