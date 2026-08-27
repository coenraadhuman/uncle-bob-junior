import java.time.Duration;

/**
 * A reusable helper that runs an operation, retrying it a fixed number of
 * times with a fixed delay between attempts if it throws an exception.
 */
public final class RetryHelper {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T call() throws Exception;
    }

    private final int maxAttempts;
    private final Duration delay;

    /**
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     */
    public RetryHelper(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delay == null || delay.isNegative()) {
            throw new IllegalArgumentException("delay must be non-null and non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    /**
     * Runs the given operation, retrying on exception up to maxAttempts times.
     *
     * @throws RetryException if all attempts fail, or if interrupted while waiting
     */
    public <T> T execute(RetryableOperation<T> operation) throws RetryException {
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
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryException(
                            "Retry interrupted after attempt " + attempt + " of " + maxAttempts,
                            interrupted);
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempts",
                lastException);
    }

    /** Overload for operations that don't return a value. */
    public void execute(Runnable operation) throws RetryException {
        execute(() -> {
            operation.run();
            return null;
        });
    }

    /** Thrown when all retry attempts are exhausted or retrying is interrupted. */
    public static final class RetryException extends Exception {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Example usage
    public static void main(String[] args) {
        RetryHelper retryHelper = new RetryHelper(3, Duration.ofSeconds(2));

        try {
            String result = retryHelper.execute(() -> {
                // Replace with the actual operation, e.g. an HTTP call or DB query
                System.out.println("Attempting operation...");
                if (Math.random() < 0.8) {
                    throw new RuntimeException("Simulated failure");
                }
                return "success";
            });
            System.out.println("Result: " + result);
        } catch (RetryException e) {
            System.err.println("All attempts failed: " + e.getCause());
        }
    }
}
