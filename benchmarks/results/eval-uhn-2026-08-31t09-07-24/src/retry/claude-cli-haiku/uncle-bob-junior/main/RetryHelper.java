import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int MIN_ATTEMPTS = 1;
    private static final long MIN_DELAY_MS = 0;
    
    public static <T> T retryOnException(
            Callable<T> operation,
            int maxAttempts,
            long delayMillis) throws Exception {
        validateArguments(operation, maxAttempts, delayMillis);
        return executeWithRetries(operation, maxAttempts, delayMillis);
    }
    
    private static <T> T executeWithRetries(
            Callable<T> operation,
            int maxAttempts,
            long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                sleepBeforeRetry(attempt, maxAttempts, delayMillis);
            }
        }
        throw lastException;
    }
    
    private static void sleepBeforeRetry(int attempt, int maxAttempts, long delayMillis)
            throws InterruptedException {
        if (attempt >= maxAttempts) {
            return;
        }
        Thread.sleep(delayMillis);
    }
    
    private static void validateArguments(Callable<?> operation, int maxAttempts, long delayMillis) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < MIN_DELAY_MS) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
    }
}
