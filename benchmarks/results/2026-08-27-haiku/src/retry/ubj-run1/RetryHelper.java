public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLISECONDS = 1000;
    
    private final int maxAttempts;
    private final long delayMilliseconds;
    
    public RetryHelper(int maxAttempts, long delayMilliseconds) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMilliseconds < 0) {
            throw new IllegalArgumentException("delayMilliseconds cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMilliseconds = delayMilliseconds;
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception exception) {
                lastException = exception;
                if (shouldRetry(attempt)) {
                    Thread.sleep(delayMilliseconds);
                }
            }
        }
        
        throw lastException;
    }
    
    private boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }
    
    public static <T> T executeWithDefaults(Operation<T> operation) throws Exception {
        return new RetryHelper(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLISECONDS).execute(operation);
    }
    
    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts, long delayMilliseconds) throws Exception {
        return new RetryHelper(maxAttempts, delayMilliseconds).execute(operation);
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
