@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}

public class Retry {
    private Retry() {}
    
    public static <T> T execute(
        Callable<T> operation,
        int maxAttempts,
        long delayMillis
    ) throws Exception {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        
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
    
    public static void execute(
        ThrowingRunnable operation,
        int maxAttempts,
        long delayMillis
    ) throws Exception {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
}
