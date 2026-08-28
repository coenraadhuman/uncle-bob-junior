import java.util.concurrent.Callable;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryHelper(int maxAttempts, long delayMs) {
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
    
    public void execute(Runnable operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
}
