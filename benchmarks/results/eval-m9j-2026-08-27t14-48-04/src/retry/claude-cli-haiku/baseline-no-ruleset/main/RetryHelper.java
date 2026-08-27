/**
 * Executes an operation with automatic retry logic on failure.
 */
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    /**
     * Executes an operation that doesn't return a value, retrying on exception.
     */
    public void execute(Operation operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
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
     * Executes an operation that returns a value, retrying on exception.
     */
    public <T> T executeWithResult(OperationWithResult<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
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
    public interface Operation {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface OperationWithResult<T> {
        T run() throws Exception;
    }
}
