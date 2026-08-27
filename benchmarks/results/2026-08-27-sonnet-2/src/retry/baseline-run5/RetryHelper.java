import java.util.concurrent.Callable;

/**
 * Retries an operation a fixed number of times, waiting a fixed delay
 * between attempts, and rethrows the last failure if all attempts fail.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on any exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delayMillis fixed delay between attempts, in milliseconds
     * @param <T>         the operation's result type
     * @return the operation's result on success
     * @throws RetryException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
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
                    Thread.sleep(delayMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RetryException(
                            "Retry interrupted while waiting between attempts", interruptedException);
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastException);
    }

    /**
     * Convenience overload for operations that throw no checked exceptions.
     */
    public static void runWithRetry(Runnable operation, int maxAttempts, long delayMillis) {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMillis);
    }

    /**
     * Thrown when an operation still fails after exhausting all retry attempts.
     */
    public static final class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Example usage:
    //
    // String result = RetryHelper.runWithRetry(() -> callFlakyService(), 3, 500);
    //
    // RetryHelper.runWithRetry(() -> writeToFile(data), 5, 1000);
}
