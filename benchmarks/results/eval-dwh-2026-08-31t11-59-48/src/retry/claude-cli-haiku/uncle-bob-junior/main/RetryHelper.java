public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public static RetryHelper withDefaults() {
        return new RetryHelper(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
