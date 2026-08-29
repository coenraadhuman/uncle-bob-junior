I'll create a clean, reusable retry helper with configuration, a custom exception, and comprehensive tests.

**Assumptions:**
- Uses standard `Callable<T>` for the operation
- Max attempts includes the first attempt (so `maxAttempts=3` means try, then 2 retries)
- Fixed delay between attempts only (not exponential backoff)
- Throws the original exception wrapped in `RetryExhaustedException` when exhausted
- Properly restores interrupt status on sleep interruption

```java
public class RetryHelper {
    public static <T> T executeWithRetry(Callable<T> operation, RetryConfig config) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < config.maxAttempts()) {
                    sleep(config.retryDelayMs());
                }
            }
        }
        throw new RetryExhaustedException(lastException);
    }

    public static <T> T executeWithRetry(Callable<T> operation, int maxAttempts, long retryDelayMs) {
        return executeWithRetry(operation, new RetryConfig(maxAttempts, retryDelayMs));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

public record RetryConfig(int maxAttempts, long retryDelayMs) {
    public RetryConfig {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be at least 1");
        if (retryDelayMs < 0) throw new IllegalArgumentException("retryDelayMs must be non-negative");
    }
}

public class RetryExhaustedException extends RuntimeException {
    public RetryExhaustedException(Throwable cause) {
        super("Retry exhausted after all attempts failed", cause);
    }
}
```

```java
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 10;
    private RetryConfig config;

    @org.junit.Before
    public void setup() {
        config = new RetryConfig(MAX_ATTEMPTS, RETRY_DELAY_MS);
    }

    @org.junit.Test
    public void succeedsOnFirstAttempt() throws Exception {
        Callable<String> operation = () -> "success";
        String result = RetryHelper.executeWithRetry(operation, config);
        assert result.equals("success");
    }

    @org.junit.Test
    public void succeedsAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new Exception("Temporary failure");
            }
            return "success";
        };

        String result = RetryHelper.executeWithRetry(operation, config);
        assert result.equals("success");
        assert attempts.get() == 3;
    }

    @org.junit.Test
    public void throwsWhenAttemptsExhausted() {
        Callable<String> operation = () -> {
            throw new Exception("Always fails");
        };

        try {
            RetryHelper.executeWithRetry(operation, config);
            assert false : "Should have thrown";
        } catch (RetryExhaustedException e) {
            assert e.getCause() != null;
        }
    }

    @org.junit.Test
    public void respectsRetryDelay() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (attempts.incrementAndGet() < 2) {
                throw new Exception("First attempt fails");
            }
            return "success";
        };

        long startTime = System.currentTimeMillis();
        RetryHelper.executeWithRetry(operation, config);
        long elapsed = System.currentTimeMillis() - startTime;

        assert elapsed >= RETRY_DELAY_MS : "Should delay between attempts";
    }

    @org.junit.Test
    public void usesSimpleOverload() throws Exception {
        Callable<String> operation = () -> "ok";
        String result = RetryHelper.executeWithRetry(operation, 3, 10);
        assert result.equals("ok");
    }
}
```

**Usage examples:**

```java
// With config object
RetryConfig config = new RetryConfig(3, 1000);
String result = RetryHelper.executeWithRetry(() -> fetchData(), config);

// With direct parameters
String result = RetryHelper.executeWithRetry(() -> fetchData(), 3, 1000);

// For void operations
RetryHelper.executeWithRetry(() -> {
    updateDatabase();
    return null;
}, 3, 1000);
```

Code: `executeWithRetry()` handles retries in one loop with guard-clause sleep; `RetryConfig` validates bounds immutably; `RetryExhaustedException` chains the cause. Tests verify success, retry count, exhaustion, and delay timing.