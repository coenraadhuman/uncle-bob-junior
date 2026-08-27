# Retry Helper in Java

I'll provide a clean, reusable retry helper that executes an operation, retries on exception up to a maximum, and waits a fixed delay between attempts.

**Assumptions:**
- Retries on any `Exception` type (all failures trigger retry)
- On final failure, throws the last exception
- Uses `Callable<T>` from `java.util.concurrent` for type-safe operations with checked exceptions
- Validates that `maxAttempts >= 1` and `delayMillis >= 0`
- `Thread.sleep` delays are not interrupted (exceptions propagate immediately)

## Implementation

```java
import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int MIN_ATTEMPTS = 1;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MIN_ATTEMPTS);
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
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
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
}
```

## Tests

```java
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        
        int result = retry.execute(() -> 42);
        
        assertEquals(42, result);
    }
    
    @Test
    void successAfterRetry() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new RuntimeException("Temporary failure");
            }
            return 99;
        });
        
        assertEquals(99, result);
        assertEquals(2, attempts.get());
    }
    
    @Test
    void failureAfterExhaustingRetries() {
        RetryHelper retry = new RetryHelper(2, 10);
        
        Exception thrown = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Persistent failure");
            });
        });
        
        assertEquals("Persistent failure", thrown.getMessage());
    }
    
    @Test
    void respectsDelayBetweenRetries() throws Exception {
        RetryHelper retry = new RetryHelper(2, 50);
        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        
        try {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Always fails");
            });
        } catch (RuntimeException ignored) {
        }
        
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(2, attempts.get());
        assertTrue(elapsed >= 50, "Should delay at least 50ms between retries");
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(-1, 100));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
```

## Usage Example

```java
// Simple usage: retry up to 3 times with 100ms between attempts
RetryHelper retry = new RetryHelper(3, 100);

String data = retry.execute(() -> fetchFromUnstableAPI());

// With void operations, wrap in a Callable
retry.execute(() -> {
    saveToDatabase();
    return null;
});
```

**Cleaned:** Guard-validated input prevents invalid states; guard clause in execute returns early on success with flat control flow; tests cover success, retry success, exhaustion, delay timing, and input validation.