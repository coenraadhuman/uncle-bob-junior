import java.time.Duration;
import java.util.function.Consumer;

/**
 * Generic retry helper that re-runs an operation on failure, up to a
 * maximum number of attempts, waiting a fixed delay between attempts.
 */
public final class Retry {

    private Retry() {
    }

    /**
     * Functional interface for an operation that may throw a checked exception.
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws Exception;
    }

    /**
     * Thrown when all retry attempts have been exhausted.
     * The last failure is available via {@link #getCause()}.
     */
    public static final class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Runs {@code operation}, retrying on any exception up to {@code maxAttempts} times,
     * waiting {@code delay} between attempts. No exception is thrown from within
     * the wait itself unless the thread is interrupted, in which case retrying stops.
     *
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param operation   the operation to run
     * @return the operation's result, if any attempt succeeds
     * @throws RetryException if every attempt fails or the thread is interrupted while waiting
     */
    public static <T> T run(int maxAttempts, Duration delay, RetryableOperation<T> operation) {
        return run(maxAttempts, delay, operation, attempt -> { });
    }

    /**
     * Same as {@link #run(int, Duration, RetryableOperation)} but invokes
     * {@code onFailure} after each failed attempt (e.g. for logging), before waiting.
     */
    public static <T> T run(int maxAttempts,
                             Duration delay,
                             RetryableOperation<T> operation,
                             Consumer<AttemptFailure> onFailure) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, was " + maxAttempts);
        }

        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastFailure = e;
                onFailure.accept(new AttemptFailure(attempt, maxAttempts, e));

                boolean isLastAttempt = attempt == maxAttempts;
                if (!isLastAttempt && delay.toMillis() > 0) {
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RetryException(
                                "Retry interrupted while waiting between attempts", ie);
                    }
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempts", lastFailure);
    }

    /**
     * Details of a single failed attempt, passed to the onFailure callback.
     */
    public static final class AttemptFailure {
        private final int attemptNumber;
        private final int maxAttempts;
        private final Exception cause;

        public AttemptFailure(int attemptNumber, int maxAttempts, Exception cause) {
            this.attemptNumber = attemptNumber;
            this.maxAttempts = maxAttempts;
            this.cause = cause;
        }

        public int getAttemptNumber() {
            return attemptNumber;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public Exception getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "Attempt " + attemptNumber + "/" + maxAttempts + " failed: " + cause;
        }
    }
}
