import java.util.concurrent.Callable;

public class Retry {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public Retry(int maxAttempts, long delayMs) {
        validateParameters(maxAttempts, delayMs);
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
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
    
    public void executeVoid(VoidOperation operation) throws Exception {
        execute(() -> {
            operation.run();
            return null;
        });
    }
    
    public static <T> T withRetry(Callable<T> operation) throws Exception {
        return new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS).execute(operation);
    }
    
    public static void withRetry(VoidOperation operation) throws Exception {
        new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS).executeVoid(operation);
    }
    
    private void validateParameters(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
    }
    
    @FunctionalInterface
    public interface VoidOperation {
        void run() throws Exception;
    }
}
