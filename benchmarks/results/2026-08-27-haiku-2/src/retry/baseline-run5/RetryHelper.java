public class RetryHelper {
    
    /**
     * Retries an operation up to maxAttempts times with a fixed delay between attempts.
     * 
     * @param operation the operation to retry
     * @param maxAttempts maximum number of attempts (including the first)
     * @param delayMillis delay in milliseconds between attempts
     * @throws Exception if all attempts fail
     */
    public static void retry(VoidOperation operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.execute();
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
    
    /**
     * Retries an operation that returns a value up to maxAttempts times with a fixed delay between attempts.
     * 
     * @param operation the operation to retry
     * @param maxAttempts maximum number of attempts (including the first)
     * @param delayMillis delay in milliseconds between attempts
     * @return the result of the operation
     * @throws Exception if all attempts fail
     */
    public static <T> T retry(Operation<T> operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
