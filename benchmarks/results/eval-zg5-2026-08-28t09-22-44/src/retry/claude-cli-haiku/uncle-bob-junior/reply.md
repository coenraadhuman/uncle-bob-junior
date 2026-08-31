```java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
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
    
    @FunctionalInterface
    public interface Operation<T> {
        T call() throws Exception;
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retryHelper = new RetryHelper(3, 10);
        
        String result = retryHelper.execute(() -> "success");
        
        assertEquals("success", result);
    }
    
    @Test
    void successAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper retryHelper = new RetryHelper(3, 10);
        
        String result = retryHelper.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("failed");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void failsAfterMaxAttempts() throws Exception {
        RetryHelper retryHelper = new RetryHelper(2, 10);
        IOException originalException = new IOException("failed");
        
        IOException thrown = assertThrows(IOException.class, () -> {
            retryHelper.execute(() -> {
                throw originalException;
            });
        });
        
        assertEquals(originalException, thrown);
    }
    
    @Test
    void waitsBeforeRetry() throws Exception {
        RetryHelper retryHelper = new RetryHelper(2, 50);
        long startTime = System.currentTimeMillis();
        
        assertThrows(Exception.class, () -> {
            retryHelper.execute(() -> {
                throw new Exception("failed");
            });
        });
        
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 50, "Should have waited at least 50ms");
    }
    
    @Test
    void validatesMaxAttemptsOnConstruction() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(0, 10);
        });
    }
}
```

**Assumptions:** Java 8+, JUnit 5. The retry helper catches all exceptions and retries unconditionally; it validates that `maxAttempts ≥ 1` to prevent infinite loops. Tests verify success on first attempt, retry logic, max attempts failure, delay timing, and constructor validation.