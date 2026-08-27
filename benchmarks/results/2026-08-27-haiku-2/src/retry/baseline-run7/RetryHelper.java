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
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return operation.execute();
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMillis);
            }
        }
    }

    public void execute(VoidOperation operation) throws Exception {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                operation.execute();
                return;
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMillis);
            }
        }
    }

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }
}
