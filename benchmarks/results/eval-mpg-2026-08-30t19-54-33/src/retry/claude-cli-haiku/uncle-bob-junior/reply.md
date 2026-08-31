```java
import java.util.function.Supplier;

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be >= 0");
        
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public RetryHelper() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
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
    
    public void executeVoid(ThrowingRunnable operation) throws Exception {
        execute(() -> {
            operation.run();
            return null;
        });
    }
    
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    void succeedsAfterRetries() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) throw new RuntimeException("Not ready");
            return 99;
        });
        
        assertEquals(3, attempts.get());
        assertEquals(99, result);
    }
    
    @Test
    void throwsExceptionAfterMaxAttemptsExceeded() {
        RetryHelper retry = new RetryHelper(2, 0);
        
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Always fails");
            });
        });
    }
    
    @Test
    void waitsDelayMillisBetweenAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(3, 50);
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        try {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Always fails");
            });
        } catch (RuntimeException ignored) {
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 100, "Expected >= 100ms delay, got " + elapsed);
    }
    
    @Test
    void supportsVoidOperations() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        AtomicInteger counter = new AtomicInteger(0);
        
        retry.executeVoid(() -> counter.incrementAndGet());
        
        assertEquals(1, counter.get());
    }
    
    @Test
    void retriesVoidOperations() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        AtomicInteger attempts = new AtomicInteger(0);
        
        retry.executeVoid(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 2) throw new RuntimeException("Not ready");
        });
        
        assertEquals(2, attempts.get());
    }
    
    @Test
    void rejectsZeroMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
    }
    
    @Test
    void rejectsNegativeMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(-1, 100));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
    
    @Test
    void usesDefaultConstructor() throws Exception {
        RetryHelper retry = new RetryHelper();
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
}
```

**Assumptions and design:** The helper accepts both generic `Supplier<T>` operations (for return values) and a custom `ThrowingRunnable` for void work. It sleeps only between retries, not after the final failure. Defaults to 3 attempts with 1-second delays. Constructor validates that attempts ≥ 1 and delays ≥ 0 to make invalid states unrepresentable.