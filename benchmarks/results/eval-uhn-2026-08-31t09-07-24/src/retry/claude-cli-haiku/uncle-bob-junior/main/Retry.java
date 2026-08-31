import java.util.concurrent.Callable;

public class Retry {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public Retry(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxAttempts) {
            attempt++;
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
    
    public static <T> T executeWithDefaults(Callable<T> operation) throws Exception {
        return new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS).execute(operation);
    }
}
