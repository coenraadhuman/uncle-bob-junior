import java.util.concurrent.Callable;

/**
 * Reusable retry helper: runs an operation, retrying up to a maximum number
 * of attempts on failure, with a fixed delay between attempts.
 */
public final class Retryer {

    private final int maxAttempts;
    private final long delayMillis;

    public Retryer(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    /**
     * Executes the given operation, retrying on any exception it throws.
     *
     * @param operation the operation to run
     * @param <T> the operation's return type
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public <T> T call(Callable<T> operation) throws RetryExhaustedException {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean attemptsRemain = attempt < maxAttempts;
                if (!attemptsRemain) {
                    break;
                }

                if (delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RetryExhaustedException(
                                "Retry interrupted while waiting between attempts", attempt, ie);
                    }
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", maxAttempts, lastException);
    }

    /** Convenience overload for operations that return nothing. */
    public void run(RunnableWithException operation) throws RetryExhaustedException {
        call(() -> {
            operation.run();
            return null;
        });
    }

    /** A Runnable variant that may throw a checked exception. */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /** Thrown when all retry attempts have been exhausted. */
    public static final class RetryExhaustedException extends Exception {
        private final int attemptsMade;

        public RetryExhaustedException(String message, int attemptsMade, Throwable cause) {
            super(message, cause);
            this.attemptsMade = attemptsMade;
        }

        public int getAttemptsMade() {
            return attemptsMade;
        }
    }

    // Example usage:
    //
    // Retryer retryer = new Retryer(3, 500); // 3 attempts, 500ms fixed delay
    //
    // try {
    //     String result = retryer.call(() -> callFlakyService());
    // } catch (Retryer.RetryExhaustedException e) {
    //     // handle final failure; e.getCause() holds the last exception
    // }
}
