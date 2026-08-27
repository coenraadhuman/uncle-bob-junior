import java.time.Duration;

/**
 * Generic retry helper: runs an operation up to a maximum number of attempts,
 * waiting a fixed delay between attempts, and rethrows the last failure if all attempts fail.
 */
public final class Retry {

    private Retry() {
    }

    @FunctionalInterface
    public interface Operation<T> {
        T call() throws Exception;
    }

    public static class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Runs {@code operation}, retrying on any exception until it succeeds
     * or {@code maxAttempts} attempts have been made.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed delay to wait between attempts
     * @return the operation's result
     * @throws RetryException if all attempts fail; the last failure is set as the cause
     */
    public static <T> T run(Operation<T> operation, int maxAttempts, Duration delay) {
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
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryException("Retry interrupted while waiting between attempts", interrupted);
                }
            }
        }

        throw new RetryException(
                "Operation failed after " + maxAttempts + " attempts",
                lastException
        );
    }

    // Convenience overload for operations that don't return a value.
    public static void run(Runnable voidOperation, int maxAttempts, Duration delay) {
        run(() -> {
            voidOperation.run();
            return null;
        }, maxAttempts, delay);
    }
}
