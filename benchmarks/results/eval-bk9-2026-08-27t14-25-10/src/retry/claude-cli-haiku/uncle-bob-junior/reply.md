Here's a clean, reusable retry helper for Java:

```java
import java.util.concurrent.Callable;

public class Retry {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;
    
    private Retry() {}
    
    public static <T> T execute(Callable<T> operation, int maxAttempts, long delayMillis) 
            throws Exception {
        validateInputs(maxAttempts, delayMillis);
        
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
    
    public static <T> T execute(Callable<T> operation, int maxAttempts) throws Exception {
        return execute(operation, maxAttempts, DEFAULT_DELAY_MILLIS);
    }
    
    public static <T> T execute(Callable<T> operation) throws Exception {
        return execute(operation, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }
    
    private static void validateInputs(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
    }
}
```

**Tests** (JUnit 5):

```java
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryTest {
    @Test
    public void succeeds_on_first_attempt() throws Exception {
        String result = Retry.execute(() -> "success", 3, 100);
        assertEquals("success", result);
    }
    
    @Test
    public void succeeds_after_multiple_retries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = Retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IOException("retry me");
            }
            return "success";
        }, 5, 50);
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throws_last_exception_after_exhausting_retries() {
        IOException thrown = assertThrows(IOException.class, () -> 
            Retry.execute(() -> {
                throw new IOException("always fails");
            }, 2, 50)
        );
        assertEquals("always fails", thrown.getMessage());
    }
    
    @Test
    public void rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException.class, () -> 
            Retry.execute(() -> "ok", 0, 100)
        );
    }
    
    @Test
    public void rejects_negative_delay() {
        assertThrows(IllegalArgumentException.class, () -> 
            Retry.execute(() -> "ok", 1, -1)
        );
    }
    
    @Test
    public void uses_defaults_when_not_specified() throws Exception {
        String result = Retry.execute(() -> "success");
        assertEquals("success", result);
    }
}
```

**Assumptions:**
- Uses `Callable<T>` (standard Java) to accept any operation returning a result
- Retries on all exceptions; customize by catching/filtering in your `Callable` if you need specific exceptions only
- Fixed delay (no exponential backoff)
- Defaults: 3 attempts, 1-second delay; override as needed
- Generic over return type; returns `null` if your operation returns `null`