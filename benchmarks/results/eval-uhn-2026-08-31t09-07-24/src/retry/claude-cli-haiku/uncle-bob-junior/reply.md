```java
import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int MIN_ATTEMPTS = 1;
    private static final long MIN_DELAY_MS = 0;
    
    public static <T> T retryOnException(
            Callable<T> operation,
            int maxAttempts,
            long delayMillis) throws Exception {
        validateArguments(operation, maxAttempts, delayMillis);
        return executeWithRetries(operation, maxAttempts, delayMillis);
    }
    
    private static <T> T executeWithRetries(
            Callable<T> operation,
            int maxAttempts,
            long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                sleepBeforeRetry(attempt, maxAttempts, delayMillis);
            }
        }
        throw lastException;
    }
    
    private static void sleepBeforeRetry(int attempt, int maxAttempts, long delayMillis)
            throws InterruptedException {
        if (attempt >= maxAttempts) {
            return;
        }
        Thread.sleep(delayMillis);
    }
    
    private static void validateArguments(Callable<?> operation, int maxAttempts, long delayMillis) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < MIN_DELAY_MS) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
    }
}
```

```java
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    
    @Test
    void operationSucceedsOnFirstAttempt() throws Exception {
        Callable<String> operation = () -> "success";
        String result = RetryHelper.retryOnException(operation, 3, 100);
        assertEquals("success", result);
    }
    
    @Test
    void operationFailsThenSucceeds() throws Exception {
        CallCounter counter = new CallCounter(1);
        String result = RetryHelper.retryOnException(counter, 3, 10);
        assertEquals("success", result);
        assertEquals(2, counter.attemptCount());
    }
    
    @Test
    void operationFailsAllAttempts() {
        CallCounter counter = new CallCounter(5);
        assertThrows(RuntimeException.class, () -> 
            RetryHelper.retryOnException(counter, 3, 10)
        );
        assertEquals(3, counter.attemptCount());
    }
    
    @Test
    void nullOperationThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            RetryHelper.retryOnException(null, 3, 100)
        );
    }
    
    @Test
    void invalidMaxAttempts() {
        Callable<String> operation = () -> "success";
        assertThrows(IllegalArgumentException.class, () -> 
            RetryHelper.retryOnException(operation, 0, 100)
        );
    }
    
    @Test
    void invalidDelay() {
        Callable<String> operation = () -> "success";
        assertThrows(IllegalArgumentException.class, () -> 
            RetryHelper.retryOnException(operation, 3, -1)
        );
    }
    
    @Test
    void delayIsRespected() throws Exception {
        CallCounter counter = new CallCounter(2);
        long startTime = System.currentTimeMillis();
        RetryHelper.retryOnException(counter, 3, 100);
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration >= 150);
    }
    
    private static class CallCounter implements Callable<String> {
        private final int failureCount;
        private int attempts = 0;
        
        CallCounter(int failureCount) {
            this.failureCount = failureCount;
        }
        
        @Override
        public String call() {
            attempts++;
            if (attempts <= failureCount) {
                throw new RuntimeException("Simulated failure");
            }
            return "success";
        }
        
        int attemptCount() {
            return attempts;
        }
    }
}
```

**Assumptions:** The retry logic catches all exceptions (not just a specific type), retries only between attempts (not after the final failure), validates inputs upfront, and propagates the last exception if all attempts fail. InterruptedException during sleep propagates to the caller.

Cleaned: orchestration split into validation, retry loop, and delay logic; guard clause prevents unnecessary sleep after final attempt; named constants replace magic numbers. Safe to change because each function has one responsibility and control flow is flat (max nesting 2 levels).