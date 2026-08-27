@FunctionalInterface
public interface Operation<T> {
    T execute() throws Exception;
}

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;

    public static <T> T retry(Operation<T> operation, int maxAttempts, long delayMillis) {
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
                    sleep(delayMillis);
                }
            }
        }
        
        throw new RetryExhaustedException("Failed after " + maxAttempts + " attempts", lastException);
    }

    public static <T> T retry(Operation<T> operation, int maxAttempts) {
        return retry(operation, maxAttempts, DEFAULT_DELAY_MILLIS);
    }

    public static <T> T retry(Operation<T> operation) {
        return retry(operation, DEFAULT_MAX_ATTEMPTS);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException("Retry interrupted", e);
        }
    }

    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RetryInterruptedException extends RuntimeException {
        public RetryInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
