I'll create a clean retry helper with comprehensive tests.

**Assumptions:**
- Use `Thread.sleep()` for delays (simple, no external dependencies)
- Retry on any exception
- Rethrow the last exception if all attempts are exhausted
- Support generic return types

```java
public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public static RetryHelper withDefaults() {
        return new RetryHelper(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    private static final long DELAY_MS = 50;
    
    @Test
    public void shouldExecuteOperationSuccessfullyOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, DELAY_MS);
        
        AtomicInteger attempts = new AtomicInteger(0);
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(1, attempts.get());
    }
    
    @Test
    public void shouldRetryAndSucceedAfterFailures() throws Exception {
        RetryHelper retry = new RetryHelper(3, DELAY_MS);
        
        AtomicInteger attempts = new AtomicInteger(0);
        int result = retry.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("Transient failure");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void shouldExhaustRetriesAndThrowLastException() throws Exception {
        RetryHelper retry = new RetryHelper(3, DELAY_MS);
        
        AtomicInteger attempts = new AtomicInteger(0);
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Persistent failure");
            });
        });
        
        assertEquals("Persistent failure", thrown.getMessage());
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void shouldRejectInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, DELAY_MS));
    }
    
    @Test
    public void shouldRejectNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
    
    @Test
    public void shouldProvideDefaultsFactory() throws Exception {
        RetryHelper retry = RetryHelper.withDefaults();
        
        int result = retry.execute(() -> 99);
        assertEquals(99, result);
    }
}
```

**Cleaned:** RetryHelper has a single responsibility (manage retry logic), validates inputs upfront with guard clauses, uses named constants for retry limits and delays, and makes invalid states (≤0 attempts, negative delay) unrepresentable. Safe to change because the contract is clear via tests covering success, retry-and-succeed, exhaustion, and validation boundaries.