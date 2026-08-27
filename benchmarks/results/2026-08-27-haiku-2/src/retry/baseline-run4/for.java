import java.util.concurrent.Callable;

/**
 * Utility class for retrying operations with exponential or fixed backoff.
 */
public class RetryHelper {

    /**
     * Executes an operation with retry logic.
     *
     * @param operation the operation to execute
     * @param maxAttempts maximum number of attempts (minimum 1)
     * @param delayMs delay in milliseconds between retries
     * @param <T> return type of the operation
     * @return the result of the operation
     * @throws Exception the last exception if all attempts fail
     */
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delayMs) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }

    /**
     * Executes a void operation with retry logic.
     *
     * @param operation the operation to execute
     * @param maxAttempts maximum number of attempts (minimum 1)
     * @param delayMs delay in milliseconds between retries
     * @throws Exception the last exception if all attempts fail
     */
    public static void retry(Runnable operation, int maxAttempts, long delayMs) throws Exception {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMs);
    }
}
