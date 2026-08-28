public class RetryHelper {
    public static <T> T execute(Retryable<T> operation, int maxAttempts, long delayMs) 
            throws Exception {
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
}
