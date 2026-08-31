import java.util.concurrent.Callable;

public class RetryExecutor<T> {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryExecutor() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }
    
    public RetryExecutor(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must be non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public T execute(Callable<T> operation) throws Exception {
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
        
        throw new RetryExhaustedException(
            "Failed after " + maxAttempts + " attempts", 
            lastException
        );
    }
}
