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
