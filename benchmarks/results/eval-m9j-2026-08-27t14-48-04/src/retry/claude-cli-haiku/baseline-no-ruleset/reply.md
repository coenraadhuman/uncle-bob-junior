I'll provide a clean, reusable retry helper that handles operations with or without return values:

```java
/**
 * Executes an operation with automatic retry logic on failure.
 */
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    /**
     * Executes an operation that doesn't return a value, retrying on exception.
     */
    public void execute(Operation operation) throws Exception {
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

    /**
     * Executes an operation that returns a value, retrying on exception.
     */
    public <T> T executeWithResult(OperationWithResult<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
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
    public interface Operation {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface OperationWithResult<T> {
        T run() throws Exception;
    }
}
```

**Usage examples:**

```java
// Simple operation without return value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

try {
    retry.execute(() -> {
        // Your operation here
        apiClient.sendRequest();
    });
} catch (Exception e) {
    System.err.println("Failed after all retries: " + e.getMessage());
}

// Operation with return value
try {
    String result = retry.executeWithResult(() -> {
        return database.fetchData();
    });
} catch (Exception e) {
    System.err.println("Failed after all retries: " + e.getMessage());
}
```

**Assumptions made:**
- Uses checked exceptions for broad compatibility
- Fixed delay (no backoff) between attempts as specified
- Does not retry on the final attempt (saves unnecessary sleep)
- Throws the last caught exception after all attempts exhausted
- Uses `Thread.sleep()` for delays (consider alternatives like `ScheduledExecutorService` for async contexts)