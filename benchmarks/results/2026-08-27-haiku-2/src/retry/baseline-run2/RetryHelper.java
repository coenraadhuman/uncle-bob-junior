public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    /**
     * Executes an operation with retry logic.
     * 
     * @param operation the operation to execute
     * @param <T> the return type of the operation
     * @return the result of the operation
     * @throws Exception the last exception thrown if all attempts fail
     */
    public <T> T execute(CheckedSupplier<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }

        throw lastException;
    }

    /**
     * Executes an operation with retry logic (void operation).
     * 
     * @param operation the operation to execute
     * @throws Exception the last exception thrown if all attempts fail
     */
    public void execute(CheckedRunnable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }

        throw lastException;
    }

    private void sleep() {
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", e);
            }
        }
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    // Convenience static methods
    public static <T> T retryOperation(int maxAttempts, long delayMillis, CheckedSupplier<T> operation) throws Exception {
        return new RetryHelper(maxAttempts, delayMillis).execute(operation);
    }

    public static void retryOperation(int maxAttempts, long delayMillis, CheckedRunnable operation) throws Exception {
        new RetryHelper(maxAttempts, delayMillis).execute(operation);
    }
}
