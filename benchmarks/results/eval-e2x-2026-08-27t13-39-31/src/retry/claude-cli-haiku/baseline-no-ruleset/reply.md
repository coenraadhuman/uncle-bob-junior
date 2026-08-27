Here's a complete, reusable retry helper in Java:

```java
import java.util.concurrent.Callable;

public class RetryHelper {
    
    private final int maxAttempts;
    private final long delayMillis;
    
    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }
    
    /**
     * Execute an operation with retries. Returns a value.
     */
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
    
    /**
     * Execute an operation with retries. No return value.
     */
    public void executeVoid(VoidOperation operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
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
    public interface VoidOperation {
        void run() throws Exception;
    }
}
```

**Usage examples:**

```java
// Example 1: Operation that returns a value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

String result = retry.execute(() -> {
    // Your operation here
    return callUnstableService();
});

// Example 2: Operation with no return value
retry.executeVoid(() -> {
    // Your operation here
    saveToDatabase();
});

// Example 3: With specific exception handling
try {
    retry.execute(() -> {
        if (Math.random() < 0.7) {
            throw new IOException("Service unavailable");
        }
        return "Success";
    });
} catch (Exception e) {
    System.err.println("Failed after retries: " + e.getMessage());
}
```

**Key assumptions:**
- Max attempts must be ≥ 1; delay must be ≥ 0
- All exceptions trigger a retry (except on the final attempt, which throws)
- Delay applies only between attempts (not after the last failure)
- `InterruptedException` from `Thread.sleep()` propagates as-is
- Generic type support for flexible return values