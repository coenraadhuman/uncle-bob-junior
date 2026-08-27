import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Reusable helper that retries an operation a fixed number of times,
 * waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Runs a value-returning operation, retrying on any thrown exception.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param unit        time unit for {@code delay}
     * @return the operation's result on the first successful attempt
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delay, TimeUnit unit) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    unit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", ie);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempts", lastFailure);
    }

    /**
     * Convenience overload for operations with no return value.
     */
    public static void retry(ThrowingRunnable operation, int maxAttempts, long delay, TimeUnit unit) {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay, unit);
    }

    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Example usage
    public static void main(String[] args) {
        int[] callCount = {0};

        String result = retry(() -> {
            callCount[0]++;
            if (callCount[0] < 3) {
                throw new RuntimeException("Simulated failure #" + callCount[0]);
            }
            return "success";
        }, 5, 500, TimeUnit.MILLISECONDS);

        System.out.println("Result: " + result + ", attempts: " + callCount[0]);
    }
}
