import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Reusable retry helper: runs an operation and retries it on exception,
 * up to a fixed number of attempts, waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on any exception up to maxAttempts times,
     * waiting delayBetweenAttempts between each attempt.
     *
     * @param operation            the operation to run
     * @param maxAttempts          total number of attempts (must be >= 1)
     * @param delayBetweenAttempts fixed delay to wait between attempts
     * @param <T>                  the result type of the operation
     * @return the result of the operation, if it eventually succeeds
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delayBetweenAttempts.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", interrupted);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastException);
    }

    /**
     * Convenience overload for operations that do not return a value.
     */
    public static void runWithRetry(Runnable operation, int maxAttempts, Duration delayBetweenAttempts) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayBetweenAttempts);
    }

    /**
     * Thrown when an operation still fails after all retry attempts are exhausted.
     */
    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
