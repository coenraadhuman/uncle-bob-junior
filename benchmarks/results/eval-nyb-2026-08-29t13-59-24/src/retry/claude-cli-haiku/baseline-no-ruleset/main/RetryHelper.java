public class RetryHelper {
    
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delayMillis) 
            throws Exception {
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
    
    public static void retryVoid(RunnableThrows operation, int maxAttempts, long delayMillis) 
            throws Exception {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMillis);
    }
    
    @FunctionalInterface
    public interface RunnableThrows {
        void run() throws Exception;
    }
}
