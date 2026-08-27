/**
 * Advanced retry helper with builder pattern.
 */
public class RetryBuilder<T> {
    private final Callable<T> operation;
    private int maxAttempts = 3;
    private long delayMs = 1000;
    private boolean exponentialBackoff = false;
    private Class<? extends Exception> retryOn = Exception.class;

    public RetryBuilder(Callable<T> operation) {
        this.operation = operation;
    }

    public RetryBuilder<T> maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RetryBuilder<T> delay(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    public RetryBuilder<T> exponentialBackoff() {
        this.exponentialBackoff = true;
        return this;
    }

    public RetryBuilder<T> retryOn(Class<? extends Exception> exceptionType) {
        this.retryOn = exceptionType;
        return this;
    }

    public T execute() throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (!retryOn.isInstance(e)) {
                    throw e; // Don't retry on this exception type
                }
                lastException = e;
                if (attempt < maxAttempts) {
                    long wait = exponentialBackoff ? delayMs * (long) Math.pow(2, attempt - 1) : delayMs;
                    Thread.sleep(wait);
                }
            }
        }
        throw lastException;
    }
}
