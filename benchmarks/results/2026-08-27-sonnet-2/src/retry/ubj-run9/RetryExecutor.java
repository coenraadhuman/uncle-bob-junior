import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed delay
 * between attempts when it throws an exception.
 *
 * <p>Thread-safe and stateless: a single instance can be reused across calls.
 */
public final class RetryExecutor {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public RetryExecutor(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1: " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
    }

    /**
     * Executes {@code operation}, retrying on any exception until it succeeds
     * or {@code maxAttempts} have been made.
     *
     * @throws RetryExhaustedException if every attempt failed; the last failure is the cause
     * @throws InterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(RetryableOperation<T> operation) throws InterruptedException {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastFailure = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayBetweenAttempts.toMillis());
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(int attemptsMade, Exception lastFailure) {
            super("Operation failed after " + attemptsMade + " attempts", lastFailure);
        }
    }
}
