I'll create a clean, reusable retry helper with comprehensive tests.

**Assumptions:** Uses `java.util.concurrent.Callable` to accept any operation (with or without return value), throws the final exception if all retries fail, sleeps only *between* attempts (not before the first), and includes sensible defaults (3 attempts, 100ms delay).

```java
import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 100;
    private static final int MIN_ATTEMPTS = 1;
    
    public static <T> T executeWithRetry(Callable<T> operation) throws Exception {
        return executeWithRetry(operation, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }
    
    public static <T> T executeWithRetry(Callable<T> operation, int maxAttempts, long delayMillis)
            throws Exception {
        validateAttempts(maxAttempts);
        
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep(delayMillis);
                }
            }
        }
        
        throw lastException;
    }
    
    private static void validateAttempts(int maxAttempts) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MIN_ATTEMPTS);
        }
    }
    
    private static void sleep(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            // Restore interrupt status for outer handlers
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry sleep was interrupted", e);
        }
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    
    @Test
    public void succeedsOnFirstAttempt() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = RetryHelper.executeWithRetry(
            () -> {
                callCount.incrementAndGet();
                return "success";
            }
        );
        
        assertEquals("success", result);
        assertEquals(1, callCount.get());
    }
    
    @Test
    public void retriesAndSucceedsOnThirdAttempt() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        
        Integer result = RetryHelper.executeWithRetry(
            () -> {
                callCount.incrementAndGet();
                if (callCount.get() < 3) {
                    throw new RuntimeException("not yet");
                }
                return 42;
            },
            3,
            10
        );
        
        assertEquals(42, result);
        assertEquals(3, callCount.get());
    }
    
    @Test
    public void throwsExceptionAfterMaxAttemptsExceeded() {
        assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(
                () -> { throw new RuntimeException("permanent failure"); },
                2,
                10
            );
        });
    }
    
    @Test
    public void validatesMaxAttemptsGreaterThanZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            RetryHelper.executeWithRetry(() -> "ok", 0, 100);
        });
    }
    
    @Test
    public void usesDefaultParametersWhenNotSpecified() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = RetryHelper.executeWithRetry(
            () -> {
                callCount.incrementAndGet();
                return "done";
            }
        );
        
        assertEquals("done", result);
        assertEquals(1, callCount.get());
    }
}
```

**Usage:**
```java
// With defaults (3 attempts, 100ms delay)
String data = RetryHelper.executeWithRetry(() -> fetchFromApi());

// Custom attempts and delay
int result = RetryHelper.executeWithRetry(
    () -> unreliableOperation(),
    5,              // max attempts
    500             // delay in milliseconds
);
```

**Cleaned:** Single-purpose helper with guard validation, flat control flow (early return on success), named constants for all meaningful values, proper interrupt handling. Tests cover the happy path, retries, failure exhaustion, and validation — safe to extend with exponential backoff or jitter if needed.