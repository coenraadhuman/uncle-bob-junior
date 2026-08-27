import java.util.concurrent.TimeUnit;

/**
 * Generic retry helper: runs an operation up to maxAttempts times,
 * waiting a fixed delay between attempts, until it succeeds or attempts are exhausted.
 */
public final class RetryHelper {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws Exception;
    }

    private RetryHelper() {
    }

    public static <T> T execute(RetryableOperation<T> operation,
                                 int maxAttempts,
                                 long delay,
                                 TimeUnit delayUnit) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                if (lastFailure != null) {
                    e.addSuppressed(lastFailure);
                }
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    delayUnit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
        }

        throw lastFailure;
    }

    // Example usage
    public static void main(String[] args) throws Exception {
        int result = execute(() -> {
            System.out.println("Attempting operation...");
            if (Math.random() < 0.8) {
                throw new RuntimeException("Simulated failure");
            }
            return 42;
        }, 5, 500, TimeUnit.MILLISECONDS);

        System.out.println("Succeeded with result: " + result);
    }
}
