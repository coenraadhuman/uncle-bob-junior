package retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Retries an operation up to a maximum number of attempts, waiting a fixed
 * delay between attempts. Instances are immutable and safe to share.
 *
 * <p>Preconditions: {@code maxAttempts >= 1}, {@code delay} is non-negative.
 */
public final class RetryPolicy {

    /** Seam for waiting, so tests never sleep for real. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        return new RetryPolicy(maxAttempts, delayBetweenAttempts,
                duration -> Thread.sleep(duration.toMillis()));
    }

    RetryPolicy(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * Runs the operation, retrying on any exception until it succeeds or
     * the attempt limit is reached.
     *
     * @throws RetryExhaustedException if every attempt failed
     * @throws InterruptedException    if interrupted while waiting between attempts
     */
    public <T> T execute(CheckedSupplier<T> operation)
            throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(operation, "operation");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (attempt > 1) {
                waitBeforeNextAttempt();
            }
            try {
                return operation.get();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw interrupted;
            } catch (Exception failure) {
                if (lastFailure != null) {
                    failure.addSuppressed(lastFailure);
                }
                lastFailure = failure;
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    /** Runs a result-less operation with the same retry behaviour as {@link #execute}. */
    public void executeVoid(CheckedRunnable operation)
            throws RetryExhaustedException, InterruptedException {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private void waitBeforeNextAttempt() throws InterruptedException {
        if (!delayBetweenAttempts.isZero()) {
            sleeper.sleep(delayBetweenAttempts);
        }
    }
}
