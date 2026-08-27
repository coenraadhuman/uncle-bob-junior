import java.time.Duration;
import java.util.Objects;

/**
 * Generic retry helper: runs an operation, retrying on exception up to a
 * maximum number of attempts, waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface RetryableAction {
        void execute() throws Exception;
    }

    /**
     * Runs the given operation, retrying on any exception.
     *
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param operation   the operation to run
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(int maxAttempts, Duration delay, RetryableOperation<T> operation) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        Objects.requireNonNull(delay, "delay must not be null");
        Objects.requireNonNull(operation, "operation must not be null");

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    if (!delay.isZero() && !delay.isNegative()) {
                        Thread.sleep(delay.toMillis());
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted after " + attempt + " attempt(s)", lastFailure);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastFailure);
    }

    /** Convenience overload for operations that don't return a value. */
    public static void runWithRetry(int maxAttempts, Duration delay, RetryableAction action) {
        Objects.requireNonNull(action, "action must not be null");
        runWithRetry(maxAttempts, delay, () -> {
            action.execute();
            return null;
        });
    }

    /** Thrown when an operation still fails after exhausting all retry attempts. */
    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
