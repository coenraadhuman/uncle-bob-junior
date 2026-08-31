public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;

    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts, long delayMillis) throws Exception {
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

    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts) throws Exception {
        return executeWithRetry(operation, maxAttempts, DEFAULT_DELAY_MILLIS);
    }

    public static <T> T executeWithRetry(Operation<T> operation) throws Exception {
        return executeWithRetry(operation, DEFAULT_MAX_ATTEMPTS);
    }
}
