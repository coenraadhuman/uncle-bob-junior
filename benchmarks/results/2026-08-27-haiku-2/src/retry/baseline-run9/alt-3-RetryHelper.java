import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;
    private final Predicate<Exception> retryOn;
    
    private RetryHelper(int maxAttempts, long delayMs, Predicate<Exception> retryOn) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
        this.retryOn = retryOn;
    }
    
    public static RetryBuilder builder() {
        return new RetryBuilder();
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (!retryOn.test(e)) {
                    throw e;
                }
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    public static class RetryBuilder {
        private int maxAttempts = 3;
        private long delayMs = 1000;
        private Predicate<Exception> retryOn = e -> true;
        
        public RetryBuilder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }
        
        public RetryBuilder delayMs(long delayMs) {
            this.delayMs = delayMs;
            return this;
        }
        
        public RetryBuilder retryOn(Predicate<Exception> retryOn) {
            this.retryOn = retryOn;
            return this;
        }
        
        public RetryHelper build() {
            return new RetryHelper(maxAttempts, delayMs, retryOn);
        }
    }
}
