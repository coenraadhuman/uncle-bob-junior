public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be >= 0");
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts && delayMillis > 0) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    public void executeVoid(VoidCallable operation) throws Exception {
        execute(() -> {
            operation.call();
            return null;
        });
    }
    
    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}
