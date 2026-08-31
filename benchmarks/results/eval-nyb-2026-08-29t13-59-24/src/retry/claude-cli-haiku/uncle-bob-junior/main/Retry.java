public class Retry {
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }

    private final int maxAttempts;
    private final long delayMs;

    public Retry(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMs);
            }
        }
        throw new AssertionError("Unreachable");
    }

    public void executeVoid(VoidOperation operation) throws Exception {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.execute();
                return;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMs);
            }
        }
    }
}
