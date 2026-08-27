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

    public <T> T execute(Operation<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }
        
        throw new RetryExhaustedException(
            "Failed after " + maxAttempts + " attempts",
            lastException
        );
    }

    private void sleep() {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException("Retry sleep interrupted", e);
        }
    }

    @FunctionalInterface
    public interface Operation<T> {
        T run() throws Exception;
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
