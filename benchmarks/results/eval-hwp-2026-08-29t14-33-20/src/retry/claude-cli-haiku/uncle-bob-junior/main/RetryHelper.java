import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 100;
    private static final int MIN_ATTEMPTS = 1;
    
    public static <T> T executeWithRetry(Callable<T> operation) throws Exception {
        return executeWithRetry(operation, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }
    
    public static <T> T executeWithRetry(Callable<T> operation, int maxAttempts, long delayMillis)
            throws Exception {
        validateAttempts(maxAttempts);
        
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    private static void validateAttempts(int maxAttempts) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MIN_ATTEMPTS);
        }
    }
    
    private static void sleep(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            // Restore interrupt status for outer handlers
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry sleep was interrupted", e);
        }
    }
}
