public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.perform();
            } catch (Exception e) {
                lastException = e;

                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw new RetryException(
            String.format("Operation failed after %d attempts", maxAttempts),
            lastException
        );
    }

    @FunctionalInterface
    public interface Operation<T> {
        T perform() throws Exception;
    }

    public static class RetryException extends Exception {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
