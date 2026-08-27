import java.util.concurrent.Callable;

/**
 * Generic retry helper: runs an operation, retrying on exception up to a
 * maximum number of attempts, with a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delayMillis fixed delay between attempts, in milliseconds
     * @param <T>         the result type
     * @return the operation's result on the first successful attempt
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;

                boolean lastAttempt = attempt == maxAttempts;
                if (lastAttempt) {
                    break;
                }

                if (delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RetryExhaustedException(
                                "Retry interrupted after " + attempt + " attempt(s)", ie);
                    }
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastFailure);
    }

    /**
     * Convenience overload for operations with no return value.
     */
    public static void runWithRetry(Runnable operation, int maxAttempts, long delayMillis) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMillis);
    }

    /**
     * Thrown when an operation still fails after all retry attempts are exhausted.
     * The original last exception is available via {@link #getCause()}.
     */
    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
