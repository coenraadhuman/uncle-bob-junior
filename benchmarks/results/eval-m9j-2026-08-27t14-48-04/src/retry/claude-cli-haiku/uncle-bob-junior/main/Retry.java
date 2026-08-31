public class Retry<T> {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;

    private final int maxAttempts;
    private final long delayMillis;

    public Retry() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }

    public Retry(int maxAttempts, long delayMillis) {
        validateAttempts(maxAttempts);
        validateDelay(delayMillis);
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public T execute(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (shouldRetry(attempt)) {
                    sleep();
                }
            }
        }
        throw lastException;
    }

    private boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }

    private void sleep() throws InterruptedException {
        Thread.sleep(delayMillis);
    }

    private void validateAttempts(int attempts) {
        if (attempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
    }

    private void validateDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
    }
}
