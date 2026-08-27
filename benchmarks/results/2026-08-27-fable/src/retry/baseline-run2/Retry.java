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
