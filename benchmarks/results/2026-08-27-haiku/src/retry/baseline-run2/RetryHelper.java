public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    private RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public static RetryHelper withAttempts(int maxAttempts) {
        return new RetryHelper(maxAttempts, 1000);
    }

    public static RetryHelper withAttemptsAndDelay(int maxAttempts, long delayMillis) {
        return new RetryHelper(maxAttempts, delayMillis);
    }

    public <T> T execute(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }

    public void execute(CheckedRunnable operation) throws Exception {
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

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}
