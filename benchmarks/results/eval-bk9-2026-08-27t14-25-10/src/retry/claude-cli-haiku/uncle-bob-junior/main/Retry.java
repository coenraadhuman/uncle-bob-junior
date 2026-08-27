import java.util.concurrent.Callable;

public class Retry {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;
    
    private Retry() {}
    
    public static <T> T execute(Callable<T> operation, int maxAttempts, long delayMillis) 
            throws Exception {
        validateInputs(maxAttempts, delayMillis);
        
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }
    
    public static <T> T execute(Callable<T> operation, int maxAttempts) throws Exception {
        return execute(operation, maxAttempts, DEFAULT_DELAY_MILLIS);
    }
    
    public static <T> T execute(Callable<T> operation) throws Exception {
        return execute(operation, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }
    
    private static void validateInputs(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
    }
}
