import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed delay
 * between attempts when it throws an exception.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    @FunctionalInterface
    public interface RetryableRunnable {
        void run() throws Exception;
    }

    /**
     * Runs the given operation, retrying on any exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed delay between attempts
     * @param unit        time unit for the delay
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T runWithRetry(Callable<T> operation, int maxAttempts, long delay, TimeUnit unit)
            throws RetryExhaustedException {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        RetryExhaustedException failure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (failure == null) {
                    failure = new RetryExhaustedException(
                            "Operation failed after " + maxAttempts + " attempts", e);
                } else {
                    failure.addSuppressed(e);
                }

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    unit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    failure.addSuppressed(ie);
                    throw failure;
                }
            }
        }

        throw failure;
    }

    /** Convenience overload for operations that do not return a value. */
    public static void runWithRetry(RetryableRunnable operation, int maxAttempts, long delay, TimeUnit unit)
            throws RetryExhaustedException {
        runWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay, unit);
    }

    public static class RetryExhaustedException extends Exception {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
