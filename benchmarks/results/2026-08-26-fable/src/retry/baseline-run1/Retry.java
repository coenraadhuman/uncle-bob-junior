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
