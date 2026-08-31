import java.util.function.Supplier;

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be >= 0");
        
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public RetryHelper() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }
    
    public <T> T execute(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    public void executeVoid(ThrowingRunnable operation) throws Exception {
        execute(() -> {
            operation.run();
            return null;
        });
    }
    
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
