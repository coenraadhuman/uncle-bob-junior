```java
import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int MIN_ATTEMPTS = 1;
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
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

```java
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    
    @Test
    public void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    public void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper retry = new RetryHelper(3, 50);
        
        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("fail");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throwsAfterMaxAttemptsExhausted() {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper retry = new RetryHelper(3, 10);
        
        assertThrows(IOException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new IOException("always fails");
            });
        });
        
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void validatesMaxAttemptsGreaterThanZero() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
    }
    
    @Test
    public void validatesDelayNonNegative() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
```

**Assumptions:**
- Operation uses standard `Callable<T>` interface; retries on any checked or unchecked exception.
- Fixed delay (not exponential backoff); delay is in milliseconds.
- Last exception is re-thrown if all attempts fail; validates constructor inputs at the boundary.