```java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be >= 0");
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
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
    public interface Operation<T> {
        T run() throws Exception;
    }
}
```

**Usage examples:**

```java
// With return value
RetryHelper retry = new RetryHelper(3, 1000);
String result = retry.execute(() -> callSomeApi());

// With void operation
retry.execute(() -> {
    performDatabaseWrite();
    return null;
});

// With lambda capturing external data
retry.execute(() -> fetchDataFromUnstableService(userId));
```

**Assumptions:**
- Operations may throw any checked or unchecked exception
- The last exception is thrown if all retries fail
- Thread.sleep() is acceptable for delays (not suitable for high-concurrency scenarios, but adequate for most use cases)
- No logging is built-in (caller can wrap with logging if needed)
- Max attempts include the first attempt (e.g., maxAttempts=3 means 1 initial + 2 retries)