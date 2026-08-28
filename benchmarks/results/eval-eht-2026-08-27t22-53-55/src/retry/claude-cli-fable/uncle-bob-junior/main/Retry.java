import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation and retries it on failure, waiting a fixed delay
 * between attempts.
 *
 * Contract: retries on any {@link Exception} (not {@link Error}); after the
 * final failed attempt the last exception is rethrown unchanged. No delay is
 * applied after the final attempt. If the waiting thread is interrupted, the
 * {@link InterruptedException} propagates immediately and no further attempt
 * is made.
 */
public final class Retry {

    /** An operation that may fail with a checked exception. */
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /** A void operation that may fail with a checked exception. */
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    /** Seam for waiting, so tests never sleep for real. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private static final int MINIMUM_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public static Retry of(int maxAttempts, Duration delayBetweenAttempts) {
        return new Retry(maxAttempts, delayBetweenAttempts, duration -> Thread.sleep(duration.toMillis()));
    }

    Retry(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < MINIMUM_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MINIMUM_ATTEMPTS + ", was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /** Runs the operation, retrying on failure; returns its result or rethrows the last exception. */
    public <T> T execute(CheckedSupplier<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    sleeper.sleep(delayBetweenAttempts);
                }
            }
        }
        throw lastFailure;
    }

    /** Runs a void operation with the same retry behaviour as {@link #execute(CheckedSupplier)}. */
    public void execute(CheckedRunnable operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }
}
