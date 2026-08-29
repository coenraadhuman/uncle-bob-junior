public class RetryHelper {
    public static <T> T executeWithRetry(Callable<T> operation, RetryConfig config) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < config.maxAttempts()) {
                    sleep(config.retryDelayMs());
                }
            }
        }
        throw new RetryExhaustedException(lastException);
    }

    public static <T> T executeWithRetry(Callable<T> operation, int maxAttempts, long retryDelayMs) {
        return executeWithRetry(operation, new RetryConfig(maxAttempts, retryDelayMs));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
