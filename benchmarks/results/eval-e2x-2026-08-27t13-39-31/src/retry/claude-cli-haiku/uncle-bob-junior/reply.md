I'll create a clean, reusable retry helper with tests.

**Assumptions:**
- Use `Thread.sleep()` for delays
- Support any result type via generics
- Throw the last exception encountered
- Require at least 1 attempt
- Fixed delay between retries (not exponential backoff)

```java
public class RetryHelper {
    private static final int MINIMUM_ATTEMPTS = 1;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < MINIMUM_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastException = e;
                if (shouldRetry(attempt)) {
                    sleep();
                }
            }
        }
        
        throw lastException;
    }
    
    private boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }
    
    private void sleep() throws InterruptedException {
        Thread.sleep(delayMillis);
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T run() throws Exception;
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    void succeeds_afterFailures() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        int[] attemptCount = {0};
        
        String result = retry.execute(() -> {
            attemptCount[0]++;
            if (attemptCount[0] < 3) {
                throw new RuntimeException("attempt " + attemptCount[0]);
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attemptCount[0]);
    }
    
    @Test
    void throwsAfterMaxAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(2, 10);
        
        Exception thrown = assertThrows(RuntimeException.class, () -> 
            retry.execute(() -> {
                throw new RuntimeException("persistent failure");
            })
        );
        
        assertEquals("persistent failure", thrown.getMessage());
    }
    
    @Test
    void throwsOnInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RetryHelper(0, 100)
        );
    }
    
    @Test
    void throwsOnNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RetryHelper(3, -1)
        );
    }
    
    @Test
    void supportsGenericReturnTypes() throws Exception {
        RetryHelper retry = new RetryHelper(1, 10);
        
        Integer number = retry.execute(() -> 42);
        assertEquals(42, number);
        
        Object[] array = retry.execute(() -> new Object[]{1, 2, 3});
        assertEquals(3, array.length);
    }
}
```

**Cleaned: guard clauses for invalid state (maxAttempts, delayMillis), early return on success, named constants and helper methods (`shouldRetry`, `sleep`) for clarity. Safe to change because the retry loop is isolated and testable — tests verify the happy path (immediate success, eventual success), error paths (exhausted retries), and boundary conditions (invalid inputs).**