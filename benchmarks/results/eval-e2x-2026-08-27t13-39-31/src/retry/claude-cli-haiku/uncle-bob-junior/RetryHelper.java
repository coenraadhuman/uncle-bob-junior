public class RetryHelper {
    private static final int MINIMUM_ATTEMPTS = 1;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < MINIMUM_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastException = e;
                if (shouldRetry(attempt)) {
                    sleep();
                }
            }
        }
        
        throw lastException;
    }
    
    private boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }
    
    private void sleep() throws InterruptedException {
        Thread.sleep(delayMillis);
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T run() throws Exception;
    }
}
