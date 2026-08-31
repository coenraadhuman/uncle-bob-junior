public class RetryHelper {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    public static <T> T executeWithRetry(Operation<T> operation) throws Exception {
        return executeWithRetry(operation, MAX_RETRIES, RETRY_DELAY_MS);
    }

    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts, long delayMs) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

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
