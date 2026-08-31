import java.util.concurrent.Callable;

public class RetryHelper {
    
    public static <T> T executeWithRetry(Callable<T> operation, int maxAttempts, long delayMs) 
            throws Exception {
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
    
    public static void executeWithRetry(Runnable operation, int maxAttempts, long delayMs) 
            throws Exception {
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
