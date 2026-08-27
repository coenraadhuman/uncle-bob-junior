import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation and retries it with a fixed delay between attempts.
 *
 * <p>Any {@link Exception} triggers a retry, except {@link InterruptedException},
 * which is rethrown immediately so cancellation is honoured. When all attempts
 * fail, the last failure is rethrown. Instances are immutable and thread-safe.
 */
public final class FixedDelayRetry {

    /** Boundary for waiting, injectable so tests never really sleep. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private static final int MINIMUM_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    /**
     * @param maxAttempts total number of attempts, at least 1 (1 means no retry)
     * @param delayBetweenAttempts non-negative wait between attempts
     */
    public FixedDelayRetry(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, duration -> Thread.sleep(duration.toMillis()));
    }

    FixedDelayRetry(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < MINIMUM_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MINIMUM_ATTEMPTS
                    + " but was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative but was "
                    + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * Runs the operation until it succeeds or all attempts are used.
     *
     * @return the operation's result
     * @throws InterruptedException immediately if the operation or the wait is interrupted
     * @throws Exception the last failure, once all attempts are exhausted
     */
    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (InterruptedException interrupted) {
                throw interrupted;
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt < maxAttempts) {
                sleeper.sleep(delayBetweenAttempts);
            }
        }
        throw lastFailure;
    }
}
