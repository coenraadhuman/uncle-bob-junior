public final class Retry {
    private final int maxAttempts;
    private final long delayMs;
    
    public Retry(int maxAttempts, long delayMs) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public void execute(Operation operation) throws Exception {
        executeWithRetry(() -> {
            operation.execute();
            return null;
        });
    }
    
    public <T> T execute(OperationWithResult<T> operation) throws Exception {
        return executeWithRetry(operation::execute);
    }
    
    private <T> T executeWithRetry(RetryOperation<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts - 1) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }
    
    @FunctionalInterface
    private interface RetryOperation<T> {
        T execute() throws Exception;
    }
}

@FunctionalInterface
public interface Operation {
    void execute() throws Exception;
}

@FunctionalInterface
public interface OperationWithResult<T> {
    T execute() throws Exception;
}
